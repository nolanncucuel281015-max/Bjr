package com.forgotten.dimensions.progression;

public enum SkillTree {
    WARRIOR, MAGE, ARCHER, EXPLORER, SUMMONER;

    public String id() {
        return name().toLowerCase();
    }

    public String translationKey() {
        return "skill_tree.forgotten_dimensions." + id();
    }

    public static SkillTree byId(String id) {
        for (SkillTree t : values()) if (t.id().equals(id)) return t;
        return null;
    }
}
