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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Raccourcis : G sort suivant, K compétences, B grimoire ; lancer = clic droit avec un bâton ou une baguette. */
public final class ModKeyBindings {
    public static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(ForgottenDimensions.id("main"));
    public static KeyBinding castSpell;
    public static KeyBinding nextSpell;
    public static KeyBinding openSkills;
    public static KeyBinding openSpellBook;

    private ModKeyBindings() {}

    public static void register() {
        // Lancer : clic droit avec un bâton ou une baguette. Cette touche (sans arme) est optionnelle et non assignée.
        castSpell = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.cast_spell", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY));
        nextSpell = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.forgotten_dimensions.next_spell", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
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
        while (openSkills.wasPressed()) {
            client.setScreen(new SkillTreeScreen());
        }
        while (openSpellBook.wasPressed()) {
            client.setScreen(new SpellBookScreen());
        }
    }

    /** Passe au sort débloqué suivant. */
    private static void cycleSpell() {
        PlayerProgress p = ClientData.progress();
        List<Spell> unlocked = new ArrayList<>();
        for (Spell s : Spells.all().values()) if (SpellCaster.isUnlocked(p, s)) unlocked.add(s);
        if (unlocked.isEmpty()) return;
        int index = 0;
        for (int i = 0; i < unlocked.size(); i++) if (unlocked.get(i).id().equals(p.selectedSpell())) index = i + 1;
        ClientPlayNetworking.send(new SelectSpellPayload(unlocked.get(index % unlocked.size()).id()));
    }
}
