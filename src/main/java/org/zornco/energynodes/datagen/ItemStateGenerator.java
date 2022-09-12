package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.zornco.energynodes.EnergyNodes;
import org.zornco.energynodes.Registration;

import java.util.Objects;

public class ItemStateGenerator extends ItemModelProvider {

    public ItemStateGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, EnergyNodes.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(Objects.requireNonNull(Registration.TEST_PAD_ITEM.getId()).getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/test_pad"));
        singleTexture(Objects.requireNonNull(Registration.ENERGY_LINKER_ITEM.getId()).getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/energy_linker"));
        singleTexture(Objects.requireNonNull(Registration.SAGE_MANIFEST_ITEM.getId()).getPath(),
                new ResourceLocation("item/handheld"),
                "layer0", new ResourceLocation(EnergyNodes.MOD_ID, "item/sages_manifest"));
        Registration.TIER_UPGRADES_MAP.values().stream().forEach(tier ->
                singleTexture(Objects.requireNonNull(tier.getId()).getPath(),
                        new ResourceLocation("item/handheld"),
                        "layer0",
                        new ResourceLocation(EnergyNodes.MOD_ID, "item/" + tier.getId().getPath())));
    }
}
