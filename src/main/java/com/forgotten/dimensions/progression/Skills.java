package com.forgotten.dimensions.progression;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Les 30 compétences des cinq arbres. Les compétences sans attribut sont lues par les
 * systèmes concernés via {@link #rank} (dégâts, mana, invocations, exploration...).
 */
public final class Skills {
    private static final Map<String, Skill> SKILLS = new LinkedHashMap<>();

    // Guerrier
    public static final Skill STRENGTH = add("strength", SkillTree.WARRIOR, 3, 1, "", EntityAttributes.ATTACK_DAMAGE, 1.0, Operation.ADD_VALUE, 0, 0);
    public static final Skill VIGOR = add("vigor", SkillTree.WARRIOR, 3, 1, "", EntityAttributes.MAX_HEALTH, 2.0, Operation.ADD_VALUE, 1, 0);
    public static final Skill IRON_SKIN = add("iron_skin", SkillTree.WARRIOR, 3, 5, "vigor", EntityAttributes.ARMOR, 1.0, Operation.ADD_VALUE, 1, 1);
    public static final Skill CLEAVE = add("cleave", SkillTree.WARRIOR, 3, 10, "strength", null, 0.15, Operation.ADD_VALUE, 0, 1);
    public static final Skill BERSERK = add("berserk", SkillTree.WARRIOR, 2, 20, "cleave", null, 0.15, Operation.ADD_VALUE, 0, 2);
    public static final Skill SECOND_WIND = add("second_wind", SkillTree.WARRIOR, 2, 25, "iron_skin", null, 2.0, Operation.ADD_VALUE, 1, 2);

    // Mage
    public static final Skill ARCANE_RESERVE = add("arcane_reserve", SkillTree.MAGE, 3, 1, "", null, 20.0, Operation.ADD_VALUE, 0, 0);
    public static final Skill MANA_FLOW = add("mana_flow", SkillTree.MAGE, 3, 1, "", null, 0.2, Operation.ADD_VALUE, 1, 0);
    public static final Skill CONCENTRATION = add("concentration", SkillTree.MAGE, 3, 8, "arcane_reserve", null, 0.08, Operation.ADD_VALUE, 0, 1);
    public static final Skill SPELL_MASTERY = add("spell_mastery", SkillTree.MAGE, 3, 12, "mana_flow", null, 0.10, Operation.ADD_VALUE, 1, 1);
    public static final Skill CELERITY = add("celerity", SkillTree.MAGE, 3, 18, "concentration", null, 0.08, Operation.ADD_VALUE, 0, 2);
    public static final Skill MANA_SHIELD = add("mana_shield", SkillTree.MAGE, 2, 25, "spell_mastery", null, 0.10, Operation.ADD_VALUE, 1, 2);

    // Archer
    public static final Skill HAWK_EYE = add("hawk_eye", SkillTree.ARCHER, 3, 1, "", null, 0.10, Operation.ADD_VALUE, 0, 0);
    public static final Skill LIGHT_STEP = add("light_step", SkillTree.ARCHER, 3, 1, "", EntityAttributes.MOVEMENT_SPEED, 0.03, Operation.ADD_MULTIPLIED_BASE, 1, 0);
    public static final Skill MARKSMAN = add("marksman", SkillTree.ARCHER, 3, 10, "hawk_eye", null, 0.05, Operation.ADD_VALUE, 0, 1);
    public static final Skill HUNTER = add("hunter", SkillTree.ARCHER, 3, 12, "light_step", null, 0.05, Operation.ADD_VALUE, 1, 1);
    public static final Skill EVASION = add("evasion", SkillTree.ARCHER, 3, 20, "hunter", null, 0.04, Operation.ADD_VALUE, 1, 2);
    public static final Skill PIERCING_SHOT = add("piercing_shot", SkillTree.ARCHER, 2, 25, "marksman", null, 0.15, Operation.ADD_VALUE, 0, 2);

    // Explorateur
    public static final Skill ENDURANCE = add("endurance", SkillTree.EXPLORER, 3, 1, "", EntityAttributes.SAFE_FALL_DISTANCE, 1.5, Operation.ADD_VALUE, 0, 0);
    public static final Skill NIMBLE = add("nimble", SkillTree.EXPLORER, 2, 1, "", EntityAttributes.STEP_HEIGHT, 0.4, Operation.ADD_VALUE, 1, 0);
    public static final Skill PROSPECTOR = add("prospector", SkillTree.EXPLORER, 3, 8, "endurance", EntityAttributes.MINING_EFFICIENCY, 2.0, Operation.ADD_VALUE, 0, 1);
    public static final Skill CARTOGRAPHER = add("cartographer", SkillTree.EXPLORER, 3, 10, "nimble", null, 0.15, Operation.ADD_VALUE, 1, 1);
    public static final Skill DEEP_LUNGS = add("deep_lungs", SkillTree.EXPLORER, 3, 15, "prospector", EntityAttributes.OXYGEN_BONUS, 1.0, Operation.ADD_VALUE, 0, 2);
    public static final Skill DARK_SIGHT = add("dark_sight", SkillTree.EXPLORER, 1, 20, "cartographer", null, 1.0, Operation.ADD_VALUE, 1, 2);

    // Invocateur
    public static final Skill SPIRIT_BOND = add("spirit_bond", SkillTree.SUMMONER, 3, 1, "", null, 10.0, Operation.ADD_VALUE, 0, 0);
    public static final Skill CALL_OF_THE_PACK = add("call_of_the_pack", SkillTree.SUMMONER, 1, 8, "", null, 1.0, Operation.ADD_VALUE, 1, 0);
    public static final Skill PACK_LEADER = add("pack_leader", SkillTree.SUMMONER, 2, 12, "call_of_the_pack", null, 1.0, Operation.ADD_VALUE, 1, 1);
    public static final Skill LASTING_BOND = add("lasting_bond", SkillTree.SUMMONER, 3, 10, "spirit_bond", null, 0.2, Operation.ADD_VALUE, 0, 1);
    public static final Skill STONE_GUARDIAN = add("stone_guardian", SkillTree.SUMMONER, 1, 25, "pack_leader", null, 1.0, Operation.ADD_VALUE, 1, 2);
    public static final Skill SOUL_LINK = add("soul_link", SkillTree.SUMMONER, 2, 20, "lasting_bond", null, 2.0, Operation.ADD_VALUE, 0, 2);

    private Skills() {}

    private static Skill add(String id, SkillTree tree, int maxRank, int level, String prereq,
                             RegistryEntry<EntityAttribute> attr, double perRank, Operation op, int col, int row) {
        Skill s = new Skill(id, tree, maxRank, level, prereq, attr, perRank, op, col, row);
        SKILLS.put(id, s);
        return s;
    }

    public static Skill get(String id) {
        return SKILLS.get(id);
    }

    public static Map<String, Skill> all() {
        return Collections.unmodifiableMap(SKILLS);
    }

    public static List<Skill> ofTree(SkillTree tree) {
        List<Skill> list = new ArrayList<>();
        for (Skill s : SKILLS.values()) if (s.tree() == tree) list.add(s);
        return list;
    }

    /** Valeur totale (rang × valeur par rang) d'une compétence pour une progression. */
    public static double value(PlayerProgress progress, Skill skill) {
        return progress.skillRank(skill.id()) * skill.perRank();
    }

    public static int rank(PlayerProgress progress, Skill skill) {
        return progress.skillRank(skill.id());
    }

    /** Raison pour laquelle une compétence ne peut pas être améliorée, ou null si c'est possible. */
    public static String canUpgrade(PlayerProgress progress, Skill skill) {
        int rank = progress.skillRank(skill.id());
        if (rank >= skill.maxRank()) return "max_rank";
        if (progress.skillPoints() <= 0) return "no_points";
        if (progress.level() < skill.levelRequired()) return "level";
        if (!skill.prerequisite().isEmpty() && progress.skillRank(skill.prerequisite()) <= 0) return "prerequisite";
        return null;
    }
}
