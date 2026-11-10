package com.forgotten.dimensions.registry;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.block.AncientTechBlock;
import com.forgotten.dimensions.block.MagicBlock;
import com.forgotten.dimensions.block.DimensionalGateBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.UntintedParticleLeavesBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import com.forgotten.dimensions.content.BlockDef;
import com.forgotten.dimensions.registry.generated.ContentTables;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Enregistre tous les blocs décrits dans {@link ContentTables#blocks()} ainsi que leurs objets. */
public final class ModBlocks {
    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<String, BlockDef> DEFS = new LinkedHashMap<>();

    private ModBlocks() {}

    public static void register() {
        for (BlockDef def : ContentTables.blocks()) {
            RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, ForgottenDimensions.id(def.id()));
            AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                    .registryKey(key)
                    .strength(def.hardness(), def.resistance())
                    .sounds(sound(def.sound()));
            if (def.luminance() > 0) {
                int light = def.luminance();
                settings.luminance(state -> light);
            }
            if (def.requiresTool()) {
                settings.requiresTool();
            }
            Block block = switch (def.kind()) {
                case CUBE -> new Block(settings);
                case ORE -> new ExperienceDroppingBlock(def.xpMax() > 0
                        ? UniformIntProvider.create(def.xpMin(), def.xpMax())
                        : ConstantIntProvider.create(0), settings);
                case STAIRS -> new StairsBlock(require(def.base()).getDefaultState(), settings);
                case SLAB -> new SlabBlock(settings);
                case WALL -> new WallBlock(settings);
                case TECH -> new AncientTechBlock(def.behavior(), settings);
                case MAGIC -> new MagicBlock(def.behavior(), settings);
                case LOG -> new PillarBlock(settings);
                case LEAVES -> new UntintedParticleLeavesBlock(0.01F, leafParticle(def.behavior()), settings
                        .ticksRandomly().nonOpaque()
                        .allowsSpawning((state, world, pos, type) -> false)
                        .suffocates((state, world, pos) -> false)
                        .blockVision((state, world, pos) -> false));
                case GATE -> new DimensionalGateBlock(settings);
            };
            Registry.register(Registries.BLOCK, key, block);
            BLOCKS.put(def.id(), block);
            DEFS.put(def.id(), def);

            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, key.getValue());
            Registry.register(Registries.ITEM, itemKey,
                    new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
        }
        ForgottenDimensions.LOGGER.info("{} blocs enregistrés", BLOCKS.size());
    }

    private static Block require(String id) {
        Block block = BLOCKS.get(id);
        if (block == null) {
            throw new IllegalStateException("Bloc de base manquant (ordre de la table) : " + id);
        }
        return block;
    }

    public static Block get(String id) {
        return BLOCKS.get(id);
    }

    public static Map<String, Block> all() {
        return Collections.unmodifiableMap(BLOCKS);
    }

    public static Map<String, BlockDef> definitions() {
        return Collections.unmodifiableMap(DEFS);
    }

    static BlockSoundGroup sound(String name) {
        return switch (name) {
            case "DEEPSLATE" -> BlockSoundGroup.DEEPSLATE;
            case "CALCITE" -> BlockSoundGroup.CALCITE;
            case "NETHERRACK" -> BlockSoundGroup.NETHERRACK;
            case "BASALT" -> BlockSoundGroup.BASALT;
            case "TUFF" -> BlockSoundGroup.TUFF;
            case "METAL" -> BlockSoundGroup.METAL;
            case "GLASS" -> BlockSoundGroup.GLASS;
            case "AMETHYST_BLOCK" -> BlockSoundGroup.AMETHYST_BLOCK;
            case "WOOD" -> BlockSoundGroup.WOOD;
            case "GRASS" -> BlockSoundGroup.GRASS;
            case "GRAVEL" -> BlockSoundGroup.ROOTED_DIRT;
            case "SAND" -> BlockSoundGroup.SAND;
            case "SNOW" -> BlockSoundGroup.SNOW;
            case "MOSS" -> BlockSoundGroup.MOSS_BLOCK;
            default -> BlockSoundGroup.STONE;
        };
    }

    private static ParticleEffect leafParticle(String name) {
        return switch (name) {
            case "CRIMSON_SPORE" -> ParticleTypes.CRIMSON_SPORE;
            case "SNOWFLAKE" -> ParticleTypes.SNOWFLAKE;
            default -> ParticleTypes.END_ROD;
        };
    }
}
