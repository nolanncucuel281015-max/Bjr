package com.forgotten.dimensions.magic;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

/** Temps de jeu (ticks absolus du monde) jusqu'auquel chaque sort est en recharge. */
public record SpellCooldowns(Map<String, Long> until) {
    public static final SpellCooldowns EMPTY = new SpellCooldowns(Map.of());
    public static final Codec<SpellCooldowns> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.LONG).xmap(SpellCooldowns::new, SpellCooldowns::until);

    public SpellCooldowns {
        until = Map.copyOf(until);
    }

    public long remaining(String spell, long now) {
        return Math.max(0, until.getOrDefault(spell, 0L) - now);
    }

    /** Ajoute une recharge et purge les entrées expirées pour garder la donnée compacte. */
    public SpellCooldowns with(String spell, long untilTick, long now) {
        Map<String, Long> copy = new HashMap<>();
        until.forEach((k, v) -> {
            if (v > now) copy.put(k, v);
        });
        copy.put(spell, untilTick);
        return new SpellCooldowns(copy);
    }
}
