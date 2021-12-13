package org.zornco.energynodes.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty PROP_FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public EnergyControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_FACING, Direction.NORTH));
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
                    .setValue(PROP_FACING, getFacingFromEntity(context.getClickedPos(), context.getPlayer()));
        }
        return super.getStateForPlacement(context);
    }

    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState p_49232_) {
        return RenderShape.MODEL;
    }
    private static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        Direction facing = Direction.getNearest(
                (float) (entity.getX() - clickedBlock.getX()),
                (float) (entity.getY() - clickedBlock.getY()),
                (float) (entity.getZ() - clickedBlock.getZ()));

        if (facing.getAxis() == Direction.Axis.Y) {
            facing = Direction.NORTH;
        }

        return facing;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return createTickerHelper(type, Registration.ENERGY_CONTROLLER_TILE.get(), EnergyControllerTile::tick);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new EnergyControllerTile(pos, state);
    }
}
