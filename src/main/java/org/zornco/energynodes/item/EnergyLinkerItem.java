package org.zornco.energynodes.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.EnergyControllerBlock;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import java.util.*;

public class EnergyLinkerItem extends Item {

    public EnergyLinkerItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .tab(Registration.ITEM_GROUP));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockState blockState = world.getBlockState(blockpos);
        CompoundTag compoundnbt = itemstack.hasTag() ? itemstack.getTag() : new CompoundTag();
        if (compoundnbt != null) {
            if (blockState.getBlock() instanceof EnergyNodeBlock) {
                compoundnbt.put(EnergyNodeConstants.NBT_NODE_POS_KEY, NbtUtils.writeBlockPos(blockpos));
                Utils.SendSystemMessage(context, new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.start_connection"), Utils.getCoordinatesAsString(blockpos)));
                itemstack.setTag(compoundnbt);
                return InteractionResult.SUCCESS;

            } else if (blockState.getBlock() instanceof EnergyControllerBlock && compoundnbt.contains(EnergyNodeConstants.NBT_NODE_POS_KEY)) {
                EnergyControllerTile controllerTile = (EnergyControllerTile) world.getBlockEntity(blockpos);
                BlockPos nodePos = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(compoundnbt.get(EnergyNodeConstants.NBT_NODE_POS_KEY)));
                EnergyNodeTile nodeTile = (EnergyNodeTile) world.getBlockEntity(nodePos);
                if (controllerTile == null) {
                    Utils.SendSystemMessage(context, new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.controller_missing")));
                    return InteractionResult.PASS;
                }
                if (nodeTile == null) {
                    Utils.SendSystemMessage(context, new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.node_missing"), Utils.getCoordinatesAsString(nodePos)));
                    return InteractionResult.PASS;
                }
                if (blockpos.distManhattan(nodePos) >= controllerTile.tier.getMaxRange()) {
                    Utils.SendSystemMessage(context, new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.node_out_of_range"), controllerTile.tier.getMaxRange()));
                    return InteractionResult.PASS;
                }

                InteractionResult result = updateControllerPosList(context,
                        controllerTile,
                        nodeTile);
                if (result == InteractionResult.SUCCESS) {
                    if(world.isClientSide)
                        controllerTile.rebuildRenderBounds();
                    compoundnbt.remove(EnergyNodeConstants.NBT_NODE_POS_KEY);
                    itemstack.setTag(compoundnbt);
                }
                return result;

            } else {
                return super.useOn(context);
            }
        }
        return InteractionResult.PASS;
    }

    // TODO - Split and transformed into link/unlink
    private static InteractionResult updateControllerPosList(@Nonnull UseOnContext context, EnergyControllerTile controller, EnergyNodeTile nodeTile) {
        final EnergyNodeBlock.Flow dir = nodeTile.getBlockState().getValue(EnergyNodeBlock.PROP_INOUT);
        Direction hit = context.getClickedFace();
        LazyOptional<IEnergyStorage> storage = nodeTile.getCapability(CapabilityEnergy.ENERGY, hit);

        BlockPos nodeFromController = nodeTile.getBlockPos().subtract(controller.getBlockPos());
        if (controller.connectedNodes.contains(nodeFromController)) {
            controller.connectedNodes.remove(nodeFromController);
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
            Utils.SendSystemMessage(context,new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.disconnected"), Utils.getCoordinatesAsString(nodeFromController)));
        } else {
            if (controller.connectedNodes.size() >= controller.tier.getMaxConnections()) {
                Utils.SendSystemMessage(context, new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.too_many_connections"), controller.tier.getMaxConnections()));
                return InteractionResult.PASS;
            }
            controller.connectedNodes.add(nodeFromController);
            switch (dir) {
                case IN:
                    controller.inputs.add(storage);
                    break;
                case OUT:
                    controller.outputs.add(storage);
                    break;
            }

            storage.addListener(removed -> {
                controller.connectedNodes.remove(nodeFromController);
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

            nodeTile.controllerPos = controller.getBlockPos().subtract(nodeTile.getBlockPos());
            nodeTile.energyStorage.setController(controller);
            Utils.SendSystemMessage(context, new TranslatableComponent(EnergyNodes.MOD_ID.concat(".linker.connected_to"), Utils.getCoordinatesAsString(nodeFromController)));
        }
        return InteractionResult.SUCCESS;
    }

}
