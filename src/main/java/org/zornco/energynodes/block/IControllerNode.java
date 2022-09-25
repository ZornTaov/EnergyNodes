package org.zornco.energynodes.block;

import net.minecraft.core.BlockPos;
import org.zornco.energynodes.graph.ConnectionGraph;
import org.zornco.energynodes.tiers.IControllerTier;

import java.util.Collection;

public interface IControllerNode {
    IControllerTier getTier();

    void rebuildRenderBounds();

    BlockPos getBlockPos();

    ConnectionGraph getGraph();

    void setChanged();
}
