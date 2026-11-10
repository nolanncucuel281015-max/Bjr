package com.forgotten.dimensions.client.tooltip;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.ArmorSetDef;
import com.forgotten.dimensions.content.FdRarity;
import com.forgotten.dimensions.item.FdBowItem;
import com.forgotten.dimensions.item.FdCrossbowItem;
import com.forgotten.dimensions.item.MagicWeaponItem;
import com.forgotten.dimensions.registry.ModItems;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

/** Ajoute rareté, description, école de magie et bonus d'ensemble aux infobulles des objets du mod. */
public final class FdTooltips {
    private FdTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Item item = stack.getItem();
            Identifier id = Registries.ITEM.getId(item);
            if (!id.getNamespace().equals(ForgottenDimensions.MOD_ID)) return;
            int at = Math.min(1, lines.size());
            List<Text> extra = new java.util.ArrayList<>();
            FdRarity rarity = ModItems.rarityOf(item);
            if (rarity != null) extra.add(Text.translatable(rarity.translationKey()).formatted(rarity.color(), Formatting.ITALIC));
            String descKey = "item." + id.getNamespace() + "." + id.getPath() + ".desc";
            if (I18n.hasTranslation(descKey)) extra.add(Text.translatable(descKey).formatted(Formatting.GRAY));
            if (item instanceof MagicWeaponItem m) {
                extra.add(Text.translatable("tooltip.forgotten_dimensions.magic_weapon",
                        Text.translatable(m.school().translationKey()).formatted(m.school().formatting),
                        Math.round((m.powerBonus() - 1F) * 100F), Math.round((m.affinityBonus() - 1F) * 100F)).formatted(Formatting.DARK_AQUA));
                if (!m.isStaff()) extra.add(Text.translatable("tooltip.forgotten_dimensions.wand_bonus").formatted(Formatting.DARK_AQUA));
                extra.add(Text.translatable("tooltip.forgotten_dimensions.cast_hint").formatted(Formatting.GRAY, Formatting.ITALIC));
            } else if (item instanceof FdBowItem b) {
                extra.add(Text.translatable("tooltip.forgotten_dimensions.arrow_damage", String.format("%.1f", b.arrowDamage())).formatted(Formatting.DARK_AQUA));
            } else if (item instanceof FdCrossbowItem c) {
                extra.add(Text.translatable("tooltip.forgotten_dimensions.arrow_damage", String.format("%.1f", c.boltDamage())).formatted(Formatting.DARK_AQUA));
            }
            ArmorSetDef set = ModItems.armorSetOf(item);
            if (set != null && !set.bonus().equals("none")) {
                extra.add(Text.translatable("tooltip.forgotten_dimensions.set_bonus",
                        Text.translatable("armor_bonus.forgotten_dimensions." + set.bonus())).formatted(Formatting.BLUE));
            }
            lines.addAll(at, extra);
        });
    }
}
