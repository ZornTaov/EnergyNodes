package org.zornco.energynodes.tile.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.IControllerNode;
import org.zornco.energynodes.capability.FluidNodeStorage;
import org.zornco.energynodes.tile.BaseNodeTile;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidNodeTile extends BaseNodeTile {
    public final FluidNodeStorage fluidStorage;
    private final LazyOptional<FluidNodeStorage> fluid;

    public FluidNodeTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.FLUID_TRANSFER_TILE.get(), pos, state);
        this.fluidStorage = new FluidNodeStorage(this);
        this.fluid = LazyOptional.of(() -> this.fluidStorage);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluid.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluid.invalidate();
    }

    @Override
    public Capability<?> getCapabilityType() {
        return ForgeCapabilities.FLUID_HANDLER;
    }

    @Override
    public void clearConnection() {
        super.clearConnection();
        this.fluidStorage.setController(null);
        this.fluidStorage.clearStorage();
    }

    @Override
    public void connectController(IControllerNode inController) {
        super.connectController(inController);
        this.fluidStorage.setController(inController);
    }
}
