package com.forgotten.dimensions.client.screen;

import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.magic.ManaState;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.progression.ExperienceCurve;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.Skill;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.progression.Skills;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fiche de personnage (touche C) : le personnage en 3D, niveau et expérience, attributs de combat,
 * magie (mana, coûts, recharges, puissance et maîtrise par école) et points investis par arbre.
 */
public class CharacterScreen extends Screen {
    private static final int GOLD = 0xFFD8B04A;

    public CharacterScreen() {
        super(Text.translatable("screen.forgotten_dimensions.character"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
                .dimensions(width / 2 - 50, height / 2 + 100, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        PlayerEntity player = client.player;
        if (player == null) return;
        PlayerProgress p = ClientData.progress();
        int cx = width / 2, top = height / 2 - 108;
        int x0 = cx - 200, w = 400;
        panel(ctx, x0, top, w, 202);
        ctx.drawCenteredTextWithShadow(textRenderer, title.copy().formatted(Formatting.GOLD, Formatting.BOLD), cx, top + 6, 0xFFFFFFFF);

        // --- Colonne 1 : personnage 3D, niveau, expérience
        int c1 = x0 + 8;
        ctx.fill(c1, top + 20, c1 + 90, top + 128, 0x60000000);
        InventoryScreen.drawEntity(ctx, c1, top + 20, c1 + 90, top + 128, 44, 0.0625F, mouseX, mouseY, player);
        ctx.drawTextWithShadow(textRenderer, player.getName(), c1, top + 132, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.forgotten_dimensions.character.level", p.level()), c1, top + 143, 0xFFFFE070);
        SkillTree spec = SkillTree.byId(p.specialization());
        ctx.drawTextWithShadow(textRenderer, spec == null ? Text.translatable("screen.forgotten_dimensions.character.no_spec").formatted(Formatting.DARK_GRAY)
                : Text.translatable(spec.translationKey()).formatted(Formatting.LIGHT_PURPLE), c1, top + 154, 0xFFFFFFFF);
        int needed = ExperienceCurve.xpToNext(p.level());
        float frac = p.level() >= PlayerProgress.MAX_LEVEL ? 1F : Math.min(1F, p.xp() / (float) needed);
        bar(ctx, c1, top + 167, 90, 5, frac, 0xFFF0C838);
        ctx.drawTextWithShadow(textRenderer, p.level() >= PlayerProgress.MAX_LEVEL ? "MAX" : p.xp() + " / " + needed, c1, top + 175, 0xFFB8A070);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("hud.forgotten_dimensions.points", p.skillPoints()), c1, top + 187,
                p.skillPoints() > 0 ? 0xFF7CFC6A : 0xFF808080);

        // --- Colonne 2 : attributs
        int c2 = x0 + 108;
        heading(ctx, Text.translatable("screen.forgotten_dimensions.character.attributes"), c2, top + 22);
        int y = top + 34;
        y = row(ctx, c2, y, "health", fmt(player.getMaxHealth()), 0xFFFF6060);
        y = row(ctx, c2, y, "armor", fmt(attr(player, EntityAttributes.ARMOR)), 0xFFC8C8D8);
        y = row(ctx, c2, y, "toughness", fmt(attr(player, EntityAttributes.ARMOR_TOUGHNESS)), 0xFFC8C8D8);
        y = row(ctx, c2, y, "attack", fmt(attr(player, EntityAttributes.ATTACK_DAMAGE)), 0xFFFF9A40);
        y = row(ctx, c2, y, "attack_speed", fmt(attr(player, EntityAttributes.ATTACK_SPEED)), 0xFFFF9A40);
        y = row(ctx, c2, y, "speed", Math.round(attr(player, EntityAttributes.MOVEMENT_SPEED) / 0.1 * 100) + " %", 0xFF80E0FF);
        y = row(ctx, c2, y, "luck", fmt(attr(player, EntityAttributes.LUCK)), 0xFF7CFC6A);
        y += 6;
        heading(ctx, Text.translatable("screen.forgotten_dimensions.character.trees"), c2, y);
        y += 12;
        Map<SkillTree, Integer> spent = new EnumMap<>(SkillTree.class);
        for (Skill s : Skills.all().values()) spent.merge(s.tree(), Skills.rank(p, s), Integer::sum);
        for (SkillTree t : SkillTree.values()) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable(t.translationKey()), c2, y, t == spec ? 0xFFE0B0FF : 0xFFD0D0D0);
            String v = String.valueOf(spent.getOrDefault(t, 0));
            ctx.drawTextWithShadow(textRenderer, v, c2 + 130 - textRenderer.getWidth(v), y, 0xFFFFE070);
            y += 10;
        }

        // --- Colonne 3 : magie
        int c3 = x0 + 256;
        heading(ctx, Text.translatable("screen.forgotten_dimensions.character.magic"), c3, top + 22);
        ManaState mana = ClientData.mana();
        y = top + 34;
        y = row(ctx, c3, y, "mana", Math.round(mana.max()) + "", 0xFF4C9CFF);
        y = row(ctx, c3, y, "mana_cost", Math.round(SpellCaster.manaCostMultiplier(player, p) * 100) + " %", 0xFF4C9CFF);
        y = row(ctx, c3, y, "cooldown", Math.round(SpellCaster.cooldownMultiplier(player, p) * 100) + " %", 0xFF4C9CFF);
        y += 4;
        for (SpellSchool s : SpellSchool.values()) {
            int mastery = p.schoolMastery(s.id());
            float power = SpellCaster.powerMultiplier(player, p, s);
            ctx.drawTextWithShadow(textRenderer, Text.translatable(s.translationKey()), c3, y, 0xFF000000 | s.color);
            String pw = "×" + String.format(java.util.Locale.ROOT, "%.2f", power);
            ctx.drawTextWithShadow(textRenderer, pw, c3 + 134 - textRenderer.getWidth(pw), y, 0xFFFFFFFF);
            bar(ctx, c3, y + 10, 134, 3, mastery / 10F, 0xFF000000 | s.color);
            y += 17;
        }
        ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.forgotten_dimensions.character.mastery_hint").formatted(Formatting.DARK_GRAY),
                c3, y, 0xFF808080);
    }

    private int row(DrawContext ctx, int x, int y, String key, String value, int color) {
        ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.forgotten_dimensions.character." + key), x, y, 0xFFD0D0D0);
        ctx.drawTextWithShadow(textRenderer, value, x + 134 - textRenderer.getWidth(value), y, color);
        return y + 11;
    }

    private void heading(DrawContext ctx, Text t, int x, int y) {
        ctx.drawTextWithShadow(textRenderer, t.copy().formatted(Formatting.GOLD), x, y, 0xFFFFFFFF);
        ctx.fill(x, y + 9, x + 134, y + 10, 0xFF6A5220);
    }

    private static void panel(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fillGradient(x, y, x + w, y + h, 0xE0141018, 0xE0262030);
        ctx.drawStrokedRectangle(x - 1, y - 1, w + 2, h + 2, 0xFF000000);
        ctx.drawStrokedRectangle(x, y, w, h, 0xFF6A5220);
        ctx.fill(x, y, x + w, y + 1, GOLD);
        for (int[] c : new int[][]{{x, y}, {x + w - 4, y}, {x, y + h - 4}, {x + w - 4, y + h - 4}}) {
            ctx.fill(c[0], c[1], c[0] + 4, c[1] + 4, GOLD);
        }
    }

    private static void bar(DrawContext ctx, int x, int y, int w, int h, float frac, int color) {
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        ctx.fill(x, y, x + w, y + h, 0xFF202020);
        ctx.fill(x, y, x + Math.round(w * MathHelper.clamp(frac, 0F, 1F)), y + h, color);
    }

    private static double attr(PlayerEntity p, RegistryEntry<EntityAttribute> a) {
        var inst = p.getAttributeInstance(a);
        return inst == null ? 0 : inst.getValue();
    }

    private static String fmt(double v) {
        return Math.abs(v - Math.round(v)) < 0.05 ? String.valueOf(Math.round(v)) : String.format(java.util.Locale.ROOT, "%.1f", v);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
