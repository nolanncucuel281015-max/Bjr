package com.forgotten.dimensions.entity.creature;

import com.forgotten.dimensions.content.CreatureCategory;
import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.content.CreatureMovement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comportement commun aux 60 créatures, configuré par leur {@link CreatureDef} :
 * déplacement (sol, vol, nage), objectifs d'IA (patrouille, poursuite, attaque, fuite, défense,
 * protection de zone, appel de renforts), capacités, barre de vie des mini-boss.
 */
public final class CreatureLogic {
    private static final Map<EntityType<?>, CreatureDef> DEFS = new HashMap<>();

    private CreatureLogic() {}

    public static void bind(EntityType<?> type, CreatureDef def) {
        DEFS.put(type, def);
    }

    /** Fonctionne dès le constructeur de MobEntity (le type est connu avant nos champs). */
    public static CreatureDef def(Entity e) {
        return DEFS.get(e.getType());
    }

    /** Contrôle de déplacement adapté (null = contrôle terrestre par défaut). Gravité coupée pour les volants. */
    public static net.minecraft.entity.ai.control.MoveControl moveControl(MobEntity mob, CreatureDef d) {
        if (d == null) return null;
        if (d.movement() == CreatureMovement.FLYING) {
            mob.setNoGravity(true);
            return new FlightMoveControl(mob, 20, true);
        }
        if (d.movement() == CreatureMovement.SWIMMING) return new AquaticMoveControl(mob, 85, 10, 0.02F, 0.1F, true);
        return null;
    }

    public static EntityNavigation navigation(MobEntity mob, World world, CreatureDef d) {
        if (d == null) return new MobNavigation(mob, world);
        return switch (d.movement()) {
            case FLYING -> {
                BirdNavigation nav = new BirdNavigation(mob, world);
                nav.setCanOpenDoors(false);
                nav.setCanSwim(true);
                yield nav;
            }
            case SWIMMING -> new SwimNavigation(mob, world);
            default -> new MobNavigation(mob, world);
        };
    }

    public static void initGoals(PathAwareEntity mob, GoalSelector goals, GoalSelector targets, CreatureDef d) {
        if (d == null) return;
        CreatureCategory cat = d.category();
        if (d.movement() != CreatureMovement.SWIMMING) goals.add(0, new SwimGoal(mob));
        if (d.does("flee_low")) {
            goals.add(1, new FleeEntityGoal<>(mob, PlayerEntity.class, p -> mob.getHealth() < mob.getMaxHealth() * 0.3F,
                    12.0F, 1.0, 1.4, e -> true));
        }
        if (cat == CreatureCategory.PASSIVE) {
            goals.add(1, new EscapeDangerGoal(mob, 1.4));
            if (d.does("skittish")) goals.add(2, new FleeEntityGoal<>(mob, PlayerEntity.class, 8.0F, 1.0, 1.3));
        } else {
            goals.add(2, new MeleeAttackGoal(mob, d.movement() == CreatureMovement.GROUND ? 1.15 : 1.0, true));
        }
        if (d.does("patrol") || d.does("guard")) goals.add(4, new StayNearHomeGoal(mob, d.does("guard") ? 16 : 24));
        switch (d.movement()) {
            case FLYING -> goals.add(5, new FlyGoal(mob, 1.0));
            case SWIMMING -> goals.add(5, new SwimAroundGoal(mob, 1.0, 10));
            default -> goals.add(5, new WanderAroundFarGoal(mob, 0.9));
        }
        goals.add(6, new LookAtEntityGoal(mob, PlayerEntity.class, 8.0F));
        goals.add(7, new LookAroundGoal(mob));

        if (cat != CreatureCategory.PASSIVE) targets.add(1, new RevengeGoal(mob));
        if (cat.aggressive()) {
            if (d.does("guard")) {
                targets.add(2, new ActiveTargetGoal<>(mob, PlayerEntity.class, true, (target, world) -> {
                    FdCreature c = (FdCreature) mob;
                    return c.creatureState().home == null || target.getBlockPos().isWithinDistance(c.creatureState().home, 16);
                }));
            } else {
                targets.add(2, new ActiveTargetGoal<>(mob, PlayerEntity.class, true));
            }
        }
    }

    /** Appelé à chaque tick serveur (mobTick). */
    public static void serverTick(MobEntity mob, ServerWorld world) {
        if (!(mob instanceof FdCreature c)) return;
        CreatureDef d = c.definition();
        CreatureState s = c.creatureState();
        if (s.home == null) s.home = mob.getBlockPos();
        if (mob.isAiDisabled()) return;
        s.tickCooldowns();
        CreatureAbilities.tick(mob, d, s, world);
        if ((d.does("pack") || d.does("reinforce")) && mob.age % 40 == 0 && mob.getTarget() != null) {
            alertKin(mob, mob.getTarget(), 12);
        }
        if (d.category() == CreatureCategory.MINIBOSS) updateBossBar(mob, s, world);
    }

    /** Appel de renforts : les créatures de même espèce proches ciblent l'agresseur. */
    public static void alertKin(MobEntity mob, LivingEntity target, double radius) {
        if (target == null || !target.isAlive() || (target instanceof PlayerEntity p && (p.isCreative() || p.isSpectator()))) return;
        List<MobEntity> kin = mob.getEntityWorld().getEntitiesByClass(MobEntity.class, mob.getBoundingBox().expand(radius),
                e -> e != mob && e.getType() == mob.getType() && e.getTarget() == null && e.isAlive());
        for (MobEntity k : kin) {
            if (def(k) != null && def(k).category() != CreatureCategory.PASSIVE) k.setTarget(target);
        }
    }

    public static void onDamaged(MobEntity mob, DamageSource source) {
        CreatureDef d = def(mob);
        if (d == null) return;
        if ((d.does("reinforce") || d.does("pack")) && source.getAttacker() instanceof LivingEntity attacker) {
            alertKin(mob, attacker, 16);
        }
    }

    public static void onAttackHit(MobEntity mob, Entity target) {
        CreatureDef d = def(mob);
        if (d != null && target instanceof LivingEntity living) CreatureAbilities.onHit(mob, d, living);
    }

    public static void onDeath(MobEntity mob, DamageSource source) {
        CreatureDef d = def(mob);
        if (d != null && mob.getEntityWorld() instanceof ServerWorld world) CreatureAbilities.onDeath(mob, d, world);
        clearBossBar(mob);
    }

    private static void updateBossBar(MobEntity mob, CreatureState s, ServerWorld world) {
        if (s.bossBar == null) {
            s.bossBar = new ServerBossBar(mob.getDisplayName(), BossBar.Color.RED, BossBar.Style.NOTCHED_10);
        }
        s.bossBar.setPercent(mob.getHealth() / mob.getMaxHealth());
        if (mob.age % 20 != 0) return;
        for (ServerPlayerEntity p : world.getPlayers()) {
            boolean near = p.squaredDistanceTo(mob) < 32 * 32;
            if (near && !s.bossBar.getPlayers().contains(p)) s.bossBar.addPlayer(p);
            else if (!near && s.bossBar.getPlayers().contains(p)) s.bossBar.removePlayer(p);
        }
    }

    public static void clearBossBar(MobEntity mob) {
        if (mob instanceof FdCreature c && c.creatureState().bossBar != null) {
            c.creatureState().bossBar.clearPlayers();
        }
    }
}
