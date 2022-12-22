package org.zornco.energynodes.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record SidedPos(Direction direction, BlockPos pos) {
}
