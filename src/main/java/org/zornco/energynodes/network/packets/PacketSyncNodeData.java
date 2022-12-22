package org.zornco.energynodes.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.network.client.ClientPacketHandler;

import java.util.function.Supplier;

public class PacketSyncNodeData {

    private final BlockPos controller;
    private final BlockPos node;

    public PacketSyncNodeData(BlockPos controllerPos, BlockPos nodePos) {
        this.node = nodePos;
        this.controller = controllerPos;
    }
    public PacketSyncNodeData(FriendlyByteBuf buf) {
        this.node = buf.readBlockPos();
        this.controller = buf.readBlockPos();
    }
    public static void encode(PacketSyncNodeData msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeBlockPos(msg.node);
        packetBuffer.writeBlockPos(msg.controller);
    }
    public static void handle(PacketSyncNodeData msg, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientPacketHandler.handleSyncNodeData(contextSupplier, msg.controller, msg.node);
    }

}
