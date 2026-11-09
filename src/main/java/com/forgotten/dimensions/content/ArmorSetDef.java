package com.forgotten.dimensions.content;

/** @param bonus bonus d'ensemble complet (voir ArmorSetBonuses), "none" sinon */
public record ArmorSetDef(String id, String repairItem, int durabilityMultiplier, int boots, int leggings, int chestplate,
                          int helmet, float toughness, float knockbackResistance, int enchantability, FdRarity rarity,
                          String bonus) {
}
