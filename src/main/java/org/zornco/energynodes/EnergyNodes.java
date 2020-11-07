package org.zornco.energynodes;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

@Mod(EnergyNodes.MOD_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class EnergyNodes
{
    public static final String MOD_ID = "energynodes";
    public static final Logger LOGGER = LogManager.getLogger();

    public EnergyNodes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.init(modEventBus);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
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
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(final FMLServerStartingEvent event) {
    }

}
