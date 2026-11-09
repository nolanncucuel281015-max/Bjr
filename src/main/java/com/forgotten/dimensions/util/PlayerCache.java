package com.forgotten.dimensions.util;

import com.forgotten.dimensions.content.ArmorSetDef;
import com.forgotten.dimensions.item.ArtifactItem;
import com.forgotten.dimensions.registry.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instantané, recalculé une fois par seconde, des artefacts portés et de l'ensemble d'armure complet
 * de chaque joueur. Évite de parcourir l'inventaire à chaque coup ou à chaque sort.
 */
public final class PlayerCache {
    public record Snapshot(Set<String> artifacts, ArmorSetDef fullSet) {
        public static final Snapshot EMPTY = new Snapshot(Set.of(), null);

        public boolean has(String artifact) {
            return artifacts.contains(artifact);
        }

        public boolean setBonus(String bonus) {
            return fullSet != null && fullSet.bonus().equals(bonus);
        }
    }

    private static final Map<UUID, Snapshot> CACHE = new ConcurrentHashMap<>();

    private PlayerCache() {}

    public static Snapshot get(PlayerEntity player) {
        return CACHE.getOrDefault(player.getUuid(), Snapshot.EMPTY);
    }

    public static Snapshot refresh(PlayerEntity player) {
        Snapshot snap = compute(player);
        CACHE.put(player.getUuid(), snap);
        return snap;
    }

    public static void remove(UUID uuid) {
        CACHE.remove(uuid);
    }

    public static void clear() {
        CACHE.clear();
    }

    /** Calcul pur (utilisable aussi côté client pour l'affichage). */
    public static Snapshot compute(PlayerEntity player) {
        Set<String> artifacts = new HashSet<>();
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArtifactItem artifact) {
                artifacts.add(artifact.definition().id());
            }
        }
        ArmorSetDef set = null;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ArmorSetDef piece = ModItems.armorSetOf(player.getEquippedStack(slot).getItem());
            if (piece == null || (set != null && set != piece)) {
                set = null;
                break;
            }
            set = piece;
        }
        return new Snapshot(Set.copyOf(artifacts), set);
    }
}
