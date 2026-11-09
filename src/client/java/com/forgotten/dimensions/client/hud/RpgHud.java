package com.forgotten.dimensions.client.hud;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.magic.ManaState;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.progression.ExperienceCurve;
import com.forgotten.dimensions.progression.PlayerProgress;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/**
 * Panneau RPG compact en haut à gauche : niveau, expérience, mana, sort actif (avec recharge)
 * et points de compétence disponibles. La vie et les effets restent ceux de Minecraft.
 */
public final class RpgHud {
    private static final int W = 112;

    private RpgHud() {}

    public static void register() {
        HudElementRegistry.addLast(ForgottenDimensions.id("rpg_panel"), RpgHud::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) return;
        TextRenderer tr = client.textRenderer;
        PlayerProgress p = ClientData.progress();
        ManaState mana = ClientData.mana();
        int x = 4, y = 4;

        ctx.fill(x - 2, y - 2, x + W + 2, y + 44, 0x88000000);
        ctx.drawTextWithShadow(tr, Text.translatable("hud.forgotten_dimensions.level", p.level()), x, y, 0xFFFFD84A);
        if (p.skillPoints() > 0) {
            Text pts = Text.translatable("hud.forgotten_dimensions.points", p.skillPoints());
            ctx.drawTextWithShadow(tr, pts, x + W - tr.getWidth(pts), y, 0xFF7CFC6A);
        }
        // Expérience
        int needed = ExperienceCurve.xpToNext(p.level());
        float xpFrac = p.level() >= PlayerProgress.MAX_LEVEL ? 1F : Math.min(1F, p.xp() / (float) needed);
        bar(ctx, x, y + 11, W, 4, xpFrac, 0xFF3A2E05, 0xFFE8C030);
        // Mana
        float manaFrac = mana.max() <= 0 ? 0 : mana.current() / mana.max();
        bar(ctx, x, y + 18, W, 7, manaFrac, 0xFF0A1A40, 0xFF3C8CFF);
        String manaText = Math.round(mana.current()) + " / " + Math.round(mana.max());
        ctx.drawTextWithShadow(tr, manaText, x + (W - tr.getWidth(manaText)) / 2, y + 18, 0xFFFFFFFF);
        // Sort actif
        Spell spell = Spells.get(p.selectedSpell());
        if (spell != null) {
            long cd = ClientData.cooldownRemaining(spell.id());
            Text name = Text.translatable(spell.translationKey());
            int color = cd > 0 ? 0xFF888888 : (0xFF000000 | spell.school().color);
            ctx.drawTextWithShadow(tr, name, x, y + 30, color);
            if (cd > 0) {
                String s = String.format("%.1fs", cd / 20.0);
                ctx.drawTextWithShadow(tr, s, x + W - tr.getWidth(s), y + 30, 0xFFFF6060);
            }
        }
    }

    private static void bar(DrawContext ctx, int x, int y, int w, int h, float frac, int bg, int fg) {
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + Math.round(w * Math.max(0, Math.min(1, frac))), y + h, fg);
        ctx.drawStrokedRectangle(x - 1, y - 1, w + 2, h + 2, 0xFF000000);
    }
}
