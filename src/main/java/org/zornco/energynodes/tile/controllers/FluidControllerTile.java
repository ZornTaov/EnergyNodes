package org.zornco.energynodes.tile.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.BaseControllerTile;

import javax.annotation.Nonnull;

public class FluidControllerTile extends BaseControllerTile {

    protected long totalFluidTransferred = 0;
    protected long totalFluidTransferredLastTick = 0;

    public long transferredThisTick;

    public FluidStack fluidStack;

    public FluidControllerTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.FLUID_CONTROLLER_TILE.get(), pos, state, pos);

        if(!state.is(Registration.FLUID_CONTROLLER_BLOCK.get()))
            EnergyNodes.LOGGER.fatal("Invalid Controller created!");
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        //TODO Load fluidStack
        this.totalFluidTransferred = tag.getLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.transferredThisTick = tag.getLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY);
        this.totalFluidTransferredLastTick = this.totalFluidTransferred;
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        //TODO Save fluidStack
        tag.putLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalFluidTransferred);
        tag.putLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
    }

    @Override
    public void tickAdditional(@Nonnull Level level) {
        if (!level.isClientSide){
            // Compute the FE transfer in this tick by taking the difference between total transfer this
            // tick and the total transfer last tick
            transferredThisTick = Math.abs(totalFluidTransferred);

            totalFluidTransferred = 0;
            if (transferredThisTick > 0) {
                setChanged();
            }

            if (ticks % 10 == 0) {
                setChanged();
            }
        }


    }

}
