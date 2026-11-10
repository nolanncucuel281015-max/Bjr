package com.forgotten.dimensions.client.render.spell;

import com.forgotten.dimensions.entity.SpellEffectEntity;
import com.forgotten.dimensions.magic.SpellSchool;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import static com.forgotten.dimensions.client.render.spell.SpellFxDraw.*;

/** Dessine les 13 effets 3D des sorts (voir SpellVisual). */
public class SpellEffectRenderer extends EntityRenderer<SpellEffectEntity, SpellEffectRenderState> {
    private final SpellFxDraw d;

    public SpellEffectRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.d = new SpellFxDraw(ctx.getPart(SpellFxDraw.LAYER));
        this.shadowRadius = 0F;
    }

    @Override
    public SpellEffectRenderState createRenderState() {
        return new SpellEffectRenderState();
    }

    @Override
    public void updateRenderState(SpellEffectEntity e, SpellEffectRenderState s, float tickDelta) {
        super.updateRenderState(e, s, tickDelta);
        s.kind = e.kind();
        s.school = e.school();
        s.radius = e.radius();
        s.duration = e.duration();
        s.tx = e.target().x();
        s.ty = e.target().y();
        s.tz = e.target().z();
        s.seed = e.getId();
    }

    @Override
    public boolean shouldRender(SpellEffectEntity entity, Frustum frustum, double x, double y, double z) {
        return true; // effets bien plus grands que leur boîte : on ne les élague pas
    }

    @Override
    public void render(SpellEffectRenderState s, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState camera) {
        d.m = matrices;
        d.q = queue;
        float age = s.age;
        float t = MathHelper.clamp(age / s.duration, 0F, 1F);
        float a = fade(age, s.duration);
        int p = primary(s.school), b = bright(s.school), dp = deep(s.school);
        switch (s.kind) {
            case CAST_CIRCLE -> castCircle(s, age, t, a, p, b);
            case NOVA -> nova(s, age, t, p, b);
            case EXPLOSION -> explosion(s, age, t, p, b, dp);
            case PILLAR -> pillar(s, age, t, a, p, b);
            case METEOR -> meteor(s, age, t, p, b, dp);
            case SPIKES -> spikes(s, age, t, a, p, b, dp);
            case ZONE -> zone(s, age, a, p, b);
            case BEAM -> beam(s, age, t, a, p, b);
            case BARRIER -> barrier(s, age, a, p, b);
            case SINGULARITY -> singularity(s, age, t, a, p, b);
            case HEAL -> heal(s, age, t, a, p, b);
            case BLINK -> blink(s, age, t, a, p, b);
            case SUMMON -> summon(s, age, t, a, p, b);
        }
        super.render(s, matrices, queue, camera);
    }

    // ------------------------------------------------------------------ effets

    private void castCircle(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        float r = s.radius * (0.55F + 0.45F * easeOut(t * 3F));
        d.disc(0.02F, r, age * 6F, argb(a * 0.95F, p));
        d.disc(0.04F, r * 0.62F, -age * 9F, argb(a, b));
        for (int k = 0; k < 2; k++) {
            float tt = MathHelper.clamp(t * 1.4F - k * 0.3F, 0F, 1F);
            d.ring(ENERGY, tt * 2.4F, r * (1F - 0.55F * tt), 18, 0.08F, 0.08F, age * 10F + k * 40, argb(a * (1F - tt), b));
        }
        for (int i = 0; i < 10; i++) {
            float ang = i * 0.628F + age * 0.18F;
            float y = ((age * 0.06F + rand(s.seed, i)) % 1F) * 2.2F;
            d.cube(CORE, MathHelper.cos(ang) * r * 0.8F, y, MathHelper.sin(ang) * r * 0.8F, 0.09F, age * 9, age * 7, 0,
                    argb(a * (1F - y / 2.2F), b));
        }
    }

    private void nova(SpellEffectRenderState s, float age, float t, int p, int b) {
        float rr = s.radius * easeOut(t);
        float al = (1F - t);
        d.disc(0.05F, rr, age * 4F, argb(al * 0.8F, p));
        d.ring(ENERGY, 0.35F, rr, 44, 0.3F, 0.55F * al + 0.15F, age * 2, argb(al, p));
        d.ring(CORE, 0.35F, rr * 0.92F, 36, 0.12F, 0.25F * al + 0.05F, -age * 3, argb(al, b));
        d.ring(ENERGY, 0.9F * al, rr * 0.75F, 28, 0.2F, 0.15F, age * 5, argb(al * 0.6F, b));
        for (int i = 0; i < 10; i++) {
            float ang = (float) (i * Math.PI / 5) + s.seed;
            float h = 2.4F * al * (0.6F + 0.4F * rand(s.seed, i));
            d.cube(CORE, MathHelper.cos(ang) * rr, h / 2F, MathHelper.sin(ang) * rr, 0.16F, h, 0.16F, age * 8, 0, 0, argb(al, b));
        }
    }

    private void explosion(SpellEffectRenderState s, float age, float t, int p, int b, int dp) {
        float g = easeOut(Math.min(1F, t * 1.8F));
        float size = s.radius * 2F * g;
        float al = (float) Math.pow(1F - t, 0.7);
        d.orb(0, 0.3F, 0, Math.min(2.6F, size * 0.35F), age, b, p, al);
        int n = 12;
        for (int i = 0; i < n; i++) {
            float yaw = i * 137.5F + s.seed * 7;
            float pitch = (float) Math.toDegrees(Math.asin(1 - 2 * (i + 0.5F) / n));
            float len = size * (0.6F + 0.5F * rand(s.seed, i)) * (0.4F + 0.6F * g);
            d.m.push();
            d.m.translate(0, 0.3F, 0);
            d.m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
            d.m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(pitch));
            d.cube(ENERGY, 0, 0, len / 2F, 0.11F * s.radius, 0.11F * s.radius, len, 0, 0, age * 12, argb(al * 0.8F, p));
            d.cube(CORE, 0, 0, len * 0.45F, 0.045F * s.radius, 0.045F * s.radius, len * 0.9F, 0, 0, age * 12, argb(al, b));
            d.m.pop();
        }
        d.ring(ENERGY, 0.15F, size * 0.75F, 30, 0.25F, 0.12F, age * 6, argb(al * 0.8F, p));
    }

    private void pillar(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        float h = 14F * easeOut(Math.min(1F, t * 3F));
        float w = s.radius;
        d.cube(ENERGY, 0, h / 2F, 0, w * 0.28F, h, w * 0.28F, age * 8, 0, 0, argb(a, b));
        d.cube(ENERGY, 0, h / 2F, 0, w * 0.7F, h, w * 0.7F, 45F - age * 5, 0, 0, argb(a * 0.5F, p));
        for (int k = 0; k < 4; k++) d.ring(ENERGY, (age * 0.4F + k * 3.5F) % Math.max(0.5F, h), w * 1.3F, 18, 0.12F, 0.12F, age * 9, argb(a * 0.8F, p));
        for (int i = 0; i < 36; i++) {
            float y = ((i / 36F) * 14F + age * 0.35F) % Math.max(0.5F, h);
            float ang = i * 0.8F + age * 0.25F;
            d.cube(CORE, MathHelper.cos(ang) * w * 0.95F, y, MathHelper.sin(ang) * w * 0.95F, 0.14F, age * 6, 0, 0, argb(a, b));
        }
        d.disc(0.05F, w * 2.5F, age * 5F, argb(a, p));
        d.orb(0, h, 0, w * 1.2F, age, b, p, a * 0.8F);
    }

    private void meteor(SpellEffectRenderState s, float age, float t, int p, int b, int dp) {
        float tt = (float) Math.pow(t, 1.6);
        float x = s.tx * (1F - tt), y = s.ty * (1F - tt), z = s.tz * (1F - tt);
        float len = MathHelper.sqrt(s.tx * s.tx + s.ty * s.ty + s.tz * s.tz);
        float nx = s.tx / len, ny = s.ty / len, nz = s.tz / len;
        float rock = 1.4F + s.radius * 0.25F;
        for (int k = 0; k < 3; k++) d.cube(ROCK, x, y, z, rock * (1F - k * 0.15F), age * (6 + k), age * (4 + k * 2), k * 40, argb(1F, 0xFFFFFF));
        for (int k = 0; k < 3; k++) d.cube(ENERGY, x, y, z, rock * 1.6F, -age * (5 + k), age * 3 + k * 50, k * 25, argb(0.55F, p));
        d.cube(ENERGY, x, y, z, rock * 2.4F, age * 2, age, 0, argb(0.2F, b));
        for (int i = 1; i <= 14; i++) {
            float off = i * 0.9F;
            float k = 1F - i / 15F;
            d.cube(i < 5 ? CORE : ENERGY, x + nx * off, y + ny * off, z + nz * off, rock * (0.3F + 1.2F * k),
                    age * 9 + i * 20, age * 7, 0, argb(0.75F * k, i < 5 ? b : (i < 9 ? p : dp)));
        }
        float pulse = 0.6F + 0.4F * MathHelper.sin(age * 0.6F);
        d.disc(0.05F, s.radius * (0.4F + 0.6F * t), age * 3F, argb(pulse, p));
        d.ring(ENERGY, 0.1F, s.radius * (0.4F + 0.6F * t), 30, 0.15F, 0.1F, -age * 4, argb(pulse, b));
    }

    private void spikes(SpellEffectRenderState s, float age, float t, float a, int p, int b, int dp) {
        int n = Math.min(26, 6 + (int) (s.radius * 4));
        float rise = MathHelper.clamp(t / 0.15F, 0F, 1F) * MathHelper.clamp((1F - t) / 0.25F, 0F, 1F);
        boolean crystal = s.school == SpellSchool.ICE || s.school == SpellSchool.VOID;
        for (int i = 0; i < n; i++) {
            float ang = rand(s.seed, i) * MathHelper.TAU;
            float dist = s.radius * MathHelper.sqrt(rand(s.seed, i + 100));
            float hMax = 1.8F + 3.2F * rand(s.seed, i + 200) * (1F - dist / (s.radius + 0.01F) * 0.4F);
            float h = hMax * easeOut(rise);
            float x = MathHelper.cos(ang) * dist, z = MathHelper.sin(ang) * dist;
            float yaw = -ang * MathHelper.DEGREES_PER_RADIAN;
            float tilt = 12F + 18F * rand(s.seed, i + 300);
            d.m.push();
            d.m.translate(x, 0, z);
            d.m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
            d.m.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(tilt));
            float w = crystal ? 0.55F : 0.3F;
            d.cube(crystal ? ENERGY : CORE, 0, h / 2F, 0, w, h, w, 45, 0, 0, argb(a * (crystal ? 0.85F : 1F), crystal ? p : dp));
            d.cube(CORE, 0, h * 0.62F, 0, w * 0.55F, h * 0.75F, w * 0.55F, 0, 0, 0, argb(a, b));
            d.cube(CORE, 0, h, 0, w * 0.5F, w * 1.2F, w * 0.5F, 45, 0, 45, argb(a, b));
            d.m.pop();
        }
        d.disc(0.04F, s.radius * 1.1F, age * 2F, argb(a * 0.7F, p));
    }

    private void zone(SpellEffectRenderState s, float age, float a, int p, int b) {
        float r = s.radius;
        float pulse = 0.75F + 0.25F * MathHelper.sin(age * 0.25F);
        d.disc(0.03F, r, age * 1.5F, argb(a * pulse, p));
        d.disc(0.05F, r * 0.55F, -age * 3F, argb(a * 0.9F, b));
        int n = Math.max(20, (int) (r * 8));
        for (int i = 0; i < n; i++) {
            float ang = (float) (2 * Math.PI * i / n) + age * 0.01F;
            float h = 0.5F + 0.9F * (0.5F + 0.5F * MathHelper.sin(age * 0.35F + i * 1.7F));
            d.cube(ENERGY, MathHelper.cos(ang) * r, h / 2F, MathHelper.sin(ang) * r, 0.18F, h, 0.18F, age * 4 + i * 20, 0, 0,
                    argb(a * 0.6F, p));
        }
        d.ring(ENERGY, 1.2F + 0.3F * MathHelper.sin(age * 0.2F), r * 0.8F, 26, 0.18F, 0.18F, age * 12, argb(a * 0.55F, b));
        d.ring(ENERGY, 2.0F + 0.3F * MathHelper.sin(age * 0.17F), r * 0.55F, 20, 0.14F, 0.14F, -age * 16, argb(a * 0.45F, p));
        int motes = 40;
        for (int i = 0; i < motes; i++) {
            float y = ((age * 0.035F + rand(s.seed, i)) % 1F) * 3.2F;
            float ang = rand(s.seed, i + 50) * MathHelper.TAU + age * 0.05F;
            float dist = r * MathHelper.sqrt(rand(s.seed, i + 90));
            d.cube(CORE, MathHelper.cos(ang) * dist, y, MathHelper.sin(ang) * dist, 0.17F, age * 7, age * 5, 0,
                    argb(a * (1F - y / 3.2F), b));
        }
    }

    private void beam(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        float len = MathHelper.sqrt(s.tx * s.tx + s.ty * s.ty + s.tz * s.tz);
        if (len < 0.1F) return;
        float w = s.radius * (1F - 0.6F * t);
        d.beam(CORE, s.tx, s.ty, s.tz, w * 0.35F, age * 25, argb(a, b));
        d.beam(ENERGY, s.tx, s.ty, s.tz, w, -age * 12, argb(a * 0.5F, p));
        d.beam(ENERGY, s.tx, s.ty, s.tz, w * 1.8F, age * 6, argb(a * 0.15F, p));
        float ux = s.tx / len, uy = s.ty / len, uz = s.tz / len;
        // repère perpendiculaire pour l'hélice
        float px = -uz, pz = ux, py = 0;
        float pl = MathHelper.sqrt(px * px + pz * pz);
        if (pl < 0.01F) { px = 1; pz = 0; pl = 1; }
        px /= pl; pz /= pl;
        float qx = uy * pz - uz * py, qy = uz * px - ux * pz, qz = ux * py - uy * px;
        int n = Math.min(120, (int) (len * 5));
        for (int i = 0; i < n; i++) {
            float along = i / (float) n * len;
            float ang = along * 1.6F + age * 0.7F;
            float r = w * 0.9F;
            for (int k = 0; k < 2; k++) {
                float an = ang + k * MathHelper.PI;
                float ox = (MathHelper.cos(an) * px + MathHelper.sin(an) * qx) * r;
                float oy = (MathHelper.cos(an) * py + MathHelper.sin(an) * qy) * r;
                float oz = (MathHelper.cos(an) * pz + MathHelper.sin(an) * qz) * r;
                d.cube(CORE, ux * along + ox, uy * along + oy, uz * along + oz, 0.07F, age * 9, 0, 0, argb(a, b));
            }
        }
        d.orb(s.tx, s.ty, s.tz, w * 2.2F, age, b, p, a);
    }

    private void barrier(SpellEffectRenderState s, float age, float a, int p, int b) {
        float r = s.radius;
        for (int k = 0; k < 3; k++) {
            d.tiltedRing(CORE, 1.0F, r, 30, 0.13F, 55F * k + 20F, age * (4 + k * 2), age * (1.5F + k), argb(a, k == 1 ? b : p));
        }
        // bouclier cylindrique : panneaux runiques qui scintillent en vagues
        int n = 18;
        float seg = (float) (2 * Math.PI * r / n) * 0.92F;
        for (int i = 0; i < n; i++) {
            float ang = (float) (2 * Math.PI * i / n) + age * 0.03F;
            float wave = 0.5F + 0.5F * MathHelper.sin(age * 0.25F + i * 0.9F);
            d.cube(ENERGY, MathHelper.cos(ang) * r, 1.1F, MathHelper.sin(ang) * r, seg, 2.2F, 0.05F,
                    -ang * MathHelper.DEGREES_PER_RADIAN + 90F, 0, 0, argb(a * (0.12F + 0.25F * wave), p));
        }
        d.disc(0.03F, r * 1.1F, age * 3F, argb(a * 0.8F, p));
    }

    private void singularity(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        float g = easeOut(Math.min(1F, t * 2.2F));
        float r = s.radius * g;
        for (int k = 0; k < 3; k++) d.cube(CORE, 0, 0, 0, r * 0.75F, age * (9 + k), age * 6 + k * 40, k * 30, argb(a, 0x0A0012));
        d.cube(ENERGY, 0, 0, 0, r * 1.05F, -age * 5, age * 3, 20, argb(a * 0.7F, p));
        d.tiltedRing(CORE, 0, r * 1.5F, 40, 0.18F * Math.max(1F, r * 0.5F), 18F, age * 14, 0, argb(a, b));
        d.tiltedRing(ENERGY, 0, r * 1.9F, 48, 0.28F * Math.max(1F, r * 0.5F), 18F, -age * 9, 0, argb(a * 0.7F, p));
        for (int i = 0; i < 16; i++) {
            float ph = (age * 0.04F + rand(s.seed, i)) % 1F;
            float dist = r * (3.2F - 2.8F * ph);
            float ang = rand(s.seed, i + 30) * MathHelper.TAU + ph * 4F;
            d.cube(CORE, MathHelper.cos(ang) * dist, (rand(s.seed, i + 60) - 0.5F) * r, MathHelper.sin(ang) * dist, 0.12F,
                    age * 9, 0, 0, argb(a * ph, b));
        }
    }

    private void heal(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        for (int i = 0; i < 18; i++) {
            float y = ((age * 0.05F + i / 18F) % 1F) * 2.4F;
            float ang = i * 0.9F + age * 0.2F;
            d.cube(CORE, MathHelper.cos(ang) * 0.7F, y, MathHelper.sin(ang) * 0.7F, 0.11F, age * 8, age * 6, 0, argb(a * (1F - y / 2.4F), b));
        }
        d.cube(ENERGY, 0, 1.1F, 0, 0.9F, 2.2F, 0.9F, age * 4, 0, 0, argb(a * 0.25F, p));
        d.ring(ENERGY, 0.1F, 0.6F + t * 0.8F, 20, 0.1F, 0.1F, age * 6, argb(a * (1F - t), b));
        d.disc(0.03F, 1.0F, age * 4F, argb(a * 0.7F, p));
    }

    private void blink(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        for (int k = 0; k < 7; k++) {
            float y = k * 0.38F;
            float r = s.radius * (1F - t) * (1F - k * 0.08F);
            d.ring(ENERGY, y, r, 16, 0.1F, 0.08F, age * (20 + k * 6), argb(a * (1F - t), k % 2 == 0 ? p : b));
        }
        d.cube(CORE, 0, 1.3F, 0, 0.12F, 2.6F * (1F - t * 0.5F), 0.12F, age * 10, 0, 0, argb(a, b));
    }

    private void summon(SpellEffectRenderState s, float age, float t, float a, int p, int b) {
        d.disc(0.03F, s.radius, age * 5F, argb(a, p));
        d.disc(0.05F, s.radius * 0.6F, -age * 7F, argb(a, b));
        float h = 7F * (1F - t);
        d.cube(CORE, 0, h / 2F, 0, 0.5F, h, 0.5F, age * 10, 0, 0, argb(a, b));
        d.cube(ENERGY, 0, h / 2F, 0, 1.2F, h, 1.2F, -age * 6, 0, 0, argb(a * 0.35F, p));
        d.ring(ENERGY, 0.2F, s.radius * easeOut(t), 30, 0.2F, 0.3F, age * 4, argb(a * (1F - t), b));
    }
}
