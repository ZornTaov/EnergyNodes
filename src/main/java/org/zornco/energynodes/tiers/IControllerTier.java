package org.zornco.energynodes.tiers;

import net.minecraft.util.IStringSerializable;

public interface IControllerTier extends IStringSerializable {

    int getLevel();

    int getMaxTransfer();

    int getMaxConnections();

    int getMaxRange();

    void setTier(IControllerTier tier);
}
