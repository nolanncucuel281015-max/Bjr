package com.forgotten.dimensions.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/** Mana courant et maximum (le maximum est recalculé côté serveur puis synchronisé pour le HUD). */
public record ManaState(float current, float max) {
    public static final ManaState DEFAULT = new ManaState(100F, 100F);

    public static final Codec<ManaState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("current").forGetter(ManaState::current),
            Codec.FLOAT.fieldOf("max").forGetter(ManaState::max)
    ).apply(i, ManaState::new));

    public static final PacketCodec<ByteBuf, ManaState> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, ManaState::current,
            PacketCodecs.FLOAT, ManaState::max,
            ManaState::new);

    public ManaState withCurrent(float value) {
        return new ManaState(Math.max(0F, Math.min(max, value)), max);
    }

    public ManaState withMax(float newMax) {
        return new ManaState(Math.min(current, newMax), newMax);
    }
}
