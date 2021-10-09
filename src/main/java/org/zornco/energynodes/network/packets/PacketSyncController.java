package org.zornco.energynodes.network.packets;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.zornco.energynodes.client.ClientPacketHandler;
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
        ClientPacketHandler.handleSyncController(contextSupplier, msg.pos, msg.tier);
    }

}
