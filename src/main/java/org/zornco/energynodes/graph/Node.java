package org.zornco.energynodes.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.zornco.energynodes.tile.INodeTile;

import java.util.List;

public record Node(BlockPos pos) implements IGraphNode {
    public static final Codec<Node> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Node::pos)
        ).apply(i,Node::new));

    public INodeTile getTile(Level level)
    {
        return (INodeTile)level.getBlockEntity(pos());
    }
}
