package com.forgotten.dimensions.gametest;

import com.forgotten.dimensions.client.screen.SkillTreeScreen;
import com.forgotten.dimensions.client.screen.SpellBookScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.option.Perspective;

import java.util.List;

/**
 * Test client : crée un monde solo, place une vitrine de blocs, équipe une armure, ouvre les écrans
 * du mod et prend des captures (build/run/clientGameTest/screenshots). Échoue si un écran plante.
 */
public class FdClientGameTest implements FabricClientGameTest {
    private static final List<String> SHOWCASE = List.of(
            "cloudstone", "cloudstone_bricks", "chiseled_cloudstone", "celestial_crystal_ore", "astral_silver_ore", "celestial_lamp",
            "blightstone", "blightstone_bricks", "chiseled_blightstone", "blood_ruby_ore", "corruptium_ore", "blood_lamp",
            "abyssal_stone", "abyssal_stone_tiles", "abyssal_pearl_ore", "tidal_cobalt_block", "abyssal_lantern", "coral_glow_block",
            "cinderstone", "cinderstone_bricks", "magmatite_ore", "deep_magmatite_ore", "magma_lamp", "ember_block",
            "frostrock", "frostrock_tiles", "everfrost_gem_ore", "frost_steel_block", "frost_lamp", "aurora_block",
            "umbral_rock", "polished_umbral_rock", "luminite_ore", "glowshroom_block", "umbral_lantern", "deep_mithril_block",
            "voidstone", "voidstone_bricks", "void_shard_ore", "cosmic_opal_block", "nebula_block", "starlight_block",
            "architect_circuit", "ancient_gear_block", "rune_terminal", "archive_shelf", "architect_power_core", "glyph_panel",
            "fire_rune_stone", "ice_rune_stone", "lightning_rune_stone", "nature_rune_stone", "void_rune_stone", "mana_crystal_block");

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            sp.getClientWorld().waitForChunksRender();
            sp.getServer().runCommand("time set noon");
            sp.getServer().runCommand("gamerule doDaylightCycle false");
            sp.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 10");
            for (int i = 0; i < SHOWCASE.size(); i++) {
                int x = (i % 9) - 4, y = (i / 9) - 1;
                sp.getServer().runCommand("execute as @p at @s run setblock ~" + x + " ~" + y + " ~7 forgotten_dimensions:" + SHOWCASE.get(i));
            }
            sp.getServer().runCommand("fdimension level set @p 30");
            sp.getServer().runCommand("fdimension give celestial_blade");
            context.waitTicks(40);
            sp.getClientWorld().waitForChunksRender();
            context.takeScreenshot(TestScreenshotOptions.of("fd_01_world_hud"));

            for (String piece : new String[]{"head:helmet", "chest:chestplate", "legs:leggings", "feet:boots"}) {
                String[] s = piece.split(":");
                sp.getServer().runCommand("item replace entity @p armor." + s[0] + " with forgotten_dimensions:frost_queen_" + s[1]);
            }
            sp.getServer().runCommand("item replace entity @p weapon.mainhand with forgotten_dimensions:volcanic_hammer");
            sp.getServer().runCommand("item replace entity @p weapon.offhand with forgotten_dimensions:void_shard_staff");
            context.runOnClient(client -> client.options.setPerspective(Perspective.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot(TestScreenshotOptions.of("fd_02_armor_third_person"));
            context.runOnClient(client -> client.options.setPerspective(Perspective.FIRST_PERSON));

            context.setScreen(SkillTreeScreen::new);
            context.waitTicks(5);
            context.takeScreenshot(TestScreenshotOptions.of("fd_03_skill_tree"));
            context.setScreen(SpellBookScreen::new);
            context.waitTicks(5);
            context.takeScreenshot(TestScreenshotOptions.of("fd_04_spell_book"));
            context.setScreen(() -> null);

            sp.getServer().runCommand("give @p forgotten_dimensions:crown_of_seven_worlds");
            sp.getServer().runCommand("give @p forgotten_dimensions:celestial_crystal_bow");
            sp.getServer().runCommand("give @p forgotten_dimensions:magmatite_crossbow");
            sp.getServer().runCommand("give @p forgotten_dimensions:frost_steel_greatsword");
            sp.getServer().runCommand("give @p forgotten_dimensions:tidal_cobalt_spear");
            context.waitTicks(10);
            context.runOnClient(client -> client.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(client.player)));
            context.waitTicks(5);
            context.takeScreenshot(TestScreenshotOptions.of("fd_05_inventory"));
            context.setScreen(() -> null);
        }
    }
}
