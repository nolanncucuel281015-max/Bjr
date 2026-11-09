package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.ArmorSetDef;
import com.forgotten.dimensions.util.StatusEffectsHelper;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/** Bonus d'ensemble complet (4 pièces du même ensemble), réappliqués chaque seconde. */
public final class ArmorSetBonuses {
    private ArmorSetBonuses() {}

    public static void apply(ServerPlayerEntity player, ArmorSetDef set) {
        if (set == null) return;
        switch (set.bonus()) {
            case "slow_falling" -> {
                if (!player.isOnGround() && player.getVelocity().y < -0.4 && !player.isSneaking())
                    StatusEffectsHelper.refresh(player, StatusEffects.SLOW_FALLING, 0);
            }
            case "poison_immunity" -> player.removeStatusEffect(StatusEffects.POISON);
            case "strength" -> StatusEffectsHelper.refresh(player, StatusEffects.STRENGTH, 0);
            case "water_breathing" -> StatusEffectsHelper.refresh(player, StatusEffects.WATER_BREATHING, 0);
            case "dolphins_grace" -> {
                if (player.isTouchingWater()) StatusEffectsHelper.refresh(player, StatusEffects.DOLPHINS_GRACE, 0);
            }
            case "fire_resistance" -> StatusEffectsHelper.refresh(player, StatusEffects.FIRE_RESISTANCE, 0);
            case "freeze_immunity" -> player.setFrozenTicks(0);
            case "night_vision" -> StatusEffectsHelper.refresh(player, StatusEffects.NIGHT_VISION, 0);
            case "haste" -> StatusEffectsHelper.refresh(player, StatusEffects.HASTE, 1);
            case "regeneration" -> StatusEffectsHelper.refresh(player, StatusEffects.REGENERATION, 0);
            case "resistance" -> StatusEffectsHelper.refresh(player, StatusEffects.RESISTANCE, 0);
            case "primordial" -> {
                StatusEffectsHelper.refresh(player, StatusEffects.RESISTANCE, 1);
                StatusEffectsHelper.refresh(player, StatusEffects.STRENGTH, 1);
                StatusEffectsHelper.refresh(player, StatusEffects.FIRE_RESISTANCE, 0);
                player.setFrozenTicks(0);
            }
            default -> { /* "mana_regen" est lu par ManaManager */ }
        }
    }
}
