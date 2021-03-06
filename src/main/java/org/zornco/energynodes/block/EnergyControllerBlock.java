package org.zornco.energynodes.block;

import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyControllerBlock extends Block implements IProbeInfoAccessor {
    public static final DirectionProperty PROP_FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public EnergyControllerBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PROP_FACING, Direction.NORTH));
    }

    @Override
    public void onReplaced(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        EnergyControllerTile tile = (EnergyControllerTile)worldIn.getTileEntity(pos);
        if (tile != null && tile.connectedInputNodes.size() != 0) {
            for (BlockPos nodePos: tile.connectedInputNodes) {
                EnergyNodeTile tile1 = (EnergyNodeTile)worldIn.getTileEntity(nodePos);
                if (tile1 != null) {
                    tile1.controllerPos = null;
                }
            }
        }
        if (tile != null && tile.connectedOutputNodes.size() != 0) {
            for (BlockPos nodePos: tile.connectedOutputNodes) {
                EnergyNodeTile tile1 = (EnergyNodeTile)worldIn.getTileEntity(nodePos);
                if (tile1 != null) {
                    tile1.controllerPos = null;
                }
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
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

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {

        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);
        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));
        EnergyControllerTile tile = (EnergyControllerTile) world.getTileEntity(iProbeHitData.getPos());
        if (tile != null) {
            v.horizontal(center)
                    .text(new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".transferred")))
                    .text(new StringTextComponent(tile.transferredThisTick + ""));
        }
    }
}
