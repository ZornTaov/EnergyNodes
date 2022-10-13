package org.zornco.energynodes.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;

public class EnergyControllerTile extends BaseControllerTile {

    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;

    public long transferredThisTick;

    public EnergyControllerTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_CONTROLLER_TILE.get(), pos, state, pos);

        if(!state.is(Registration.ENERGY_CONTROLLER_BLOCK.get()))
            EnergyNodes.LOGGER.fatal("Invalid Controller created!");
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);

        this.totalEnergyTransferred = tag.getLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY);
        this.transferredThisTick = tag.getLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY);
        this.totalEnergyTransferredLastTick = this.totalEnergyTransferred;
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalEnergyTransferred);
        tag.putLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
    }

    @Override
    public void tickAdditional(@Nonnull Level level) {
        if (!level.isClientSide){
            // Compute the FE transfer in this tick by taking the difference between total transfer this
            // tick and the total transfer last tick
            transferredThisTick = Math.abs(totalEnergyTransferred);

            totalEnergyTransferred = 0;
            if (transferredThisTick > 0) {
                setChanged();
            }

            if (ticks % 10 == 0) {
                setChanged();
            }
        }


    }

}
