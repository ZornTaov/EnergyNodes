package org.zornco.energynodes.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.network.client.ClientPacketHandler;
import org.zornco.energynodes.tile.BaseControllerTile;

import java.util.function.Supplier;

public class PacketTransferredResponse {

    private final BlockPos pos;
    private final long transferredThisTick;

    public PacketTransferredResponse(BaseControllerTile te) {
        this.transferredThisTick = te.transferredThisTick;
        this.pos = te.getBlockPos();
    }
    public PacketTransferredResponse(FriendlyByteBuf buf) {
        this.transferredThisTick = buf.readLong();
        this.pos = buf.readBlockPos();
    }
    public static void encode(PacketTransferredResponse msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeLong(msg.transferredThisTick);
        packetBuffer.writeBlockPos(msg.pos);
    }
    public static void handle(PacketTransferredResponse msg, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientPacketHandler.handleTransferred(contextSupplier, msg.pos, msg.transferredThisTick);
    }

}
