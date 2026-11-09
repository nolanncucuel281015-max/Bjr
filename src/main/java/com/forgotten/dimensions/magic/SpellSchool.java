package com.forgotten.dimensions.magic;

import net.minecraft.util.Formatting;

public enum SpellSchool {
    FIRE(0xFF7020, Formatting.GOLD),
    ICE(0xA0E8FF, Formatting.AQUA),
    LIGHTNING(0xFFF060, Formatting.YELLOW),
    NATURE(0x60E060, Formatting.GREEN),
    VOID(0xB050FF, Formatting.DARK_PURPLE);

    public final int color;
    public final Formatting formatting;

    SpellSchool(int color, Formatting formatting) {
        this.color = color;
        this.formatting = formatting;
    }

    public String id() {
        return name().toLowerCase();
    }

    public String translationKey() {
        return "school.forgotten_dimensions." + id();
    }

    public static SpellSchool byId(String id) {
        for (SpellSchool s : values()) {
            if (s.id().equals(id)) return s;
        }
        return FIRE;
    }
}
