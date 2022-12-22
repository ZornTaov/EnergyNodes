package org.zornco.energynodes.tile.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.graph.CapabilityNode;
import org.zornco.energynodes.graph.SidedPos;
import org.zornco.energynodes.tile.BaseControllerTile;
import org.zornco.energynodes.tile.BaseNodeTile;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public class EnergyControllerTile extends BaseControllerTile {

    protected long totalEnergyTransferred = 0;
    protected long totalEnergyTransferredLastTick = 0;

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
        }
    }

    @Override
    public int receiveInput(BaseNodeTile nodeTile, Object maxReceive, boolean simulate) {
        int outputCount = getGraph().getOutputNodes().size();
        if (outputCount == 0) return 0;
        int amountReceived = 0;
        int totalAmount, amountToTransfer = totalAmount = (int)maxReceive;
        Predicate<IEnergyStorage> storagePredicate = storage ->
            (storage.canReceive() && storage.getEnergyStored() != storage.getMaxEnergyStored());
        Set<IEnergyStorage> optionals = getGraph().getAllOutputs(storagePredicate);

        // Iterate over and distribute to valid optionals,
        long filteredOutputCount = optionals.size();
        for (var storage : optionals) {

            int transferredThisTile = 0;
            int amountReceivedThisBlock = 0;
            {
                //IEnergyStorage adjacentStorage = storage.map(IEnergyStorage.class::cast).orElseThrow(
                //    () -> new RuntimeException("Failed to get present adjacent storage for pos " + this.worldPosition));
                {
                    int amountToSend = (int) Math.floor((this.tier.getMaxTransfer() == EnergyNodeConstants.UNLIMITED_RATE ?
                        (float) amountToTransfer / filteredOutputCount :
                        Math.min(amountToTransfer, this.tier.getMaxTransfer())) / filteredOutputCount);
                    amountReceivedThisBlock = storage.receiveEnergy(amountToSend, simulate);
                }
            }

            if (!simulate) {
                transferredThisTile += amountReceivedThisBlock;
                this.totalEnergyTransferred += amountReceivedThisBlock;
            }
            amountReceived += amountReceivedThisBlock;
        }
        return amountReceived;
    }
}
