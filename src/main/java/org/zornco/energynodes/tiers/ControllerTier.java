package org.zornco.energynodes.tiers;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;
import org.zornco.energynodes.Registration;

import javax.annotation.Nonnull;
import java.util.Locale;

public class ControllerTier extends ForgeRegistryEntry<ControllerTier> implements IControllerTier, INBTSerializable<Tag> {
    private String name;
    private int level;
    private int maxTransfer;
    private int maxConnections;
    private int maxRange;

    public ControllerTier()
    {
        this(Registration.BASE.get());
    }

    public ControllerTier(IControllerTier tier) {
        this.setTier(tier);
    }

    public ControllerTier(String name, int level, int maxTransfer, int maxConnections, int maxRange) {
        this.name = name;
        this.level = level;
        this.maxTransfer = maxTransfer;
        this.maxConnections = maxConnections;
        this.maxRange = maxRange;
    }

    @Nonnull
    public static ControllerTier getTierFromString(String name) {
        return Registration.TIERS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(tier -> tier.getSerializedName().contains(name))
                .findFirst()
                .orElse(Registration.BASE.get());
    }

    public void setTier(IControllerTier newTier) {
        this.name = newTier.getSerializedName();
        this.maxTransfer = newTier.getMaxTransfer();
        this.maxConnections = newTier.getMaxConnections();
        this.maxRange = newTier.getMaxRange();
    }

    @Override
    public int getLevel() {
        return level;
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
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public Tag serializeNBT() {
        return StringTag.valueOf(this.getSerializedName());
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        this.setTier(ControllerTier.getTierFromString(nbt.getAsString()));
    }
}
