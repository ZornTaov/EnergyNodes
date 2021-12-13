package org.zornco.energynodes.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.tile.EnergyNodeTile;
//import mcjty.theoneprobe.api.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class EnergyNodeBlock extends Block implements EntityBlock {

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

    public EnergyNodeBlock(Properties properties, Flow flow) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_INOUT, flow));
    }

    @Override
    public void onPlace(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {
        if (state.getValue(PROP_INOUT) == Flow.OUT && !world.isClientSide()) {
            for (Direction facing : Direction.values()) {
                BlockPos neighbor = pos.relative(facing);
                connectToEnergyStorage(world, pos, facing, neighbor);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block changedBlock, @Nonnull BlockPos neighbor, boolean flags) {
        if (state.getValue(PROP_INOUT) == Flow.OUT && !world.isClientSide())
        {
            Direction facing = Utils.getFacingFromBlockPos(neighbor, pos);
            connectToEnergyStorage(world, pos, facing, neighbor);
        }
    }

    public static void connectToEnergyStorage(@Nonnull Level world, @Nonnull BlockPos pos, Direction facing, BlockPos neighbor) {
        EnergyNodeTile nodeTile = (EnergyNodeTile) world.getBlockEntity(pos);
        if (nodeTile != null) {
        BlockEntity otherTile = world.getBlockEntity(neighbor);
        if (otherTile != null && !(otherTile instanceof EnergyNodeTile)) {
            LazyOptional<IEnergyStorage> adjacentStorageOptional = otherTile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
            if (adjacentStorageOptional.isPresent()) {
                IEnergyStorage adjacentStorage = adjacentStorageOptional.orElseThrow(
                        () -> new RuntimeException("Failed to get present adjacent storage for pos " + neighbor));
                int i = adjacentStorage.receiveEnergy(1, true);
                if (i >0)
                    nodeTile.connectedTiles.put(facing, otherTile);
            }
        }
        else
            nodeTile.connectedTiles.remove(facing);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROP_INOUT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new EnergyNodeTile(pos, state);
    }
}
