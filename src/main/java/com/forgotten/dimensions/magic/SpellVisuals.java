package com.forgotten.dimensions.magic;

import com.forgotten.dimensions.entity.SpellEffectEntity;
import com.forgotten.dimensions.registry.ModEntities;
import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/** Fait apparaître les effets 3D des sorts (désactivables avec l'option spellVisuals). */
public final class SpellVisuals {
    private SpellVisuals() {}

    public static SpellEffectEntity spawn(ServerWorld world, SpellVisual kind, SpellSchool school, Vec3d pos,
                                          float radius, int duration) {
        return spawn(world, kind, school, pos, radius, duration, Vec3d.ZERO, null);
    }

    /**
     * @param target vecteur relatif (fin du rayon, décalage d'apparition du météore...)
     * @param follow entité suivie (dôme, zone autour du lanceur), ou null
     */
    public static SpellEffectEntity spawn(ServerWorld world, SpellVisual kind, SpellSchool school, Vec3d pos,
                                          float radius, int duration, Vec3d target, Entity follow) {
        if (!FdConfig.get().spellVisuals) return null;
        SpellEffectEntity fx = new SpellEffectEntity(ModEntities.SPELL_EFFECT, world);
        fx.setPosition(pos.x, pos.y, pos.z);
        fx.setup(kind, school, radius, duration, new Vector3f((float) target.x, (float) target.y, (float) target.z),
                follow == null ? -1 : follow.getId());
        world.spawnEntity(fx);
        return fx;
    }

    public static void beam(ServerWorld world, SpellSchool school, Vec3d from, Vec3d to, float width, int duration) {
        spawn(world, SpellVisual.BEAM, school, from, width, duration, to.subtract(from), null);
    }
}
