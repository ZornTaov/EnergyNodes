package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

public class ItemStateGenerator extends ItemModelProvider {

    public ItemStateGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, EnergyNodes.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(Registration.TEST_PAD_ITEM.get().getRegistryName().getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/test_pad"));
        singleTexture(Registration.ENERGY_LINKER_ITEM.get().getRegistryName().getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/energy_linker"));
    }
}
