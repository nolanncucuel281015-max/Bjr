package com.forgotten.dimensions.content;

/** Catégories d'armes : dégâts de base ajoutés au bonus du palier, et vitesse d'attaque. */
public enum WeaponCategory {
    SWORD(3.0F, -2.4F),
    GREATSWORD(6.0F, -3.0F),
    DAGGER(1.0F, -1.6F),
    BATTLEAXE(5.5F, -3.1F),
    SPEAR(0F, 0F),
    STAFF(1.0F, -3.0F),
    BOW(0F, 0F),
    CROSSBOW(0F, 0F),
    WAND(0.5F, -2.0F);

    public final float baseDamage;
    public final float attackSpeed;

    WeaponCategory(float baseDamage, float attackSpeed) {
        this.baseDamage = baseDamage;
        this.attackSpeed = attackSpeed;
    }

    public boolean isMagic() {
        return this == STAFF || this == WAND;
    }
}
