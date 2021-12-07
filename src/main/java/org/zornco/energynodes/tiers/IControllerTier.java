package org.zornco.energynodes.tiers;

import net.minecraft.core.Direction;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public interface IControllerTier extends StringRepresentable {

    int getLevel();

    int getMaxTransfer();

    int getMaxConnections();

    int getMaxRange();

    void setTier(IControllerTier tier);
}
