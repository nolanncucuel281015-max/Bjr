package com.forgotten.dimensions.item;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.magic.SpellTargeting;
import com.forgotten.dimensions.util.PlayerCache;
import com.forgotten.dimensions.util.StatusEffectsHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Effets des 30 artefacts. Passifs : réappliqués chaque seconde tant que l'artefact est dans
 * l'inventaire. Actifs : déclenchés par un clic droit, avec un temps de recharge vanilla.
 * Les effets « réactifs » (calice, couronne de ronces, plume de phénix, charme œil d'aigle) sont
 * branchés dans {@link com.forgotten.dimensions.event.CombatEvents}.
 */
public final class ArtifactEffects {
    /** Artefacts réactifs ou lus par d'autres systèmes (mana, expérience, dégâts). */
    public static final List<String> HANDLED_ELSEWHERE = List.of("heart_of_the_ancients", "mana_prism", "archmage_tome",
            "architect_cog", "explorer_astrolabe", "blood_chalice", "briar_crown", "phoenix_feather", "eagle_eye_charm");
    /** Artefacts à utilisation active. */
    public static final List<String> ACTIVE = List.of("ring_of_time", "dimensional_eye", "frozen_hourglass", "void_compass",
            "storm_orb", "tidal_bell", "spirit_bell");

    public static final TagKey<Structure> ANCIENT_RUINS = TagKey.of(RegistryKeys.STRUCTURE, ForgottenDimensions.id("ancient_ruins"));

    private record Mod(String artifact, RegistryEntry<EntityAttribute> attribute, double value, Operation op, int index) {
        Identifier id() {
            return ForgottenDimensions.id("artifact/" + artifact + "/" + index);
        }
    }

    private static final List<Mod> MODS = new ArrayList<>();

    static {
        mod("crown_of_seven_worlds", EntityAttributes.MAX_HEALTH, 10, Operation.ADD_VALUE);
        mod("crown_of_seven_worlds", EntityAttributes.ARMOR, 2, Operation.ADD_VALUE);
        mod("crown_of_seven_worlds", EntityAttributes.ATTACK_DAMAGE, 0.10, Operation.ADD_MULTIPLIED_TOTAL);
        mod("corrupted_heart", EntityAttributes.ATTACK_DAMAGE, 3, Operation.ADD_VALUE);
        mod("corrupted_heart", EntityAttributes.MAX_HEALTH, -4, Operation.ADD_VALUE);
        mod("titan_belt", EntityAttributes.ARMOR, 2, Operation.ADD_VALUE);
        mod("titan_belt", EntityAttributes.KNOCKBACK_RESISTANCE, 0.3, Operation.ADD_VALUE);
        mod("sky_feather", EntityAttributes.MOVEMENT_SPEED, 0.10, Operation.ADD_MULTIPLIED_BASE);
        mod("guardian_emblem", EntityAttributes.ARMOR_TOUGHNESS, 2, Operation.ADD_VALUE);
        mod("sigil_of_war", EntityAttributes.ATTACK_DAMAGE, 0.10, Operation.ADD_MULTIPLIED_TOTAL);
        mod("stellar_charm", EntityAttributes.LUCK, 1, Operation.ADD_VALUE);
        mod("seed_of_life", EntityAttributes.MAX_HEALTH, 4, Operation.ADD_VALUE);
        mod("amulet_of_winds", EntityAttributes.FALL_DAMAGE_MULTIPLIER, -1.0, Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void mod(String artifact, RegistryEntry<EntityAttribute> attr, double value, Operation op) {
        int index = (int) MODS.stream().filter(m -> m.artifact.equals(artifact)).count();
        MODS.add(new Mod(artifact, attr, value, op, index));
    }

    private ArtifactEffects() {}

    /** Liste de tous les artefacts dont l'effet est implémenté (vérifiée par les tests). */
    public static boolean isImplemented(String id) {
        if (HANDLED_ELSEWHERE.contains(id) || ACTIVE.contains(id)) return true;
        if (MODS.stream().anyMatch(m -> m.artifact.equals(id))) return true;
        return List.of("abyssal_amulet", "ember_core", "lantern_of_the_lost", "shadow_cloak", "deep_lantern").contains(id);
    }

    /** Effets passifs (appel toutes les 20 ticks). */
    public static void applyPassives(ServerPlayerEntity player, PlayerCache.Snapshot snap, long tick) {
        for (Mod m : MODS) {
            EntityAttributeInstance inst = player.getAttributeInstance(m.attribute);
            if (inst == null) continue;
            boolean active = snap.has(m.artifact);
            boolean present = inst.hasModifier(m.id());
            if (active && !present) inst.addTemporaryModifier(new EntityAttributeModifier(m.id(), m.value, m.op));
            else if (!active && present) inst.removeModifier(m.id());
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());

        if (snap.has("amulet_of_winds")) StatusEffectsHelper.refresh(player, StatusEffects.JUMP_BOOST, 0);
        if (snap.has("abyssal_amulet")) {
            StatusEffectsHelper.refresh(player, StatusEffects.WATER_BREATHING, 0);
            if (player.isTouchingWater()) StatusEffectsHelper.refresh(player, StatusEffects.DOLPHINS_GRACE, 0);
        }
        if (snap.has("ember_core")) StatusEffectsHelper.refresh(player, StatusEffects.FIRE_RESISTANCE, 0);
        if (snap.has("lantern_of_the_lost")) StatusEffectsHelper.refresh(player, StatusEffects.NIGHT_VISION, 0);
        if (snap.has("deep_lantern")) StatusEffectsHelper.refresh(player, StatusEffects.HASTE, 0);
        if (snap.has("shadow_cloak") && player.isSneaking()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 40, 0, true, false, true));
        }
        if (snap.has("seed_of_life") && tick % 100 == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0F);
        }
    }

    /** Effet actif. @return durée de recharge en ticks si l'effet s'est déclenché, 0 sinon. */
    public static int use(ServerPlayerEntity player, String id) {
        ServerWorld world = player.getEntityWorld();
        switch (id) {
            case "ring_of_time" -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 200, 1));
                sound(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.6F);
                particles(world, player, ParticleTypes.END_ROD, 30);
                return 1200;
            }
            case "dimensional_eye" -> {
                BlockPos found = world.locateStructure(ANCIENT_RUINS, player.getBlockPos(), 64, false);
                if (found == null) {
                    player.sendMessage(Text.translatable("message.forgotten_dimensions.eye.none").formatted(Formatting.GRAY), true);
                } else {
                    int dist = (int) Math.sqrt(found.getSquaredDistance(player.getBlockPos()));
                    player.sendMessage(Text.translatable("message.forgotten_dimensions.eye.found", found.getX(), found.getZ(), dist)
                            .formatted(Formatting.LIGHT_PURPLE), false);
                }
                sound(player, SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F);
                return 600;
            }
            case "frozen_hourglass" -> {
                for (LivingEntity e : SpellTargeting.enemiesAround(player, player.getEntityPos(), 8.0)) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 9));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 120, 4));
                    e.setFrozenTicks(e.getMinFreezeDamageTicks() + 40);
                }
                sound(player, SoundEvents.BLOCK_GLASS_BREAK, 0.6F);
                particles(world, player, ParticleTypes.SNOWFLAKE, 60);
                return 900;
            }
            case "void_compass" -> {
                TeleportTarget target = player.getRespawnTarget(false, TeleportTarget.NO_OP);
                particles(world, player, ParticleTypes.REVERSE_PORTAL, 40);
                player.teleportTo(target);
                sound(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.8F);
                return 6000;
            }
            case "storm_orb" -> {
                LivingEntity target = SpellTargeting.lookTarget(player, 32.0);
                if (target == null) {
                    player.sendMessage(Text.translatable("message.forgotten_dimensions.no_target").formatted(Formatting.GRAY), true);
                    return 0;
                }
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                if (bolt != null) {
                    bolt.refreshPositionAndAngles(target.getX(), target.getY(), target.getZ(), 0, 0);
                    bolt.setCosmetic(true);
                    world.spawnEntity(bolt);
                }
                target.damage(world, world.getDamageSources().indirectMagic(player, player), 12.0F);
                return 400;
            }
            case "tidal_bell" -> {
                player.heal(6.0F);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0));
                sound(player, SoundEvents.BLOCK_BELL_USE, 1.2F);
                particles(world, player, ParticleTypes.SPLASH, 40);
                return 900;
            }
            case "spirit_bell" -> {
                for (ServerPlayerEntity ally : SpellTargeting.alliesAround(player, 10.0)) {
                    ally.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
                }
                sound(player, SoundEvents.BLOCK_BELL_RESONATE, 1.0F);
                particles(world, player, ParticleTypes.HAPPY_VILLAGER, 30);
                return 800;
            }
            default -> {
                return 0;
            }
        }
    }

    private static void sound(ServerPlayerEntity player, net.minecraft.sound.SoundEvent sound, float pitch) {
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundCategory.PLAYERS, 1.0F, pitch);
    }

    private static void particles(ServerWorld world, ServerPlayerEntity p, net.minecraft.particle.SimpleParticleType type, int count) {
        com.forgotten.dimensions.magic.SpellFx.particles(world, type, p.getX(), p.getY() + 1.0, p.getZ(), count, 0.6, 0.8, 0.6, 0.05);
    }
}
