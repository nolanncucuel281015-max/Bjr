package com.forgotten.dimensions.registry;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.BlockKind;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.function.Predicate;
import java.util.function.Supplier;

/** Cinq onglets créatifs : blocs, ressources, armes, armures, artefacts. */
public final class ModItemGroups {
    private ModItemGroups() {}

    public static void register() {
        group("blocks", () -> ModBlocks.get("cloudstone_bricks").asItem(), (ItemGroup.Entries e) -> ModBlocks.all().forEach((id, b) -> {
            BlockKind kind = ModBlocks.definitions().get(id).kind();
            if (kind != BlockKind.ORE) e.add(b);
        }));
        group("resources", () -> ModItems.get("celestial_crystal"), e -> {
            ModBlocks.all().forEach((id, b) -> {
                if (ModBlocks.definitions().get(id).kind() == BlockKind.ORE) e.add(b);
            });
            ModItems.all().forEach((id, item) -> {
                if (!ModItems.weapons().containsKey(id) && !ModItems.artifacts().containsKey(id)
                        && ModItems.armorSetOf(item) == null && !id.endsWith("_spell_bolt") && !id.endsWith("_spawn_egg")) e.add(item);
            });
        });
        group("weapons", () -> ModItems.get("celestial_blade"), e -> ModItems.weapons().keySet().forEach(id -> e.add(ModItems.get(id))));
        group("armor", () -> ModItems.get("frost_queen_chestplate"), e -> ModItems.armorPieces().keySet().forEach(e::add));
        group("creatures", () -> ModItems.get("sky_drake_spawn_egg"), e -> ModItems.all().forEach((id, item) -> {
            if (id.endsWith("_spawn_egg")) e.add(item);
        }));
        group("artifacts", () -> ModItems.get("crown_of_seven_worlds"), e -> ModItems.artifacts().keySet().forEach(id -> e.add(ModItems.get(id))));
    }

    private static void group(String name, Supplier<Item> icon, java.util.function.Consumer<ItemGroup.Entries> entries) {
        ItemGroup group = FabricItemGroup.builder()
                .icon(() -> new ItemStack(icon.get()))
                .displayName(Text.translatable("itemGroup.forgotten_dimensions." + name))
                .entries((ctx, e) -> entries.accept(e))
                .build();
        Registry.register(Registries.ITEM_GROUP, ForgottenDimensions.id(name), group);
    }

    @SuppressWarnings("unused")
    private static Predicate<String> any() {
        return s -> true;
    }
}
