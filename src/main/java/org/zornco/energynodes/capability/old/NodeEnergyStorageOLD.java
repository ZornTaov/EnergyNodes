package org.zornco.energynodes.capability.old;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;
import org.zornco.energynodes.block.old.EnergyNodeBlockOLD;
import org.zornco.energynodes.tile.old.EnergyControllerTileOLD;
import org.zornco.energynodes.tile.old.EnergyNodeTileOLD;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodeEnergyStorageOLD implements IEnergyStorage, INBTSerializable<CompoundTag> {
    private static final String NBT_ENERGY_KEY = "energy";
    private int energy;
    @Nonnull
    private final EnergyNodeTileOLD nodeTile;

    @Nullable
    private EnergyControllerTileOLD controllerTile;
    public NodeEnergyStorageOLD(@Nonnull EnergyNodeTileOLD tile) {
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
        if (this.controllerTile != null && nodeTile.getBlockState().getValue(EnergyNodeBlockOLD.PROP_INOUT) == EnergyNodeBlockOLD.Flow.IN) {
            return this.controllerTile.receiveEnergy(nodeTile, maxReceive, simulate);
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
        return nodeTile.getBlockState().getValue(EnergyNodeBlockOLD.PROP_INOUT) == EnergyNodeBlockOLD.Flow.OUT;
    }

    @Override
    public boolean canReceive() {
        if (this.controllerTile != null && nodeTile.getBlockState().getValue(EnergyNodeBlockOLD.PROP_INOUT) == EnergyNodeBlockOLD.Flow.IN) {
            return this.controllerTile.canReceiveEnergy(nodeTile);
        }
        return false;
    }

    @Nonnull
    public EnergyNodeTileOLD getNodeTile() {
        return nodeTile;
    }

    public BlockPos getLocation() {
        return this.nodeTile.getBlockPos();
    }

    public void setController(EnergyControllerTileOLD controllerTile) {
        this.controllerTile = controllerTile;
    }

    @Nullable
    public EnergyControllerTileOLD getControllerTile() {
        return controllerTile;
    }

    public void setEnergyStored(int amountReceivedThisBlock) {
        this.energy = amountReceivedThisBlock;
    }
}
