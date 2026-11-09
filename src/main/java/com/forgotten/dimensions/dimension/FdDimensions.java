package com.forgotten.dimensions.dimension;

import com.forgotten.dimensions.ForgottenDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.List;

/**
 * Identifiants des sept dimensions. Leur génération (fichiers data/forgotten_dimensions/dimension)
 * fait l'objet de la phase 4 : tant qu'une dimension n'existe pas, /fdimension tp le signale.
 */
public final class FdDimensions {
    public static final List<String> IDS = List.of("celestial", "corrupted", "ocean", "chaos", "eternal", "depths", "void");

    private FdDimensions() {}

    public static RegistryKey<World> key(String id) {
        return RegistryKey.of(RegistryKeys.WORLD, ForgottenDimensions.id(id));
    }
}
