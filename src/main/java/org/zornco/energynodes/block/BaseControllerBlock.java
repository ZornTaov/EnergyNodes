package org.zornco.energynodes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty PROP_FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public BaseControllerBlock(Properties p_49224_) {
        super(p_49224_);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_FACING, Direction.NORTH));
    }

    private static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        return Direction.getNearest(
            (float) (entity.getX() - clickedBlock.getX()),
            0,
            (float) (entity.getZ() - clickedBlock.getZ()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROP_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() != null) {
            return this.defaultBlockState()
                .setValue(PROP_FACING, BaseControllerBlock.getFacingFromEntity(context.getClickedPos(), context.getPlayer()));
        }
        return super.getStateForPlacement(context);
    }

    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState p_49232_) {
        return RenderShape.MODEL;
    }

}
