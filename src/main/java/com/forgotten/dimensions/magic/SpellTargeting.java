package com.forgotten.dimensions.magic;

import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;

/** Sélection des cibles : les sorts offensifs épargnent le lanceur, ses alliés, ses invocations et les PNJ. */
public final class SpellTargeting {
    private SpellTargeting() {}

    public static boolean isEnemy(ServerPlayerEntity caster, Entity e) {
        if (!(e instanceof LivingEntity living) || !living.isAlive() || e == caster) return false;
        if (e instanceof ArmorStandEntity || e instanceof MerchantEntity) return false;
        if (e instanceof PlayerEntity other) return FdConfig.get().spellFriendlyFire && !other.isCreative() && !other.isSpectator();
        if (e instanceof TameableEntity t && t.isTamed()) return false;
        if (e instanceof IronGolemEntity g && g.isPlayerCreated()) return false;
        return !SummonManager.isSummon(e);
    }

    public static List<LivingEntity> enemiesAround(ServerPlayerEntity caster, Vec3d center, double radius) {
        Box box = new Box(center, center).expand(radius);
        return caster.getEntityWorld().getEntitiesByClass(LivingEntity.class, box,
                e -> isEnemy(caster, e) && e.getEntityPos().squaredDistanceTo(center) <= radius * radius);
    }

    public static List<ServerPlayerEntity> alliesAround(ServerPlayerEntity caster, double radius) {
        Box box = caster.getBoundingBox().expand(radius);
        return caster.getEntityWorld().getEntitiesByClass(ServerPlayerEntity.class, box, p -> p.isAlive() && !p.isSpectator());
    }

    /** Point visé (bloc touché ou portée maximale). */
    public static Vec3d lookPos(ServerPlayerEntity caster, double range) {
        Vec3d eye = caster.getEyePos();
        Vec3d end = eye.add(caster.getRotationVec(1.0F).multiply(range));
        BlockHitResult hit = caster.getEntityWorld().raycast(new RaycastContext(eye, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getPos();
    }

    /** Premier ennemi visé, en tenant compte des blocs. */
    public static LivingEntity lookTarget(ServerPlayerEntity caster, double range) {
        Vec3d eye = caster.getEyePos();
        Vec3d blockEnd = lookPos(caster, range);
        Vec3d dir = caster.getRotationVec(1.0F);
        Box box = caster.getBoundingBox().stretch(dir.multiply(range)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(caster, eye, blockEnd, box, e -> isEnemy(caster, e),
                eye.squaredDistanceTo(blockEnd));
        return hit != null && hit.getEntity() instanceof LivingEntity l ? l : null;
    }
}
