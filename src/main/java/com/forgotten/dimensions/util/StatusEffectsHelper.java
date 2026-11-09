package com.forgotten.dimensions.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

public final class StatusEffectsHelper {
    private StatusEffectsHelper() {}

    /**
     * Applique un effet « passif » discret (ambiant, sans particules) et ne le renouvelle que lorsqu'il
     * devient court : évite le clignotement de la vision nocturne et les envois réseau inutiles.
     */
    public static void refresh(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier) {
        StatusEffectInstance current = entity.getStatusEffect(effect);
        if (current == null || current.getAmplifier() < amplifier || current.getDuration() < 240) {
            entity.addStatusEffect(new StatusEffectInstance(effect, 320, amplifier, true, false, true));
        }
    }

    public static void apply(LivingEntity entity, RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier));
    }
}
