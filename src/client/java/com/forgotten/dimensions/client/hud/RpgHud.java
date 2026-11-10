package com.forgotten.dimensions.client.hud;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.item.MagicWeaponItem;
import com.forgotten.dimensions.magic.ManaState;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.progression.ExperienceCurve;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface RPG :
 * <ul>
 *   <li>cadre du personnage (haut gauche) : portrait, badge de niveau, nom et spécialisation,
 *   barres chiffrées de vie, de mana et d'expérience, points de compétence à dépenser ;</li>
 *   <li>barre de sorts façon MMO (bas droite) : les sorts débloqués du grimoire actif autour du sort
 *   sélectionné, icône de leur école, recharge en volet sombre avec secondes restantes, grimoire
 *   actif (attaque ou invocation) et rappel des touches.</li>
 * </ul>
 */
public final class RpgHud {
    private static final int GOLD = 0xFFD8B04A;
    private static final int GOLD_DARK = 0xFF6A5220;
    private static final int SLOT = 20;

    private RpgHud() {}

    public static void register() {
        HudElementRegistry.addLast(ForgottenDimensions.id("rpg_panel"), RpgHud::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) return;
        if (!com.forgotten.dimensions.util.config.FdConfig.get().rpgHud) return;
        PlayerProgress p = ClientData.progress();
        unitFrame(ctx, client, p);
        spellBar(ctx, client, p);
    }

    // ------------------------------------------------------------------ cadre du personnage

    private static void unitFrame(DrawContext ctx, MinecraftClient client, PlayerProgress p) {
        TextRenderer tr = client.textRenderer;
        ManaState mana = ClientData.mana();
        int x = 4, y = 4, w = 156, h = 48;
        frame(ctx, x, y, w, h);
        // Portrait
        int px = x + 4, py = y + 4;
        ctx.fill(px - 1, py - 1, px + 33, py + 33, GOLD);
        ctx.fill(px, py, px + 32, py + 32, 0xFF000000);
        PlayerSkinDrawer.draw(ctx, client.player.getSkin(), px, py, 32);
        // Badge de niveau
        String lv = String.valueOf(p.level());
        int bw = Math.max(14, tr.getWidth(lv) + 6);
        int bx = px - 2, by = py + 25;
        ctx.fill(bx - 1, by - 1, bx + bw + 1, by + 11, GOLD);
        ctx.fill(bx, by, bx + bw, by + 10, 0xFF2A1A06);
        ctx.drawText(tr, lv, bx + (bw - tr.getWidth(lv)) / 2 + 1, by + 1, 0xFFFFE070, false);
        // Nom et spécialisation
        int tx = x + 42;
        ctx.drawTextWithShadow(tr, client.player.getName(), tx, y + 4, 0xFFFFFFFF);
        SkillTree spec = SkillTree.byId(p.specialization());
        if (spec != null) {
            Text st = Text.translatable(spec.translationKey());
            ctx.drawTextWithShadow(tr, st, x + w - 4 - tr.getWidth(st), y + 4, 0xFFC8A0FF);
        } else if (p.skillPoints() > 0) {
            boolean blink = (client.player.age / 10) % 2 == 0;
            Text pts = Text.translatable("hud.forgotten_dimensions.points", p.skillPoints());
            ctx.drawTextWithShadow(tr, pts, x + w - 4 - tr.getWidth(pts), y + 4, blink ? 0xFF7CFC6A : 0xFF3C9C3A);
        }
        int bwBar = w - 46;
        // Vie
        float hp = client.player.getHealth() + client.player.getAbsorptionAmount();
        float maxHp = client.player.getMaxHealth();
        bar(ctx, tr, tx, y + 15, bwBar, 9, hp / Math.max(1F, maxHp), 0xFF3A0808, 0xFFB01818, 0xFFE84848,
                Math.round(hp) + " / " + Math.round(maxHp));
        // Mana
        bar(ctx, tr, tx, y + 26, bwBar, 9, mana.max() <= 0 ? 0 : mana.current() / mana.max(), 0xFF081838, 0xFF1A50C0, 0xFF4C9CFF,
                Math.round(mana.current()) + " / " + Math.round(mana.max()));
        // Expérience
        int needed = ExperienceCurve.xpToNext(p.level());
        float xpFrac = p.level() >= PlayerProgress.MAX_LEVEL ? 1F : Math.min(1F, p.xp() / (float) needed);
        bar(ctx, tr, tx, y + 38, bwBar, 5, xpFrac, 0xFF2A2005, 0xFFB08A18, 0xFFF0C838, null);
        if (spec != null && p.skillPoints() > 0) {
            Text pts = Text.translatable("hud.forgotten_dimensions.points", p.skillPoints());
            ctx.drawTextWithShadow(tr, pts, x + 2, y + h + 3, 0xFF7CFC6A);
        }
    }

    // ------------------------------------------------------------------ barre de sorts

    private static void spellBar(DrawContext ctx, MinecraftClient client, PlayerProgress p) {
        TextRenderer tr = client.textRenderer;
        Spell selected = Spells.get(p.selectedSpell());
        boolean summonMode = selected != null && selected.isSummon();
        List<Spell> unlocked = new ArrayList<>();
        for (Spell s : Spells.ofMode(summonMode)) if (SpellCaster.isUnlocked(p, s)) unlocked.add(s);
        if (selected == null && unlocked.isEmpty()) return;
        int index = Math.max(0, unlocked.indexOf(selected));
        int count = Math.min(7, unlocked.size());
        int start = MathHelper.clamp(index - count / 2, 0, Math.max(0, unlocked.size() - count));
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int barW = count * (SLOT + 2) + 4;
        // En bas à droite si la place le permet (à droite de la barre d'objets), sinon sous le cadre du personnage.
        boolean room = sw - barW - 6 > sw / 2 + 96;
        int x0 = room ? sw - barW - 4 : 4;
        int y0 = room ? sh - SLOT - 8 : 86;
        // Titre : grimoire actif et sort sélectionné
        Text mode = Text.translatable(summonMode ? "hud.forgotten_dimensions.mode_summon" : "hud.forgotten_dimensions.mode_attack");
        int labelY = y0 - 22;
        if (selected != null) {
            Text name = Text.translatable(selected.translationKey());
            ctx.drawTextWithShadow(tr, name, room ? Math.min(x0 + 2, sw - 4 - tr.getWidth(name)) : x0 + 2, labelY + 10,
                    0xFF000000 | selected.school().color);
        }
        ctx.drawTextWithShadow(tr, mode, room ? Math.min(x0 + 2, sw - 4 - tr.getWidth(mode)) : x0 + 2, labelY,
                summonMode ? 0xFFE0A0FF : 0xFFFFC870);
        frame(ctx, x0, y0 - 2, barW, SLOT + 6);
        long now = client.world == null ? 0 : client.world.getTime();
        float cdMult = SpellCaster.cooldownMultiplier(client.player, p);
        for (int i = 0; i < count; i++) {
            Spell s = unlocked.get(start + i);
            int sx = x0 + 3 + i * (SLOT + 2), sy = y0 + 1;
            boolean active = s == selected;
            int border = active ? 0xFFFFE070 : (0xFF000000 | s.school().color);
            ctx.fill(sx - 1, sy - 1, sx + SLOT + 1, sy + SLOT + 1, active ? border : darken(border));
            ctx.fill(sx, sy, sx + SLOT, sy + SLOT, active ? 0xFF2A2236 : 0xFF14121C);
            Item icon = ModItems.get(s.school().id() + "_spell_bolt");
            if (icon != null) ctx.drawItem(new ItemStack(icon), sx + 2, sy + 2);
            if (s.isSummon()) ctx.drawTextWithShadow(tr, "✦", sx + SLOT - 6, sy, 0xFFE0A0FF);
            long cd = ClientData.cooldownRemaining(s.id());
            if (cd > 0) {
                float total = Math.max(1F, s.cooldown() * cdMult);
                int hFill = Math.round(SLOT * MathHelper.clamp(cd / total, 0F, 1F));
                ctx.fill(sx, sy, sx + SLOT, sy + hFill, 0xB0000000);
                String sec = cd >= 200 ? String.valueOf(Math.round(cd / 20F)) : String.format(java.util.Locale.ROOT, "%.1f", cd / 20F);
                ctx.drawTextWithShadow(tr, sec, sx + (SLOT - tr.getWidth(sec)) / 2, sy + 6, 0xFFFFFFFF);
            } else if (ClientData.mana().current() < s.mana()) {
                ctx.fill(sx, sy, sx + SLOT, sy + SLOT, 0x702040A0);
            }
            if (active) {
                // Halo pulsant du sort sélectionné
                int a = (int) (90 + 70 * MathHelper.sin((client.player.age + now % 7) * 0.25F));
                ctx.drawStrokedRectangle(sx - 2, sy - 2, SLOT + 4, SLOT + 4, (a << 24) | 0xFFE070);
            }
        }
        // Rappel des touches
        boolean staff = client.player.getMainHandStack().getItem() instanceof MagicWeaponItem
                || client.player.getOffHandStack().getItem() instanceof MagicWeaponItem;
        Text hint = Text.translatable(staff ? "hud.forgotten_dimensions.hint_cast" : "hud.forgotten_dimensions.hint_staff");
        int hy = y0 + SLOT + 6;
        if (!room) ctx.drawTextWithShadow(tr, hint, x0 + 2, hy, staff ? 0xFFB8E0FF : 0xFF909090);
        else ctx.drawTextWithShadow(tr, hint, Math.min(x0 + 2, sw - 4 - tr.getWidth(hint)), labelY - 11, staff ? 0xFFB8E0FF : 0xFF909090);
    }

    // ------------------------------------------------------------------ outils de dessin

    /** Cadre doré à double liseré sur fond sombre dégradé. */
    private static void frame(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fillGradient(x, y, x + w, y + h, 0xD0141018, 0xD0262030);
        ctx.drawStrokedRectangle(x - 1, y - 1, w + 2, h + 2, 0xFF000000);
        ctx.drawStrokedRectangle(x, y, w, h, GOLD_DARK);
        ctx.fill(x, y, x + w, y + 1, GOLD);
        // coins ornés
        for (int[] c : new int[][]{{x, y}, {x + w - 3, y}, {x, y + h - 3}, {x + w - 3, y + h - 3}}) {
            ctx.fill(c[0], c[1], c[0] + 3, c[1] + 3, GOLD);
        }
    }

    private static void bar(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, float frac, int bg, int fg, int hi, String label) {
        frac = MathHelper.clamp(frac, 0F, 1F);
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        ctx.fill(x, y, x + w, y + h, bg);
        int fw = Math.round(w * frac);
        ctx.fillGradient(x, y, x + fw, y + h, hi, fg);
        ctx.fill(x, y, x + fw, y + 1, 0x60FFFFFF);
        if (label != null) {
            ctx.drawTextWithShadow(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2 + 1, 0xFFFFFFFF);
        }
    }

    private static int darken(int argb) {
        return 0xFF000000 | (((argb >> 16) & 0xFF) / 2) << 16 | (((argb >> 8) & 0xFF) / 2) << 8 | ((argb & 0xFF) / 2);
    }
}
