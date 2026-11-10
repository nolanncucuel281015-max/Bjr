package com.forgotten.dimensions.entity;

import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.SpellVisual;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Effet visuel 3D d'un sort. Purement visuel : pas de collision, pas de sauvegarde, disparaît seul
 * après sa durée. Le serveur le fait apparaître ; les données (type, école, rayon, durée, cible,
 * entité suivie) sont synchronisées vers les clients qui le dessinent (SpellEffectRenderer).
 */
public class SpellEffectEntity extends Entity {
    private static final TrackedData<Integer> KIND = DataTracker.registerData(SpellEffectEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SCHOOL = DataTracker.registerData(SpellEffectEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> RADIUS = DataTracker.registerData(SpellEffectEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> DURATION = DataTracker.registerData(SpellEffectEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Vector3fc> TARGET = DataTracker.registerData(SpellEffectEntity.class, TrackedDataHandlerRegistry.VECTOR_3F);
    private static final TrackedData<Integer> FOLLOW = DataTracker.registerData(SpellEffectEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public SpellEffectEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public void setup(SpellVisual kind, SpellSchool school, float radius, int duration, Vector3f target, int followId) {
        dataTracker.set(KIND, kind.ordinal());
        dataTracker.set(SCHOOL, school.ordinal());
        dataTracker.set(RADIUS, radius);
        dataTracker.set(DURATION, Math.max(1, duration));
        dataTracker.set(TARGET, target);
        dataTracker.set(FOLLOW, followId);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(KIND, 0).add(SCHOOL, 0).add(RADIUS, 1.0F).add(DURATION, 20)
                .add(TARGET, new Vector3f()).add(FOLLOW, -1);
    }

    public SpellVisual kind() {
        int k = dataTracker.get(KIND);
        SpellVisual[] all = SpellVisual.values();
        return all[Math.max(0, Math.min(all.length - 1, k))];
    }

    public SpellSchool school() {
        SpellSchool[] all = SpellSchool.values();
        return all[Math.max(0, Math.min(all.length - 1, dataTracker.get(SCHOOL)))];
    }

    public float radius() {
        return dataTracker.get(RADIUS);
    }

    public int duration() {
        return dataTracker.get(DURATION);
    }

    public Vector3fc target() {
        return dataTracker.get(TARGET);
    }

    public int followId() {
        return dataTracker.get(FOLLOW);
    }

    @Override
    public void tick() {
        super.tick();
        int follow = followId();
        if (follow >= 0) {
            Entity e = getEntityWorld().getEntityById(follow);
            if (e != null && e.isAlive()) setPosition(e.getX(), e.getY(), e.getZ());
        }
        if (!getEntityWorld().isClient() && age >= duration()) discard();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 160 * 160;
    }

    @Override
    protected void readCustomData(ReadView view) {
    }

    @Override
    protected void writeCustomData(WriteView view) {
    }
}
