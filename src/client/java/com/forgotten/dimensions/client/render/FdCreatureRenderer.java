package com.forgotten.dimensions.client.render;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.content.CreatureMovement;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

/** Rendu d'une créature : modèle de sa forme, texture propre, échelle de sa définition. */
public class FdCreatureRenderer extends MobEntityRenderer<MobEntity, FdCreatureRenderState, FdCreatureModel> {
    private final CreatureDef def;
    private final Identifier texture;

    public FdCreatureRenderer(EntityRendererFactory.Context ctx, CreatureDef def) {
        super(ctx, new FdCreatureModel(ctx.getPart(CreatureModels.layer(def.archetype()))), 0.4F * def.width() / 0.6F);
        this.def = def;
        this.texture = ForgottenDimensions.id("textures/entity/creature/" + def.id() + ".png");
    }

    @Override
    public FdCreatureRenderState createRenderState() {
        return new FdCreatureRenderState();
    }

    @Override
    public void updateRenderState(MobEntity entity, FdCreatureRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.texture = texture;
        state.attackProgress = entity.getHandSwingProgress(tickDelta);
        state.modelScale = def.scale();
        state.flying = def.movement() == CreatureMovement.FLYING;
        state.swimming = def.movement() == CreatureMovement.SWIMMING;
    }

    @Override
    public Identifier getTexture(FdCreatureRenderState state) {
        return state.texture;
    }

    @Override
    protected void scale(FdCreatureRenderState state, MatrixStack matrices) {
        matrices.scale(state.modelScale, state.modelScale, state.modelScale);
    }
}
