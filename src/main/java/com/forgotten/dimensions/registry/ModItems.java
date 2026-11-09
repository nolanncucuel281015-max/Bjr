package com.forgotten.dimensions.registry;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.ArmorSetDef;
import com.forgotten.dimensions.content.ArtifactDef;
import com.forgotten.dimensions.content.FdRarity;
import com.forgotten.dimensions.content.SimpleItemDef;
import com.forgotten.dimensions.content.TierDef;
import com.forgotten.dimensions.content.WeaponDef;
import com.forgotten.dimensions.item.ArmorMaterials;
import com.forgotten.dimensions.item.ArtifactItem;
import com.forgotten.dimensions.item.FdBowItem;
import com.forgotten.dimensions.item.FdCrossbowItem;
import com.forgotten.dimensions.item.MagicWeaponItem;
import com.forgotten.dimensions.item.MeleeWeaponItem;
import com.forgotten.dimensions.registry.generated.ContentTables;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Enregistre matériaux, armes, armures et artefacts. */
public final class ModItems {
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<String, WeaponDef> WEAPONS = new LinkedHashMap<>();
    private static final Map<String, TierDef> TIERS = new LinkedHashMap<>();
    private static final Map<String, ToolMaterial> TOOL_MATERIALS = new LinkedHashMap<>();
    private static final Map<Item, ArmorSetDef> ARMOR_PIECE_TO_SET = new LinkedHashMap<>();
    private static final Map<String, ArtifactDef> ARTIFACTS = new LinkedHashMap<>();
    private static final Map<Item, FdRarity> RARITIES = new LinkedHashMap<>();

    private ModItems() {}

    public static void register() {
        for (SimpleItemDef def : ContentTables.items()) {
            Item.Settings s = new Item.Settings();
            if (def.fireproof()) s.fireproof();
            register(def.id(), Item::new, rarity(s, def.id(), def.rarity()));
        }

        for (TierDef tier : ContentTables.tiers()) {
            TIERS.put(tier.id(), tier);
            TagKey<Item> repair = TagKey.of(RegistryKeys.ITEM, ForgottenDimensions.id("repair/" + tier.repairItem()));
            TOOL_MATERIALS.put(tier.id(), new ToolMaterial(incorrectFor(tier.incorrectFor()), tier.durability(),
                    tier.speed(), tier.damageBonus(), tier.enchantability(), repair));
        }

        for (WeaponDef w : ContentTables.weapons()) {
            TierDef tier = TIERS.get(w.tier());
            ToolMaterial mat = TOOL_MATERIALS.get(w.tier());
            Item.Settings s = rarity(new Item.Settings(), w.id(), w.rarity());
            if (w.rarity() == FdRarity.MYTHIC || w.tier().equals("magmatite")) s.fireproof();
            WEAPONS.put(w.id(), w);
            switch (w.category()) {
                case SWORD, GREATSWORD, DAGGER -> register(w.id(), st -> new MeleeWeaponItem(w, st),
                        s.sword(mat, w.category().baseDamage, w.category().attackSpeed));
                case BATTLEAXE -> register(w.id(), st -> new MeleeWeaponItem(w, st),
                        s.axe(mat, w.category().baseDamage, w.category().attackSpeed));
                // Paramètres de la lance vanilla en fer (Items.IRON_SPEAR), le matériau modifie les dégâts.
                case SPEAR -> register(w.id(), st -> new MeleeWeaponItem(w, st),
                        s.spear(mat, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F));
                case STAFF, WAND -> register(w.id(), st -> new MagicWeaponItem(w, tier.school(), st),
                        s.sword(mat, w.category().baseDamage, w.category().attackSpeed));
                case BOW -> register(w.id(), st -> new FdBowItem(w, tier, st),
                        s.maxDamage(tier.durability()).enchantable(tier.enchantability()));
                case CROSSBOW -> register(w.id(), st -> new FdCrossbowItem(w, tier, st),
                        s.maxDamage(tier.durability()).enchantable(tier.enchantability()));
            }
        }

        for (ArmorSetDef set : ContentTables.armorSets()) {
            ArmorMaterial material = ArmorMaterials.create(set);
            String[] pieces = {"helmet", "chestplate", "leggings", "boots"};
            EquipmentType[] types = {EquipmentType.HELMET, EquipmentType.CHESTPLATE, EquipmentType.LEGGINGS, EquipmentType.BOOTS};
            for (int i = 0; i < 4; i++) {
                EquipmentType type = types[i];
                Item.Settings s = rarity(new Item.Settings(), set.id() + "_" + pieces[i], set.rarity());
                if (set.id().equals("magmatite") || set.id().equals("volcanic_titan") || set.id().equals("primordial")) s.fireproof();
                Item item = register(set.id() + "_" + pieces[i], Item::new, s.armor(material, type));
                ARMOR_PIECE_TO_SET.put(item, set);
            }
        }

        for (ArtifactDef a : ContentTables.artifacts()) {
            ARTIFACTS.put(a.id(), a);
            register(a.id(), st -> new ArtifactItem(a, st), rarity(new Item.Settings().maxCount(1), a.id(), a.rarity()));
        }
        ForgottenDimensions.LOGGER.info("{} objets enregistrés ({} armes, {} pièces d'armure, {} artefacts)",
                ITEMS.size(), WEAPONS.size(), ARMOR_PIECE_TO_SET.size(), ARTIFACTS.size());
    }

    private static final Map<String, FdRarity> PENDING_RARITY = new LinkedHashMap<>();

    private static Item.Settings rarity(Item.Settings s, String id, FdRarity rarity) {
        PENDING_RARITY.put(id, rarity);
        s.rarity(rarity.vanilla());
        if (rarity.needsCustomNameColor()) {
            s.component(DataComponentTypes.ITEM_NAME,
                    Text.translatable("item." + ForgottenDimensions.MOD_ID + "." + id).formatted(rarity.color()));
        }
        return s;
    }

    private static TagKey<net.minecraft.block.Block> incorrectFor(String level) {
        return switch (level) {
            case "IRON" -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case "DIAMOND" -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

    public static Item register(String id, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, ForgottenDimensions.id(id));
        Item item = factory.apply(settings.registryKey(key));
        Registry.register(Registries.ITEM, key, item);
        ITEMS.put(id, item);
        FdRarity r = PENDING_RARITY.remove(id);
        if (r != null) RARITIES.put(item, r);
        return item;
    }

    public static Item get(String id) {
        return ITEMS.get(id);
    }

    public static Map<String, Item> all() {
        return Collections.unmodifiableMap(ITEMS);
    }

    public static Map<String, WeaponDef> weapons() {
        return Collections.unmodifiableMap(WEAPONS);
    }

    /** Rareté RPG d'un objet du mod (null pour les blocs et objets sans rareté). */
    public static FdRarity rarityOf(Item item) {
        return RARITIES.get(item);
    }

    public static Map<String, ArtifactDef> artifacts() {
        return Collections.unmodifiableMap(ARTIFACTS);
    }

    /** Ensemble d'armure auquel appartient une pièce, ou null. */
    public static ArmorSetDef armorSetOf(Item item) {
        return ARMOR_PIECE_TO_SET.get(item);
    }

    public static Map<Item, ArmorSetDef> armorPieces() {
        return Collections.unmodifiableMap(ARMOR_PIECE_TO_SET);
    }
}
