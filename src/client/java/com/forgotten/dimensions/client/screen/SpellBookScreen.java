package com.forgotten.dimensions.client.screen;

import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.network.packet.SelectSpellPayload;
import com.forgotten.dimensions.progression.PlayerProgress;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Livre de magie : les 55 sorts par école (invocations marquées ✦), état de déblocage, sélection du sort actif. */
public class SpellBookScreen extends Screen {
    private static SpellSchool selected = SpellSchool.FIRE;
    private PlayerProgress shown;

    public SpellBookScreen() {
        super(Text.translatable("screen.forgotten_dimensions.spell_book"));
    }

    @Override
    protected void init() {
        shown = ClientData.progress();
        int cx = width / 2;
        int top = height / 2 - 100;
        SpellSchool[] schools = SpellSchool.values();
        int tabW = 64;
        int tabsX = cx - (schools.length * (tabW + 2)) / 2;
        for (int i = 0; i < schools.length; i++) {
            SpellSchool s = schools[i];
            MutableText label = Text.translatable(s.translationKey()).formatted(s.formatting);
            if (s == selected) label = label.formatted(Formatting.UNDERLINE);
            addDrawableChild(ButtonWidget.builder(label, b -> {
                selected = s;
                clearAndInit();
            }).dimensions(tabsX + i * (tabW + 2), top + 24, tabW, 20).build());
        }
        List<Spell> spells = Spells.ofSchool(selected);
        for (int i = 0; i < spells.size(); i++) {
            Spell sp = spells.get(i);
            boolean unlocked = SpellCaster.isUnlocked(shown, sp);
            boolean active = sp.id().equals(shown.selectedSpell());
            MutableText label = Text.literal(active ? "▶ " : sp.isSummon() ? "✦ " : "").append(Text.translatable(sp.translationKey()));
            if (!unlocked) label = Text.literal("🔒 ").append(Text.translatable(sp.translationKey())).formatted(Formatting.DARK_GRAY);
            else if (active) label = label.formatted(Formatting.YELLOW);
            int bx = cx - 155 + (i % 2) * 160;
            int by = top + 52 + (i / 2) * 22;
            ButtonWidget btn = ButtonWidget.builder(label, b -> ClientPlayNetworking.send(new SelectSpellPayload(sp.id())))
                    .dimensions(bx, by, 150, 20).tooltip(Tooltip.of(tooltip(sp, unlocked))).build();
            btn.active = unlocked;
            addDrawableChild(btn);
        }
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
                .dimensions(cx - 50, top + 192, 100, 20).build());
    }

    private Text tooltip(Spell sp, boolean unlocked) {
        MutableText t = Text.translatable(sp.translationKey()).formatted(sp.school().formatting).append("\n")
                .append(Text.translatable(sp.descriptionKey()).formatted(Formatting.GRAY)).append("\n")
                .append(Text.translatable("screen.forgotten_dimensions.spell_stats", Math.round(sp.mana()),
                        String.format("%.1f", sp.cooldown() / 20.0), sp.power() > 0 ? String.format("%.0f", sp.power()) : "—")
                        .formatted(Formatting.AQUA));
        if (!unlocked) {
            t.append("\n").append(Text.translatable("message.forgotten_dimensions.spell.level", sp.level()).formatted(Formatting.RED));
            if (!sp.requiredSkill().isEmpty()) {
                t.append("\n").append(Text.translatable("message.forgotten_dimensions.spell.skill",
                        Text.translatable("skill.forgotten_dimensions." + sp.requiredSkill())).formatted(Formatting.RED));
            }
        }
        return t;
    }

    @Override
    public void tick() {
        super.tick();
        if (!ClientData.progress().equals(shown)) clearAndInit();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = width / 2;
        int top = height / 2 - 100;
        ctx.drawCenteredTextWithShadow(textRenderer, title.copy().formatted(Formatting.LIGHT_PURPLE), cx, top, 0xFFFFFFFF);
        Text mastery = Text.translatable("screen.forgotten_dimensions.mastery", Text.translatable(selected.translationKey()),
                shown.schoolMastery(selected.id()), shown.schoolCasts().getOrDefault(selected.id(), 0));
        ctx.drawCenteredTextWithShadow(textRenderer, mastery, cx, top + 12, 0xFF000000 | selected.color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
