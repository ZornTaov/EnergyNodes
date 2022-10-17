package org.zornco.energynodes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.BaseControllerTile;
import org.zornco.energynodes.tile.controllers.FluidControllerTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidControllerBlock extends BaseControllerBlock {

    public FluidControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        return createTickerHelper(type, Registration.FLUID_CONTROLLER_TILE.get(), BaseControllerTile::tickCommon);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new FluidControllerTile(pos, state);
    }
}
