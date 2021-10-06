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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
    public ActionResultType useOn(@Nonnull ItemUseContext context) {
        BlockPos blockpos = context.getClickedPos();
        World world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();
        BlockState blockState = world.getBlockState(blockpos);
        CompoundNBT compoundnbt = itemstack.hasTag() ? itemstack.getTag() : new CompoundNBT();
        if (compoundnbt != null) {
            if (blockState.getBlock() instanceof EnergyNodeBlock) {
                compoundnbt.put(EnergyNodeConstants.NBT_NODE_POS_KEY, NBTUtil.writeBlockPos(blockpos));
                // TODO - convert to using lang instead
                Utils.SendSystemMessage(context, new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.start_connection"), Utils.getCoordinatesAsString(blockpos)));
                itemstack.setTag(compoundnbt);
                return ActionResultType.SUCCESS;

            } else if (blockState.getBlock() instanceof EnergyControllerBlock && compoundnbt.contains(EnergyNodeConstants.NBT_NODE_POS_KEY)) {
                EnergyControllerTile tile1 = (EnergyControllerTile) world.getBlockEntity(blockpos);
                BlockPos otherPos = NBTUtil.readBlockPos((CompoundNBT) Objects.requireNonNull(compoundnbt.get(EnergyNodeConstants.NBT_NODE_POS_KEY)));
                EnergyNodeTile tile2 = (EnergyNodeTile) world.getBlockEntity(otherPos);
                if (tile1 == null) {
                    Utils.SendSystemMessage(context, new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.controller_missing")));
                    return ActionResultType.PASS;
                }
                if (tile2 == null) {
                    Utils.SendSystemMessage(context, new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.node_missing"), Utils.getCoordinatesAsString(otherPos)));
                    return ActionResultType.PASS;
                }
                if (blockpos.distManhattan(otherPos) >= tile1.tier.getMaxRange()) {
                    Utils.SendSystemMessage(context, new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.node_out_of_range"), tile1.tier.getMaxRange()));
                    return ActionResultType.PASS;
                }

                ActionResultType result = updateControllerPosList(context,
                        tile1,
                        tile2);
                if (result == ActionResultType.SUCCESS) {
                    compoundnbt.remove(EnergyNodeConstants.NBT_NODE_POS_KEY);
                    itemstack.setTag(compoundnbt);
                }
                return result;

            } else {
                return super.useOn(context);
            }
        }
        return ActionResultType.PASS;
    }

    // TODO - Split and transformed into link/unlink
    private static ActionResultType updateControllerPosList(@Nonnull ItemUseContext context, EnergyControllerTile controller, EnergyNodeTile nodeTile) {
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
            Utils.SendSystemMessage(context,new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.disconnected"), Utils.getCoordinatesAsString(checkPos)));
        } else {
            if (controller.connectedNodes.size() >= controller.tier.getMaxConnections()) {
                Utils.SendSystemMessage(context, new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.too_many_connections"), controller.tier.getMaxConnections()));
                return ActionResultType.PASS;
            }
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
            Utils.SendSystemMessage(context, new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".linker.connected_to"), Utils.getCoordinatesAsString(checkPos)));
        }
        controller.rebuildRenderBounds();
        return ActionResultType.SUCCESS;
    }

}
