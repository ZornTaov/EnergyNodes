package org.zornco.energynodes.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tile.EnergyControllerTile;

import java.util.function.Supplier;

public class PacketEnergyTransferredResponse {

    private final BlockPos pos;
    private final long transferredThisTick;

    public PacketEnergyTransferredResponse(EnergyControllerTile te) {
        this.transferredThisTick = te.transferredThisTick;
        this.pos = te.getBlockPos();
    }
    public PacketEnergyTransferredResponse(PacketBuffer buf) {
        this.transferredThisTick = buf.readLong();
        this.pos = buf.readBlockPos();
    }
    public static void encode(PacketEnergyTransferredResponse msg, PacketBuffer packetBuffer) {
        packetBuffer.writeLong(msg.transferredThisTick);
        packetBuffer.writeBlockPos(msg.pos);
    }
    public static void handle(PacketEnergyTransferredResponse msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                TileEntity te = world.getBlockEntity(msg.pos);
                if (!(te instanceof EnergyControllerTile)) {
                    EnergyNodes.LOGGER.warn("TileEntity is not a EnergyControllerTile!");
                    return;
                }
                ((EnergyControllerTile) te).transferredThisTick = msg.transferredThisTick;
            }
            ctx.setPacketHandled(true);
        });
    }
}
