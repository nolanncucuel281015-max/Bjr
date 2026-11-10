package com.forgotten.dimensions.entity.creature;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.CreatureCategory;
import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.entity.MagicBoltEntity;
import com.forgotten.dimensions.magic.SpellFx;
import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.Spells;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Capacités spéciales des créatures, déclenchées côté serveur avec des temps de recharge. */
public final class CreatureAbilities {
    public static final List<String> ALL = List.of("BOLT_FIRE", "BOLT_ICE", "BOLT_LIGHTNING", "BOLT_NATURE", "BOLT_VOID", "LEAP",
            "CHARGE", "SUMMON", "AURA_SLOW", "AURA_POISON", "AURA_WITHER", "AURA_FATIGUE", "HEAL", "TELEPORT", "LIGHTNING",
            "FREEZE_BREATH", "FIRE_NOVA", "ROOT", "HIT_POISON", "HIT_WITHER", "HIT_FIRE", "HIT_FREEZE", "HIT_BLIND", "STALK",
            "EXPLODE_ON_DEATH");

    private CreatureAbilities() {}

    private static int cd(CreatureDef d, int base) {
        return switch (d.category()) {
            case ELITE -> base * 3 / 4;
            case MINIBOSS -> base * 2 / 3;
            default -> base;
        };
    }

    public static void tick(MobEntity mob, CreatureDef d, CreatureState s, ServerWorld world) {
        LivingEntity target = mob.getTarget();
        boolean hasTarget = target != null && target.isAlive();
        double dist = hasTarget ? mob.distanceTo(target) : Double.MAX_VALUE;

        for (String a : d.abilities()) {
            if (!s.ready(a)) continue;
            switch (a) {
                case "BOLT_FIRE", "BOLT_ICE", "BOLT_LIGHTNING", "BOLT_NATURE", "BOLT_VOID" -> {
                    if (hasTarget && dist < 20 && dist > 2.5 && mob.canSee(target)) {
                        SpellSchool school = SpellSchool.valueOf(a.substring(5));
                        MagicBoltEntity bolt = new MagicBoltEntity(world, mob, Spells.basicBolt(school).id(), d.damage() * 0.8F, 0F);
                        Vec3d from = mob.getEyePos();
                        Vec3d to = target.getEyePos().subtract(0, 0.3, 0);
                        Vec3d dir = to.subtract(from);
                        bolt.setPosition(from.x, from.y - 0.2, from.z);
                        bolt.setVelocity(dir.x, dir.y, dir.z, 1.1F, 2.0F);
                        world.spawnEntity(bolt);
                        world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 0.7F, 1.3F);
                        s.cooldown(a, cd(d, 50));
                    }
                }
                case "LEAP" -> {
                    if (hasTarget && dist > 2 && dist < 7 && (mob.isOnGround() || mob.hasNoGravity())) {
                        Vec3d dir = target.getEntityPos().subtract(mob.getEntityPos()).normalize();
                        mob.setVelocity(dir.x * 0.9, 0.45, dir.z * 0.9);
                        mob.velocityDirty = true;
                        s.cooldown(a, cd(d, 60));
                    }
                }
                case "CHARGE" -> {
                    if (hasTarget && dist > 4 && dist < 14) {
                        Vec3d dir = target.getEntityPos().subtract(mob.getEntityPos()).normalize();
                        mob.setVelocity(dir.x * 1.3, mob.getVelocity().y + 0.1, dir.z * 1.3);
                        mob.velocityDirty = true;
                        s.chargeTicks = 15;
                        world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 0.8F, 1.2F);
                        s.cooldown(a, cd(d, 120));
                    }
                }
                case "SUMMON" -> {
                    if (hasTarget && !d.minion().isEmpty()) {
                        summon(mob, d, target, world);
                        s.cooldown(a, cd(d, 300));
                    }
                }
                case "AURA_SLOW" -> aura(mob, world, StatusEffects.SLOWNESS, 1, s, a, d);
                case "AURA_POISON" -> aura(mob, world, StatusEffects.POISON, 0, s, a, d);
                case "AURA_WITHER" -> aura(mob, world, StatusEffects.WITHER, 0, s, a, d);
                case "AURA_FATIGUE" -> aura(mob, world, StatusEffects.MINING_FATIGUE, 1, s, a, d);
                case "HEAL" -> {
                    if (mob.getHealth() < mob.getMaxHealth() * 0.5F) {
                        mob.heal(mob.getMaxHealth() * 0.2F);
                        SpellFx.particles(world, ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getHeight(), mob.getZ(), 6, 0.4, 0.3, 0.4, 0);
                        s.cooldown(a, cd(d, 300));
                    }
                }
                case "TELEPORT" -> {
                    if (hasTarget && dist > 6 && dist < 24) {
                        double x = target.getX() + (mob.getRandom().nextDouble() - 0.5) * 6;
                        double z = target.getZ() + (mob.getRandom().nextDouble() - 0.5) * 6;
                        SpellFx.particles(world, ParticleTypes.PORTAL, mob.getX(), mob.getY() + 1, mob.getZ(), 20, 0.3, 0.6, 0.3, 0.3);
                        if (mob.teleport(x, target.getY(), z, true)) {
                            world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.0F);
                        }
                        s.cooldown(a, cd(d, 160));
                    }
                }
                case "LIGHTNING" -> {
                    if (hasTarget && dist < 24 && mob.canSee(target)) {
                        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.refreshPositionAndAngles(target.getX(), target.getY(), target.getZ(), 0, 0);
                            bolt.setCosmetic(true);
                            world.spawnEntity(bolt);
                        }
                        target.damage(world, world.getDamageSources().indirectMagic(mob, mob), d.damage());
                        s.cooldown(a, cd(d, 160));
                    }
                }
                case "FREEZE_BREATH" -> {
                    if (hasTarget && dist < 8 && mob.canSee(target)) {
                        SpellFx.line(world, ParticleTypes.SNOWFLAKE, mob.getEyePos(), target.getEyePos(), 0.3);
                        target.damage(world, world.getDamageSources().indirectMagic(mob, mob), d.damage() * 0.6F);
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 2));
                        target.setFrozenTicks(Math.min(target.getMinFreezeDamageTicks() + 80, target.getFrozenTicks() + 100));
                        world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.HOSTILE, 0.8F, 0.8F);
                        s.cooldown(a, cd(d, 100));
                    }
                }
                case "FIRE_NOVA" -> {
                    if (hasTarget && dist < 5) {
                        for (PlayerEntity p : players(mob, 4.5)) {
                            p.damage(world, world.getDamageSources().indirectMagic(mob, mob), d.damage() * 0.8F);
                            p.setOnFireFor(4.0F);
                        }
                        SpellFx.ring(world, ParticleTypes.FLAME, mob.getEntityPos(), 4.5, 36);
                        world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.0F, 0.6F);
                        s.cooldown(a, cd(d, 140));
                    }
                }
                case "ROOT" -> {
                    if (hasTarget && dist < 10 && mob.canSee(target)) {
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 9));
                        SpellFx.particles(world, ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 0.2, target.getZ(), 15, 0.4, 0.1, 0.4, 0);
                        s.cooldown(a, cd(d, 200));
                    }
                }
                case "STALK" -> {
                    if (mob.age % 20 == 0) {
                        if (!hasTarget || dist > 5) mob.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 30, 0, false, false));
                        else mob.removeStatusEffect(StatusEffects.INVISIBILITY);
                    }
                }
                default -> { /* capacités au contact ou à la mort */ }
            }
        }
        // Charge : dégâts au contact pendant la ruée.
        if (s.chargeTicks > 0) {
            s.chargeTicks--;
            if (hasTarget && dist < 2.2) {
                target.damage(world, world.getDamageSources().mobAttack(mob), d.damage() * 1.2F);
                Vec3d push = target.getEntityPos().subtract(mob.getEntityPos()).normalize();
                target.addVelocity(push.x * 1.2, 0.4, push.z * 1.2);
                target.velocityDirty = true;
                s.chargeTicks = 0;
            }
        }
    }

    private static List<PlayerEntity> players(MobEntity mob, double r) {
        return mob.getEntityWorld().getEntitiesByClass(PlayerEntity.class, mob.getBoundingBox().expand(r),
                p -> p.isAlive() && !p.isCreative() && !p.isSpectator());
    }

    private static void aura(MobEntity mob, ServerWorld world, RegistryEntry<StatusEffect> effect, int amp, CreatureState s, String a, CreatureDef d) {
        List<PlayerEntity> near = players(mob, 6);
        if (near.isEmpty()) return;
        for (PlayerEntity p : near) p.addStatusEffect(new StatusEffectInstance(effect, 60, amp));
        SpellFx.particles(world, ParticleTypes.WITCH, mob.getX(), mob.getY() + mob.getHeight() / 2, mob.getZ(), 10, 1.5, 0.5, 1.5, 0);
        s.cooldown(a, 40);
    }

    private static void summon(MobEntity mob, CreatureDef d, LivingEntity target, ServerWorld world) {
        EntityType<?> type = Registries.ENTITY_TYPE.get(ForgottenDimensions.id(d.minion()));
        long alive = world.getEntitiesByType(type, mob.getBoundingBox().expand(24), e -> true).size();
        if (alive >= 4) return;
        for (int i = 0; i < 2; i++) {
            var e = type.create(world, SpawnReason.MOB_SUMMONED);
            if (!(e instanceof MobEntity minion)) continue;
            double a = mob.getRandom().nextDouble() * Math.PI * 2;
            minion.refreshPositionAndAngles(mob.getX() + Math.cos(a) * 2, mob.getY(), mob.getZ() + Math.sin(a) * 2, mob.getYaw(), 0);
            minion.setTarget(target);
            world.spawnEntity(minion);
            SpellFx.particles(world, ParticleTypes.LARGE_SMOKE, minion.getX(), minion.getY() + 0.5, minion.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        }
        world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.HOSTILE, 1.0F, 1.0F);
    }

    /** Effets appliqués quand une attaque au corps à corps touche. */
    public static void onHit(MobEntity mob, CreatureDef d, LivingEntity target) {
        if (d.has("HIT_POISON")) target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));
        if (d.has("HIT_WITHER")) target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
        if (d.has("HIT_FIRE")) target.setOnFireFor(4.0F);
        if (d.has("HIT_FREEZE")) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
            target.setFrozenTicks(Math.min(target.getMinFreezeDamageTicks() + 60, target.getFrozenTicks() + 80));
        }
        if (d.has("HIT_BLIND")) target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
    }

    public static void onDeath(MobEntity mob, CreatureDef d, ServerWorld world) {
        if (d.has("EXPLODE_ON_DEATH")) {
            for (PlayerEntity p : players(mob, 3.5)) {
                p.damage(world, world.getDamageSources().indirectMagic(mob, mob), d.damage());
            }
            SpellFx.particles(world, ParticleTypes.EXPLOSION, mob.getX(), mob.getY() + 0.5, mob.getZ(), 3, 0.5, 0.5, 0.5, 0);
            world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 0.8F, 1.4F);
        }
        if (d.category() == CreatureCategory.MINIBOSS) {
            SpellFx.particles(world, ParticleTypes.TOTEM_OF_UNDYING, mob.getX(), mob.getY() + 1, mob.getZ(), 40, 0.8, 1.0, 0.8, 0.3);
        }
    }
}
