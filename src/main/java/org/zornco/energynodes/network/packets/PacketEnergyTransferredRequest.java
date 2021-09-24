package org.zornco.energynodes.network.packets;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
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
    public PacketEnergyTransferredRequest(PacketBuffer buf) {
        this.transferredThisTick = buf.readLong();
        this.pos = buf.readBlockPos();
    }

    public static void encode(PacketEnergyTransferredRequest msg, PacketBuffer packetBuffer) {
        packetBuffer.writeLong(msg.transferredThisTick);
        packetBuffer.writeBlockPos(msg.pos);
    }

    public static void handle(PacketEnergyTransferredRequest msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            World world = Objects.requireNonNull(ctx.getSender()).getCommandSenderWorld();
            if (world.hasChunkAt(msg.pos)) {
                TileEntity te = world.getBlockEntity(msg.pos);
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("TileEntity is not a EnergyControllerTile!");
                    return;
                }
                NetworkManager.INSTANCE.sendTo(new PacketEnergyTransferredResponse((EnergyControllerTile) te),
                        ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
            ctx.setPacketHandled(true);
        });
    }
}
