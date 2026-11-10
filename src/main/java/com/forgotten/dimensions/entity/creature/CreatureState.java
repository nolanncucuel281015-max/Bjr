package com.forgotten.dimensions.entity.creature;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

/** État d'exécution d'une créature (non sauvegardé : recalculé après chargement). */
public final class CreatureState {
    public final Map<String, Integer> cooldowns = new HashMap<>();
    public BlockPos home;
    public int chargeTicks;
    public ServerBossBar bossBar;

    public boolean ready(String ability) {
        return cooldowns.getOrDefault(ability, 0) <= 0;
    }

    public void cooldown(String ability, int ticks) {
        cooldowns.put(ability, ticks);
    }

    public void tickCooldowns() {
        cooldowns.replaceAll((k, v) -> v > 0 ? v - 1 : 0);
    }
}
