package com.forgotten.dimensions.world.generation;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.registry.generated.ContentTables;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;

/** Ajoute les minerais de fragments (data/forgotten_dimensions/worldgen) aux biomes de la Surface. */
public final class ModWorldGen {
    private ModWorldGen() {}

    public static void register() {
        for (String ore : ContentTables.overworldOres()) {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES,
                    RegistryKey.of(RegistryKeys.PLACED_FEATURE, ForgottenDimensions.id(ore)));
        }
    }
}
