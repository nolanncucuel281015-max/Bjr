package com.forgotten.dimensions.client.render.spell;

import com.forgotten.dimensions.magic.SpellSchool;
import net.minecraft.client.render.entity.state.EntityRenderState;

public class MagicBoltRenderState extends EntityRenderState {
    public SpellSchool school = SpellSchool.FIRE;
    public float size = 1F;
    public float yaw, pitch;
    public int seed;
}
