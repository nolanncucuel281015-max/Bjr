package com.forgotten.dimensions.registry;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.CreatureCategory;
import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.content.CreatureMovement;
import com.forgotten.dimensions.entity.creature.CreatureLogic;
import com.forgotten.dimensions.entity.creature.FdHostileCreature;
import com.forgotten.dimensions.entity.creature.FdPassiveCreature;
import com.forgotten.dimensions.registry.generated.ContentTables;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Enregistre les 60 créatures : types, attributs, règles d'apparition naturelle et œufs d'apparition. */
public final class ModCreatures {
    private static final Map<String, EntityType<? extends MobEntity>> TYPES = new LinkedHashMap<>();
    private static final Map<String, CreatureDef> DEFS = new LinkedHashMap<>();

    private ModCreatures() {}

    @SuppressWarnings("unchecked")
    public static void register() {
        for (CreatureDef d : ContentTables.creatures()) {
            RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, ForgottenDimensions.id(d.id()));
            SpawnGroup group = switch (d.spawnGroup()) {
                case "monster" -> SpawnGroup.MONSTER;
                case "water_creature" -> SpawnGroup.WATER_CREATURE;
                default -> SpawnGroup.CREATURE;
            };
            EntityType.Builder<? extends MobEntity> builder = d.category().aggressive()
                    ? EntityType.Builder.<FdHostileCreature>create(FdHostileCreature::new, group)
                    : EntityType.Builder.<FdPassiveCreature>create(FdPassiveCreature::new, group);
            builder.dimensions(d.width(), d.height()).maxTrackingRange(d.category() == CreatureCategory.MINIBOSS ? 12 : 10);
            if (d.fireImmune()) builder.makeFireImmune();
            EntityType<? extends MobEntity> type = Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
            CreatureLogic.bind(type, d);
            TYPES.put(d.id(), type);
            DEFS.put(d.id(), d);
            FabricDefaultAttributeRegistry.register(type, attributes(d));
            registerSpawn(type, d);
            ModItems.register(d.id() + "_spawn_egg", SpawnEggItem::new, new Item.Settings().spawnEgg(type));
        }
        ForgottenDimensions.LOGGER.info("{} créatures enregistrées", TYPES.size());
    }

    private static DefaultAttributeContainer.Builder attributes(CreatureDef d) {
        DefaultAttributeContainer.Builder b = d.category().aggressive() ? HostileEntity.createHostileAttributes() : MobEntity.createMobAttributes();
        b.add(EntityAttributes.MAX_HEALTH, d.maxHealth())
                .add(EntityAttributes.ATTACK_DAMAGE, Math.max(0.5, d.damage()))
                .add(EntityAttributes.MOVEMENT_SPEED, d.speed())
                .add(EntityAttributes.ARMOR, d.armor())
                .add(EntityAttributes.FOLLOW_RANGE, d.category().aggressive() ? 32.0 : 16.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, switch (d.category()) {
                    case ELITE -> 0.5;
                    case MINIBOSS -> 0.8;
                    default -> 0.0;
                });
        if (d.movement() == CreatureMovement.FLYING) b.add(EntityAttributes.FLYING_SPEED, d.speed());
        return b;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerSpawn(EntityType<? extends MobEntity> type, CreatureDef d) {
        EntityType raw = type;
        boolean aggressive = d.category().aggressive();
        int base = switch (d.category()) {
            case ELITE -> 3;
            case MINIBOSS -> 8;
            default -> 1;
        };
        // Les positions « dans les airs » sont presque toujours valides : les volants sont rendus plus rares.
        int rarity = d.movement() == CreatureMovement.FLYING ? base * 3 : base;
        switch (d.movement()) {
            case SWIMMING -> SpawnRestriction.register(raw, SpawnLocationTypes.IN_WATER, Heightmap.Type.OCEAN_FLOOR,
                    (t, world, reason, pos, random) -> notCrowded(t, world, pos, d) && world.getFluidState(pos).isStill()
                            && (!aggressive || world.getDifficulty() != Difficulty.PEACEFUL)
                            && chance(random, rarity));
            case FLYING -> SpawnRestriction.register(raw, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING,
                    (t, world, reason, pos, random) -> notCrowded(t, world, pos, d) && world.getBlockState(pos).isAir()
                            && (!aggressive || world.getDifficulty() != Difficulty.PEACEFUL)
                            && chance(random, rarity));
            default -> SpawnRestriction.register(raw, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    (t, world, reason, pos, random) -> notCrowded(t, world, pos, d) && MobEntity.canMobSpawn(t, world, reason, pos, random)
                            && (!aggressive || (world.getDifficulty() != Difficulty.PEACEFUL && world.getLightLevel(LightType.BLOCK, pos) <= 7))
                            && chance(random, rarity));
        }
    }

    /**
     * Limite de densité par espèce : 5 dans 32 blocs (4 pour les volants), 1 élite dans 48 blocs,
     * 1 mini-boss dans 96 blocs.
     */
    private static boolean notCrowded(EntityType<?> type, net.minecraft.world.ServerWorldAccess world, net.minecraft.util.math.BlockPos pos, CreatureDef d) {
        int radius, max;
        switch (d.category()) {
            case ELITE -> { radius = 48; max = 1; }
            case MINIBOSS -> { radius = 96; max = 1; }
            default -> { radius = 32; max = d.movement() == CreatureMovement.FLYING ? 4 : 5; }
        }
        return world.getEntitiesByType(type, new net.minecraft.util.math.Box(pos).expand(radius), e -> true).size() < max;
    }

    private static boolean chance(net.minecraft.util.math.random.Random random, int rarity) {
        return random.nextDouble() < com.forgotten.dimensions.util.config.FdConfig.get().creatureSpawnMultiplier / rarity;
    }

    public static EntityType<? extends MobEntity> get(String id) {
        return TYPES.get(id);
    }

    public static Map<String, EntityType<? extends MobEntity>> all() {
        return Collections.unmodifiableMap(TYPES);
    }

    public static Map<String, CreatureDef> definitions() {
        return Collections.unmodifiableMap(DEFS);
    }
}
