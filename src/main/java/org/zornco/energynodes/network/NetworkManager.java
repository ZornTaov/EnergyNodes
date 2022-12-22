package org.zornco.energynodes.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.network.packets.*;
import org.zornco.energynodes.tile.BaseControllerTile;

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
        INSTANCE.messageBuilder(PacketTransferredRequest.class,0, NetworkDirection.PLAY_TO_SERVER)
            .encoder(PacketTransferredRequest::encode)
            .decoder(PacketTransferredRequest::new)
            .consumerMainThread(PacketTransferredRequest::handle)
            .add();
        INSTANCE.messageBuilder(PacketTransferredResponse.class,1,NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketTransferredResponse::encode)
            .decoder(PacketTransferredResponse::new)
            .consumerMainThread(PacketTransferredResponse::handle)
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
        INSTANCE.messageBuilder(PacketSyncNodeData.class,4, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PacketSyncNodeData::encode)
            .decoder(PacketSyncNodeData::new)
            .consumerMainThread(PacketSyncNodeData::handle)
            .add();
    }

    public static void RequestTransferred(BaseControllerTile te, int interval) {
        if (te.lastTransferredRequest == -1 || Objects.requireNonNull(te.getLevel()).getGameTime() - te.lastTransferredRequest >= interval) {
            te.lastTransferredRequest = interval;
            INSTANCE.sendToServer(new PacketTransferredRequest(te));
        }
    }
}
