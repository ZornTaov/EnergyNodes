package org.zornco.energynodes.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.data.event.GatherDataEvent;
import org.zornco.energynodes.EnergyNodes;

@Mod.EventBusSubscriber(modid = EnergyNodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var generator = event.getGenerator();
        final var helper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new RecipeGenerator(generator));
        generator.addProvider(event.includeServer(), new LootTableGenerator(generator));
        generator.addProvider(event.includeServer(), new BlockTagGenerator(generator, helper));

        generator.addProvider(event.includeClient(), new BlockStateGenerator(generator, helper));
        generator.addProvider(event.includeClient(), new ItemStateGenerator(generator, helper));
        generator.addProvider(event.includeClient(), new LangGenerator(generator));
    }
}