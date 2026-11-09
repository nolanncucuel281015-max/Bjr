package com.forgotten.dimensions.progression;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * Compétence d'un arbre.
 *
 * @param prerequisite compétence qui doit avoir au moins un rang ("" si aucune)
 * @param attribute    attribut modifié à chaque rang (null si l'effet est codé dans un événement)
 * @param perRank      valeur ajoutée par rang
 */
public record Skill(String id, SkillTree tree, int maxRank, int levelRequired, String prerequisite,
                    RegistryEntry<EntityAttribute> attribute, double perRank, EntityAttributeModifier.Operation operation,
                    int column, int row) {

    public String translationKey() {
        return "skill.forgotten_dimensions." + id;
    }
}
