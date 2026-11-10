package com.forgotten.dimensions.entity.creature;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/** Patrouille / protection de zone : la créature revient vers son point d'apparition si elle s'en éloigne. */
public class StayNearHomeGoal extends Goal {
    private final PathAwareEntity mob;
    private final int radius;

    public StayNearHomeGoal(PathAwareEntity mob, int radius) {
        this.mob = mob;
        this.radius = radius;
        setControls(EnumSet.of(Control.MOVE));
    }

    private BlockPos home() {
        return mob instanceof FdCreature c ? c.creatureState().home : null;
    }

    @Override
    public boolean canStart() {
        BlockPos home = home();
        return home != null && mob.getTarget() == null && !home.isWithinDistance(mob.getBlockPos(), radius);
    }

    @Override
    public boolean shouldContinue() {
        BlockPos home = home();
        return home != null && mob.getTarget() == null && !mob.getNavigation().isIdle()
                && !home.isWithinDistance(mob.getBlockPos(), radius / 2.0);
    }

    @Override
    public void start() {
        BlockPos home = home();
        if (home != null) mob.getNavigation().startMovingTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
    }
}
