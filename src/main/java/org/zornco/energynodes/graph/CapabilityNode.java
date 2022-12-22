package org.zornco.energynodes.graph;

import net.minecraftforge.common.util.LazyOptional;

public record CapabilityNode(LazyOptional<?> cap, SidedPos sPos) implements IGraphNode  {
}
