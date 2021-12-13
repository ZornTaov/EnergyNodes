package org.zornco.energynodes.tiers;

import net.minecraft.util.StringRepresentable;

public interface IControllerTier extends StringRepresentable {

    int getLevel();

    int getMaxTransfer();

    int getMaxConnections();

    int getMaxRange();

    void setTier(IControllerTier tier);
}
