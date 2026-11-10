package com.forgotten.dimensions.dimension;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.List;

/** Identifiants des sept dimensions (générées par data/forgotten_dimensions/dimension/*.json). */
public final class FdDimensions {
    public static final List<String> IDS = List.of("celestial", "corrupted", "ocean", "chaos", "eternal", "depths", "void");

    private FdDimensions() {}

    /** Dimension ouverte par une clé, ou null si l'objet n'est pas une clé dimensionnelle. */
    public static String dimensionOfKey(net.minecraft.item.Item item) {
        for (String id : IDS) {
            if (com.forgotten.dimensions.registry.ModItems.get(id + "_key") == item) return id;
        }
        return null;
    }

    public static RegistryKey<World> key(String id) {
        return RegistryKey.of(RegistryKeys.WORLD, ForgottenDimensions.id(id));
    }
}
