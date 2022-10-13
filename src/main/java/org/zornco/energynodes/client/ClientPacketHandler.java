package org.zornco.energynodes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.block.BaseNodeBlock;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.BaseNodeTile;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleTransferred(Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, long transferredThisTick) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                //TODO EnergyControllerTile
                if (world.getBlockEntity(pos) instanceof EnergyControllerTile controller) {
                    controller.transferredThisTick = transferredThisTick;
                }else {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleTransferred: TileEntity is not a EnergyControllerTile!");
                    return;
                }
            }
            ctx.setPacketHandled(true);
        });
    }

    public static void handleSyncControllerTier(@Nonnull Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, IControllerTier tier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                //TODO EnergyControllerTile
                if (world.getBlockEntity(pos) instanceof EnergyControllerTile controller) {
                    controller.setTier(tier);
                }else {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncController: TileEntity is not a EnergyControllerTile!");
                    return;
                }
            }
            ctx.setPacketHandled(true);
        });
    }
    public static void handleRemoveNode(@Nonnull Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, BlockPos nodePos) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                if (world.getBlockEntity(pos) instanceof IControllerNode controller) {
                    controller.getGraph().removeInput(nodePos);
                    controller.getGraph().removeOutput(nodePos);
                    if (world.isClientSide) controller.rebuildRenderBounds();
                }
                else {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleRemoveNode: TileEntity is not a EnergyControllerTile!");
                }
            }
            ctx.setPacketHandled(true);
        });
    }

    public static void handleSyncNodeData(Supplier<NetworkEvent.Context> contextSupplier, BlockPos controllerPos, BlockPos nodePos) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                if (world.getBlockEntity(nodePos) instanceof BaseNodeTile node) {
                    if (world.getBlockEntity(controllerPos) instanceof IControllerNode controller) {
                        node.controller = controller;
                        final BaseNodeBlock.Flow flowDir = node.getBlockState().getValue(BaseNodeBlock.PROP_INOUT);
                        node.setNodeRef(controller.getGraph().getNode(flowDir, nodePos));
                    }
                    else {
                        EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncNodeData: TileEntity is not a Controller Tile!");
                    }
                }
                else {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncNodeData: TileEntity is not a Node Tile!");
                }
            }
            ctx.setPacketHandled(true);
        });
    }
}
