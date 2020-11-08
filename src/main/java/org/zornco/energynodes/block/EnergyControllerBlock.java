package org.zornco.energynodes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nullable;

public class EnergyControllerBlock extends Block {
    public static final DirectionProperty PROP_FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public EnergyControllerBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PROP_FACING, Direction.NORTH));
    }
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROP_FACING);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnergyControllerTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getPlayer() != null) {
            return this.getDefaultState()
                    .with(PROP_FACING, getFacingFromEntity(context.getPos(), context.getPlayer()));
        }
        return super.getStateForPlacement(context);
    }

    private static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entity) {
        Direction facing =  Direction.getFacingFromVector(
                (float) (entity.getPosX() - clickedBlock.getX()),
                (float) (entity.getPosY() - clickedBlock.getY()),
                (float) (entity.getPosZ() - clickedBlock.getZ()));

        if (facing.getAxis() == Direction.Axis.Y) {
            facing = Direction.NORTH;
        }

        return facing;
    }
}
