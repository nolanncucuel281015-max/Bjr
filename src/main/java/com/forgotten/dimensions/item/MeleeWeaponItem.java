package com.forgotten.dimensions.item;

import com.forgotten.dimensions.content.WeaponDef;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

/** Épées, grandes épées, dagues, haches de combat et lances. */
public class MeleeWeaponItem extends Item {
    private final WeaponDef def;

    public MeleeWeaponItem(WeaponDef def, Settings settings) {
        super(settings);
        this.def = def;
    }

    public WeaponDef definition() {
        return def;
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        LegendaryEffects.onMeleeHit(def, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (def.isLegendary() && slot == EquipmentSlot.MAINHAND && entity instanceof LivingEntity living && world.getTime() % 20 == 0) {
            LegendaryEffects.whileHeld(def.legendary(), living);
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return def.isLegendary() || super.hasGlint(stack);
    }
}
