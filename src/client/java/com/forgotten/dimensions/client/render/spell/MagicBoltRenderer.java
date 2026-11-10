package com.forgotten.dimensions.client.render.spell;

import com.forgotten.dimensions.entity.MagicBoltEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import static com.forgotten.dimensions.client.render.spell.SpellFxDraw.*;

/** Projectile magique en 3D, orienté dans le sens du vol, avec traînée ; une forme par école. */
public class MagicBoltRenderer extends EntityRenderer<MagicBoltEntity, MagicBoltRenderState> {
    private final SpellFxDraw d;

    public MagicBoltRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.d = new SpellFxDraw(ctx.getPart(SpellFxDraw.LAYER));
        this.shadowRadius = 0F;
    }

    @Override
    public MagicBoltRenderState createRenderState() {
        return new MagicBoltRenderState();
    }

    @Override
    public void updateRenderState(MagicBoltEntity e, MagicBoltRenderState s, float tickDelta) {
        super.updateRenderState(e, s, tickDelta);
        s.school = e.school();
        s.size = e.size();
        Vec3d v = e.getVelocity();
        double h = Math.sqrt(v.x * v.x + v.z * v.z);
        s.yaw = (float) Math.toDegrees(Math.atan2(v.x, v.z));
        s.pitch = (float) -Math.toDegrees(Math.atan2(v.y, h));
        s.seed = e.getId();
    }

    @Override
    public void render(MagicBoltRenderState s, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera) {
        d.m = matrices;
        d.q = queue;
        float age = s.age, z = s.size;
        int p = primary(s.school), b = bright(s.school), dp = deep(s.school);
        matrices.push();
        matrices.translate(0, 0.15F, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(s.yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(s.pitch));
        switch (s.school) {
            case FIRE -> {
                d.orb(0, 0, 0, 0.55F * z, age, b, p, 1F);
                for (int i = 0; i < 6; i++) {
                    float a = age * 0.5F + i * 1.05F;
                    d.cube(ENERGY, MathHelper.cos(a) * 0.3F * z, MathHelper.sin(a) * 0.3F * z, -0.1F, 0.18F * z, age * 11, i * 30, 0, argb(0.7F, p));
                }
            }
            case ICE -> {
                d.cube(ENERGY, 0, 0, 0, 0.26F * z, 0.26F * z, 1.0F * z, 0, 0, 45 + age * 6, argb(0.9F, p));
                d.cube(CORE, 0, 0, 0.1F * z, 0.13F * z, 0.13F * z, 0.8F * z, 0, 0, 45 + age * 6, argb(1F, b));
                for (int i = 0; i < 3; i++) {
                    float a = i * 2.09F + age * 0.3F;
                    d.cube(ENERGY, MathHelper.cos(a) * 0.22F * z, MathHelper.sin(a) * 0.22F * z, -0.15F * z, 0.1F * z, 0.1F * z, 0.45F * z,
                            0, 0, 45, argb(0.8F, b));
                }
                d.cube(ENERGY, 0, 0, 0, 0.7F * z, age * 3, age * 2, 0, argb(0.15F, p));
            }
            case LIGHTNING -> {
                d.orb(0, 0, 0, 0.5F * z, age * 2, b, p, 1F);
                int tick = (int) age;
                for (int i = 0; i < 7; i++) {
                    float yaw = rand(s.seed + tick, i) * 360F, pitch = rand(s.seed + tick, i + 9) * 180F - 90F;
                    float len = (0.4F + 0.5F * rand(s.seed + tick, i + 20)) * z;
                    d.m.push();
                    d.m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
                    d.m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
                    d.cube(CORE, 0, 0, len / 2F, 0.05F * z, 0.05F * z, len, 0, 0, 0, argb(1F, b));
                    d.m.pop();
                }
                d.tiltedRing(CORE, 0, 0.42F * z, 14, 0.06F * z, 90F, age * 25, 0, argb(1F, dp));
            }
            case NATURE -> {
                d.cube(CORE, 0, 0, 0.1F * z, 0.12F * z, 0.12F * z, 0.75F * z, 0, 0, age * 8, argb(1F, b));
                d.cube(ENERGY, 0, 0, 0, 0.22F * z, 0.22F * z, 0.55F * z, 0, 0, age * 8, argb(0.8F, p));
                for (int i = 0; i < 4; i++) {
                    float a = age * 0.4F + i * MathHelper.HALF_PI;
                    d.cube(ENERGY, MathHelper.cos(a) * 0.35F * z, MathHelper.sin(a) * 0.35F * z, 0, 0.22F * z, 0.03F * z, 0.12F * z,
                            a * 57, 30, 0, argb(0.9F, dp));
                }
            }
            case VOID -> {
                for (int k = 0; k < 3; k++) d.cube(CORE, 0, 0, 0, 0.32F * z, age * 7, age * 5 + k * 40, k * 30, argb(1F, 0x10001C));
                d.cube(ENERGY, 0, 0, 0, 0.48F * z, -age * 5, age * 3, 0, argb(0.6F, p));
                d.tiltedRing(CORE, 0, 0.45F * z, 18, 0.07F * z, 70F, age * 18, 0, argb(1F, b));
            }
        }
        // Traînée : copies qui s'estompent derrière le projectile.
        for (int i = 1; i <= 9; i++) {
            float k = 1F - i / 10F;
            int col = i < 3 ? b : (i < 6 ? p : dp);
            d.cube(i < 3 ? CORE : ENERGY, 0, 0, -i * 0.24F * z, (0.12F + 0.32F * k) * z, age * 9 + i * 25, age * 6, 0, argb(0.7F * k, col));
        }
        matrices.pop();
        super.render(s, matrices, queue, camera);
    }
}
