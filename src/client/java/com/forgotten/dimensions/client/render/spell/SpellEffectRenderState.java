package com.forgotten.dimensions.client.render.spell;

import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.SpellVisual;
import net.minecraft.client.render.entity.state.EntityRenderState;

public class SpellEffectRenderState extends EntityRenderState {
    public SpellVisual kind = SpellVisual.CAST_CIRCLE;
    public SpellSchool school = SpellSchool.FIRE;
    public float radius = 1F;
    public float duration = 20F;
    public float tx, ty, tz;
    public int seed;
}
