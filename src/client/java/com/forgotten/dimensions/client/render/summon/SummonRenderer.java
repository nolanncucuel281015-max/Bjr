package com.forgotten.dimensions.client.render.summon;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.client.render.CreatureModels;
import com.forgotten.dimensions.client.render.FdCreatureModel;
import com.forgotten.dimensions.client.render.spell.SpellFxDraw;
import com.forgotten.dimensions.entity.summon.SummonedCreatureEntity;
import com.forgotten.dimensions.magic.SummonType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import static com.forgotten.dimensions.client.render.spell.SpellFxDraw.*;

/**
 * Rendu d'une invocation : modèle 3D propre (tools/gen/modelgen.py, formes summon_*), passe
 * lumineuse ({@link SummonGlowFeature}), translucidité des spectres, et aura magique animée
 * autour de la créature (cercle runique, anneaux, flammes, éclats...).
 */
public class SummonRenderer extends MobEntityRenderer<SummonedCreatureEntity, SummonRenderState, FdCreatureModel> {
    private final SummonType type;
    private final Identifier texture;
    private final Identifier glow;
    private final SpellFxDraw d;

    public SummonRenderer(EntityRendererFactory.Context ctx, SummonType type) {
        super(ctx, new FdCreatureModel(ctx.getPart(CreatureModels.layer(archetype(type)))), 0.45F * type.width);
        this.type = type;
        this.texture = ForgottenDimensions.id("textures/entity/summon/" + type.id + ".png");
        this.glow = ForgottenDimensions.id("textures/entity/summon/" + type.id + "_glow.png");
        this.d = new SpellFxDraw(ctx.getPart(SpellFxDraw.LAYER));
        addFeature(new SummonGlowFeature(this));
    }

    public static String archetype(SummonType t) {
        return switch (t) {
            case SPIRIT_WOLF -> "summon_wolf";
            case STONE_GUARDIAN -> "summon_guardian";
            case FIRE_ELEMENTAL -> "summon_elemental";
            case PHOENIX -> "summon_phoenix";
            case FROST_SENTINEL -> "summon_sentinel";
            case STORM_SPIRIT -> "summon_storm";
            case VOID_WRAITH -> "summon_wraith";
        };
    }

    @Override
    public SummonRenderState createRenderState() {
        return new SummonRenderState();
    }

    @Override
    public void updateRenderState(SummonedCreatureEntity e, SummonRenderState s, float tickDelta) {
        super.updateRenderState(e, s, tickDelta);
        s.summon = type;
        s.texture = texture;
        s.glowTexture = glow;
        s.attackProgress = e.getHandSwingProgress(tickDelta);
        s.modelScale = type.scale;
        s.flying = type.flying;
        s.seed = e.getId();
    }

    @Override
    public Identifier getTexture(SummonRenderState s) {
        return s.texture;
    }

    @Override
    protected void scale(SummonRenderState s, MatrixStack matrices) {
        matrices.scale(s.modelScale, s.modelScale, s.modelScale);
    }

    @Override
    protected RenderLayer getRenderLayer(SummonRenderState s, boolean showBody, boolean translucent, boolean showOutline) {
        if (type.ghostly() && showBody) return RenderLayers.entityTranslucent(texture);
        return super.getRenderLayer(s, showBody, translucent, showOutline);
    }

    @Override
    protected int getMixColor(SummonRenderState s) {
        return type.ghostly() ? 0xB4FFFFFF : super.getMixColor(s);
    }

    @Override
    public void render(SummonRenderState s, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera) {
        super.render(s, matrices, queue, camera);
        d.m = matrices;
        d.q = queue;
        aura(s);
    }

    /** Aura 3D propre à chaque invocation (coordonnées en blocs, origine aux pieds). */
    private void aura(SummonRenderState s) {
        float age = s.age;
        int p = primary(type.school), b = bright(type.school), dp = deep(type.school);
        float h = type.height;
        float pulse = 0.5F + 0.5F * MathHelper.sin(age * 0.12F + s.seed);
        switch (type) {
            case SPIRIT_WOLF -> {
                d.disc(0.03F, 0.9F, age * 4F, argb(0.35F + 0.2F * pulse, p));
                for (int i = 0; i < 6; i++) {
                    float y = ((age * 0.05F + rand(s.seed, i)) % 1F) * 1.2F;
                    float a = i * 1.05F + age * 0.1F;
                    d.cube(CORE, MathHelper.cos(a) * 0.5F, 0.3F + y, MathHelper.sin(a) * 0.7F, 0.07F, age * 9, age * 5, 0,
                            argb(0.8F * (1F - y / 1.2F), b));
                }
            }
            case STONE_GUARDIAN -> {
                d.disc(0.03F, 1.7F, age * 1.5F, argb(0.45F + 0.2F * pulse, p));
                d.ring(ENERGY, 0.08F, 1.9F, 24, 0.08F, 0.08F, age * 2, argb(0.5F, b));
                for (int i = 0; i < 8; i++) {
                    float y = ((age * 0.03F + rand(s.seed, i)) % 1F) * 2.6F;
                    float a = i * 0.785F + age * 0.05F;
                    d.cube(CORE, MathHelper.cos(a) * 1.3F, y, MathHelper.sin(a) * 1.3F, 0.1F, age * 6, 0, 45,
                            argb(0.8F * (1F - y / 2.6F), b));
                }
            }
            case FROST_SENTINEL -> {
                d.disc(0.03F, 1.8F, -age * 1.5F, argb(0.5F + 0.2F * pulse, p));
                for (int i = 0; i < 5; i++) {
                    float a = age * 0.06F + i * MathHelper.TAU / 5;
                    float y = 1.6F + MathHelper.sin(age * 0.09F + i) * 0.3F;
                    d.cube(CORE, MathHelper.cos(a) * 1.5F, y, MathHelper.sin(a) * 1.5F, 0.16F, 0.42F, 0.16F, age * 4, 0, 0,
                            argb(0.9F, b));
                    d.cube(ENERGY, MathHelper.cos(a) * 1.5F, y, MathHelper.sin(a) * 1.5F, 0.3F, 0.7F, 0.3F, age * 4, 0, 0,
                            argb(0.35F, p));
                }
            }
            case FIRE_ELEMENTAL -> {
                float cy = h * 0.62F;
                d.orb(0, cy, 0, 0.45F + 0.05F * pulse, age, b, p, 0.85F);
                d.tiltedRing(ENERGY, cy, 1.0F, 24, 0.07F, 25F, age * 7, age * 1.5F, argb(0.75F, p));
                d.tiltedRing(CORE, cy, 0.8F, 20, 0.05F, -35F, -age * 9, age, argb(0.8F, b));
                // flammes qui s'élèvent sous le corps
                for (int i = 0; i < 10; i++) {
                    float t = (age * 0.07F + rand(s.seed, i)) % 1F;
                    float a = rand(s.seed, i + 20) * MathHelper.TAU;
                    float r = 0.25F * (1F - t);
                    d.cube(ENERGY, MathHelper.cos(a) * r, 0.2F + t * 1.0F, MathHelper.sin(a) * r, 0.22F * (1F - t), age * 7, age * 5, 0,
                            argb(0.8F * (1F - t), t < 0.4F ? b : p));
                }
            }
            case PHOENIX -> {
                float cy = 0.55F;
                d.tiltedRing(CORE, cy + 0.2F, 1.1F, 26, 0.06F, 80F, age * 6, s.bodyYaw * -1F, argb(0.7F, b));
                for (int i = 0; i < 14; i++) {
                    float t = (age * 0.06F + rand(s.seed, i)) % 1F;
                    float yawRad = -s.bodyYaw * MathHelper.RADIANS_PER_DEGREE;
                    float back = 0.6F + t * 2.2F;
                    float sx = MathHelper.sin(yawRad) * -back + (rand(s.seed, i + 40) - 0.5F) * 0.8F * t;
                    float sz = -MathHelper.cos(yawRad) * back + (rand(s.seed, i + 60) - 0.5F) * 0.8F * t;
                    d.cube(ENERGY, sx, cy + (rand(s.seed, i + 80) - 0.5F) * 0.6F * t, sz, 0.28F * (1F - t), age * 9, age * 4, 0,
                            argb(0.85F * (1F - t), t < 0.35F ? b : p));
                }
            }
            case STORM_SPIRIT -> {
                float cy = 0.85F;
                d.orb(0, cy, 0, 0.4F + 0.08F * pulse, age, b, p, 0.9F);
                d.tiltedRing(CORE, cy, 0.95F, 26, 0.06F, 60F, age * 14, age * 3, argb(0.9F, b));
                d.tiltedRing(ENERGY, cy, 1.15F, 30, 0.09F, -50F, -age * 11, age * 2, argb(0.6F, p));
                d.tiltedRing(CORE, cy, 0.7F, 20, 0.05F, 10F, age * 20, 0, argb(0.8F, b));
                // arcs électriques aléatoires (changent toutes les 3 images)
                int frame = (int) (age / 3);
                for (int i = 0; i < 3; i++) {
                    float a = rand(s.seed + frame, i) * MathHelper.TAU;
                    float pt = (rand(s.seed + frame, i + 9) - 0.5F) * 1.6F;
                    d.beam(CORE, MathHelper.cos(a) * 1.2F, pt, MathHelper.sin(a) * 1.2F, 0.04F, 0, argb(0.9F, b));
                }
            }
            case VOID_WRAITH -> {
                d.disc(0.05F, 1.2F, -age * 5F, argb(0.5F + 0.2F * pulse, dp));
                d.disc(0.07F, 0.8F, age * 7F, argb(0.6F, p));
                for (int i = 0; i < 12; i++) {
                    float t = (age * 0.04F + rand(s.seed, i)) % 1F;
                    float a = rand(s.seed, i + 20) * MathHelper.TAU + t * 5F;
                    float r = 1.4F * (1F - t) + 0.2F;
                    d.cube(CORE, MathHelper.cos(a) * r, 0.2F + t * 1.8F, MathHelper.sin(a) * r, 0.08F, age * 9, 0, 0,
                            argb(0.9F * t, b));
                }
            }
        }
    }
}
