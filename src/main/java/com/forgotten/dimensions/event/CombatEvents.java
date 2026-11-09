package com.forgotten.dimensions.event;

import com.forgotten.dimensions.item.LegendaryEffects;
import com.forgotten.dimensions.magic.ManaManager;
import com.forgotten.dimensions.magic.SpellFx;
import com.forgotten.dimensions.magic.SummonManager;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.progression.Skills;
import com.forgotten.dimensions.progression.XpSource;
import com.forgotten.dimensions.util.PlayerCache;
import com.forgotten.dimensions.util.config.FdConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

/** Expérience de combat, compétences réactives et artefacts réactifs. */
public final class CombatEvents {
    private CombatEvents() {}

    private static boolean inCleave = false;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(CombatEvents::afterDeath);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(CombatEvents::afterDamage);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(CombatEvents::allowDamage);
        ServerLivingEntityEvents.ALLOW_DEATH.register(CombatEvents::allowDeath);
    }

    /** Expérience accordée pour la mort d'une créature (formule pure, testée). */
    public static int killXp(float maxHealth, boolean hostile, boolean boss) {
        if (boss) return 200 + Math.round(maxHealth);
        if (hostile) return 4 + Math.round(maxHealth / 3F);
        return 1;
    }

    private static void afterDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof PlayerEntity) return;
        Entity attacker = source.getAttacker();
        ServerPlayerEntity player = attacker instanceof ServerPlayerEntity p ? p : null;
        if (player == null && attacker != null && SummonManager.isTracked(attacker)) {
            var owner = SummonManager.ownerOf(attacker);
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            player = owner == null ? null : world.getServer().getPlayerManager().getPlayer(owner);
            if (player != null) {
                int soul = Skills.rank(ProgressionManager.get(player), Skills.SOUL_LINK);
                if (soul > 0) player.heal(soul * 2.0F);
            }
        }
        if (player == null) return;
        boolean boss = entity.getType().isIn(LegendaryEffects.BOSSES);
        int xp = (int) Math.round(killXp(entity.getMaxHealth(), entity instanceof Monster, boss) * FdConfig.get().rewardMultiplier);
        ProgressionManager.addXp(player, xp, boss ? XpSource.BOSS : XpSource.COMBAT);
        int secondWind = Skills.rank(ProgressionManager.get(player), Skills.SECOND_WIND);
        if (secondWind > 0 && source.getAttacker() == player) player.heal(secondWind * 2.0F);
    }

    private static void afterDamage(LivingEntity entity, DamageSource source, float baseTaken, float taken, boolean blocked) {
        if (taken <= 0 || !(entity.getEntityWorld() instanceof ServerWorld world)) return;
        Entity direct = source.getSource();
        Entity attacker = source.getAttacker();

        // Coups au corps à corps d'un joueur.
        if (attacker instanceof ServerPlayerEntity player && direct == player && !inCleave) {
            PlayerCache.Snapshot snap = PlayerCache.get(player);
            if (snap.has("blood_chalice")) player.heal(taken * 0.10F);
            double cleave = Skills.value(ProgressionManager.get(player), Skills.CLEAVE);
            if (cleave > 0) {
                inCleave = true;
                try {
                    for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(2.5),
                            e -> e != entity && e != player && e.isAlive() && !(e instanceof PlayerEntity) && !SummonManager.isSummon(e))) {
                        e.damage(world, world.getDamageSources().playerAttack(player), (float) (taken * cleave));
                    }
                } finally {
                    inCleave = false;
                }
            }
        }
        // Couronne de ronces : renvoie des dégâts aux attaquants au corps à corps.
        if (entity instanceof ServerPlayerEntity victim && attacker instanceof LivingEntity living && direct == attacker
                && PlayerCache.get(victim).has("briar_crown")) {
            living.damage(world, world.getDamageSources().thorns(victim), 2.0F);
        }
        // Flèches légendaires.
        if (direct instanceof PersistentProjectileEntity arrow) {
            if (arrow.getCommandTags().contains(LegendaryEffects.STORM_ARROW)) {
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
                if (bolt != null) {
                    bolt.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), 0, 0);
                    bolt.setCosmetic(true);
                    world.spawnEntity(bolt);
                }
                arrow.removeCommandTag(LegendaryEffects.STORM_ARROW);
                entity.damage(world, world.getDamageSources().indirectMagic(arrow, attacker), 5.0F);
            } else if (arrow.getCommandTags().contains(LegendaryEffects.VOID_ARROW)) {
                SpellFx.particles(world, ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 30, 0));
            }
        }
    }

    /** Esquive (archer) et bouclier de mana (mage). */
    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player) || source.getAttacker() == null) return true;
        PlayerProgress p = ProgressionManager.get(player);
        double evasion = Skills.value(p, Skills.EVASION);
        if (evasion > 0 && player.getRandom().nextDouble() < evasion) {
            player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, SoundCategory.PLAYERS, 1.0F, 1.5F);
            return false;
        }
        return true;
    }

    /** Plume de phénix : empêche un coup fatal, puis se recharge 10 minutes. */
    private static boolean allowDeath(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player) || !PlayerCache.compute(player).has("phoenix_feather")) return true;
        ItemStack feather = null;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.isOf(com.forgotten.dimensions.registry.ModItems.get("phoenix_feather"))) {
                feather = s;
                break;
            }
        }
        if (feather == null || player.getItemCooldownManager().isCoolingDown(feather)) return true;
        player.setHealth(8.0F);
        player.clearStatusEffects();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 400, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1));
        player.getItemCooldownManager().set(feather, 12000);
        ServerWorld world = player.getEntityWorld();
        SpellFx.particles(world, ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 60, 0.6, 1.0, 0.6, 0.1);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 1.2F);
        return false;
    }

    /**
     * Modificateurs de dégâts (appelés par le mixin sur LivingEntity#damage) : compétences d'archer,
     * chasseur, berserker, charme œil d'aigle, bouclier de mana.
     */
    public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
        float result = amount;
        Entity direct = source.getSource();
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            PlayerProgress p = ProgressionManager.get(attacker);
            if (direct instanceof PersistentProjectileEntity) {
                result *= (float) (1.0 + Skills.value(p, Skills.HAWK_EYE));
                if (PlayerCache.get(attacker).has("eagle_eye_charm")) result *= 1.25F;
                if (SkillTree.ARCHER.id().equals(p.specialization())) result *= 1.15F;
                double dist = Math.min(30.0, attacker.distanceTo(target));
                result *= (float) (1.0 + Skills.value(p, Skills.MARKSMAN) * dist / 10.0);
                double armor = Math.min(1.0, target.getArmor() / 10.0);
                result *= (float) (1.0 + Skills.value(p, Skills.PIERCING_SHOT) * armor);
            }
            if (!(target instanceof PlayerEntity)) result *= (float) (1.0 + Skills.value(p, Skills.HUNTER));
        }
        if (target instanceof ServerPlayerEntity victim && source.getAttacker() != null) {
            double shield = Skills.value(ProgressionManager.get(victim), Skills.MANA_SHIELD);
            if (shield > 0) {
                float absorbed = (float) (result * shield);
                float mana = ManaManager.get(victim).current();
                absorbed = Math.min(absorbed, mana);
                if (absorbed > 0 && ManaManager.consume(victim, absorbed)) result -= absorbed;
            }
        }
        return result;
    }

    static Box around(LivingEntity e, double r) {
        return e.getBoundingBox().expand(r);
    }
}
