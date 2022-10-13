package org.zornco.energynodes.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.client.ClientPacketHandler;
import org.zornco.energynodes.tiers.ControllerTier;
import org.zornco.energynodes.tiers.IControllerTier;
import org.zornco.energynodes.tile.EnergyControllerTile;

import java.util.function.Supplier;

public class PacketSyncControllerTier {

    private final BlockPos pos;
    private final IControllerTier tier;

    public PacketSyncControllerTier(EnergyControllerTile te) {
        this.tier = te.getTier();
        this.pos = te.getBlockPos();
    }
    public PacketSyncControllerTier(FriendlyByteBuf buf) {
        this.tier = ControllerTier.getTierFromString(buf.readUtf());
        this.pos = buf.readBlockPos();
    }
    public static void encode(PacketSyncControllerTier msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUtf(msg.tier.getSerializedName());
        packetBuffer.writeBlockPos(msg.pos);
    }
    public static void handle(PacketSyncControllerTier msg, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientPacketHandler.handleSyncControllerTier(contextSupplier, msg.pos, msg.tier);
    }

}
