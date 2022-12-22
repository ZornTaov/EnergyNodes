package org.zornco.energynodes.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.tile.BaseControllerTile;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketTransferredRequest {

    private final BlockPos pos;
    private final long transferredThisTick;

    public PacketTransferredRequest(BaseControllerTile te) {
        this.transferredThisTick = te.transferredThisTick;
        this.pos = te.getBlockPos();
    }
    public PacketTransferredRequest(FriendlyByteBuf buf) {
        this.transferredThisTick = buf.readLong();
        this.pos = buf.readBlockPos();
    }

    public static void encode(PacketTransferredRequest msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeLong(msg.transferredThisTick);
        packetBuffer.writeBlockPos(msg.pos);
    }

    public static void handle(PacketTransferredRequest msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            Level world = Objects.requireNonNull(ctx.getSender()).getCommandSenderWorld();
            if (world.hasChunkAt(msg.pos)) {
                BlockEntity te = world.getBlockEntity(msg.pos);
                if (te == null) {
                    //EnergyNodes.LOGGER.warn("PacketTransferredRequest#handle: TileEntity is null!");
                    //ctx.setPacketHandled(false);
                    //throw new NullPointerException("PacketTransferredRequest#handle: TileEntity is null!");
                    return;
                }
                if (!(te instanceof BaseControllerTile)) {
                    EnergyNodes.LOGGER.warn("PacketTransferredRequest#handle: TileEntity is not a BaseControllerTile!");
                    //ctx.setPacketHandled(false);
                    return;
                }
                NetworkManager.INSTANCE.sendTo(new PacketTransferredResponse((BaseControllerTile) te),
                        ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
            ctx.setPacketHandled(true);
        });
    }
}
