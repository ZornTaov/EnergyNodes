package org.zornco.energynodes.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.EnergyControllerTile;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketEnergyTransferredRequest {

    private final BlockPos pos;
    private final long transferredThisTick;

    public PacketEnergyTransferredRequest(EnergyControllerTile te) {
        this.transferredThisTick = te.transferredThisTick;
        this.pos = te.getBlockPos();
    }
    public PacketEnergyTransferredRequest(FriendlyByteBuf buf) {
        this.transferredThisTick = buf.readLong();
        this.pos = buf.readBlockPos();
    }

    public static void encode(PacketEnergyTransferredRequest msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeLong(msg.transferredThisTick);
        packetBuffer.writeBlockPos(msg.pos);
    }

    public static void handle(PacketEnergyTransferredRequest msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Objects.requireNonNull(ctx.getSender()).getCommandSenderWorld();
            if (world.hasChunkAt(msg.pos)) {
                BlockEntity te = world.getBlockEntity(msg.pos);
                if (te == null) {
                    //EnergyNodes.LOGGER.warn("PacketEnergyTransferredRequest#handle: TileEntity is null!");
                    //ctx.setPacketHandled(false);
                    //throw new NullPointerException("PacketEnergyTransferredRequest#handle: TileEntity is null!");
                    return;
                }
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("PacketEnergyTransferredRequest#handle: TileEntity is not a EnergyControllerTile!");
                    //ctx.setPacketHandled(false);
                    return;
                }
                NetworkManager.INSTANCE.sendTo(new PacketEnergyTransferredResponse((EnergyControllerTile) te),
                        ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
            ctx.setPacketHandled(true);
        });
    }
}
