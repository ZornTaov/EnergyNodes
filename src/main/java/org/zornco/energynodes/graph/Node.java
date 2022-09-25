package org.zornco.energynodes.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public record Node(BlockPos pos, List<Direction> connections) {
    public static final Codec<Node> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Node::pos),
            Direction.CODEC.listOf().fieldOf("connections").forGetter(Node::connections)
        ).apply(i,Node::new));


}
