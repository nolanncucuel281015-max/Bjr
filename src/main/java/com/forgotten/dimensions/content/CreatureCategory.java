package com.forgotten.dimensions.content;

public enum CreatureCategory {
    PASSIVE, NEUTRAL, HOSTILE, ELITE, MINIBOSS;

    public boolean aggressive() {
        return this == HOSTILE || this == ELITE || this == MINIBOSS;
    }

    public String translationKey() {
        return "creature_category.forgotten_dimensions." + name().toLowerCase();
    }
}
