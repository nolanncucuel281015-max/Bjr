package com.forgotten.dimensions.content;

/** @param legendary identifiant de l'effet légendaire, vide pour une arme ordinaire */
public record WeaponDef(String id, WeaponCategory category, String tier, FdRarity rarity, String legendary) {
    public boolean isLegendary() {
        return !legendary.isEmpty();
    }
}
