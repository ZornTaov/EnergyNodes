package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import java.util.Objects;

public class ItemStateGenerator extends ItemModelProvider {

    public ItemStateGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, EnergyNodes.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(Objects.requireNonNull(Registration.TEST_PAD_ITEM.get().getRegistryName()).getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/test_pad"));
        singleTexture(Objects.requireNonNull(Registration.ENERGY_LINKER_ITEM.get().getRegistryName()).getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/energy_linker"));
        singleTexture(Objects.requireNonNull(Registration.SAGE_MANIFEST_ITEM.get().getRegistryName()).getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/sages_manifest"));
        Registration.TIER_UPGRADES.stream().map(RegistryObject::get).forEach(tier ->
                singleTexture(Objects.requireNonNull(tier.getRegistryName()).getPath(),
                        new ResourceLocation("item/handheld"),
                        "layer0",
                        new ResourceLocation(EnergyNodes.MOD_ID, "item/" + tier.getRegistryName().getPath())));
    }
}
