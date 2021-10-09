package org.zornco.energynodes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleTransferred(Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, long transferredThisTick) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                TileEntity te = world.getBlockEntity(pos);
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("TileEntity is not a EnergyControllerTile!");
                    return;
                }
                ((EnergyControllerTile) te).transferredThisTick = transferredThisTick;
            }
            ctx.setPacketHandled(true);
        });
    }

    public static void handleSyncController(Supplier<NetworkEvent.Context> contextSupplier, BlockPos pos, IControllerTier tier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                TileEntity te = world.getBlockEntity(pos);
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("TileEntity is not a EnergyControllerTile!");
                    return;
                }
                ((EnergyControllerTile) te).setTier(tier);
            }
            ctx.setPacketHandled(true);
        });
    }
}
