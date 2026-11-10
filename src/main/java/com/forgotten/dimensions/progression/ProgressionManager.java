package com.forgotten.dimensions.progression;

import com.forgotten.dimensions.registry.ModAttachments;
import com.forgotten.dimensions.util.Advancements;
import com.forgotten.dimensions.util.PlayerCache;
import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Toute modification de progression passe par ici (côté serveur uniquement : le serveur fait autorité). */
public final class ProgressionManager {
    private ProgressionManager() {}

    public static PlayerProgress get(PlayerEntity player) {
        PlayerProgress p = player.getAttached(ModAttachments.PROGRESS);
        return p != null ? p : PlayerProgress.DEFAULT;
    }

    public static void set(ServerPlayerEntity player, PlayerProgress progress) {
        player.setAttached(ModAttachments.PROGRESS, progress);
    }

    /** Multiplicateur d'expérience selon la source, la configuration, les compétences et les artefacts. */
    public static double multiplier(ServerPlayerEntity player, PlayerProgress progress, XpSource source) {
        double m = FdConfig.get().xpMultiplier;
        if (source == XpSource.EXPLORATION) {
            m *= 1.0 + Skills.value(progress, Skills.CARTOGRAPHER);
            if (PlayerCache.get(player).has("explorer_astrolabe")) m *= 1.25;
            if (SkillTree.EXPLORER.id().equals(progress.specialization())) m *= 1.20;
        }
        return m;
    }

    public static void addXp(ServerPlayerEntity player, int base, XpSource source) {
        if (base <= 0) return;
        PlayerProgress progress = get(player);
        if (progress.level() >= PlayerProgress.MAX_LEVEL) return;
        long amount = Math.round(base * multiplier(player, progress, source));
        if (amount <= 0) return;
        int[] result = ExperienceCurve.addXp(progress.level(), progress.xp(), amount);
        int oldLevel = progress.level();
        int newLevel = result[0];
        int points = progress.skillPoints() + (ExperienceCurve.totalSkillPoints(newLevel) - ExperienceCurve.totalSkillPoints(oldLevel));
        set(player, progress.withLevelXp(newLevel, result[1], points));
        if (newLevel > oldLevel) {
            onLevelUp(player, oldLevel, newLevel);
        }
    }

    /** Fixe le niveau (commande d'administration) : les points dépensés sont conservés, l'expérience remise à 0. */
    public static void setLevel(ServerPlayerEntity player, int level) {
        level = Math.max(1, Math.min(PlayerProgress.MAX_LEVEL, level));
        PlayerProgress progress = get(player);
        int points = ExperienceCurve.totalSkillPoints(level) - progress.spentPoints();
        PlayerProgress updated = progress.withLevelXp(level, 0, points);
        if (points < 0) {
            // Niveau abaissé sous les points déjà dépensés : on rembourse tout.
            updated = updated.withSkillsReset(ExperienceCurve.totalSkillPoints(level));
        }
        set(player, updated);
        SkillEffects.apply(player, updated);
        grantLevelAdvancements(player, level);
    }

    /** @return null en cas de succès, sinon la clé de la raison du refus */
    public static String upgradeSkill(ServerPlayerEntity player, String skillId) {
        Skill skill = Skills.get(skillId);
        if (skill == null) return "unknown";
        PlayerProgress progress = get(player);
        String reason = Skills.canUpgrade(progress, skill);
        if (reason != null) return reason;
        PlayerProgress updated = progress.withSkill(skillId, progress.skillRank(skillId) + 1, progress.skillPoints() - 1);
        set(player, updated);
        SkillEffects.apply(player, updated);
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.8F, 1.2F);
        if (updated.spentPoints() >= 10) Advancements.grant(player, "skill_adept");
        return null;
    }

    public static final int SPECIALIZATION_LEVEL = 20;

    public static String chooseSpecialization(ServerPlayerEntity player, String treeId) {
        SkillTree tree = SkillTree.byId(treeId);
        if (tree == null) return "unknown";
        PlayerProgress progress = get(player);
        if (!progress.specialization().isEmpty()) return "already_chosen";
        if (progress.level() < SPECIALIZATION_LEVEL) return "level";
        PlayerProgress updated = progress.withSpecialization(tree.id());
        set(player, updated);
        SkillEffects.apply(player, updated);
        Advancements.grant(player, "specialization");
        return null;
    }

    public static void resetSkills(ServerPlayerEntity player) {
        PlayerProgress progress = get(player);
        PlayerProgress updated = progress.withSkillsReset(ExperienceCurve.totalSkillPoints(progress.level()));
        set(player, updated);
        SkillEffects.apply(player, updated);
    }

    private static void onLevelUp(ServerPlayerEntity player, int oldLevel, int newLevel) {
        player.sendMessage(Text.translatable("message.forgotten_dimensions.level_up", newLevel)
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 0.8F);
        player.getEntityWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0,
                player.getZ(), 30, 0.5, 0.8, 0.5, 0.3);
        // Célébration façon RPG : grand titre, colonne de lumière dorée, onde et cercle runique.
        int points = ExperienceCurve.totalSkillPoints(newLevel) - ExperienceCurve.totalSkillPoints(oldLevel);
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(8, 50, 20));
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(
                Text.translatable("title.forgotten_dimensions.level_up", newLevel).formatted(Formatting.GOLD, Formatting.BOLD)));
        player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(
                Text.translatable("title.forgotten_dimensions.level_up.sub", points).formatted(Formatting.YELLOW)));
        var world = player.getEntityWorld();
        var pos = player.getEntityPos().add(0, 0.05, 0);
        com.forgotten.dimensions.magic.SpellVisuals.spawn(world, com.forgotten.dimensions.magic.SpellVisual.PILLAR,
                com.forgotten.dimensions.magic.SpellSchool.LIGHTNING, pos, 1.4F, 40, net.minecraft.util.math.Vec3d.ZERO, player);
        com.forgotten.dimensions.magic.SpellVisuals.spawn(world, com.forgotten.dimensions.magic.SpellVisual.NOVA,
                com.forgotten.dimensions.magic.SpellSchool.LIGHTNING, pos, 5F, 24);
        com.forgotten.dimensions.magic.SpellVisuals.spawn(world, com.forgotten.dimensions.magic.SpellVisual.CAST_CIRCLE,
                com.forgotten.dimensions.magic.SpellSchool.LIGHTNING, pos, 2.6F, 50, net.minecraft.util.math.Vec3d.ZERO, player);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS, 0.8F, 1.0F);
        grantLevelAdvancements(player, newLevel);
    }

    private static void grantLevelAdvancements(ServerPlayerEntity player, int level) {
        for (int milestone : new int[]{10, 25, 50, 75, 100}) {
            if (level >= milestone) Advancements.grant(player, "level_" + milestone);
        }
    }
}
