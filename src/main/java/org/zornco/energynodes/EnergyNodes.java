package org.zornco.energynodes;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnergyNodes.MOD_ID)
public class EnergyNodes
{
    public static final String MOD_ID = "energynodes";
    public static final Logger LOGGER = LogManager.getLogger();

    public EnergyNodes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        Registration.init(modEventBus);
    }

    public void setup(final FMLCommonSetupEvent event)
    {
        Registration.register();
    }

    /*@SubscribeEvent
    public void doClientStuff(final FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public void enqueueIMC(final InterModEnqueueEvent event)
    {
    }

    @SubscribeEvent
    public void processIMC(final InterModProcessEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(final FMLServerStartingEvent event) {
    }*/

}
