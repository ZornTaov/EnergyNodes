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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public void onPlace(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {

    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Block changedBlock, @Nonnull BlockPos neighbor, boolean flags) {

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
