package org.zornco.energynodes.item.old;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.block.old.EnergyControllerBlockOLD;
import org.zornco.energynodes.block.old.EnergyNodeBlockOLD;
import org.zornco.energynodes.tile.old.EnergyControllerTileOLD;
import org.zornco.energynodes.tile.old.EnergyNodeTileOLD;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EnergyLinkerItemOLD extends Item {

    public EnergyLinkerItemOLD() {
        super(new Properties()
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
            if (blockState.getBlock() instanceof EnergyNodeBlockOLD) {
                compoundnbt.put(EnergyNodeConstants.NBT_NODE_POS_KEY, NbtUtils.writeBlockPos(blockpos));
                Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.start_connection"), Utils.getCoordinatesAsString(blockpos)));
                itemstack.setTag(compoundnbt);
                return InteractionResult.SUCCESS;

            } else if (blockState.getBlock() instanceof EnergyControllerBlockOLD && compoundnbt.contains(EnergyNodeConstants.NBT_NODE_POS_KEY)) {
                EnergyControllerTileOLD controllerTile = (EnergyControllerTileOLD) world.getBlockEntity(blockpos);
                BlockPos nodePos = NbtUtils.readBlockPos((CompoundTag) Objects.requireNonNull(compoundnbt.get(EnergyNodeConstants.NBT_NODE_POS_KEY)));
                EnergyNodeTileOLD nodeTile = (EnergyNodeTileOLD) world.getBlockEntity(nodePos);
                if (controllerTile == null) {
                    Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.controller_missing")));
                    return InteractionResult.PASS;
                }
                if (nodeTile == null) {
                    Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.node_missing"), Utils.getCoordinatesAsString(nodePos)));
                    return InteractionResult.PASS;
                }
                if (blockpos.distManhattan(nodePos) >= controllerTile.tier.getMaxRange()) {
                    Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.node_out_of_range"), controllerTile.tier.getMaxRange()));
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
    private static InteractionResult updateControllerPosList(@Nonnull UseOnContext context, EnergyControllerTileOLD controller, EnergyNodeTileOLD nodeTile) {
        final EnergyNodeBlockOLD.Flow dir = nodeTile.getBlockState().getValue(EnergyNodeBlockOLD.PROP_INOUT);
        Direction hit = context.getClickedFace();
        LazyOptional<IEnergyStorage> storage = nodeTile.getCapability(ForgeCapabilities.ENERGY, hit);

        BlockPos nodeFromController = nodeTile.getBlockPos().subtract(controller.getBlockPos());
        if (controller.connectedNodes.contains(nodeFromController)) {
            controller.connectedNodes.remove(nodeFromController);
            switch (dir) {
                case IN -> controller.inputs.remove(storage);
                case OUT -> controller.outputs.remove(storage);
            }

            nodeTile.controllerPos = null;
            nodeTile.energyStorage.setController(null);
            nodeTile.energyStorage.setEnergyStored(0);
            Utils.SendSystemMessage(context,Component.translatable(EnergyNodes.MOD_ID.concat(".linker.disconnected"), Utils.getCoordinatesAsString(nodeFromController)));
        } else {
            if (controller.connectedNodes.size() >= controller.tier.getMaxConnections()) {
                Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.too_many_connections"), controller.tier.getMaxConnections()));
                return InteractionResult.PASS;
            }
            controller.connectedNodes.add(nodeFromController);
            switch (dir) {
                case IN -> controller.inputs.add(storage);
                case OUT -> controller.outputs.add(storage);
            }

            storage.addListener(removed -> {
                controller.connectedNodes.remove(nodeFromController);
                switch (dir) {
                    case IN -> controller.inputs.remove(removed);
                    case OUT -> controller.outputs.remove(removed);
                }

                controller.setChanged();
            });

            nodeTile.controllerPos = controller.getBlockPos().subtract(nodeTile.getBlockPos());
            nodeTile.energyStorage.setController(controller);
            Utils.SendSystemMessage(context, Component.translatable(EnergyNodes.MOD_ID.concat(".linker.connected_to"), Utils.getCoordinatesAsString(nodeFromController)));
        }
        return InteractionResult.SUCCESS;
    }

}
