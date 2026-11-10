package com.forgotten.dimensions.magic;

import com.forgotten.dimensions.entity.MagicBoltEntity;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.progression.Skills;
import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Exécution des sorts côté serveur. Chaque {@link SpellKind} correspond à un comportement générique. */
public final class SpellExecutor {
    private static final double RANGE = 24.0;

    private SpellExecutor() {}

    public static boolean execute(ServerPlayerEntity caster, Spell spell, float power) {
        ServerWorld world = caster.getEntityWorld();
        SpellSchool school = spell.school();
        float big = 0.8F + Math.min(1.6F, spell.level() / 40F);
        if (spell.kind() != SpellKind.BLINK && spell.kind() != SpellKind.DASH) {
            SpellVisuals.spawn(world, SpellVisual.CAST_CIRCLE, school, caster.getEntityPos().add(0, 0.05, 0), 1.4F * big, 22,
                    Vec3d.ZERO, caster);
        }
        switch (spell.kind()) {
            case BOLT -> {
                MagicBoltEntity bolt = new MagicBoltEntity(world, caster, spell.id(), power, spell.radius());
                float speed = spell.id().equals("ball_lightning") ? 0.8F : 1.8F;
                bolt.setVelocity(caster, caster.getPitch(), caster.getYaw(), 0.0F, speed, 0.5F);
                world.spawnEntity(bolt);
                sound(world, caster.getEyePos(), castSound(school), 1.0F, 1.2F);
            }
            case RAY -> {
                LivingEntity target = SpellTargeting.lookTarget(caster, RANGE);
                Vec3d end = target != null ? target.getEntityPos().add(0, target.getHeight() / 2, 0) : SpellTargeting.lookPos(caster, RANGE);
                SpellFx.line(world, particle(school), caster.getEyePos().add(0, -0.2, 0), end, 0.4);
                SpellVisuals.beam(world, school, caster.getEyePos().add(0, -0.25, 0), end, spell.id().equals("dimensional_ray") ? 0.7F : 0.45F, 14);
                SpellVisuals.spawn(world, SpellVisual.EXPLOSION, school, end, 1.1F, 12);
                sound(world, caster.getEyePos(), castSound(school), 1.0F, 1.5F);
                if (target == null) return true;
                hit(caster, spell, target, power);
                if (spell.id().equals("ignite")) target.setOnFireForTicks(spell.duration());
                if (spell.id().equals("frost_lance")) target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, spell.duration(), 2));
                if (spell.id().equals("entropy")) target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, spell.duration(), 2));
            }
            case NOVA -> {
                Vec3d c = caster.getEntityPos();
                for (LivingEntity e : SpellTargeting.enemiesAround(caster, c, spell.radius())) {
                    hit(caster, spell, e, power);
                    double kb = spell.id().equals("thunderclap") ? 1.6 : 0.6;
                    Vec3d push = e.getEntityPos().subtract(c).normalize().multiply(kb);
                    e.addVelocity(push.x, 0.3, push.z);
                    e.velocityDirty = true;
                    if (spell.id().equals("gaia_wrath")) root(e, 60);
                    if (spell.id().equals("absolute_zero")) root(e, 80);
                }
                SpellFx.ring(world, particle(school), c, spell.radius(), 48);
                SpellFx.ring(world, particle(school), c, spell.radius() * 0.5, 24);
                SpellVisuals.spawn(world, SpellVisual.NOVA, school, c.add(0, 0.1, 0), spell.radius(), 20);
                if (school == SpellSchool.ICE || school == SpellSchool.NATURE) SpellVisuals.spawn(world, SpellVisual.SPIKES, school, c, spell.radius(), 36);
                if (school == SpellSchool.LIGHTNING || school == SpellSchool.FIRE) SpellVisuals.spawn(world, SpellVisual.PILLAR, school, c, 1.2F * big, 18);
                sound(world, c, impactSound(school), 1.2F, 0.9F);
            }
            case AREA -> {
                Vec3d p = aimPos(caster, RANGE);
                if (school == SpellSchool.LIGHTNING) lightningFx(world, p);
                switch (spell.id()) {
                    case "volcanic_eruption" -> SpellVisuals.spawn(world, SpellVisual.PILLAR, school, p, spell.radius() * 0.7F, 30);
                    case "glacial_spike" -> SpellVisuals.spawn(world, SpellVisual.SPIKES, school, p, spell.radius(), 40);
                    case "lightning_strike" -> SpellVisuals.spawn(world, SpellVisual.PILLAR, school, p, 0.8F, 14);
                    case "void_explosion" -> SpellVisuals.spawn(world, SpellVisual.SINGULARITY, school, p, spell.radius() * 0.6F, 20);
                    default -> { }
                }
                SpellVisuals.spawn(world, SpellVisual.EXPLOSION, school, p, spell.radius(), 20);
                beamFromCaster(world, caster, school, p);
                areaDamage(caster, spell, p, spell.radius(), power);
                if (spell.id().equals("volcanic_eruption")) {
                    for (LivingEntity e : SpellTargeting.enemiesAround(caster, p, spell.radius())) {
                        e.addVelocity(0, 0.9, 0);
                        e.velocityDirty = true;
                    }
                    SpellFx.particles(world, ParticleTypes.LAVA, p.x, p.y + 0.5, p.z, 30, 1.0, 0.5, 1.0, 0.2);
                }
            }
            case DELAYED_AREA -> {
                Vec3d p = aimPos(caster, RANGE * 1.5);
                int delay = spell.duration();
                boolean pull = spell.id().equals("singularity");
                if (pull) SpellVisuals.spawn(world, SpellVisual.SINGULARITY, school, p.add(0, 1.5, 0), spell.radius() * 0.6F, delay + 6);
                else if (spell.id().equals("sun_strike")) SpellVisuals.spawn(world, SpellVisual.ZONE, school, p, spell.radius(), delay);
                else SpellVisuals.spawn(world, SpellVisual.METEOR, school, p, spell.radius(), delay, new Vec3d(-8, 32, -8), null);
                ZoneManager.add(world, p, null, spell.radius(), 0, delay, 2, z -> {
                    double y = p.y + 12.0 * (1.0 - z.age() / (double) delay);
                    SpellFx.particles(world, pull ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.FLAME, p.x, y, p.z, 6, 0.3, 0.3, 0.3, 0.01);
                    SpellFx.ring(world, particle(school), p, spell.radius(), 16);
                    if (pull) pullEnemies(caster, p, spell.radius() * 1.5, 0.25);
                });
                ZoneManager.add(world, p, null, spell.radius(), delay, 1, 1, z -> {
                    areaDamage(caster, spell, p, spell.radius(), power);
                    SpellVisuals.spawn(world, SpellVisual.EXPLOSION, school, p, spell.radius() * 1.3F, 26);
                    SpellVisuals.spawn(world, SpellVisual.NOVA, school, p.add(0, 0.1, 0), spell.radius() * 1.6F, 22);
                    if (spell.id().equals("sun_strike")) SpellVisuals.spawn(world, SpellVisual.PILLAR, school, p, spell.radius() * 0.6F, 34);
                    SpellFx.particles(world, ParticleTypes.EXPLOSION_EMITTER, p.x, p.y + 0.5, p.z, 1, 0, 0, 0, 0);
                    sound(world, p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 2.0F, 0.7F);
                });
                sound(world, caster.getEyePos(), castSound(school), 1.0F, 0.6F);
            }
            case ZONE_SELF, ZONE_TARGET -> {
                boolean self = spell.kind() == SpellKind.ZONE_SELF;
                Vec3d p = self ? caster.getEntityPos() : aimPos(caster, RANGE);
                boolean storm = spell.id().equals("storm") || spell.id().equals("tempest");
                boolean added = ZoneManager.add(world, p, self ? caster : null, spell.radius(), 0, spell.duration(), 10, z -> {
                    SpellFx.ring(world, particle(school), z.center, spell.radius(), 24);
                    SpellFx.particles(world, particle(school), z.center.x, z.center.y + 1, z.center.z, 10,
                            spell.radius() / 2, 0.6, spell.radius() / 2, 0.02);
                    List<LivingEntity> enemies = SpellTargeting.enemiesAround(caster, z.center, spell.radius());
                    if (storm) {
                        if (!enemies.isEmpty()) {
                            LivingEntity t = enemies.get(world.random.nextInt(enemies.size()));
                            lightningFx(world, t.getEntityPos());
                            hit(caster, spell, t, power);
                        }
                    } else {
                        for (LivingEntity e : enemies) hit(caster, spell, e, power);
                    }
                });
                if (!added) return false;
                SpellVisuals.spawn(world, SpellVisual.ZONE, school, p, spell.radius(), spell.duration(), Vec3d.ZERO, self ? caster : null);
                if (!self) beamFromCaster(world, caster, school, p);
                sound(world, p, castSound(school), 1.0F, 0.8F);
            }
            case CHAIN -> {
                LivingEntity current = SpellTargeting.lookTarget(caster, RANGE);
                if (current == null) return false;
                Set<LivingEntity> done = new HashSet<>();
                Vec3d from = caster.getEyePos();
                float dmg = power;
                for (int i = 0; i <= spell.duration() && current != null; i++) {
                    Vec3d to = current.getEntityPos().add(0, current.getHeight() / 2, 0);
                    SpellFx.line(world, ParticleTypes.ELECTRIC_SPARK, from, to, 0.3);
                    SpellVisuals.beam(world, school, from, to, 0.35F, 12);
                    hit(caster, spell, current, dmg);
                    done.add(current);
                    from = to;
                    dmg *= 0.8F;
                    LivingEntity next = null;
                    double best = Double.MAX_VALUE;
                    for (LivingEntity e : SpellTargeting.enemiesAround(caster, current.getEntityPos(), spell.radius())) {
                        double d = e.squaredDistanceTo(current);
                        if (!done.contains(e) && d < best) {
                            best = d;
                            next = e;
                        }
                    }
                    current = next;
                }
                sound(world, caster.getEyePos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 1.4F);
            }
            case BUFF_SELF -> {
                int d = spell.duration();
                switch (spell.id()) {
                    case "phoenix_blessing" -> {
                        effect(caster, StatusEffects.FIRE_RESISTANCE, d, 0);
                        effect(caster, StatusEffects.REGENERATION, d / 4, 1);
                    }
                    case "frost_ward" -> effect(caster, StatusEffects.RESISTANCE, d, 0);
                    case "ice_armor" -> {
                        effect(caster, StatusEffects.RESISTANCE, d, 1);
                        effect(caster, StatusEffects.ABSORPTION, d, 0);
                    }
                    case "overcharge" -> {
                        effect(caster, StatusEffects.SPEED, d, 1);
                        effect(caster, StatusEffects.HASTE, d, 1);
                    }
                    case "barkskin" -> {
                        effect(caster, StatusEffects.RESISTANCE, d, 0);
                        effect(caster, StatusEffects.ABSORPTION, d, 1);
                    }
                    case "phase_shift" -> {
                        effect(caster, StatusEffects.INVISIBILITY, d, 0);
                        effect(caster, StatusEffects.RESISTANCE, d, 2);
                    }
                    default -> {
                        return false;
                    }
                }
                SpellFx.particles(world, particle(school), caster.getX(), caster.getY() + 1, caster.getZ(), 30, 0.5, 0.8, 0.5, 0.05);
                SpellVisuals.spawn(world, SpellVisual.BARRIER, school, caster.getEntityPos(), 1.6F, 60, Vec3d.ZERO, caster);
                sound(world, caster.getEntityPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.3F);
            }
            case HEAL -> {
                List<ServerPlayerEntity> targets = spell.radius() > 0 ? SpellTargeting.alliesAround(caster, spell.radius()) : List.of(caster);
                for (ServerPlayerEntity p : targets) {
                    p.heal(power);
                    if (spell.id().equals("regrowth")) effect(p, StatusEffects.REGENERATION, 100, 0);
                    SpellFx.particles(world, ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 15, 0.4, 0.6, 0.4, 0.0);
                    SpellVisuals.spawn(world, SpellVisual.HEAL, school, p.getEntityPos(), 1.0F, 30, Vec3d.ZERO, p);
                }
                sound(world, caster.getEntityPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
            }
            case BLINK -> {
                Vec3d dest = blinkDestination(caster, spell.radius());
                if (dest == null) return false;
                SpellFx.particles(world, ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY() + 1, caster.getZ(), 30, 0.3, 0.6, 0.3, 0.05);
                SpellVisuals.spawn(world, SpellVisual.BLINK, school, caster.getEntityPos(), 1.2F, 16);
                SpellVisuals.spawn(world, SpellVisual.BLINK, school, dest, 1.2F, 16);
                caster.requestTeleport(dest.x, dest.y, dest.z);
                caster.fallDistance = 0;
                SpellFx.particles(world, ParticleTypes.PORTAL, dest.x, dest.y + 1, dest.z, 30, 0.3, 0.6, 0.3, 0.3);
                sound(world, dest, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.1F);
            }
            case DASH -> {
                Vec3d start = caster.getEntityPos();
                Vec3d dest = blinkDestination(caster, spell.radius());
                if (dest == null) return false;
                Set<LivingEntity> hitSet = new HashSet<>();
                for (int i = 0; i <= 10; i++) {
                    Vec3d p = start.lerp(dest, i / 10.0);
                    hitSet.addAll(SpellTargeting.enemiesAround(caster, p, 1.8));
                }
                for (LivingEntity e : hitSet) hit(caster, spell, e, power);
                SpellFx.line(world, ParticleTypes.ELECTRIC_SPARK, start.add(0, 1, 0), dest.add(0, 1, 0), 0.3);
                SpellVisuals.beam(world, school, start.add(0, 1, 0), dest.add(0, 1, 0), 0.9F, 12);
                SpellVisuals.spawn(world, SpellVisual.BLINK, school, start, 1.0F, 14);
                SpellVisuals.spawn(world, SpellVisual.NOVA, school, dest.add(0, 0.1, 0), 3.0F, 14);
                caster.requestTeleport(dest.x, dest.y, dest.z);
                caster.fallDistance = 0;
                sound(world, dest, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 0.8F, 1.8F);
            }
            case ROOT -> {
                LivingEntity target = SpellTargeting.lookTarget(caster, RANGE);
                if (target == null) return false;
                hit(caster, spell, target, power);
                root(target, spell.duration());
                SpellVisuals.spawn(world, SpellVisual.SPIKES, school, target.getEntityPos(), 1.6F, Math.min(60, spell.duration()));
                beamFromCaster(world, caster, school, target.getEntityPos().add(0, 1, 0));
                SpellFx.particles(world, particle(school), target.getX(), target.getY() + 0.3, target.getZ(), 25, 0.4, 0.2, 0.4, 0.0);
                sound(world, target.getEntityPos(), impactSound(school), 1.0F, 0.7F);
            }
            case PULL -> {
                Vec3d p = aimPos(caster, RANGE);
                boolean added = ZoneManager.add(world, p, null, spell.radius(), 0, spell.duration(), 5, z -> {
                    pullEnemies(caster, z.center, spell.radius(), 0.35);
                    if (z.age() % 20 == 0) {
                        for (LivingEntity e : SpellTargeting.enemiesAround(caster, z.center, spell.radius() * 0.5)) hit(caster, spell, e, power);
                    }
                    SpellFx.particles(world, ParticleTypes.REVERSE_PORTAL, z.center.x, z.center.y + 1, z.center.z, 12,
                            spell.radius() / 2, 0.8, spell.radius() / 2, 0.02);
                });
                if (!added) return false;
                SpellVisuals.spawn(world, SpellVisual.SINGULARITY, school, p.add(0, 1.2, 0), spell.radius() * 0.5F, spell.duration());
                SpellVisuals.spawn(world, SpellVisual.ZONE, school, p, spell.radius(), spell.duration());
                sound(world, p, SoundEvents.BLOCK_PORTAL_TRIGGER, 0.6F, 1.6F);
            }
            case DISTORTION -> {
                List<LivingEntity> enemies = SpellTargeting.enemiesAround(caster, caster.getEntityPos(), spell.radius());
                for (LivingEntity e : enemies) {
                    hit(caster, spell, e, power);
                    effect(e, StatusEffects.NAUSEA, spell.duration(), 0);
                    effect(e, StatusEffects.BLINDNESS, spell.duration() / 2, 0);
                    double dx = (world.random.nextDouble() - 0.5) * 8, dz = (world.random.nextDouble() - 0.5) * 8;
                    BlockPos dest = BlockPos.ofFloored(e.getX() + dx, e.getY(), e.getZ() + dz);
                    if (world.getBlockState(dest).isAir() && world.getBlockState(dest.up()).isAir()) {
                        e.requestTeleport(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
                    }
                }
                SpellFx.ring(world, ParticleTypes.PORTAL, caster.getEntityPos(), spell.radius(), 40);
                SpellVisuals.spawn(world, SpellVisual.NOVA, school, caster.getEntityPos().add(0, 0.1, 0), spell.radius(), 20);
                sound(world, caster.getEntityPos(), SoundEvents.ENTITY_ENDERMAN_SCREAM, 0.6F, 1.5F);
            }
            case SUMMON_WOLVES -> {
                SpellVisuals.spawn(world, SpellVisual.SUMMON, school, caster.getEntityPos(), 3.0F, 30);
                return summonWolves(caster);
            }
            case SUMMON_GOLEM -> {
                SpellVisuals.spawn(world, SpellVisual.SUMMON, school, caster.getEntityPos().add(caster.getRotationVec(1.0F).multiply(2.0)), 2.5F, 34);
                return summonGolem(caster);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ outils

    /** Point visé : la créature dans le viseur s'il y en a une (à ses pieds), sinon le bloc regardé. */
    static Vec3d aimPos(ServerPlayerEntity caster, double range) {
        LivingEntity target = SpellTargeting.lookTarget(caster, range);
        return target != null ? target.getEntityPos() : SpellTargeting.lookPos(caster, range);
    }

    /** Trait d'énergie fin entre la main du lanceur et le point visé (sorts de zone à distance). */
    private static void beamFromCaster(ServerWorld world, ServerPlayerEntity caster, SpellSchool school, Vec3d to) {
        Vec3d hand = caster.getEyePos().add(caster.getRotationVec(1.0F).multiply(0.6)).add(0, -0.35, 0);
        SpellVisuals.beam(world, school, hand, to, 0.22F, 8);
    }

    /** Dégâts de sort + effet d'école. */
    public static void hit(ServerPlayerEntity caster, Spell spell, LivingEntity target, float damage) {
        ServerWorld world = caster.getEntityWorld();
        float dmg = damage;
        if (spell.school() == SpellSchool.LIGHTNING && target.isTouchingWaterOrRain()) dmg *= 1.5F;
        target.damage(world, world.getDamageSources().indirectMagic(caster, caster), dmg);
        schoolEffect(spell.school(), target, damage);
    }

    public static void schoolEffect(SpellSchool school, LivingEntity target, float power) {
        switch (school) {
            case FIRE -> target.setOnFireFor(Math.min(8F, 2F + power / 4F));
            case ICE -> {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
                target.setFrozenTicks(Math.min(target.getMinFreezeDamageTicks() + 60, target.getFrozenTicks() + 60));
            }
            case NATURE -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0));
            case VOID -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 0));
            case LIGHTNING -> { /* bonus contre les cibles mouillées, appliqué dans hit() */ }
        }
    }

    public static void areaDamage(ServerPlayerEntity caster, Spell spell, Vec3d p, double radius, float power) {
        ServerWorld world = caster.getEntityWorld();
        for (LivingEntity e : SpellTargeting.enemiesAround(caster, p, radius)) {
            double falloff = 1.0 - 0.5 * Math.sqrt(e.getEntityPos().squaredDistanceTo(p)) / Math.max(radius, 0.1);
            hit(caster, spell, e, (float) (power * falloff));
        }
        SpellFx.particles(world, particle(spell.school()), p.x, p.y + 0.5, p.z, 40, radius / 2, 0.5, radius / 2, 0.05);
        SpellFx.particles(world, ParticleTypes.EXPLOSION, p.x, p.y + 0.5, p.z, 2, radius / 3, 0.2, radius / 3, 0);
        sound(world, p, impactSound(spell.school()), 1.2F, 1.0F);
    }

    private static void root(LivingEntity e, int ticks) {
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 9));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ticks, 0));
        e.setVelocity(0, Math.min(0, e.getVelocity().y), 0);
        e.velocityDirty = true;
    }

    private static void pullEnemies(ServerPlayerEntity caster, Vec3d center, double radius, double strength) {
        for (LivingEntity e : SpellTargeting.enemiesAround(caster, center, radius)) {
            Vec3d d = center.subtract(e.getEntityPos());
            if (d.lengthSquared() < 0.5) continue;
            Vec3d v = d.normalize().multiply(strength);
            e.addVelocity(v.x, 0.05, v.z);
            e.velocityDirty = true;
        }
    }

    private static Vec3d blinkDestination(ServerPlayerEntity caster, double distance) {
        Vec3d target = SpellTargeting.lookPos(caster, distance);
        Vec3d dir = caster.getRotationVec(1.0F);
        ServerWorld world = caster.getEntityWorld();
        // Recule jusqu'à trouver deux blocs d'air libres.
        for (int i = 0; i < 16; i++) {
            Vec3d p = target.subtract(dir.multiply(i * 0.5));
            BlockPos feet = BlockPos.ofFloored(p.x, p.y, p.z);
            if (world.getBlockState(feet).getCollisionShape(world, feet).isEmpty()
                    && world.getBlockState(feet.up()).getCollisionShape(world, feet.up()).isEmpty()) {
                return new Vec3d(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
            }
        }
        return null;
    }

    private static void lightningFx(ServerWorld world, Vec3d p) {
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.refreshPositionAndAngles(p.x, p.y, p.z, 0, 0);
            bolt.setCosmetic(true);
            world.spawnEntity(bolt);
        }
    }

    private static void effect(LivingEntity e, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect,
                               int duration, int amp) {
        e.addStatusEffect(new StatusEffectInstance(effect, duration, amp));
    }

    // ------------------------------------------------------------------ invocations

    private static int summonDuration(ServerPlayerEntity caster, PlayerProgress p) {
        double d = FdConfig.get().summonDurationSeconds * 20.0 * (1.0 + Skills.value(p, Skills.LASTING_BOND));
        if (SkillTree.SUMMONER.id().equals(p.specialization())) d *= 1.5;
        return (int) d;
    }

    private static boolean summonWolves(ServerPlayerEntity caster) {
        ServerWorld world = caster.getEntityWorld();
        PlayerProgress p = ProgressionManager.get(caster);
        int count = 2 + Skills.rank(p, Skills.PACK_LEADER) + (SkillTree.SUMMONER.id().equals(p.specialization()) ? 1 : 0);
        if (SummonManager.countOwnedBy(caster.getUuid()) >= 8) return false;
        long expires = world.getTime() + summonDuration(caster, p);
        for (int i = 0; i < count; i++) {
            WolfEntity wolf = EntityType.WOLF.create(world, SpawnReason.MOB_SUMMONED);
            if (wolf == null) continue;
            double a = Math.PI * 2 * i / count;
            wolf.refreshPositionAndAngles(caster.getX() + Math.cos(a) * 1.5, caster.getY(), caster.getZ() + Math.sin(a) * 1.5, caster.getYaw(), 0);
            wolf.setTamedBy(caster);
            buffSummon(wolf, p);
            world.spawnEntity(wolf);
            SummonManager.track(wolf, caster.getUuid(), expires);
            SpellFx.particles(world, ParticleTypes.HAPPY_VILLAGER, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 10, 0.3, 0.3, 0.3, 0);
        }
        sound(world, caster.getEntityPos(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
        return true;
    }

    private static boolean summonGolem(ServerPlayerEntity caster) {
        ServerWorld world = caster.getEntityWorld();
        PlayerProgress p = ProgressionManager.get(caster);
        if (SummonManager.countOwnedBy(caster.getUuid()) >= 8) return false;
        IronGolemEntity golem = EntityType.IRON_GOLEM.create(world, SpawnReason.MOB_SUMMONED);
        if (golem == null) return false;
        Vec3d front = caster.getEntityPos().add(caster.getRotationVec(1.0F).multiply(2.0));
        golem.refreshPositionAndAngles(front.x, caster.getY(), front.z, caster.getYaw(), 0);
        golem.setPlayerCreated(true);
        buffSummon(golem, p);
        world.spawnEntity(golem);
        SummonManager.track(golem, caster.getUuid(), world.getTime() + summonDuration(caster, p));
        SpellFx.particles(world, ParticleTypes.CLOUD, golem.getX(), golem.getY() + 1, golem.getZ(), 30, 0.6, 1.0, 0.6, 0.02);
        sound(world, front, SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0F, 0.8F);
        return true;
    }

    private static void buffSummon(LivingEntity e, PlayerProgress p) {
        int rank = Skills.rank(p, Skills.SPIRIT_BOND);
        if (rank <= 0) return;
        EntityAttributeInstance hp = e.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() + Skills.value(p, Skills.SPIRIT_BOND));
            e.setHealth(e.getMaxHealth());
        }
    }

    // ------------------------------------------------------------------ apparence

    public static ParticleEffect particle(SpellSchool school) {
        return switch (school) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case NATURE -> ParticleTypes.HAPPY_VILLAGER;
            case VOID -> ParticleTypes.PORTAL;
        };
    }

    private static SoundEvent castSound(SpellSchool school) {
        return switch (school) {
            case FIRE -> SoundEvents.ENTITY_BLAZE_SHOOT;
            case ICE -> SoundEvents.BLOCK_POWDER_SNOW_PLACE;
            case LIGHTNING -> SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT;
            case NATURE -> SoundEvents.BLOCK_AZALEA_LEAVES_PLACE;
            case VOID -> SoundEvents.ENTITY_ENDERMAN_TELEPORT;
        };
    }

    public static SoundEvent impactSound(SpellSchool school) {
        return switch (school) {
            case FIRE -> SoundEvents.ENTITY_GENERIC_BURN;
            case ICE -> SoundEvents.BLOCK_GLASS_BREAK;
            case LIGHTNING -> SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER;
            case NATURE -> SoundEvents.BLOCK_GRASS_BREAK;
            case VOID -> SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value();
        };
    }

    private static void sound(ServerWorld world, Vec3d p, SoundEvent s, float vol, float pitch) {
        SpellFx.sound(world, p, s, vol, pitch);
    }

    /** Liste utilitaire pour les tests : tous les sorts dont le comportement est implémenté. */
    public static List<String> implementedKinds() {
        List<String> l = new ArrayList<>();
        for (SpellKind k : SpellKind.values()) l.add(k.name());
        return l;
    }
}
