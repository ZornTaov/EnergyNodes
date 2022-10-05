package org.zornco.energynodes.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.block.BaseNodeBlock;
import org.zornco.energynodes.tile.BaseNodeTile;

import javax.annotation.Nonnull;

public class EnergyNodeStorage extends BaseNodeStorage implements IEnergyStorage {
    private static final String NBT_ENERGY_KEY = "energy";
    private int energy;

    public EnergyNodeStorage(@Nonnull BaseNodeTile tile) {
        super(tile);
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_ENERGY_KEY, this.energy);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.energy = nbt.getInt(NBT_ENERGY_KEY);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (this.getControllerTile() != null && getNodeTile().getFlow() == BaseNodeBlock.Flow.IN) {
            //return this.controllerTile.receiveEnergy(nodeTile, maxReceive, simulate);
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
        return this.energy * 2 + 1;
    }

    @Override
    public boolean canExtract() {
        return getNodeTile().getFlow() == BaseNodeBlock.Flow.OUT;
    }

    @Override
    public boolean canReceive() {
        if (this.getControllerTile() != null && getNodeTile().getFlow() == BaseNodeBlock.Flow.IN) {
            //return this.controllerTile.canReceiveEnergy(nodeTile);
        }
        return false;
    }

    public void setEnergyStored(int amountReceivedThisBlock) {
        this.energy = amountReceivedThisBlock;
    }
}
