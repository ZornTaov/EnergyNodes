package org.zornco.energynodes.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;

import java.util.function.Supplier;

public class PacketSyncController {

    private final BlockPos pos;
    private final IControllerTier tier;

    public PacketSyncController(EnergyControllerTile te) {
        this.tier = te.tier;
        this.pos = te.getBlockPos();
    }
    public PacketSyncController(PacketBuffer buf) {
        this.tier = ControllerTier.getTierFromString(buf.readUtf());
        this.pos = buf.readBlockPos();
    }
    public static void encode(PacketSyncController msg, PacketBuffer packetBuffer) {
        packetBuffer.writeUtf(msg.tier.getSerializedName());
        packetBuffer.writeBlockPos(msg.pos);
    }
    public static void handle(PacketSyncController msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                TileEntity te = world.getBlockEntity(msg.pos);
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("TileEntity is not a EnergyControllerTile!");
                    return;
                }
                ((EnergyControllerTile) te).setTier(msg.tier);
            }
            ctx.setPacketHandled(true);
        });
    }

}
