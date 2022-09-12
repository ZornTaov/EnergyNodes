package org.zornco.energynodes.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.block.EnergyNodeBlock;
import org.zornco.energynodes.tile.EnergyControllerTile;
import org.zornco.energynodes.tile.EnergyNodeTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeEnergyStorage implements IEnergyStorage, INBTSerializable<CompoundTag> {
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
        if (this.controllerTile != null && nodeTile.getBlockState().getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.IN) {
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
        return nodeTile.getBlockState().getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.OUT;
    }

    @Override
    public boolean canReceive() {
        if (this.controllerTile != null && nodeTile.getBlockState().getValue(EnergyNodeBlock.PROP_INOUT) == EnergyNodeBlock.Flow.IN) {
            //return this.controllerTile.canReceiveEnergy(nodeTile);
        }
        return false;
    }

    @Nonnull
    public EnergyNodeTile getNodeTile() {
        return nodeTile;
    }

    public BlockPos getLocation() {
        return this.nodeTile.getBlockPos();
    }

    public void setController(EnergyControllerTile controllerTile) {
        this.controllerTile = controllerTile;
    }

    @Nullable
    public EnergyControllerTile getControllerTile() {
        return controllerTile;
    }

    public void setEnergyStored(int amountReceivedThisBlock) {
        this.energy = amountReceivedThisBlock;
    }
}
