package org.zornco.energynodes.block;

import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.graph.Node;

import java.lang.ref.WeakReference;

public interface INodeTile {
    boolean canExtract(LazyOptional<?> adjacentStorageOptional);

    boolean canReceive(LazyOptional<?> adjacentStorageOptional);

    WeakReference<Node> getNodeRef();

    void setNodeRef(WeakReference<Node> nodeRef);

    void clearConnection();

    void connectController(IControllerNode controller);

    IControllerNode getController();

    Capability<?> getCapabilityType();

    BlockPos getBlockPos();

    BaseNodeBlock.Flow getFlow();
}
