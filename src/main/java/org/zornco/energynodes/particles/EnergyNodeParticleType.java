package org.zornco.energynodes.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

import javax.annotation.Nonnull;

public class EnergyNodeParticleType extends ParticleType<EnergyNodeParticleData> {
    public EnergyNodeParticleType() {
        super(false, EnergyNodeParticleData.DESERIALIZER);
    }

    @Nonnull
    @Override
    public Codec<EnergyNodeParticleData> codec() {
        return EnergyNodeParticleData.CODEC;
    }
}
