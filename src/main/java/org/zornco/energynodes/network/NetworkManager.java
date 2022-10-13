package org.zornco.energynodes.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.network.packets.*;
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
        INSTANCE.messageBuilder(PacketEnergyTransferredRequest.class,0, NetworkDirection.PLAY_TO_SERVER)
            .encoder(PacketEnergyTransferredRequest::encode)
            .decoder(PacketEnergyTransferredRequest::new)
            .consumerMainThread(PacketEnergyTransferredRequest::handle)
            .add();
        INSTANCE.messageBuilder(PacketEnergyTransferredResponse.class,1,NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketEnergyTransferredResponse::encode)
            .decoder(PacketEnergyTransferredResponse::new)
            .consumerMainThread(PacketEnergyTransferredResponse::handle)
            .add();
        INSTANCE.messageBuilder(PacketSyncControllerTier.class,2, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketSyncControllerTier::encode)
            .decoder(PacketSyncControllerTier::new)
            .consumerMainThread(PacketSyncControllerTier::handle)
            .add();
        INSTANCE.messageBuilder(PacketRemoveNode.class,3, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketRemoveNode::encode)
            .decoder(PacketRemoveNode::new)
            .consumerMainThread(PacketRemoveNode::handle)
            .add();
    }

    static long lastEnergyTransferredRequest = -1;
    public static void RequestEnergyTransferred(EnergyControllerTile te, int interval) {
        if (lastEnergyTransferredRequest == -1 || Objects.requireNonNull(te.getLevel()).getGameTime() - lastEnergyTransferredRequest >= interval) {
            lastEnergyTransferredRequest = interval;
            INSTANCE.sendToServer(new PacketEnergyTransferredRequest(te));
        }
    }
}
