package org.zornco.energynodes.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.zornco.energynodes.ClientRegistration;

import javax.annotation.Nonnull;
import java.util.Locale;

import net.minecraft.core.particles.ParticleOptions.Deserializer;

public class EnergyNodeParticleData implements ParticleOptions {

    public static final Codec<EnergyNodeParticleData> CODEC = RecordCodecBuilder.create(val -> val.group(
            Codec.FLOAT.fieldOf("r").forGetter((data) -> data.r),
            Codec.FLOAT.fieldOf("g").forGetter((data) -> data.g),
            Codec.FLOAT.fieldOf("b").forGetter((data) -> data.b)
    ).apply(val, EnergyNodeParticleData::new));

    public static final Deserializer<EnergyNodeParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        @Nonnull
        public EnergyNodeParticleData fromCommand(@Nonnull ParticleType<EnergyNodeParticleData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float f = (float) reader.readDouble();
            reader.expect(' ');
            float f1 = (float) reader.readDouble();
            reader.expect(' ');
            float f2 = (float) reader.readDouble();
            return new EnergyNodeParticleData(f, f1, f2);
        }

        @Override
        @Nonnull
        public EnergyNodeParticleData fromNetwork(@Nonnull ParticleType<EnergyNodeParticleData> type, @Nonnull FriendlyByteBuf buf) {
            return new EnergyNodeParticleData(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

    public final Float r;
    public final Float g;
    public final Float b;

    public EnergyNodeParticleData(Float r, Float g, Float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Nonnull
    @Override
    public ParticleType<EnergyNodeParticleData> getType() {
        return ClientRegistration.ENERGY.get();
    }

    @Override
    public void writeToNetwork(@Nonnull FriendlyByteBuf buf) {
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
    }

    @Nonnull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.5f %.5f %.5f", getType().getRegistryName(), r, g, b);
    }

}