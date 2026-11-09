package com.forgotten.dimensions.item;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.ArmorSetDef;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.Map;

/** Construit les {@link ArmorMaterial} à partir des tables (l'apparence vient de assets/.../equipment/&lt;id&gt;.json). */
public final class ArmorMaterials {
    private ArmorMaterials() {}

    public static ArmorMaterial create(ArmorSetDef def) {
        Map<EquipmentType, Integer> defense = new EnumMap<>(EquipmentType.class);
        defense.put(EquipmentType.BOOTS, def.boots());
        defense.put(EquipmentType.LEGGINGS, def.leggings());
        defense.put(EquipmentType.CHESTPLATE, def.chestplate());
        defense.put(EquipmentType.HELMET, def.helmet());
        defense.put(EquipmentType.BODY, def.chestplate());
        TagKey<Item> repair = TagKey.of(RegistryKeys.ITEM, ForgottenDimensions.id("repair/" + def.repairItem()));
        RegistryKey<EquipmentAsset> asset = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, ForgottenDimensions.id(def.id()));
        return new ArmorMaterial(def.durabilityMultiplier(), defense, def.enchantability(),
                SoundEvents.ITEM_ARMOR_EQUIP_IRON, def.toughness(), def.knockbackResistance(), repair, asset);
    }
}
