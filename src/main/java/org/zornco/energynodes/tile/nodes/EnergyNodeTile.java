package org.zornco.energynodes.tile.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.tile.IControllerTile;
import org.zornco.energynodes.capability.EnergyNodeStorage;
import org.zornco.energynodes.tile.BaseNodeTile;
import org.zornco.energynodes.tile.EnergyControllerTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyNodeTile extends BaseNodeTile {
    public final EnergyNodeStorage energyStorage;
    private final LazyOptional<EnergyNodeStorage> energy;

    public EnergyNodeTile(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(Registration.ENERGY_TRANSFER_TILE.get(), pos, state);
        this.energyStorage = new EnergyNodeStorage(this);
        this.energy = LazyOptional.of(() -> this.energyStorage);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY)
            return energy.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }

    @Override
    public Capability<?> getCapabilityType() {
        return ForgeCapabilities.ENERGY;
    }

    @Override
    public void clearConnection() {
        super.clearConnection();
        this.energyStorage.setController(null);
        this.energyStorage.setEnergyStored(0);
    }

    @Override
    public void connectController(IControllerTile inController) {
        super.connectController(inController);
        this.energyStorage.setController(inController);
    }

    public boolean canExtract(LazyOptional<?> adjacentStorageOptional) {
        final boolean[] validCap = {false};
        LazyOptional<IEnergyStorage> cap = adjacentStorageOptional.cast();
        cap.ifPresent( cap2-> {
            int i = cap2.extractEnergy(1, true);
            validCap[0] = i > 0;
        });
        return validCap[0];
    }

    public boolean canReceive(LazyOptional<?> adjacentStorageOptional) {
        final boolean[] validCap = {false};
        LazyOptional<IEnergyStorage> cap = adjacentStorageOptional.cast();
        cap.ifPresent( cap2-> {
            int i = cap2.receiveEnergy(1, true);
            validCap[0] = i > 0;
        });
        return validCap[0];
    }
}
