package com.forgotten.dimensions.content;

import com.forgotten.dimensions.magic.SpellSchool;

/**
 * Palier d'arme : matériau, statistiques, école de magie associée aux bâtons et baguettes.
 *
 * @param incorrectFor niveau d'outil vanilla équivalent (IRON, DIAMOND, NETHERITE)
 */
public record TierDef(String id, FdRarity rarity, int durability, float speed, float damageBonus, int enchantability,
                      String incorrectFor, SpellSchool school, String repairItem) {
}
