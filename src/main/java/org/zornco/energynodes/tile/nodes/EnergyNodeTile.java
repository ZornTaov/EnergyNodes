package org.zornco.energynodes.tile.nodes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.zornco.energynodes.Registration;
import org.zornco.energynodes.block.IControllerNode;
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
    public void connectController(IControllerNode inController) {
        super.connectController(inController);
        this.energyStorage.setController((EnergyControllerTile) inController);
    }
}
