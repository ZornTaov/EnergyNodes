package org.zornco.energynodes.particles;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.zornco.energynodes.ClientRegistration;

import javax.annotation.Nonnull;

public class EnergyNodeParticleData implements IParticleData {
    @Nonnull
    @Override
    public ParticleType<?> getType() {
        return ClientRegistration.CARBON.get();
    }

    @Override
    public void writeToNetwork(@Nonnull PacketBuffer buffer) {
    }

    @Nonnull
    @Override
    public String writeToString() {
        return null;
    }
}
