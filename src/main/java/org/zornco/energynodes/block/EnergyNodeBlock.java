package org.zornco.energynodes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.tile.EnergyNodeTile;
import mcjty.theoneprobe.api.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyNodeBlock extends Block {

    public enum Flow implements IStringSerializable {
        OUT,
        IN;

        @Nonnull
        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<Flow> PROP_INOUT = EnumProperty.create("inout", Flow.class);

    public EnergyNodeBlock(Properties properties, Flow flow) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_INOUT, flow));
    }

    @Override
    public void onPlace(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {
        if (state.getValue(PROP_INOUT) == Flow.OUT && !world.isClientSide()) {
            for (Direction facing : Direction.values()) {
                BlockPos neighbor = pos.relative(facing);
                connectToEnergyStorage(world, pos, facing, neighbor);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block changedBlock, @Nonnull BlockPos neighbor, boolean flags) {
        if (state.getValue(PROP_INOUT) == Flow.OUT && !world.isClientSide())
        {
            Direction facing = Utils.getFacingFromBlockPos(pos, neighbor);
            connectToEnergyStorage(world, pos, facing, neighbor);
        }
    }

    private void connectToEnergyStorage(@Nonnull World world, @Nonnull BlockPos pos, Direction facing, BlockPos neighbor) {
        EnergyNodeTile nodeTile = (EnergyNodeTile) world.getBlockEntity(pos);
        TileEntity otherTile = world.getBlockEntity(neighbor);
        if (otherTile != null) {
            LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
            if (adjacentStorageOptional.isPresent()) {
                /*IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                        () -> new RuntimeException("Failed to get present adjacent storage for pos " + neighbor));*/
                if (nodeTile != null) {
                    nodeTile.connectedTiles.put(facing, otherTile);
                }
            }
        }
        else
        if (nodeTile != null) {
            nodeTile.connectedTiles.remove(facing);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
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
}
