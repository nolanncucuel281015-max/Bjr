package com.forgotten.dimensions.magic;

/**
 * Définition d'un sort.
 *
 * @param level         niveau RPG requis
 * @param cooldown      temps de recharge de base (ticks)
 * @param power         dégâts ou soins de base (avant multiplicateurs)
 * @param radius        rayon d'effet ou distance (blink)
 * @param duration      durée (zones, buffs, contrôle) ou délai (sorts différés) en ticks ; nombre de rebonds pour CHAIN
 * @param requiredSkill compétence nécessaire ("" si aucune)
 */
public record Spell(String id, SpellSchool school, SpellKind kind, int level, float mana, int cooldown, float power,
                    float radius, int duration, String requiredSkill) {

    public String translationKey() {
        return "spell.forgotten_dimensions." + id;
    }

    public String descriptionKey() {
        return "spell.forgotten_dimensions." + id + ".desc";
    }
}
