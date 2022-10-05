package org.zornco.energynodes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zornco.energynodes.compat.TheOneProbeCompat;
import org.zornco.energynodes.network.NetworkManager;
//import org.zornco.energynodes.compat.TheOneProbeCompat;

@Mod(EnergyNodes.MOD_ID)
public class EnergyNodes
{
    public static final String MOD_ID = "energynodes";
    public static final Logger LOGGER = LogManager.getLogger();

    public EnergyNodes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::enqueueIMC);
        //EnergyNodesConfig.setupConfigs();
        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EnergyNodesConfig.CLIENT_CONFIG);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EnergyNodesConfig.SERVER_CONFIG);
        Registration.init(modEventBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkManager.Register();
    }

    public void enqueueIMC(final InterModEnqueueEvent event) {
        LOGGER.trace("Sending IMC setup to TOP and other mods.");
        if (ModList.get().isLoaded("theoneprobe"))
            TheOneProbeCompat.sendIMC();
    }

    /*@SubscribeEvent
    public void doClientStuff(final FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public void processIMC(final InterModProcessEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(final FMLServerStartingEvent event) {
    }*/

}
