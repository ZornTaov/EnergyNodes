package org.zornco.energynodes.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.client.ClientPacketHandler;

import java.util.function.Supplier;

public class PacketRemoveNode {

    private final BlockPos pos;
    private final BlockPos node;

    public PacketRemoveNode(IControllerNode te, BlockPos nodePos) {
        this.node = nodePos;
        this.pos = te.getBlockPos();
    }
    public PacketRemoveNode(FriendlyByteBuf buf) {
        this.node = buf.readBlockPos();
        this.pos = buf.readBlockPos();
    }
    public static void encode(PacketRemoveNode msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeBlockPos(msg.node);
        packetBuffer.writeBlockPos(msg.pos);
    }
    public static void handle(PacketRemoveNode msg, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientPacketHandler.handleRemoveNode(contextSupplier, msg.pos, msg.node);
    }

}
