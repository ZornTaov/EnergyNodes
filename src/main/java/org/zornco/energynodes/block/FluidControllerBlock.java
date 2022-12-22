package org.zornco.energynodes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.BaseControllerTile;
import org.zornco.energynodes.tile.controllers.FluidControllerTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

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

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                 @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result) {
        ItemStack itemInHand = player.getItemInHand(hand);
        Optional<FluidStack> fluidContained = FluidUtil.getFluidContained(itemInHand);
        if (fluidContained.isPresent() && level.getBlockEntity(pos) instanceof FluidControllerTile controller) {
            controller.fluidStack = fluidContained.get().copy();
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
