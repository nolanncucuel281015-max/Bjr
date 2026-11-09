package com.forgotten.dimensions.magic;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Zones magiques persistantes (murs de flammes, blizzards, failles...) et effets différés (météores).
 * Ticks traités une seule fois par tick serveur et par monde ; nombre de zones plafonné.
 */
public final class ZoneManager {
    public static final int MAX_ZONES_PER_WORLD = 64;

    public static final class Zone {
        public final ServerWorld world;
        public final Entity follow;
        public Vec3d center;
        public final double radius;
        int delay;
        int remaining;
        final int interval;
        final Consumer<Zone> pulse;
        int age;

        Zone(ServerWorld world, Vec3d center, Entity follow, double radius, int delay, int duration, int interval, Consumer<Zone> pulse) {
            this.world = world;
            this.center = center;
            this.follow = follow;
            this.radius = radius;
            this.delay = delay;
            this.remaining = duration;
            this.interval = Math.max(1, interval);
            this.pulse = pulse;
        }

        public int age() {
            return age;
        }
    }

    private static final Map<ServerWorld, List<Zone>> ZONES = new IdentityHashMap<>();

    private ZoneManager() {}

    /** Zone qui déclenche {@code pulse} toutes les {@code interval} ticks pendant {@code duration} ticks après {@code delay}. */
    public static boolean add(ServerWorld world, Vec3d center, Entity follow, double radius, int delay, int duration,
                              int interval, Consumer<Zone> pulse) {
        List<Zone> list = ZONES.computeIfAbsent(world, w -> new ArrayList<>());
        if (list.size() >= MAX_ZONES_PER_WORLD) return false;
        list.add(new Zone(world, center, follow, radius, delay, duration, interval, pulse));
        return true;
    }

    public static void tick(ServerWorld world) {
        List<Zone> list = ZONES.get(world);
        if (list == null || list.isEmpty()) return;
        Iterator<Zone> it = list.iterator();
        while (it.hasNext()) {
            Zone z = it.next();
            if (z.follow != null) {
                if (!z.follow.isAlive()) {
                    it.remove();
                    continue;
                }
                z.center = z.follow.getEntityPos();
            }
            if (z.delay > 0) {
                z.delay--;
                continue;
            }
            if (z.age % z.interval == 0) z.pulse.accept(z);
            z.age++;
            if (--z.remaining <= 0) it.remove();
        }
    }

    public static int count(ServerWorld world) {
        List<Zone> list = ZONES.get(world);
        return list == null ? 0 : list.size();
    }

    public static void clear() {
        ZONES.clear();
    }
}
