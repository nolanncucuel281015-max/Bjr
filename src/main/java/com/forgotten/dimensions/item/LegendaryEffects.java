package com.forgotten.dimensions.item;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.magic.SpellFx;
import com.forgotten.dimensions.util.StatusEffectsHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Effets des armes légendaires et passifs des catégories d'armes (côté serveur). */
public final class LegendaryEffects {
    public static final TagKey<EntityType<?>> BOSSES = TagKey.of(RegistryKeys.ENTITY_TYPE, ForgottenDimensions.id("bosses"));
    /** Tags posés sur les flèches tirées par les arcs légendaires. */
    public static final String VOID_ARROW = "forgotten_dimensions.void_arrow";
    public static final String STORM_ARROW = "forgotten_dimensions.storm_arrow";

    public static final List<String> IMPLEMENTED = List.of("celestial_blade", "blade_of_corruption", "abyssal_trident",
            "volcanic_hammer", "void_bow", "frost_queen_scepter", "sky_emperor_greatsword", "depths_fang",
            "stormcaller_crossbow", "world_ender");

    private LegendaryEffects() {}

    private static boolean inCleave = false;

    /** Coup au corps à corps porté avec une arme du mod. */
    public static void onMeleeHit(com.forgotten.dimensions.content.WeaponDef def, LivingEntity target, LivingEntity attacker) {
        if (!(attacker.getEntityWorld() instanceof ServerWorld world) || inCleave) return;
        inCleave = true;
        try {
            switch (def.legendary()) {
                case "celestial_blade" -> {
                    if (world.random.nextFloat() < 0.3F || attacker.fallDistance > 0.0F) {
                        splash(world, attacker, target, 3.0, 4.0F);
                        SpellFx.ring(world, ParticleTypes.END_ROD, target.getEntityPos(), 3.0, 32);
                        world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.8F, 1.8F);
                    }
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0));
                }
                case "blade_of_corruption" -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 1));
                    attacker.heal(1.5F);
                    SpellFx.particles(world, ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1, target.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                }
                case "abyssal_trident" -> {
                    if (attacker.isTouchingWater()) {
                        target.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), 4.0F);
                        SpellFx.particles(world, ParticleTypes.BUBBLE, target.getX(), target.getY() + 1, target.getZ(), 20, 0.4, 0.5, 0.4, 0.1);
                    }
                }
                case "volcanic_hammer" -> {
                    for (LivingEntity e : nearby(world, attacker, target, 3.5)) {
                        e.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), 4.0F);
                        e.setOnFireFor(3.0F);
                        Vec3d push = e.getEntityPos().subtract(target.getEntityPos()).normalize().multiply(0.8);
                        e.addVelocity(push.x, 0.35, push.z);
                        e.velocityDirty = true;
                    }
                    target.setOnFireFor(4.0F);
                    SpellFx.ring(world, ParticleTypes.FLAME, target.getEntityPos(), 3.5, 36);
                    world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.6F, 1.2F);
                }
                case "sky_emperor_greatsword" -> {
                    if (world.random.nextFloat() < 0.3F) {
                        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.refreshPositionAndAngles(target.getX(), target.getY(), target.getZ(), 0, 0);
                            bolt.setCosmetic(true);
                            world.spawnEntity(bolt);
                        }
                        target.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), 6.0F);
                    }
                }
                case "depths_fang" -> {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 1));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0));
                    Vec3d look = target.getRotationVec(1.0F);
                    Vec3d toAttacker = attacker.getEntityPos().subtract(target.getEntityPos()).normalize();
                    if (look.dotProduct(toAttacker) < -0.3) {
                        target.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), 4.0F);
                        SpellFx.particles(world, ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 15, 0.3, 0.5, 0.3, 0.2);
                    }
                }
                case "world_ender" -> {
                    if (target.getType().isIn(BOSSES)) {
                        target.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), 10.0F);
                    }
                    attacker.heal(2.0F);
                    splash(world, attacker, target, 3.0, 5.0F);
                    SpellFx.particles(world, ParticleTypes.WITCH, target.getX(), target.getY() + 1, target.getZ(), 20, 0.6, 0.6, 0.6, 0.02);
                }
                default -> categoryPassive(def, target, attacker, world);
            }
        } finally {
            inCleave = false;
        }
    }

    private static void categoryPassive(com.forgotten.dimensions.content.WeaponDef def, LivingEntity target, LivingEntity attacker, ServerWorld world) {
        switch (def.category()) {
            case GREATSWORD -> splash(world, attacker, target, 2.5, 2.0F + rarityBonus(def));
            case DAGGER -> {
                if (world.random.nextFloat() < 0.15F) {
                    target.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), 2.0F + rarityBonus(def));
                    SpellFx.particles(world, ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1, target.getZ(), 5, 0.2, 0.3, 0.2, 0.1);
                }
            }
            case BATTLEAXE -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0));
            default -> { }
        }
    }

    private static float rarityBonus(com.forgotten.dimensions.content.WeaponDef def) {
        return def.rarity().ordinal() * 0.5F;
    }

    private static List<LivingEntity> nearby(ServerWorld world, LivingEntity attacker, LivingEntity center, double radius) {
        Box box = center.getBoundingBox().expand(radius);
        return world.getEntitiesByClass(LivingEntity.class, box, e -> e != attacker && e != center && e.isAlive()
                && !(e instanceof PlayerEntity) && !com.forgotten.dimensions.magic.SummonManager.isSummon(e)
                && !(e instanceof net.minecraft.entity.passive.TameableEntity t && t.isTamed()));
    }

    private static void splash(ServerWorld world, LivingEntity attacker, LivingEntity center, double radius, float damage) {
        for (LivingEntity e : nearby(world, attacker, center, radius)) {
            e.damage(world, world.getDamageSources().indirectMagic(attacker, attacker), damage);
        }
        SpellFx.particles(world, ParticleTypes.SWEEP_ATTACK, center.getX(), center.getY() + 1, center.getZ(), 1, 0, 0, 0, 0);
    }

    /** Effet passif quand l'arme est tenue en main principale (appelé chaque seconde). */
    public static void whileHeld(String legendary, LivingEntity holder) {
        if (legendary.equals("abyssal_trident")) {
            StatusEffectsHelper.refresh(holder, StatusEffects.WATER_BREATHING, 0);
            if (holder.isTouchingWater()) StatusEffectsHelper.refresh(holder, StatusEffects.DOLPHINS_GRACE, 0);
        }
    }
}
