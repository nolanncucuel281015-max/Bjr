package com.forgotten.dimensions.magic;

import java.util.HashMap;
import java.util.Map;

/**
 * Les créatures des sorts d'invocation. L'apparence (modèle 3D, texture) est générée par
 * tools/gen/summons.py ; ici : statistiques et style de combat.
 *
 * @param hp      points de vie de base (augmentés avec le niveau du lanceur)
 * @param speed   vitesse de déplacement
 * @param flying  vole (sans gravité) au lieu de marcher
 * @param ranged  attaque à distance (projectile ou éclair) plutôt qu'au corps à corps
 * @param width   largeur de la boîte de collision
 * @param height  hauteur de la boîte de collision
 * @param scale   échelle du modèle
 * @param sounds  jeu de sons (CreatureSounds)
 */
public enum SummonType {
    SPIRIT_WOLF("spirit_wolf", "summon_wolves", SpellSchool.NATURE, 22, 0.36, false, false, 0.7F, 0.9F, 1.1F, "vex"),
    STONE_GUARDIAN("stone_guardian", "summon_golem", SpellSchool.NATURE, 110, 0.26, false, false, 1.4F, 2.7F, 1.0F, "golem"),
    FIRE_ELEMENTAL("fire_elemental", "summon_fire_elemental", SpellSchool.FIRE, 40, 0.3, true, true, 0.9F, 2.0F, 1.0F, "blaze"),
    PHOENIX("phoenix", "summon_phoenix", SpellSchool.FIRE, 70, 0.38, true, true, 1.3F, 1.1F, 1.6F, "phantom"),
    FROST_SENTINEL("frost_sentinel", "summon_frost_sentinel", SpellSchool.ICE, 80, 0.25, false, false, 1.4F, 2.7F, 1.0F, "golem"),
    STORM_SPIRIT("storm_spirit", "summon_storm_spirit", SpellSchool.LIGHTNING, 32, 0.34, true, true, 0.8F, 1.2F, 1.3F, "allay"),
    VOID_WRAITH("void_wraith", "summon_void_wraith", SpellSchool.VOID, 50, 0.33, true, false, 0.8F, 2.0F, 1.1F, "vex");

    private static final Map<String, SummonType> BY_SPELL = new HashMap<>();

    static {
        for (SummonType t : values()) BY_SPELL.put(t.spellId, t);
    }

    public final String id;
    public final String spellId;
    public final SpellSchool school;
    public final double hp;
    public final double speed;
    public final boolean flying;
    public final boolean ranged;
    public final float width;
    public final float height;
    public final float scale;
    public final String sounds;

    SummonType(String id, String spellId, SpellSchool school, double hp, double speed, boolean flying, boolean ranged,
               float width, float height, float scale, String sounds) {
        this.id = id;
        this.spellId = spellId;
        this.school = school;
        this.hp = hp;
        this.speed = speed;
        this.flying = flying;
        this.ranged = ranged;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.sounds = sounds;
    }

    public static SummonType bySpell(String spellId) {
        return BY_SPELL.get(spellId);
    }

    /** Créatures translucides (fantômes) : rendu semi-transparent. */
    public boolean ghostly() {
        return this == SPIRIT_WOLF || this == VOID_WRAITH || this == STORM_SPIRIT;
    }
}
