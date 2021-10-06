package org.zornco.energynodes.item;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;

public class SageManifestItem extends SwordItem {
    public SageManifestItem() {
        super(ItemTier.IRON, 3, -1.4F, new Properties());
    }

    @Override
    public int getEnchantmentValue() {
        return super.getEnchantmentValue() + 6;
    }
}
