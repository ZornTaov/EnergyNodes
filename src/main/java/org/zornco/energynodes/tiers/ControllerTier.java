package org.zornco.energynodes.tiers;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;
import java.util.Locale;

public class ControllerTier implements IControllerTier {
    private int maxTransfer;
    private int maxConnections;
    private int maxRange;

    public ControllerTier()
    {
        this(0,0,0);
    }

    ControllerTier(int maxTransfer, int maxConnections, int maxRange) {
        this.maxTransfer = maxTransfer;
        this.maxConnections = maxConnections;
        this.maxRange = maxRange;
    }

    public void setTier(IControllerTier newTier) {
        this.maxTransfer = newTier.getMaxTransfer();
        this.maxConnections = newTier.getMaxConnections();
        this.maxRange = newTier.getMaxRange();
    }

    @Override
    public int getMaxTransfer() {
        return maxTransfer;
    }

    @Override
    public int getMaxConnections() {
        return maxConnections;
    }

    @Override
    public int getMaxRange() {
        return maxRange;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return getClass().getName().toLowerCase(Locale.ROOT);
    }
}
