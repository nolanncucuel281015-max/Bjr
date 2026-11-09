package com.forgotten.dimensions.mixin;

import com.forgotten.dimensions.event.CombatEvents;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Point d'entrée unique pour modifier les dégâts reçus (compétences, artefacts). Côté serveur uniquement. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float forgottenDimensions$modifyDamage(float amount, @Local(argsOnly = true) ServerWorld world,
                                                   @Local(argsOnly = true) DamageSource source) {
        return CombatEvents.modifyDamage((LivingEntity) (Object) this, source, amount);
    }
}
