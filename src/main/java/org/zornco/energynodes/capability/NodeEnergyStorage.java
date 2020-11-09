package org.zornco.energynodes.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeEnergyStorage implements IEnergyStorage, INBTSerializable<CompoundNBT> {
    private static final String NBT_ENERGY_KEY = "energy";
    private int energy;
    @Nonnull
    private final EnergyNodeTile nodeTile;
    @Nullable
    private EnergyControllerTile controllerTile;
    public NodeEnergyStorage(@Nonnull EnergyNodeTile tile) {
        this.nodeTile = tile;
    }
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(NBT_ENERGY_KEY, this.energy);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.energy = nbt.getInt(NBT_ENERGY_KEY);

    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (this.controllerTile != null) {
            return this.controllerTile.receiveEnergy(maxReceive, simulate);
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return nodeTile.getBlockState().get(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.OUT;
    }

    @Override
    public boolean canReceive() {
        if (this.controllerTile != null) {
            return this.controllerTile.canReceiveEnergy(nodeTile);
        }
        return false;
    }

    public void setController(EnergyControllerTile controllerTile) {
        this.controllerTile = controllerTile;
    }
}
