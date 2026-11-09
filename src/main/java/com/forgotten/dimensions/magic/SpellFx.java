package com.forgotten.dimensions.magic;

import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

/** Effets visuels et sonores des sorts, limités par la configuration (particleMultiplier). */
public final class SpellFx {
    private SpellFx() {}

    public static void particles(ServerWorld world, ParticleEffect type, double x, double y, double z, int count,
                                 double dx, double dy, double dz, double speed) {
        double mult = FdConfig.get().particleMultiplier;
        if (mult <= 0) return;
        int n = Math.max(1, (int) Math.round(Math.min(count, 200) * mult));
        world.spawnParticles(type, x, y, z, n, dx, dy, dz, speed);
    }

    public static void ring(ServerWorld world, ParticleEffect type, Vec3d center, double radius, int points) {
        double mult = FdConfig.get().particleMultiplier;
        if (mult <= 0) return;
        int n = Math.max(4, (int) Math.round(points * mult));
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            world.spawnParticles(type, center.x + Math.cos(a) * radius, center.y + 0.2, center.z + Math.sin(a) * radius,
                    1, 0, 0.05, 0, 0.0);
        }
    }

    public static void line(ServerWorld world, ParticleEffect type, Vec3d from, Vec3d to, double step) {
        double mult = FdConfig.get().particleMultiplier;
        if (mult <= 0) return;
        Vec3d d = to.subtract(from);
        double len = d.length();
        int n = (int) Math.min(80, Math.max(1, len / step * mult));
        for (int i = 0; i <= n; i++) {
            Vec3d p = from.add(d.multiply(i / (double) n));
            world.spawnParticles(type, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    public static void sound(ServerWorld world, Vec3d pos, SoundEvent sound, float volume, float pitch) {
        world.playSound(null, pos.x, pos.y, pos.z, sound, SoundCategory.PLAYERS, volume, pitch);
    }
}
