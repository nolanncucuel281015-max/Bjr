package com.forgotten.dimensions.client.render.summon;

import com.forgotten.dimensions.client.render.FdCreatureModel;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Passe lumineuse des invocations : les parties magiques (veines de magma, cristaux, runes, yeux)
 * brillent dans le noir et pulsent ; les créatures spectrales luisent en entier.
 */
public class SummonGlowFeature extends FeatureRenderer<SummonRenderState, FdCreatureModel> {
    public SummonGlowFeature(FeatureRendererContext<SummonRenderState, FdCreatureModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, SummonRenderState s, float limbAngle, float limbDistance) {
        if (s.summon == null) return;
        float pulse = 0.75F + 0.25F * MathHelper.sin(s.age * 0.15F + s.seed);
        int a = Math.round(255 * pulse);
        queue.submitModel(getContextModel(), s, matrices, RenderLayers.entityTranslucentEmissive(s.glowTexture),
                LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, (a << 24) | 0xFFFFFF, null, 0, null);
        if (s.summon.ghostly()) {
            int ga = Math.round(255 * (0.25F + 0.12F * MathHelper.sin(s.age * 0.1F)));
            queue.submitModel(getContextModel(), s, matrices, RenderLayers.entityTranslucentEmissive(s.texture),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, (ga << 24) | 0xFFFFFF, null, 0, null);
        }
    }
}
