package com.forgotten.dimensions.client.input;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.client.screen.SkillTreeScreen;
import com.forgotten.dimensions.client.screen.SpellBookScreen;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.network.packet.CastSpellPayload;
import com.forgotten.dimensions.network.packet.SelectSpellPayload;
import com.forgotten.dimensions.progression.PlayerProgress;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Raccourcis : G sort suivant (dans le grimoire actif), V bascule entre sorts d'attaque et sorts
 * d'invocation, K compétences, B grimoire ; lancer = clic droit avec un bâton ou une baguette.
 */
public final class ModKeyBindings {
    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(ForgottenDimensions.id("main"));
    public static KeyBinding castSpell;
    public static KeyBinding nextSpell;
    public static KeyBinding switchMode;
    public static KeyBinding openSkills;
    public static KeyBinding openSpellBook;

    private ModKeyBindings() {}

    public static void register() {
        // Lancer : clic droit avec un bâton ou une baguette. Cette touche (sans arme) est optionnelle et non assignée.
        castSpell = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.cast_spell", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY));
        nextSpell = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.next_spell", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
        switchMode = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.switch_spell_mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));
        openSkills = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.open_skills", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
        openSpellBook = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.open_spell_book", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(ModKeyBindings::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null) return;
        while (castSpell.wasPressed()) {
            ClientPlayNetworking.send(new CastSpellPayload(ClientData.progress().selectedSpell()));
        }
        while (nextSpell.wasPressed()) {
            cycleSpell();
        }
        while (switchMode.wasPressed()) {
            switchMode(client);
        }
        while (openSkills.wasPressed()) {
            client.setScreen(new SkillTreeScreen());
        }
        while (openSpellBook.wasPressed()) {
            client.setScreen(new SpellBookScreen());
        }
    }

    /** Dernier sort choisi dans chaque grimoire, pour y revenir en rebasculant. */
    private static String lastAttack = "", lastSummon = "";

    /** Le sort actif est-il une invocation ? */
    public static boolean summonMode() {
        Spell s = Spells.get(ClientData.progress().selectedSpell());
        return s != null && s.isSummon();
    }

    private static List<Spell> unlocked(PlayerProgress p, boolean summon) {
        List<Spell> list = new ArrayList<>();
        for (Spell s : Spells.ofMode(summon)) if (SpellCaster.isUnlocked(p, s)) list.add(s);
        return list;
    }

    /** V : passe du grimoire d'attaque au grimoire d'invocation (et inversement). */
    private static void switchMode(MinecraftClient client) {
        PlayerProgress p = ClientData.progress();
        boolean toSummon = !summonMode();
        if (toSummon) lastAttack = p.selectedSpell(); else lastSummon = p.selectedSpell();
        List<Spell> list = unlocked(p, toSummon);
        if (list.isEmpty()) {
            int first = Integer.MAX_VALUE;
            for (Spell s : Spells.ofMode(true)) first = Math.min(first, s.level());
            client.player.sendMessage(toSummon
                    ? Text.translatable("message.forgotten_dimensions.mode.none_summon", first).formatted(Formatting.GOLD)
                    : Text.translatable("message.forgotten_dimensions.mode.none_attack").formatted(Formatting.GOLD), true);
            return;
        }
        String remembered = toSummon ? lastSummon : lastAttack;
        Spell pick = list.get(0);
        for (Spell s : list) if (s.id().equals(remembered)) pick = s;
        ClientPlayNetworking.send(new SelectSpellPayload(pick.id()));
        client.player.sendMessage(Text.translatable(toSummon ? "message.forgotten_dimensions.mode.summon" : "message.forgotten_dimensions.mode.attack",
                Text.translatable(pick.translationKey())).formatted(toSummon ? Formatting.LIGHT_PURPLE : Formatting.AQUA), true);
    }

    /** G : passe au sort débloqué suivant du grimoire actif. */
    private static void cycleSpell() {
        PlayerProgress p = ClientData.progress();
        List<Spell> unlocked = unlocked(p, summonMode());
        if (unlocked.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < unlocked.size(); i++) if (unlocked.get(i).id().equals(p.selectedSpell())) index = i + 1;
        ClientPlayNetworking.send(new SelectSpellPayload(unlocked.get(index % unlocked.size()).id()));
    }
}
