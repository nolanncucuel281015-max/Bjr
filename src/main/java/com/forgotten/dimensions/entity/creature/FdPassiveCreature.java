package com.forgotten.dimensions.entity.creature;

import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.content.CreatureMovement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

/** Créature du mod basée sur {@link PathAwareEntity} ; tout le comportement vient de sa {@link CreatureDef}. */
public class FdPassiveCreature extends PathAwareEntity implements FdCreature {
    private final CreatureDef def;
    private final CreatureState state = new CreatureState();

    public FdPassiveCreature(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.def = CreatureLogic.def(this);
        MoveControl control = CreatureLogic.moveControl(this, def);
        if (control != null) this.moveControl = control;
        if (def != null) this.experiencePoints = def.xp();
    }

    @Override
    public CreatureDef definition() {
        return def;
    }

    @Override
    public CreatureState creatureState() {
        return state;
    }

    @Override
    protected void initGoals() {
        CreatureLogic.initGoals(this, this.goalSelector, this.targetSelector, CreatureLogic.def(this));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return CreatureLogic.navigation(this, world, CreatureLogic.def(this));
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        CreatureLogic.serverTick(this, world);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);
        if (hit) CreatureLogic.onAttackHit(this, target);
        return hit;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        boolean hurt = super.damage(world, source, amount);
        if (hurt) CreatureLogic.onDamaged(this, source);
        return hurt;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        CreatureLogic.onDeath(this, source);
    }

    @Override
    public void remove(RemovalReason reason) {
        CreatureLogic.clearBossBar(this);
        super.remove(reason);
    }

    @Override
    public boolean canBreatheInWater() {
        return def != null && def.movement() == CreatureMovement.SWIMMING || super.canBreatheInWater();
    }

    /** Les nageurs apparaissent dans l'eau (MobEntity refuse par défaut tout fluide). */
    @Override
    public boolean canSpawn(net.minecraft.world.WorldView world) {
        if (def != null && def.movement() == CreatureMovement.SWIMMING) return world.doesNotIntersectEntities(this);
        return super.canSpawn(world);
    }

    @Override
    public boolean isPushedByFluids() {
        return def == null || def.movement() != CreatureMovement.SWIMMING;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return def == null ? null : CreatureSounds.of(def.sounds()).ambient();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return def == null ? null : CreatureSounds.of(def.sounds()).hurt();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return def == null ? null : CreatureSounds.of(def.sounds()).death();
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, net.minecraft.util.math.BlockPos pos) {
        if (def == null || def.movement() == CreatureMovement.GROUND) super.fall(heightDifference, onGround, state, pos);
    }
}
