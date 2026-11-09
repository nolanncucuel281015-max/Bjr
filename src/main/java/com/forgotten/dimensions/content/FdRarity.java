package com.forgotten.dimensions.content;

import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

/** Les six raretés RPG du mod. Vanilla n'en possède que quatre : on les projette dessus. */
public enum FdRarity {
    COMMON(Formatting.WHITE, Rarity.COMMON),
    UNCOMMON(Formatting.GREEN, Rarity.UNCOMMON),
    RARE(Formatting.AQUA, Rarity.RARE),
    EPIC(Formatting.LIGHT_PURPLE, Rarity.EPIC),
    LEGENDARY(Formatting.GOLD, Rarity.EPIC),
    MYTHIC(Formatting.RED, Rarity.EPIC);

    private final Formatting color;
    private final Rarity vanilla;

    FdRarity(Formatting color, Rarity vanilla) {
        this.color = color;
        this.vanilla = vanilla;
    }

    public Formatting color() {
        return color;
    }

    public Rarity vanilla() {
        return vanilla;
    }

    /** Vrai si la couleur du nom doit être forcée (au-delà de ce que vanilla sait afficher). */
    public boolean needsCustomNameColor() {
        return this == LEGENDARY || this == MYTHIC;
    }

    public String translationKey() {
        return "rarity.forgotten_dimensions." + name().toLowerCase();
    }
}
