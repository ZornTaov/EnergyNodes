package org.zornco.energynodes.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.client.ClientPacketHandler;
import org.zornco.energynodes.tile.controllers.EnergyControllerTile;

import java.util.function.Supplier;

public class PacketEnergyTransferredResponse {

    private final BlockPos pos;
    private final long transferredThisTick;

    public PacketEnergyTransferredResponse(EnergyControllerTile te) {
        this.transferredThisTick = te.transferredThisTick;
        this.pos = te.getBlockPos();
    }
    public PacketEnergyTransferredResponse(FriendlyByteBuf buf) {
        this.transferredThisTick = buf.readLong();
        this.pos = buf.readBlockPos();
    }
    public static void encode(PacketEnergyTransferredResponse msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeLong(msg.transferredThisTick);
        packetBuffer.writeBlockPos(msg.pos);
    }
    public static void handle(PacketEnergyTransferredResponse msg, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientPacketHandler.handleTransferred(contextSupplier, msg.pos, msg.transferredThisTick);
    }

}
