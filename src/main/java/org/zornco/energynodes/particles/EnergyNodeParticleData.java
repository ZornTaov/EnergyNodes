package org.zornco.energynodes.particles;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.zornco.energynodes.ClientRegistration;

public class EnergyNodeParticleData implements IParticleData {
    @Override
    public ParticleType<?> getType() {
        return ClientRegistration.CARBON.get();
    }

    @Override
    public void write(PacketBuffer buffer) {
    }

    @Override
    public String getParameters() {
        return null;
    }
}
