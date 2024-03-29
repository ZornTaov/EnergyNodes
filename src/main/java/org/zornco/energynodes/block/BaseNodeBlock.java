package org.zornco.energynodes.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.graph.Node;
import org.zornco.energynodes.network.NetworkManager;
import org.zornco.energynodes.network.packets.PacketRemoveNode;
import org.zornco.energynodes.tile.BaseNodeTile;
import org.zornco.energynodes.tile.IControllerTile;
import org.zornco.energynodes.tile.INodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class BaseNodeBlock<T extends BaseNodeTile> extends Block implements EntityBlock {

    private final Class<T> tileType;

    public enum Flow implements StringRepresentable {
        OUT,
        IN;

        @Nonnull
        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<Flow> PROP_INOUT = EnumProperty.create("inout", Flow.class);

    public BaseNodeBlock(Class<T> tileType, Properties properties, Flow flow) {
        super(properties);
        this.tileType = tileType;
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_INOUT, flow));
    }

    @Override
    public void onPlace(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos,
                        @Nonnull BlockState oldState, boolean isMoving) {
//        for (Direction facing : Direction.values()) {
//            BlockPos neighbor = pos.relative(facing);
//            connectToStorage(world, pos, facing, neighbor);
//        }
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos,
                                @Nonnull Block changedBlock, @Nonnull BlockPos neighbor, boolean flags) {
        Direction facing = Utils.getFacingFromBlockPos(neighbor, pos);
        connectToStorage(world, pos, facing, neighbor);
    }

    public static void connectToStorage(@Nonnull Level level, @Nonnull BlockPos pos,
                                        Direction facing, BlockPos neighbor) {
        if (level.getBlockEntity(pos) instanceof BaseNodeTile nodeTile && nodeTile.getFlow() == Flow.OUT && nodeTile.getController() != null) {
            BlockEntity otherTile = level.getBlockEntity(neighbor);
            if (otherTile != null && !(otherTile instanceof BaseNodeTile)) {
                LazyOptional<?> adjacentStorageOptional = otherTile.getCapability(nodeTile.getCapabilityType(), facing.getOpposite());

                if (adjacentStorageOptional.isPresent()) {
                    nodeTile.getController().getGraph().addOutputCap(neighbor, facing.getOpposite(), adjacentStorageOptional);
                }
            }
            //else
                //nodeTile.connectedTiles.remove(facing);
        }
    }

    @Override
    public void onRemove(@Nonnull BlockState oldState, @Nonnull Level world, @Nonnull BlockPos pos,
                         @Nonnull BlockState newState, boolean p_60519_) {
        if(world.getBlockEntity(pos) instanceof INodeTile nodeTile) {
            WeakReference<Node> nodeRef = nodeTile.getNodeRef();

            IControllerTile controllerTile = nodeTile.getController();
            if(nodeRef != null) {
                Node node = nodeRef.get();
                if (node != null && controllerTile != null) {
                    switch (oldState.getValue(PROP_INOUT)) {
                        case IN -> controllerTile.getGraph().removeInput(node.pos());
                        case OUT -> controllerTile.getGraph().removeOutput(node.pos());
                    }

                    NetworkManager.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() ->
                        world.getChunkAt(controllerTile.getBlockPos())), new PacketRemoveNode(controllerTile, node.pos()));
                }
            }
        }
        super.onRemove(oldState, world, pos, newState, p_60519_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROP_INOUT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        try {
            return tileType.getDeclaredConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
