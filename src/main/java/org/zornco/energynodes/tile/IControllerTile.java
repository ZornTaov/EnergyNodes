package org.zornco.energynodes.tile;

import net.minecraft.core.BlockPos;
import org.zornco.energynodes.graph.ConnectionGraph;
import org.zornco.energynodes.tiers.IControllerTier;


public interface IControllerTile {
    IControllerTier getTier();

    void rebuildRenderBounds();

    BlockPos getBlockPos();

    ConnectionGraph getGraph();

    void setChanged();

    int receiveInput(BaseNodeTile nodeTile, Object maxReceive, boolean simulate);

    boolean canReceiveInput(BaseNodeTile nodeTile);
}
