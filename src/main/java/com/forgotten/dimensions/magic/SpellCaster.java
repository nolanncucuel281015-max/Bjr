package com.forgotten.dimensions.magic;

import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.progression.Skills;
import com.forgotten.dimensions.progression.XpSource;
import com.forgotten.dimensions.registry.ModAttachments;
import com.forgotten.dimensions.util.Advancements;
import com.forgotten.dimensions.util.PlayerCache;
import com.forgotten.dimensions.util.config.FdConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Validation et lancement des sorts. Toutes les vérifications ont lieu sur le serveur. */
public final class SpellCaster {
    private SpellCaster() {}

    public enum Result { OK, UNKNOWN, LOCKED_LEVEL, LOCKED_SKILL, COOLDOWN, NO_MANA, FAILED }

    public static boolean isUnlocked(PlayerProgress progress, Spell spell) {
        return progress.level() >= spell.level()
                && (spell.requiredSkill().isEmpty() || progress.skillRank(spell.requiredSkill()) > 0);
    }

    public static float manaCostMultiplier(PlayerEntity player, PlayerProgress p) {
        float m = 1F - (float) Skills.value(p, Skills.CONCENTRATION);
        if (PlayerCache.get(player).has("archmage_tome")) m *= 0.85F;
        return Math.max(0.3F, m);
    }

    public static float cooldownMultiplier(PlayerEntity player, PlayerProgress p) {
        float m = 1F - (float) Skills.value(p, Skills.CELERITY);
        if (PlayerCache.get(player).has("architect_cog")) m *= 0.8F;
        return (float) Math.max(0.2, m * FdConfig.get().spellCooldownMultiplier);
    }

    /** Multiplicateur de puissance (formule pure, testée unitairement). */
    public static float powerMultiplier(int level, int masterySkillRank, int schoolMastery, boolean mageSpec, double config) {
        double m = (1.0 + 0.01 * (level - 1)) * (1.0 + 0.10 * masterySkillRank) * (1.0 + 0.03 * schoolMastery);
        if (mageSpec) m *= 1.10;
        return (float) (m * config);
    }

    public static float powerMultiplier(PlayerEntity player, PlayerProgress p, SpellSchool school) {
        return powerMultiplier(p.level(), Skills.rank(p, Skills.SPELL_MASTERY), p.schoolMastery(school.id()),
                SkillTree.MAGE.id().equals(p.specialization()), FdConfig.get().spellDamageMultiplier);
    }

    public static Result tryCast(ServerPlayerEntity player, String spellId) {
        Spell spell = Spells.get(spellId);
        if (spell == null) return Result.UNKNOWN;
        PlayerProgress progress = ProgressionManager.get(player);
        if (progress.level() < spell.level()) return feedback(player, Result.LOCKED_LEVEL, spell);
        if (!spell.requiredSkill().isEmpty() && progress.skillRank(spell.requiredSkill()) <= 0)
            return feedback(player, Result.LOCKED_SKILL, spell);
        long now = player.getEntityWorld().getTime();
        SpellCooldowns cds = player.getAttachedOrElse(ModAttachments.SPELL_COOLDOWNS, SpellCooldowns.EMPTY);
        if (cds.remaining(spell.id(), now) > 0) return feedback(player, Result.COOLDOWN, spell);
        float cost = spell.mana() * manaCostMultiplier(player, progress);
        if (ManaManager.get(player).current() < cost) return feedback(player, Result.NO_MANA, spell);

        float power = spell.power() * powerMultiplier(player, progress, spell.school());
        if (!SpellExecutor.execute(player, spell, power)) return feedback(player, Result.FAILED, spell);

        ManaManager.consume(player, cost);
        int cd = Math.max(5, Math.round(spell.cooldown() * cooldownMultiplier(player, progress)));
        player.setAttached(ModAttachments.SPELL_COOLDOWNS, cds.with(spell.id(), now + cd, now));
        ProgressionManager.set(player, ProgressionManager.get(player).withSchoolCast(spell.school().id()));
        ProgressionManager.addXp(player, 1 + Math.round(spell.mana() / 20F), XpSource.MAGIC);
        Advancements.grant(player, "first_spell");
        return Result.OK;
    }

    /**
     * Tir d'arme magique (bâton, baguette) : projectile de base de l'école, sans prérequis de niveau,
     * avec la puissance de l'arme. @return vrai si le tir a eu lieu.
     */
    public static boolean castWeaponBolt(ServerPlayerEntity player, SpellSchool school, float weaponPower, float manaCost) {
        PlayerProgress progress = ProgressionManager.get(player);
        float cost = manaCost * manaCostMultiplier(player, progress);
        if (!ManaManager.consume(player, cost)) {
            feedback(player, Result.NO_MANA, Spells.basicBolt(school));
            return false;
        }
        float power = weaponPower * powerMultiplier(player, progress, school);
        SpellExecutor.execute(player, Spells.basicBolt(school), power);
        return true;
    }

    private static Result feedback(ServerPlayerEntity player, Result result, Spell spell) {
        Text msg = switch (result) {
            case LOCKED_LEVEL -> Text.translatable("message.forgotten_dimensions.spell.level", spell.level());
            case LOCKED_SKILL -> Text.translatable("message.forgotten_dimensions.spell.skill",
                    Text.translatable("skill.forgotten_dimensions." + spell.requiredSkill()));
            case COOLDOWN -> Text.translatable("message.forgotten_dimensions.spell.cooldown");
            case NO_MANA -> Text.translatable("message.forgotten_dimensions.spell.no_mana");
            case FAILED -> Text.translatable("message.forgotten_dimensions.spell.failed");
            default -> null;
        };
        if (msg != null) player.sendMessage(msg.copy().formatted(Formatting.RED), true);
        return result;
    }
}
