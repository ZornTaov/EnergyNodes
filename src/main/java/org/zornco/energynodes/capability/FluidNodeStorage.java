package org.zornco.energynodes.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.zornco.energynodes.block.BaseNodeBlock;
import org.zornco.energynodes.tile.BaseNodeTile;

import javax.annotation.Nonnull;

public class FluidNodeStorage extends BaseNodeStorage implements IFluidHandler {
    @NotNull
    protected FluidStack fluid = FluidStack.EMPTY;

    public FluidNodeStorage(@Nonnull BaseNodeTile tile) {
        super(tile);
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        fluid.writeToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
        setFluid(fluid);
    }

    public void setFluid(@Nonnull FluidStack stack)
    {
        this.fluid = stack;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return fluid;
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluid.getAmount() * 2 + 1;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (this.controllerTile != null && nodeTile.getFlow() == BaseNodeBlock.Flow.IN) {
            //return this.controllerTile.receiveEnergy(nodeTile, maxReceive, simulate);
        }
        return 0;

    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    public void clearStorage() {
        fluid = FluidStack.EMPTY;
    }
}
