package com.forgotten.dimensions.client.render.summon;

import com.forgotten.dimensions.client.render.FdCreatureRenderState;
import com.forgotten.dimensions.magic.SummonType;
import net.minecraft.util.Identifier;

public class SummonRenderState extends FdCreatureRenderState {
    public SummonType summon;
    public Identifier glowTexture;
    public int seed;
}
