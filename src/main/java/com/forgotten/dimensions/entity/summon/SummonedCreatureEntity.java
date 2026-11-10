package com.forgotten.dimensions.entity.summon;

import com.forgotten.dimensions.entity.MagicBoltEntity;
import com.forgotten.dimensions.entity.creature.CreatureSounds;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellExecutor;
import com.forgotten.dimensions.magic.SpellFx;
import com.forgotten.dimensions.magic.SpellTargeting;
import com.forgotten.dimensions.magic.SpellVisual;
import com.forgotten.dimensions.magic.SpellVisuals;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.magic.SummonManager;
import com.forgotten.dimensions.magic.SummonType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Créature invoquée par un sort d'invocation : elle suit son maître, attaque les monstres proches et
 * ceux qui s'en prennent à lui (ou que le maître frappe), puis disparaît à la fin de sa durée.
 * Les volants (élémentaire, phénix, esprit de l'orage, spectre) se déplacent en vol libre ;
 * les autres marchent avec la navigation normale. Non sauvegardée : une invocation ne survit pas
 * au déchargement de sa zone ni à un redémarrage.
 */
public class SummonedCreatureEntity extends PathAwareEntity {
    private final SummonType summon;
    private UUID ownerId;
    /** Référence directe (joueurs factices compris) ; repli sur la liste des joueurs après une reconnexion. */
    private ServerPlayerEntity ownerRef;
    private float power = 4F;
    private int attackCooldown;
    private int targetScan;

    public SummonedCreatureEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.summon = SummonEntities.typeOf(type);
        if (summon != null && summon.flying) setNoGravity(true);
        this.experiencePoints = 0;
    }

    public static DefaultAttributeContainer.Builder attributes(SummonType t) {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, t.hp)
                .add(EntityAttributes.MOVEMENT_SPEED, t.speed)
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.ATTACK_KNOCKBACK, t == SummonType.STONE_GUARDIAN ? 1.5 : 0.3)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, t == SummonType.STONE_GUARDIAN || t == SummonType.FROST_SENTINEL ? 0.9 : 0.2)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.ARMOR, t == SummonType.STONE_GUARDIAN || t == SummonType.FROST_SENTINEL ? 10.0 : 2.0);
    }

    public SummonType summonType() {
        return summon;
    }

    /** Lie l'invocation à son maître ; {@code power} = dégâts par attaque (déjà multipliés par le niveau). */
    public void bind(ServerPlayerEntity owner, float power, double maxHealth) {
        this.ownerId = owner.getUuid();
        this.ownerRef = owner;
        this.power = power;
        var hp = getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (hp != null) hp.setBaseValue(maxHealth);
        var dmg = getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(power);
        setHealth(getMaxHealth());
        setPersistent();
    }

    public UUID ownerId() {
        return ownerId;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
    }

    // ------------------------------------------------------------------ IA

    private ServerPlayerEntity owner(ServerWorld world) {
        if (ownerId == null) return null;
        ServerPlayerEntity p = ownerRef != null && !ownerRef.isRemoved() ? ownerRef : world.getServer().getPlayerManager().getPlayer(ownerId);
        if (p != null) ownerRef = p;
        return p != null && p.isAlive() && p.getEntityWorld() == world ? p : null;
    }

    /** Maître vivant dans le même monde, ou null. */
    public ServerPlayerEntity master() {
        return getEntityWorld() instanceof ServerWorld sw ? owner(sw) : null;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        if (summon == null) {
            discard();
            return;
        }
        ServerPlayerEntity owner = owner(world);
        if (owner == null) {
            vanish(world);
            return;
        }
        if (attackCooldown > 0) attackCooldown--;
        if (--targetScan <= 0) {
            targetScan = 10;
            setTarget(chooseTarget(world, owner));
        }
        LivingEntity target = getTarget();
        if (target != null && (!target.isAlive() || target.squaredDistanceTo(owner) > 28 * 28)) {
            setTarget(null);
            target = null;
        }
        double ownerDist = squaredDistanceTo(owner);
        if (ownerDist > 30 * 30) {
            // Trop loin : rejoint son maître.
            Vec3d p = owner.getEntityPos().add(-owner.getRotationVec(1F).x * 2, summon.flying ? 2 : 0, -owner.getRotationVec(1F).z * 2);
            refreshPositionAndAngles(p.x, p.y, p.z, owner.getYaw(), 0);
            getNavigation().stop();
            SpellVisuals.spawn(world, SpellVisual.BLINK, summon.school, getEntityPos(), 1.2F, 14);
            return;
        }
        if (summon.flying) flyTick(world, owner, target);
        else walkTick(owner, target);
        if (target != null) {
            getLookControl().lookAt(target, 30F, 30F);
            attackTick(world, owner, target);
        }
    }

    /** Cible : celui qui frappe le maître, celui que le maître frappe, puis le monstre le plus proche. */
    private LivingEntity chooseTarget(ServerWorld world, ServerPlayerEntity owner) {
        LivingEntity current = getTarget();
        if (current != null && current.isAlive() && SpellTargeting.isEnemy(owner, current) && current.squaredDistanceTo(owner) < 24 * 24) {
            return current;
        }
        LivingEntity attacker = owner.getAttacker();
        if (attacker != null && attacker.isAlive() && SpellTargeting.isEnemy(owner, attacker)) return attacker;
        LivingEntity attacked = owner.getAttacking();
        if (attacked != null && attacked.isAlive() && SpellTargeting.isEnemy(owner, attacked)
                && owner.age - owner.getLastAttackTime() < 200) return attacked;
        LivingEntity best = null;
        double bestD = 16 * 16;
        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, owner.getBoundingBox().expand(16),
                e -> SpellTargeting.isEnemy(owner, e) && (e instanceof Monster || e instanceof MobEntity m && m.getTarget() == owner))) {
            double d = e.squaredDistanceTo(this);
            if (d < bestD) {
                bestD = d;
                best = e;
            }
        }
        return best;
    }

    private void walkTick(ServerPlayerEntity owner, LivingEntity target) {
        if (target != null) {
            if (age % 8 == 0) getNavigation().startMovingTo(target, 1.25);
        } else if (squaredDistanceTo(owner) > 4.5 * 4.5) {
            if (age % 10 == 0) getNavigation().startMovingTo(owner, 1.3);
        } else if (age % 10 == 0) {
            getNavigation().stop();
        }
    }

    /** Vol libre : rejoint un point au-dessus du maître ou à bonne distance de la cible. */
    private void flyTick(ServerWorld world, ServerPlayerEntity owner, LivingEntity target) {
        Vec3d goal;
        if (target != null) {
            Vec3d t = target.getEntityPos().add(0, target.getHeight() * 0.6, 0);
            if (summon.ranged) {
                Vec3d away = getEntityPos().subtract(t);
                double hd = Math.sqrt(away.x * away.x + away.z * away.z);
                Vec3d dir = hd < 0.01 ? new Vec3d(1, 0, 0) : new Vec3d(away.x / hd, 0, away.z / hd);
                double orbit = age * 0.03 + getId();
                dir = dir.rotateY((float) (Math.sin(orbit) * 0.6));
                goal = t.add(dir.multiply(6.5)).add(0, 3.0, 0);
            } else {
                goal = t;
            }
        } else {
            double a = getId() * 1.7 + age * 0.02;
            goal = owner.getEntityPos().add(Math.cos(a) * 2.4, 2.2 + Math.sin(age * 0.07) * 0.3, Math.sin(a) * 2.4);
        }
        // Ne descend pas dans le sol.
        BlockPos below = BlockPos.ofFloored(goal.x, goal.y - 0.6, goal.z);
        if (!world.getBlockState(below).isAir()) goal = goal.add(0, 1.0, 0);
        Vec3d delta = goal.subtract(getEntityPos());
        double len = delta.length();
        double max = summon.speed * 1.6;
        Vec3d want = len < 0.2 ? Vec3d.ZERO : delta.multiply(Math.min(max, len * 0.18) / len);
        setVelocity(getVelocity().multiply(0.75).add(want.multiply(0.35)));
        Vec3d v = getVelocity();
        if (target == null && v.horizontalLengthSquared() > 0.0025) {
            float yaw = (float) (MathHelper.atan2(v.z, v.x) * MathHelper.DEGREES_PER_RADIAN) - 90F;
            setYaw(yaw);
            setBodyYaw(yaw);
            setHeadYaw(yaw);
        } else if (target != null) {
            Vec3d d = target.getEntityPos().subtract(getEntityPos());
            float yaw = (float) (MathHelper.atan2(d.z, d.x) * MathHelper.DEGREES_PER_RADIAN) - 90F;
            setYaw(yaw);
            setBodyYaw(yaw);
        }
    }

    private void attackTick(ServerWorld world, ServerPlayerEntity owner, LivingEntity target) {
        if (attackCooldown > 0) return;
        double d2 = squaredDistanceTo(target);
        if (summon.ranged) {
            if (d2 > 20 * 20 || !canSee(target)) return;
            switch (summon) {
                case STORM_SPIRIT -> stormZap(world, owner, target);
                case PHOENIX -> shoot(world, owner, target, "fireball", 2.2F);
                default -> shoot(world, owner, target, "fire_bolt", 0F);
            }
            attackCooldown = summon == SummonType.PHOENIX ? 30 : 22;
        } else {
            double reach = getWidth() * 1.6 + target.getWidth() + 0.6;
            if (d2 <= reach * reach) {
                swingHand(net.minecraft.util.Hand.MAIN_HAND);
                tryAttack(world, target);
                attackCooldown = switch (summon) {
                    case STONE_GUARDIAN, FROST_SENTINEL -> 26;
                    case SPIRIT_WOLF -> 14;
                    default -> 18;
                };
            }
        }
    }

    private void shoot(ServerWorld world, ServerPlayerEntity owner, LivingEntity target, String spellId, float splash) {
        MagicBoltEntity bolt = new MagicBoltEntity(world, this, spellId, power, splash);
        Vec3d from = getEyePos();
        Vec3d to = target.getEyePos().subtract(0, 0.3, 0);
        Vec3d dir = to.subtract(from);
        bolt.setPosition(from.x, from.y - 0.2, from.z);
        bolt.setVelocity(dir.x, dir.y, dir.z, 1.3F, 1.0F);
        world.spawnEntity(bolt);
        swingHand(net.minecraft.util.Hand.MAIN_HAND);
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, getSoundCategory(), 0.7F, 1.2F);
    }

    /** Esprit de l'orage : un arc électrique qui rebondit sur deux ennemis supplémentaires. */
    private void stormZap(ServerWorld world, ServerPlayerEntity owner, LivingEntity first) {
        Spell spell = Spells.get(summon.spellId);
        Vec3d from = getEntityPos().add(0, getHeight() * 0.5, 0);
        LivingEntity current = first;
        java.util.Set<LivingEntity> done = new java.util.HashSet<>();
        float dmg = power;
        for (int i = 0; i < 3 && current != null; i++) {
            Vec3d to = current.getEntityPos().add(0, current.getHeight() / 2, 0);
            SpellVisuals.beam(world, summon.school, from, to, 0.3F, 8);
            SpellFx.line(world, ParticleTypes.ELECTRIC_SPARK, from, to, 0.4);
            SpellExecutor.hit(owner, spell, current, dmg);
            done.add(current);
            from = to;
            dmg *= 0.75F;
            LivingEntity next = null;
            double best = 6 * 6;
            for (LivingEntity e : SpellTargeting.enemiesAround(owner, current.getEntityPos(), 6)) {
                double d = e.squaredDistanceTo(current);
                if (!done.contains(e) && e instanceof Monster && d < best) {
                    best = d;
                    next = e;
                }
            }
            current = next;
        }
        world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, getSoundCategory(), 0.5F, 1.6F);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);
        if (hit && target instanceof LivingEntity living) {
            switch (summon) {
                case FROST_SENTINEL -> {
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
                    living.setFrozenTicks(Math.max(living.getFrozenTicks(), 160));
                    SpellVisuals.spawn(world, SpellVisual.SPIKES, summon.school, living.getEntityPos(), 1.6F, 24);
                }
                case STONE_GUARDIAN -> {
                    living.addVelocity(0, 0.55, 0);
                    living.velocityDirty = true;
                    SpellVisuals.spawn(world, SpellVisual.NOVA, summon.school, living.getEntityPos().add(0, 0.1, 0), 2.2F, 14);
                }
                case VOID_WRAITH -> {
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1));
                    heal(power * 0.4F);
                    if (getEntityWorld() instanceof ServerWorld sw) {
                        ServerPlayerEntity owner = owner(sw);
                        if (owner != null) owner.heal(power * 0.15F);
                    }
                }
                case SPIRIT_WOLF -> living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0));
                default -> {
                }
            }
        }
        return hit;
    }

    // ------------------------------------------------------------------ cycle de vie

    private void vanish(ServerWorld world) {
        SpellVisuals.spawn(world, SpellVisual.BLINK, summon == null ? com.forgotten.dimensions.magic.SpellSchool.VOID : summon.school,
                getEntityPos(), 1.2F, 14);
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (getEntityWorld().isClient() && summon != null && age % 2 == 0) {
            ParticleEffect p = switch (summon) {
                case FIRE_ELEMENTAL, PHOENIX -> random.nextInt(3) == 0 ? ParticleTypes.LAVA : ParticleTypes.FLAME;
                case FROST_SENTINEL -> ParticleTypes.SNOWFLAKE;
                case STORM_SPIRIT -> ParticleTypes.ELECTRIC_SPARK;
                case VOID_WRAITH -> ParticleTypes.REVERSE_PORTAL;
                case SPIRIT_WOLF -> ParticleTypes.SOUL_FIRE_FLAME;
                case STONE_GUARDIAN -> ParticleTypes.SPORE_BLOSSOM_AIR;
            };
            getEntityWorld().addParticleClient(p, getParticleX(0.6), getRandomBodyY(), getParticleZ(0.6), 0, 0.02, 0);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        // Ni le maître ni ses autres invocations ne blessent l'invocation.
        if (attacker != null && ownerId != null && (attacker.getUuid().equals(ownerId)
                || attacker instanceof SummonedCreatureEntity s && ownerId.equals(s.ownerId)
                || ownerId.equals(SummonManager.ownerOf(attacker)))) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (getEntityWorld() instanceof ServerWorld world && summon != null) {
            SpellVisuals.spawn(world, SpellVisual.EXPLOSION, summon.school, getEntityPos().add(0, getHeight() * 0.4, 0), 1.0F, 14);
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos pos) {
        if (summon == null || !summon.flying) super.fall(heightDifference, onGround, state, pos);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return summon == null ? null : CreatureSounds.of(summon.sounds).ambient();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return summon == null ? null : CreatureSounds.of(summon.sounds).hurt();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return summon == null ? null : CreatureSounds.of(summon.sounds).death();
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 240;
    }
}
