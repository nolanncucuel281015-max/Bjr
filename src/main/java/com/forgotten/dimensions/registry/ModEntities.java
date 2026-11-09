package com.forgotten.dimensions.registry;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.entity.MagicBoltEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModEntities {
    private static final RegistryKey<EntityType<?>> MAGIC_BOLT_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, ForgottenDimensions.id("magic_bolt"));

    public static final EntityType<MagicBoltEntity> MAGIC_BOLT = Registry.register(Registries.ENTITY_TYPE, MAGIC_BOLT_KEY,
            EntityType.Builder.<MagicBoltEntity>create(MagicBoltEntity::new, SpawnGroup.MISC)
                    .dimensions(0.3F, 0.3F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build(MAGIC_BOLT_KEY));

    private ModEntities() {}

    public static void register() {
        // chargement de la classe
    }
}
