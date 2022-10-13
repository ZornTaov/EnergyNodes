package org.zornco.energynodes.tile;

import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.zornco.energynodes.block.BaseNodeBlock;
import org.zornco.energynodes.graph.Node;

import java.lang.ref.WeakReference;

public interface INodeTile {
    boolean canExtract(LazyOptional<?> adjacentStorageOptional);

    boolean canReceive(LazyOptional<?> adjacentStorageOptional);

    WeakReference<Node> getNodeRef();

    void setNodeRef(WeakReference<Node> nodeRef);

    void clearConnection();

    void connectController(IControllerTile controller);

    IControllerTile getController();

    Capability<?> getCapabilityType();

    BlockPos getBlockPos();

    BaseNodeBlock.Flow getFlow();
}
