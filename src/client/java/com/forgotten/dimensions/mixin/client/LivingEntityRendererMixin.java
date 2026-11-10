package com.forgotten.dimensions.mixin.client;

import com.forgotten.dimensions.client.render.RpgPlates;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Plaques RPG (niveau, nom, barre de vie) et chiffres de dégâts au-dessus des créatures. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void forgotten_dimensions$extractPlate(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        RpgPlates.extract(entity, state, tickDelta);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("TAIL"))
    private void forgotten_dimensions$renderPlate(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue,
                                                 CameraRenderState camera, CallbackInfo ci) {
        RpgPlates.render(state, matrices, queue, camera);
    }
}
