package com.forgotten.dimensions.client.render;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

public class FdCreatureRenderState extends LivingEntityRenderState {
    public Identifier texture;
    public float attackProgress;
    public float modelScale = 1.0F;
    public boolean flying;
    public boolean swimming;
}
