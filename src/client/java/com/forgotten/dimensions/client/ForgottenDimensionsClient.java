package com.forgotten.dimensions.client;

import com.forgotten.dimensions.client.hud.RpgHud;
import com.forgotten.dimensions.client.input.ModKeyBindings;
import com.forgotten.dimensions.client.tooltip.FdTooltips;
import com.forgotten.dimensions.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.entity.EntityRendererFactories;

public final class ForgottenDimensionsClient implements ClientModInitializer {
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerArmorDetails(net.minecraft.client.render.entity.LivingEntityRenderer<?, ?, ?> renderer,
                                             net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper helper,
                                             net.minecraft.client.render.entity.EntityRendererFactory.Context ctx,
                                             net.minecraft.client.render.entity.model.EntityModelLayer layer) {
        if (renderer.getModel() instanceof net.minecraft.client.render.entity.model.BipedEntityModel<?>) {
            helper.register(new com.forgotten.dimensions.client.render.ArmorDetailsFeatureRenderer(
                    (net.minecraft.client.render.entity.feature.FeatureRendererContext) renderer, ctx.getPart(layer)));
        }
    }

    @Override
    public void onInitializeClient() {
        com.forgotten.dimensions.client.render.spell.SpellFxDraw.registerLayer();
        EntityRendererFactories.register(ModEntities.MAGIC_BOLT, com.forgotten.dimensions.client.render.spell.MagicBoltRenderer::new);
        EntityRendererFactories.register(ModEntities.SPELL_EFFECT, com.forgotten.dimensions.client.render.spell.SpellEffectRenderer::new);
        com.forgotten.dimensions.registry.ModCreatures.all().forEach((id, type) -> {
            var def = com.forgotten.dimensions.registry.ModCreatures.definitions().get(id);
            com.forgotten.dimensions.client.render.CreatureModels.layer(def.archetype());
            EntityRendererFactories.register(type, ctx -> new com.forgotten.dimensions.client.render.FdCreatureRenderer(ctx, def));
        });
        // Pièces d'armure en relief sur tous les rendus humanoïdes (joueurs, supports d'armure, zombies...).
        var detailsLayer = com.forgotten.dimensions.client.render.CreatureModels.layer("armor_details");
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
                (type, renderer, helper, ctx) -> registerArmorDetails(renderer, helper, ctx, detailsLayer));
        ModKeyBindings.register();
        RpgHud.register();
        FdTooltips.register();
    }
}
