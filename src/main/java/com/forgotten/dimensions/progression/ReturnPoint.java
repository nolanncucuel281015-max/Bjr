package com.forgotten.dimensions.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

/** Position (hors dimensions du mod) où renvoyer le joueur quand il rentre par un portail. */
public record ReturnPoint(String dimension, BlockPos pos) {
    public static final Codec<ReturnPoint> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("dimension").forGetter(ReturnPoint::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(ReturnPoint::pos)
    ).apply(i, ReturnPoint::new));
}
