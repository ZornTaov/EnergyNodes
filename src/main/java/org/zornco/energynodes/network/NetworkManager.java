package org.zornco.energynodes.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.network.packets.PacketEnergyTransferredRequest;
import org.zornco.energynodes.network.packets.PacketEnergyTransferredResponse;
import org.zornco.energynodes.tile.EnergyControllerTile;

import java.util.Objects;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EnergyNodes.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void Register()
    {
        INSTANCE.registerMessage(0, PacketEnergyTransferredRequest.class,PacketEnergyTransferredRequest::encode, PacketEnergyTransferredRequest::new, PacketEnergyTransferredRequest::handle);
        INSTANCE.registerMessage(1, PacketEnergyTransferredResponse.class,PacketEnergyTransferredResponse::encode, PacketEnergyTransferredResponse::new, PacketEnergyTransferredResponse::handle);
    }

    static long lastEnergyTransferredRequest = -1;
    public static void RequestEnergyTransferred(EnergyControllerTile te, int interval) {
        if (lastEnergyTransferredRequest == -1 || Objects.requireNonNull(te.getLevel()).getGameTime() - lastEnergyTransferredRequest >= interval)
            INSTANCE.sendToServer(new PacketEnergyTransferredRequest(te));
    }
}
