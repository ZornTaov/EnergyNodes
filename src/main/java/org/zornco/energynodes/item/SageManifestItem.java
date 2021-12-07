package org.zornco.energynodes.item;

import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;

import net.minecraft.world.item.Item.Properties;

public class SageManifestItem extends SwordItem {
    public SageManifestItem() {
        super(Tiers.IRON, 3, -1.4F, new Properties());
    }

    @Override
    public int getEnchantmentValue() {
        return super.getEnchantmentValue() + 6;
    }
}
