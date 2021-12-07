package org.zornco.energynodes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.zornco.energynodes.particles.EnergyNodeParticle;
import org.zornco.energynodes.particles.EnergyNodeParticleType;
import org.zornco.energynodes.tile.client.EnergyControllerTileRenderer;

@Mod.EventBusSubscriber(modid = EnergyNodes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistration {
    // ================================================================================================================
    //    Registries
    // ================================================================================================================
    public static final DeferredRegister<ParticleType<?>> PARTICLE = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, EnergyNodes.MOD_ID);

    // ================================================================================================================
    //    Particles
    // ================================================================================================================
    public static final RegistryObject<EnergyNodeParticleType> ENERGY = PARTICLE.register("energy", EnergyNodeParticleType::new);


    @SubscribeEvent
    public static void registerFactories(ParticleFactoryRegisterEvent event) {
        ParticleEngine manager = Minecraft.getInstance().particleEngine;
        manager.register(ENERGY.get(), EnergyNodeParticle.FACTORY::new);

    }

    @SubscribeEvent
    public static void regRenderer(final EntityRenderersEvent.RegisterRenderers evt) {

        evt.registerBlockEntityRenderer(Registration.ENERGY_CONTROLLER_TILE.get(), EnergyControllerTileRenderer::new);
    }
}
