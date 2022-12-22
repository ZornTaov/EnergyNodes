package org.zornco.energynodes.tile.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.zornco.energynodes.EnergyNodeConstants;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.Utils;
import org.zornco.energynodes.graph.CapabilityNode;
import org.zornco.energynodes.graph.SidedPos;
import org.zornco.energynodes.tile.BaseControllerTile;
import org.zornco.energynodes.tile.BaseNodeTile;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class FluidControllerTile extends BaseControllerTile {

    protected long totalFluidTransferred = 0;
    protected long totalFluidTransferredLastTick = 0;

    public FluidStack fluidStack = FluidStack.EMPTY;

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

        if (tag.get(EnergyNodeConstants.NBT_FLUID_STACK_TYPE) != null)
            FluidStack.CODEC.decode(NbtOps.INSTANCE, tag.get(EnergyNodeConstants.NBT_FLUID_STACK_TYPE))
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(nBTPair -> this.fluidStack = nBTPair.getFirst());
;
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        //TODO Save fluidStack
        tag.putLong(EnergyNodeConstants.NBT_TOTAL_ENERGY_TRANSFERRED_KEY, this.totalFluidTransferred);
        tag.putLong(EnergyNodeConstants.NBT_TRANSFERRED_THIS_TICK_KEY, this.transferredThisTick);
        if (!fluidStack.isEmpty())
            FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, fluidStack)
                .resultOrPartial(EnergyNodes.LOGGER::error)
                .ifPresent(inbt -> tag.put(EnergyNodeConstants.NBT_FLUID_STACK_TYPE, inbt));
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

    @Override
    public int receiveInput(BaseNodeTile nodeTile, Object maxReceive, boolean simulate) {
        int outputCount = getGraph().getOutputNodes().size();
        if (outputCount == 0) return 0;
        if (!fluidStack.isFluidEqual((FluidStack)maxReceive)) return 0;
        int amountReceived = 0;
        FluidStack amountToTransfer = ((FluidStack)maxReceive).copy();
        FluidStack dummyFluidStack = ((FluidStack)maxReceive).copy();

        Predicate<IFluidHandler> storagePredicate = storage ->
            ((storage.getFluidInTank(0).isEmpty() || storage.getFluidInTank(0).isFluidEqual(fluidStack)) &&
                storage.isFluidValid(0, fluidStack) && storage.getFluidInTank(0).getAmount() != storage.getTankCapacity(0));
        Set<IFluidHandler> optionals = getGraph().getAllOutputs(storagePredicate);


        // Iterate over and distribute to valid optionals,
        long filteredOutputCount = optionals.size();
        for (var storage : optionals) {

            int transferredThisTile = 0;
            int amountReceivedThisBlock = 0;
            //if (storage.isPresent()) {
            //    IFluidHandler adjacentStorage = storage.map(IFluidHandler.class::cast).orElseThrow(
            //        () -> new RuntimeException("Failed to get present adjacent storage for pos " + this.worldPosition));
                {
                    int amountToSend = (int) Math.floor((this.tier.getMaxTransfer() == EnergyNodeConstants.UNLIMITED_RATE ?
                        (float) amountToTransfer.getAmount() / filteredOutputCount :
                        Math.min((float) amountToTransfer.getAmount(), this.tier.getMaxTransfer())) / filteredOutputCount);
                    dummyFluidStack.setAmount(amountToSend);
                    amountReceivedThisBlock = storage.fill(dummyFluidStack, simulate ? SIMULATE : EXECUTE);
                }
            //}

            if (!simulate) {
                transferredThisTile += amountReceivedThisBlock;
                this.totalFluidTransferred += amountReceivedThisBlock;
            }
            amountReceived += amountReceivedThisBlock;
        }
        return amountReceived;
    }
}
