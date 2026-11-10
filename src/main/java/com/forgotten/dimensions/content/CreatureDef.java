package com.forgotten.dimensions.content;

import java.util.List;

/**
 * Créature des dimensions oubliées.
 *
 * @param archetype  forme du modèle (assets/forgotten_dimensions/fd_entity_models/&lt;archetype&gt;.json)
 * @param abilities  capacités (voir CreatureAbilities)
 * @param behaviors  comportements (patrol, guard, reinforce, flee_low, skittish, pack)
 * @param minion     créature invoquée par la capacité SUMMON ("" si aucune)
 * @param spawnGroup groupe d'apparition (monster, creature, water_creature)
 */
public record CreatureDef(String id, String dimension, CreatureCategory category, String archetype, CreatureMovement movement,
                          int maxHealth, float damage, float speed, float armor, float scale, float width, float height, int xp,
                          List<String> abilities, List<String> behaviors, String sounds, String minion, boolean fireImmune,
                          String spawnGroup, int primaryColor, int secondaryColor) {

    public boolean has(String ability) {
        return abilities.contains(ability);
    }

    public boolean does(String behavior) {
        return behaviors.contains(behavior);
    }
}
