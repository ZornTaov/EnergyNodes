package org.zornco.energynodes.item;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import java.util.*;

public class EnergyLinkerItem extends Item {
    private static final String NBT_NODE_POS_KEY = "node-pos";

    public EnergyLinkerItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .tab(Registration.ITEM_GROUP));
    }

    @Nonnull
    @Override
    public ActionResultType useOn(@Nonnull ItemUseContext context) {
        BlockPos blockpos = context.getClickedPos();
        World world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockState blockState = world.getBlockState(blockpos);
        CompoundNBT compoundnbt = itemstack.hasTag() ? itemstack.getTag() : new CompoundNBT();
        if (compoundnbt != null) {
            if (blockState.getBlock() instanceof EnergyControllerBlock && compoundnbt.contains(NBT_NODE_POS_KEY)) {

                EnergyControllerTile tile1 = (EnergyControllerTile) world.getBlockEntity(blockpos);
                if (tile1 != null) {
                    BlockPos otherPos = NBTUtil.readBlockPos((CompoundNBT) Objects.requireNonNull(compoundnbt.get(NBT_NODE_POS_KEY)));
                    //BlockState blockState1 = world.getBlockState(otherPos);
                    EnergyNodeTile tile2 = (EnergyNodeTile) world.getBlockEntity(otherPos);
                    if (tile2 != null) { // TODO: disconnect old controllers if they exist
                        /*final EnergyNodeBlock.Flow flowType = blockState1.getValue(EnergyNodeBlock.PROP_INOUT);

                        Set<BlockPos> positions = new HashSet<>();
                        switch (flowType) {
                            case IN:
                                positions = tile1.inputs.stream()
                                        .map(in -> in.map(storage -> ((NodeEnergyStorage) storage).getLocation()).orElse(null))
                                        .filter(Objects::nonNull)
                                        .map(BlockPos::immutable)
                                        .collect(Collectors.toSet());
                                break;

                            case OUT:
                                positions = tile1.outputs.stream()
                                        .map(out -> out.map(storage -> ((NodeEnergyStorage) storage).getLocation()).orElse(null))
                                        .filter(Objects::nonNull)
                                        .map(BlockPos::immutable)
                                        .collect(Collectors.toSet());
                                break;
                        }*/

                        updateControllerPosList(context,
                                tile1,
                                tile2);
                    } else {
                        //tile1.connectedInputNodes.remove(otherPos);
                        SendSystemMessage(context, "Node missing at: " + Utils.getCoordinatesAsString(otherPos));
                        return ActionResultType.PASS;
                    }
                    compoundnbt.remove(NBT_NODE_POS_KEY);
                    itemstack.setTag(compoundnbt);
                    return ActionResultType.SUCCESS;
                } else {
                    SendSystemMessage(context, "Controller has no Tile?!");
                    return ActionResultType.PASS;
                }
            } else if ((blockState.getBlock() instanceof EnergyNodeBlock)) {
                compoundnbt.put(NBT_NODE_POS_KEY, NBTUtil.writeBlockPos(blockpos));
                SendSystemMessage(context, "Starting connection from: " + Utils.getCoordinatesAsString(blockpos));
                itemstack.setTag(compoundnbt);
                return ActionResultType.SUCCESS;
            } else {
                return super.useOn(context);
            }
        }
        return ActionResultType.PASS;
    }

    // TODO - Split and transformed into link/unlink
    private void updateControllerPosList(@Nonnull ItemUseContext context, EnergyControllerTile controller, EnergyNodeTile nodeTile) {
        final EnergyNodeBlock.Flow dir = nodeTile.getBlockState().getValue(EnergyNodeBlock.PROP_INOUT);
        Direction hit = context.getClickedFace();
        LazyOptional<IEnergyStorage> storage = nodeTile.getCapability(CapabilityEnergy.ENERGY, hit);

        BlockPos checkPos = nodeTile.getBlockPos();
        if (controller.connectedNodes.contains(checkPos)) {
            controller.connectedNodes.remove(checkPos);
            switch (dir) {
                case IN:
                    controller.inputs.remove(storage);
                    break;
                case OUT:
                    controller.outputs.remove(storage);
                    break;
            }

            nodeTile.controllerPos = null;
            nodeTile.energyStorage.setController(null);
            nodeTile.energyStorage.setEnergyStored(0);
            SendSystemMessage(context, "Disconnected to: " + Utils.getCoordinatesAsString(checkPos));
        } else {
            controller.connectedNodes.add(checkPos);
            switch (dir) {
                case IN:
                    controller.inputs.add(storage);
                    break;
                case OUT:
                    controller.outputs.add(storage);
                    break;
            }

            storage.addListener(removed -> {
                controller.connectedNodes.remove(checkPos);
                switch (dir) {
                    case IN:
                        controller.inputs.remove(removed);
                        break;
                    case OUT:
                        controller.outputs.remove(removed);
                        break;
                }

                controller.setChanged();
            });

            nodeTile.controllerPos = controller.getBlockPos();
            nodeTile.energyStorage.setController(controller);
            SendSystemMessage(context, "Connected to: " + Utils.getCoordinatesAsString(checkPos));
        }
    }

    private void SendSystemMessage(@Nonnull ItemUseContext context, String s) {
        if (!context.getLevel().isClientSide) {
            Utils.sendSystemMessage(context.getPlayer(), s);
        }
    }
}
