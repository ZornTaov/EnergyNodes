package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.zornco.energynodes.EnergyNodes;

@Mod.EventBusSubscriber(modid = EnergyNodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer())
            registerServerProviders(event.getGenerator(), event);

        if (event.includeClient())
            registerClientProviders(event.getGenerator(), event);
    }

    private static void registerServerProviders(DataGenerator generator, GatherDataEvent event) {
        // TODO - add recipes for blocks and linker
        generator.addProvider(new RecipeGenerator(generator));
    }

    private static void registerClientProviders(DataGenerator generator, GatherDataEvent event) {
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(new BlockStateGenerator(generator, helper));
        generator.addProvider(new ItemStateGenerator(generator, helper));
    }
}