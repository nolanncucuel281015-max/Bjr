package com.forgotten.dimensions.client.render.spell;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.magic.SpellSchool;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Boîte à outils de dessin des effets de sorts : un cube unitaire et un plan unitaire, teintés
 * (couleur ARGB, donc fondu possible) et dessinés en pleine lumière avec une couche émissive
 * translucide. Tous les effets sont composés de ces deux formes.
 */
public final class SpellFxDraw {
    public static final EntityModelLayer LAYER = new EntityModelLayer(ForgottenDimensions.id("spell_fx"), "main");
    public static final RenderLayer ENERGY = RenderLayers.entityTranslucentEmissive(ForgottenDimensions.id("textures/entity/spell/energy.png"));
    public static final RenderLayer CORE = RenderLayers.entityTranslucentEmissive(ForgottenDimensions.id("textures/entity/spell/core.png"));
    public static final RenderLayer RUNE = RenderLayers.entityTranslucentEmissive(ForgottenDimensions.id("textures/entity/spell/rune.png"));
    public static final RenderLayer ROCK = RenderLayers.entityTranslucent(ForgottenDimensions.id("textures/entity/spell/rock.png"));
    private static final int LIGHT = LightmapTextureManager.MAX_LIGHT_COORDINATE;

    public final ModelPart cube;
    public final ModelPart plane;
    public MatrixStack m;
    public OrderedRenderCommandQueue q;

    public SpellFxDraw(ModelPart root) {
        this.cube = root.getChild("cube");
        this.plane = root.getChild("plane");
    }

    public static void registerLayer() {
        EntityModelLayerRegistry.registerModelLayer(LAYER, () -> {
            ModelData data = new ModelData();
            data.getRoot().addChild("cube", ModelPartBuilder.create().uv(0, 0).cuboid(-8, -8, -8, 16, 16, 16), ModelTransform.NONE);
            data.getRoot().addChild("plane", ModelPartBuilder.create().uv(0, 0).cuboid(-8, 0, -8, 16, 0, 16), ModelTransform.NONE);
            return TexturedModelData.of(data, 64, 32);
        });
    }

    // ------------------------------------------------------------------ couleurs

    /** Couleur principale de l'école. */
    public static int primary(SpellSchool s) {
        return switch (s) {
            case FIRE -> 0xFF5A14;
            case ICE -> 0x4FC8FF;
            case LIGHTNING -> 0xFFD830;
            case NATURE -> 0x3EDC52;
            case VOID -> 0x9030FF;
        };
    }

    /** Couleur claire (cœur incandescent). */
    public static int bright(SpellSchool s) {
        return switch (s) {
            case FIRE -> 0xFFE27A;
            case ICE -> 0xEAFBFF;
            case LIGHTNING -> 0xFFF6A8;
            case NATURE -> 0xDFFFA8;
            case VOID -> 0xF2C8FF;
        };
    }

    /** Couleur secondaire (contraste). */
    public static int deep(SpellSchool s) {
        return switch (s) {
            case FIRE -> 0xB01808;
            case ICE -> 0x2060E0;
            case LIGHTNING -> 0x7AA0FF;
            case NATURE -> 0x1E7A28;
            case VOID -> 0x2A0050;
        };
    }

    public static int argb(float alpha, int rgb) {
        int a = MathHelper.clamp((int) (alpha * 255F), 0, 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public static float easeOut(float t) {
        t = MathHelper.clamp(t, 0F, 1F);
        return 1F - (1F - t) * (1F - t) * (1F - t);
    }

    /** Apparition rapide, disparition douce sur la fin. */
    public static float fade(float age, float duration) {
        float in = MathHelper.clamp(age / 3F, 0F, 1F);
        float out = MathHelper.clamp((duration - age) / Math.max(4F, duration * 0.35F), 0F, 1F);
        return in * out;
    }

    // ------------------------------------------------------------------ formes

    public void cube(RenderLayer layer, float x, float y, float z, float sx, float sy, float sz,
                     float yaw, float pitch, float roll, int argb) {
        if ((argb >>> 24) < 4 || sx <= 0.001F || sy <= 0.001F || sz <= 0.001F) return;
        m.push();
        m.translate(x, y, z);
        if (yaw != 0) m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        if (pitch != 0) m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        if (roll != 0) m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        m.scale(sx, sy, sz);
        q.submitModelPart(cube, m, layer, LIGHT, OverlayTexture.DEFAULT_UV, null, false, false, argb, null, 0);
        m.pop();
    }

    public void cube(RenderLayer layer, float x, float y, float z, float s, float yaw, float pitch, float roll, int argb) {
        cube(layer, x, y, z, s, s, s, yaw, pitch, roll, argb);
    }

    /** Disque runique horizontal de rayon r. */
    public void disc(float y, float r, float yaw, int argb) {
        if ((argb >>> 24) < 4 || r <= 0.01F) return;
        m.push();
        m.translate(0, y, 0);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        m.scale(r * 2F, 1F, r * 2F);
        q.submitModelPart(plane, m, RUNE, LIGHT, OverlayTexture.DEFAULT_UV, null, false, false, argb, null, 0);
        m.pop();
    }

    /** Anneau horizontal de n segments tangents. */
    public void ring(RenderLayer layer, float y, float r, int n, float w, float h, float spin, int argb) {
        if (r <= 0.01F) return;
        float seg = (float) (2 * Math.PI * r / n) * 1.05F;
        for (int i = 0; i < n; i++) {
            float a = (float) (2 * Math.PI * i / n) + spin * MathHelper.RADIANS_PER_DEGREE;
            float x = MathHelper.cos(a) * r, z = MathHelper.sin(a) * r;
            cube(layer, x, y, z, seg, h, w, -a * MathHelper.DEGREES_PER_RADIAN + 90F, 0, 0, argb);
        }
    }

    /** Anneau incliné (cercle complet) : utilisé pour les dômes et disques d'accrétion. */
    public void tiltedRing(RenderLayer layer, float cy, float r, int n, float s, float tiltX, float spinY, float extraY, int argb) {
        m.push();
        m.translate(0, cy, 0);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(extraY));
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tiltX));
        for (int i = 0; i < n; i++) {
            float a = (float) (2 * Math.PI * i / n) + spinY * MathHelper.RADIANS_PER_DEGREE;
            cube(layer, MathHelper.cos(a) * r, 0, MathHelper.sin(a) * r, s, a * 57F, a * 31F, 0, argb);
        }
        m.pop();
    }

    /** Boule d'énergie : trois cubes croisés tournants (forme d'étoile) + coquille translucide. */
    public void orb(float x, float y, float z, float size, float age, int coreRgb, int shellRgb, float alpha) {
        for (int k = 0; k < 3; k++) {
            cube(CORE, x, y, z, size * 0.5F, age * (7 + k * 3), age * (5 + k * 2) + k * 30, k * 45, argb(alpha * 0.9F, coreRgb));
        }
        for (int k = 0; k < 3; k++) {
            cube(ENERGY, x, y, z, size * 0.85F, -age * (4 + k), age * 3 + k * 60, k * 30 + 15, argb(alpha * 0.35F, shellRgb));
        }
        // étincelles en orbite
        for (int i = 0; i < 8; i++) {
            float a = age * 0.35F + i * 0.785F;
            float b = age * 0.21F + i * 1.3F;
            float r = size * 0.85F;
            cube(CORE, x + MathHelper.cos(a) * MathHelper.cos(b) * r, y + MathHelper.sin(b) * r, z + MathHelper.sin(a) * MathHelper.cos(b) * r,
                    size * 0.09F, age * 13, age * 9, 0, argb(alpha, coreRgb));
        }
    }

    /** Segment orienté de (0,0,0) vers (dx,dy,dz) : rend un faisceau ou un éclat. */
    public void beam(RenderLayer layer, float dx, float dy, float dz, float w, float roll, int argb) {
        float len = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.01F) return;
        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
        float pitch = (float) -Math.toDegrees(Math.asin(dy / len));
        m.push();
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        cube(layer, 0, 0, len / 2F, w, w, len, 0, 0, roll, argb);
        m.pop();
    }

    /** Pseudo-aléatoire déterministe (même résultat chaque image). */
    public static float rand(int seed, int i) {
        int h = seed * 73856093 ^ i * 19349663;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }
}
