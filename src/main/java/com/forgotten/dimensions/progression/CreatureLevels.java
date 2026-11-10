package com.forgotten.dimensions.progression;

import com.forgotten.dimensions.content.CreatureCategory;
import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.entity.creature.FdCreature;
import com.forgotten.dimensions.entity.summon.SummonedCreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.Map;

/**
 * Niveau affiché des créatures (plaques RPG) et utilisé pour moduler l'expérience gagnée.
 * Créatures du mod : niveau de base de leur dimension (même ordre que la difficulté DIM_SCALE)
 * + rang (élite, mini-boss). Créatures de Minecraft : d'après leur vie maximale et la dimension.
 */
public final class CreatureLevels {
    public static final Map<String, Integer> DIMENSION_BASE = Map.of(
            "celestial", 6, "corrupted", 16, "ocean", 26, "chaos", 40, "eternal", 54, "depths", 68, "void", 82);

    private CreatureLevels() {}

    /** Niveau de la créature, ou -1 pour les joueurs et les invocations (pas de niveau propre). */
    public static int of(LivingEntity e) {
        if (e instanceof PlayerEntity || e instanceof SummonedCreatureEntity) return -1;
        int jitter = Math.floorMod(e.getUuid().hashCode(), 3);
        if (e instanceof FdCreature c && c.definition() != null) {
            CreatureDef d = c.definition();
            int base = DIMENSION_BASE.getOrDefault(d.dimension(), 10);
            int rank = switch (d.category()) {
                case PASSIVE -> -4;
                case NEUTRAL -> -1;
                case HOSTILE -> 0;
                case ELITE -> 5;
                case MINIBOSS -> 10;
            };
            return clamp(base + rank + jitter);
        }
        EntityType<?> t = e.getType();
        if (t == EntityType.ENDER_DRAGON) return 70;
        if (t == EntityType.WITHER) return 60;
        if (t == EntityType.WARDEN) return 75;
        if (t == EntityType.ELDER_GUARDIAN) return 45;
        int lvl = Math.round(e.getMaxHealth() / 4F) + jitter;
        World w = e.getEntityWorld();
        if (w.getRegistryKey() == World.NETHER) lvl += 12;
        else if (w.getRegistryKey() == World.END) lvl += 25;
        return clamp(lvl);
    }

    /** Rang d'une créature du mod (null pour les autres). */
    public static CreatureCategory category(LivingEntity e) {
        return e instanceof FdCreature c && c.definition() != null ? c.definition().category() : null;
    }

    /** Multiplicateur d'expérience selon l'écart de niveau (créatures « grises » : presque rien). */
    public static double xpFactor(int playerLevel, int creatureLevel) {
        if (creatureLevel < 0) return 1.0;
        int diff = creatureLevel - playerLevel;
        if (diff <= -15) return 0.1;
        if (diff <= -8) return 0.4;
        if (diff <= -4) return 0.75;
        if (diff >= 8) return 1.5;
        if (diff >= 4) return 1.25;
        return 1.0;
    }

    /** Couleur du niveau selon l'écart avec le joueur (gris, vert, jaune, orange, rouge). */
    public static int color(int playerLevel, int creatureLevel) {
        int diff = creatureLevel - playerLevel;
        if (diff <= -8) return 0xFF9A9A9A;
        if (diff <= -3) return 0xFF5CE05C;
        if (diff <= 2) return 0xFFFFE060;
        if (diff <= 7) return 0xFFFF9030;
        return 0xFFFF3A3A;
    }

    private static int clamp(int lvl) {
        return Math.max(1, Math.min(99, lvl));
    }
}
