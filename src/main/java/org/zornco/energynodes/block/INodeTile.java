package org.zornco.energynodes.block;

import net.minecraftforge.common.capabilities.Capability;
import org.zornco.energynodes.graph.Node;

import java.lang.ref.WeakReference;

public interface INodeTile {
    WeakReference<Node> getNodeRef();

    void setNodeRef(WeakReference<Node> nodeRef);

    void clearConnection();

    void connectController(IControllerNode controller);

    Capability<?> getCapabilityType();
}
