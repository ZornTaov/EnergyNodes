package org.zornco.energynodes.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import org.zornco.energynodes.ClientRegistration;

import javax.annotation.Nonnull;
import java.util.Locale;

public class EnergyNodeParticleData implements IParticleData {

    public static final Codec<EnergyNodeParticleData> CODEC = RecordCodecBuilder.create(val -> val.group(
            Codec.FLOAT.fieldOf("r").forGetter((data) -> data.r),
            Codec.FLOAT.fieldOf("g").forGetter((data) -> data.g),
            Codec.FLOAT.fieldOf("b").forGetter((data) -> data.b)
    ).apply(val, EnergyNodeParticleData::new));

    public static final IDeserializer<EnergyNodeParticleData> DESERIALIZER = new IDeserializer<EnergyNodeParticleData>() {
        @Override
        @Nonnull
        public EnergyNodeParticleData fromCommand(@Nonnull ParticleType<EnergyNodeParticleData> type, @Nonnull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float f = (float)reader.readDouble();
            reader.expect(' ');
            float f1 = (float)reader.readDouble();
            reader.expect(' ');
            float f2 = (float)reader.readDouble();
            return new EnergyNodeParticleData(f, f1, f2);
        }

        @Override
        @Nonnull
        public EnergyNodeParticleData fromNetwork(@Nonnull ParticleType<EnergyNodeParticleData> type, @Nonnull PacketBuffer buf) {
            return new EnergyNodeParticleData(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

    public Float r;
    public Float g;
    public Float b;

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
    public void writeToNetwork(@Nonnull PacketBuffer buf) {
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