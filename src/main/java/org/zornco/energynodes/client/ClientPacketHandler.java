package org.zornco.energynodes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleTransferred(Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, long transferredThisTick) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te == null) {
                    //EnergyNodes.LOGGER.warn("ClientPacketHandler#handleTransferred: TileEntity is null!");
                    //ctx.setPacketHandled(false);
                    //throw new NullPointerException("ClientPacketHandler#handleTransferred: TileEntity is null!");
                    return;
                }
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleTransferred: TileEntity is not a EnergyControllerTile!");
                    //ctx.setPacketHandled(false);
                    return;
                }
                ((EnergyControllerTile) te).transferredThisTick = transferredThisTick;
            }
            ctx.setPacketHandled(true);
        });
    }

    public static void handleSyncControllerTier(@Nonnull Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, IControllerTier tier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te == null) {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncController: TileEntity is null!");
                    throw new NullPointerException("ClientPacketHandler#handleSyncController: TileEntity is null!");
                }
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncController: TileEntity is not a EnergyControllerTile!");
                    return;
                }
                ((EnergyControllerTile) te).setTier(tier);
            }
            ctx.setPacketHandled(true);
        });
    }
    public static void handleRemoveNode(@Nonnull Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, BlockPos nodePos) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                BlockEntity te = world.getBlockEntity(pos);
                if (te == null) {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncController: TileEntity is null!");
                    throw new NullPointerException("ClientPacketHandler#handleSyncController: TileEntity is null!");
                }
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("ClientPacketHandler#handleSyncController: TileEntity is not a EnergyControllerTile!");
                    return;
                }
                ((EnergyControllerTile) te).getGraph().removeInput(nodePos);
                ((EnergyControllerTile) te).getGraph().removeOutput(nodePos);
                if (world.isClientSide) ((EnergyControllerTile) te).rebuildRenderBounds();

            }
            ctx.setPacketHandled(true);
        });
    }
}
