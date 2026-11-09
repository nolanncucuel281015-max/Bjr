package com.forgotten.dimensions.client.screen;

import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.network.packet.ChooseSpecializationPayload;
import com.forgotten.dimensions.network.packet.UpgradeSkillPayload;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.Skill;
import com.forgotten.dimensions.progression.SkillTree;
import com.forgotten.dimensions.progression.Skills;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Arbre de compétences : 5 onglets, 6 compétences par arbre, choix de la spécialisation. */
public class SkillTreeScreen extends Screen {
    private static SkillTree selected = SkillTree.WARRIOR;
    private PlayerProgress shown;

    public SkillTreeScreen() {
        super(Text.translatable("screen.forgotten_dimensions.skills"));
    }

    @Override
    protected void init() {
        shown = ClientData.progress();
        int cx = width / 2;
        int top = height / 2 - 95;
        SkillTree[] trees = SkillTree.values();
        int tabW = 70;
        int tabsX = cx - (trees.length * (tabW + 2)) / 2;
        for (int i = 0; i < trees.length; i++) {
            SkillTree t = trees[i];
            MutableText label = Text.translatable(t.translationKey());
            if (t == selected) label = label.formatted(Formatting.YELLOW, Formatting.UNDERLINE);
            addDrawableChild(ButtonWidget.builder(label, b -> {
                selected = t;
                clearAndInit();
            }).dimensions(tabsX + i * (tabW + 2), top + 24, tabW, 20).build());
        }
        for (Skill s : Skills.ofTree(selected)) {
            int rank = shown.skillRank(s.id());
            String reason = Skills.canUpgrade(shown, s);
            MutableText label = Text.translatable(s.translationKey()).append(" " + rank + "/" + s.maxRank());
            if (rank >= s.maxRank()) label = label.formatted(Formatting.GOLD);
            int bx = cx - 155 + s.column() * 160;
            int by = top + 56 + s.row() * 40;
            ButtonWidget btn = ButtonWidget.builder(label, b -> ClientPlayNetworking.send(new UpgradeSkillPayload(s.id())))
                    .dimensions(bx, by, 150, 20).tooltip(Tooltip.of(tooltip(s, reason))).build();
            btn.active = reason == null;
            addDrawableChild(btn);
        }
        boolean canSpec = shown.specialization().isEmpty() && shown.level() >= ProgressionManager.SPECIALIZATION_LEVEL;
        ButtonWidget spec = ButtonWidget.builder(Text.translatable("screen.forgotten_dimensions.choose_spec",
                        Text.translatable(selected.translationKey())),
                b -> ClientPlayNetworking.send(new ChooseSpecializationPayload(selected.id())))
                .dimensions(cx - 100, top + 180, 200, 20)
                .tooltip(Tooltip.of(Text.translatable("spec.forgotten_dimensions." + selected.id() + ".desc")))
                .build();
        spec.active = canSpec;
        spec.visible = shown.specialization().isEmpty();
        addDrawableChild(spec);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
                .dimensions(cx - 50, top + 206, 100, 20).build());
    }

    private static Text tooltip(Skill s, String reason) {
        MutableText t = Text.translatable(s.translationKey()).formatted(Formatting.YELLOW).append("\n")
                .append(Text.translatable(s.translationKey() + ".desc").formatted(Formatting.GRAY));
        t.append("\n").append(Text.translatable("screen.forgotten_dimensions.requires_level", s.levelRequired()).formatted(Formatting.DARK_AQUA));
        if (!s.prerequisite().isEmpty()) {
            t.append("\n").append(Text.translatable("screen.forgotten_dimensions.requires_skill",
                    Text.translatable("skill.forgotten_dimensions." + s.prerequisite())).formatted(Formatting.DARK_AQUA));
        }
        if (reason != null) {
            t.append("\n").append(Text.translatable("message.forgotten_dimensions.skill." + reason).formatted(Formatting.RED));
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
        int top = height / 2 - 95;
        ctx.drawCenteredTextWithShadow(textRenderer, title.copy().formatted(Formatting.GOLD), cx, top, 0xFFFFFFFF);
        Text info = Text.translatable("screen.forgotten_dimensions.skills_info", shown.level(), shown.skillPoints(),
                shown.specialization().isEmpty() ? Text.translatable("screen.forgotten_dimensions.no_spec")
                        : Text.translatable("skill_tree.forgotten_dimensions." + shown.specialization()));
        ctx.drawCenteredTextWithShadow(textRenderer, info, cx, top + 12, 0xFFDDDDDD);
        // Liens entre compétences (prérequis) : traits verticaux discrets entre les rangées.
        for (Skill s : Skills.ofTree(selected)) {
            if (s.prerequisite().isEmpty()) continue;
            Skill pre = Skills.get(s.prerequisite());
            int x1 = cx - 155 + pre.column() * 160 + 75, y1 = top + 56 + pre.row() * 40 + 20;
            int x2 = cx - 155 + s.column() * 160 + 75, y2 = top + 56 + s.row() * 40;
            int color = shown.skillRank(pre.id()) > 0 ? 0xFFE8C030 : 0xFF555555;
            ctx.fill(Math.min(x1, x2), y1, Math.max(x1, x2) + 1, y1 + 1, color);
            ctx.fill(x2, y1, x2 + 1, y2, color);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
