package org.zornco.energynodes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;
import mcjty.theoneprobe.api.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyNodeBlock extends Block implements IProbeInfoAccessor {

    public static class Flow {
        public static final boolean OUT = true;
        public static final boolean IN = false;
    }

    public static final BooleanProperty PROP_INOUT = BooleanProperty.create("inout");

    public EnergyNodeBlock(Properties properties, boolean flow) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PROP_INOUT, flow));
    }

    @Override
    public void onBlockAdded(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {
        if (state.get(PROP_INOUT) == Flow.OUT && !world.isRemote()) {
            for (Direction facing : Direction.values()) {
                BlockPos neighbor = pos.offset(facing);
                EnergyNodeTile nodeTile = (EnergyNodeTile) world.getTileEntity(pos);
                TileEntity otherTile = world.getTileEntity(neighbor);
                if (otherTile != null) {
                    LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                    if (adjacentStorageOptional.isPresent()) {
                        IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                                () -> new RuntimeException("Failed to get present adjacent storage for pos " + neighbor));
                        if (nodeTile != null) {
                            nodeTile.connectedTiles.put(neighbor, otherTile);
                        }
                    }
                }
                else
                if (nodeTile != null) {
                    nodeTile.connectedTiles.remove(neighbor);
                }
            }
        }
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        if (state.get(PROP_INOUT) == Flow.OUT && !world.isRemote())
        {
            EnergyNodeTile nodeTile = (EnergyNodeTile) world.getTileEntity(pos);
            TileEntity otherTile = world.getTileEntity(neighbor);
            if (otherTile != null) {
                Direction facing =  Direction.getFacingFromVector(
                        (float) (pos.getX() - neighbor.getX()),
                        (float) (pos.getY() - neighbor.getY()),
                        (float) (pos.getZ() - neighbor.getZ()));

                LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                if (adjacentStorageOptional.isPresent()) {
                    IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                            () -> new RuntimeException("Failed to get present adjacent storage for pos " + neighbor));
                    if (nodeTile != null) {
                        nodeTile.connectedTiles.put(neighbor, otherTile);
                    }
                }
            }
            else
            if (nodeTile != null) {
                nodeTile.connectedTiles.remove(neighbor);
            }
        }
    }

    @Override
    public void onReplaced(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        EnergyNodeTile tile = (EnergyNodeTile)worldIn.getTileEntity(pos);
        if (tile != null && tile.controllerPos != null) {
            EnergyControllerTile tile1 = (EnergyControllerTile)worldIn.getTileEntity(tile.controllerPos);
            if (tile1 != null) {
                (state.get(EnergyNodeBlock.PROP_INOUT) ? tile1.connectedOutputNodes : tile1.connectedInputNodes).remove(pos);
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROP_INOUT);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnergyNodeTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {

        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);
        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));
        EnergyNodeTile tile = (EnergyNodeTile) world.getTileEntity(iProbeHitData.getPos());
        if (tile != null && tile.controllerPos != null) {
            v.horizontal(center)
                    .text(new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".connected_to")))
                    .text(new StringTextComponent(Utils.getCoordinatesAsString(tile.controllerPos)));
            /*if (blockState.get(PROP_INOUT) == Flow.OUT )
                v.horizontal(center)
                        .text(new TranslationTextComponent(EnergyNodes.MOD_ID.concat(".connected_to")))
                        .text(new StringTextComponent(tile.connectedTiles.size() + ""));*/
        }
    }

}
