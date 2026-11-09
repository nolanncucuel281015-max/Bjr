package com.forgotten.dimensions.magic;

import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.progression.Skills;
import com.forgotten.dimensions.registry.ModAttachments;
import com.forgotten.dimensions.registry.ModBlocks;
import com.forgotten.dimensions.util.PlayerCache;
import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/** Mana : maximum, régénération et consommation. Côté serveur, faisant autorité. */
public final class ManaManager {
    private ManaManager() {}

    public static ManaState get(PlayerEntity player) {
        ManaState s = player.getAttached(ModAttachments.MANA);
        return s != null ? s : ManaState.DEFAULT;
    }

    /** Formule pure du mana maximum (testée unitairement). */
    public static float maxMana(int level, int arcaneReserveRank, boolean mageSpec, int artifactBonus) {
        float base = 100F + 5F * (level - 1) + 20F * arcaneReserveRank + artifactBonus;
        return mageSpec ? base * 1.25F : base;
    }

    public static float computeMax(PlayerEntity player) {
        PlayerProgress p = ProgressionManager.get(player);
        PlayerCache.Snapshot snap = PlayerCache.get(player);
        int artifacts = (snap.has("mana_prism") ? 50 : 0) + (snap.has("crown_of_seven_worlds") ? 100 : 0);
        return maxMana(p.level(), Skills.rank(p, Skills.ARCANE_RESERVE), SkillTree.MAGE.id().equals(p.specialization()), artifacts);
    }

    /** Mana régénéré par seconde. */
    public static float regenPerSecond(ServerPlayerEntity player) {
        PlayerProgress p = ProgressionManager.get(player);
        PlayerCache.Snapshot snap = PlayerCache.get(player);
        double mult = 1.0 + Skills.value(p, Skills.MANA_FLOW);
        if (snap.has("heart_of_the_ancients")) mult += 0.5;
        if (snap.setBonus("mana_regen") || snap.setBonus("primordial")) mult += 0.5;
        if (player.getEntityWorld().getBlockState(player.getBlockPos().down()).isOf(ModBlocks.get("ley_line_block"))) mult += 1.0;
        return (float) ((2.0 + p.level() * 0.05) * mult * FdConfig.get().manaRegenMultiplier);
    }

    /** Appelé toutes les 10 ticks (0,5 s). */
    public static void tick(ServerPlayerEntity player) {
        ManaState state = get(player);
        if (state.current() < state.max()) {
            set(player, state.withCurrent(state.current() + regenPerSecond(player) * 0.5F));
        }
    }

    public static void updateMax(ServerPlayerEntity player) {
        ManaState state = get(player);
        float max = computeMax(player);
        if (Math.abs(max - state.max()) > 0.01F) {
            set(player, state.withMax(max));
        }
    }

    public static boolean consume(ServerPlayerEntity player, float amount) {
        ManaState state = get(player);
        if (state.current() + 1e-4F < amount) return false;
        set(player, state.withCurrent(state.current() - amount));
        return true;
    }

    public static void restore(ServerPlayerEntity player, float amount) {
        ManaState state = get(player);
        set(player, state.withCurrent(state.current() + amount));
    }

    public static void set(ServerPlayerEntity player, ManaState state) {
        player.setAttached(ModAttachments.MANA, state);
    }
}
