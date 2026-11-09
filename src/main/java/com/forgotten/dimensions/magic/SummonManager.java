package com.forgotten.dimensions.magic;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Invocations temporaires. Chaque invocation porte le tag de commande {@link #TAG} ; toute entité
 * portant ce tag sans être suivie (après un redémarrage du serveur) est supprimée à son chargement.
 */
public final class SummonManager {
    public static final String TAG = "forgotten_dimensions.summon";

    private record Entry(Entity entity, UUID owner, long expiresAt) {}

    private static final Map<UUID, Entry> ACTIVE = new HashMap<>();

    private SummonManager() {}

    public static void track(Entity entity, UUID owner, long expiresAt) {
        entity.addCommandTag(TAG);
        ACTIVE.put(entity.getUuid(), new Entry(entity, owner, expiresAt));
    }

    public static boolean isSummon(Entity e) {
        return e.getCommandTags().contains(TAG);
    }

    public static boolean isTracked(Entity e) {
        return ACTIVE.containsKey(e.getUuid());
    }

    public static UUID ownerOf(Entity e) {
        Entry entry = ACTIVE.get(e.getUuid());
        return entry == null ? null : entry.owner;
    }

    public static int countOwnedBy(UUID owner) {
        int n = 0;
        for (Entry e : ACTIVE.values()) if (e.owner.equals(owner) && e.entity.isAlive()) n++;
        return n;
    }

    /** Appelé chaque seconde. */
    public static void tick(MinecraftServer server) {
        Iterator<Entry> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            Entity ent = e.entity;
            if (!ent.isAlive() || ent.isRemoved()) {
                it.remove();
                continue;
            }
            if (ent.getEntityWorld() instanceof ServerWorld sw && sw.getTime() >= e.expiresAt) {
                SpellFx.particles(sw, ParticleTypes.POOF, ent.getX(), ent.getY() + 0.5, ent.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                ent.discard();
                it.remove();
            }
        }
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
