package com.forgotten.dimensions.client;

import com.forgotten.dimensions.client.hud.RpgHud;
import com.forgotten.dimensions.client.input.ModKeyBindings;
import com.forgotten.dimensions.client.tooltip.FdTooltips;
import com.forgotten.dimensions.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public final class ForgottenDimensionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererFactories.register(ModEntities.MAGIC_BOLT, FlyingItemEntityRenderer::new);
        ModKeyBindings.register();
        RpgHud.register();
        FdTooltips.register();
    }
}
