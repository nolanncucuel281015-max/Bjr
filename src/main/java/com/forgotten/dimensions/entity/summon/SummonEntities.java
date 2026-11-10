package com.forgotten.dimensions.entity.summon;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.magic.SummonType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** Un type d'entité par créature d'invocation (taille et modèle propres). */
public final class SummonEntities {
    private static final Map<SummonType, EntityType<SummonedCreatureEntity>> TYPES = new EnumMap<>(SummonType.class);
    private static final Map<EntityType<?>, SummonType> BY_TYPE = new IdentityHashMap<>();

    private SummonEntities() {}

    public static void register() {
        for (SummonType t : SummonType.values()) {
            RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, ForgottenDimensions.id(t.id));
            EntityType.Builder<SummonedCreatureEntity> builder = EntityType.Builder.<SummonedCreatureEntity>create(SummonedCreatureEntity::new, SpawnGroup.MISC)
                    .dimensions(t.width, t.height)
                    .maxTrackingRange(10)
                    .disableSaving()
                    .disableSummon();
            if (t.school == com.forgotten.dimensions.magic.SpellSchool.FIRE) builder.makeFireImmune();
            // Le type doit être connu avant la construction de la première entité.
            EntityType<SummonedCreatureEntity> type = builder.build(key);
            BY_TYPE.put(type, t);
            Registry.register(Registries.ENTITY_TYPE, key, type);
            TYPES.put(t, type);
            FabricDefaultAttributeRegistry.register(type, SummonedCreatureEntity.attributes(t));
        }
    }

    public static EntityType<SummonedCreatureEntity> get(SummonType t) {
        return TYPES.get(t);
    }

    public static Map<SummonType, EntityType<SummonedCreatureEntity>> all() {
        return TYPES;
    }

    public static SummonType typeOf(EntityType<?> type) {
        return BY_TYPE.get(type);
    }
}
