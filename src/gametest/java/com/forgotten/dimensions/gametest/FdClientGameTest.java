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
    private static final java.util.concurrent.atomic.AtomicInteger SPAWNED = new java.util.concurrent.atomic.AtomicInteger();
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

            // Phase 5 : galerie des 60 créatures (immobiles, face à la caméra), 10 par capture.
            sp.getServer().runCommand("execute as @p at @s run tp @s ~200 ~ ~ 0 12");
            context.waitTicks(80);
            java.util.List<String> creatures = new java.util.ArrayList<>(com.forgotten.dimensions.registry.ModCreatures.all().keySet());
            for (int batch = 0; batch * 10 < creatures.size(); batch++) {
                sp.getServer().runCommand("kill @e[type=!player]");
                for (int i = 0; i < 10 && batch * 10 + i < creatures.size(); i++) {
                    int row = i / 5, col = i % 5;
                    double x = (col - 2) * 3.6, z = 9 + row * 6, y = row * 1.5;
                    sp.getServer().runCommand("execute as @p at @s run summon forgotten_dimensions:" + creatures.get(batch * 10 + i)
                            + " ~" + x + " ~" + y + " ~" + z + " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
                }
                context.waitTicks(30);
                context.takeScreenshot(TestScreenshotOptions.of("fd_creatures_" + batch));
            }
            sp.getServer().runCommand("kill @e[type=!player]");

            // Phase 4 : voyage réel dans chacune des sept dimensions.
            context.runOnClient(client -> client.options.getViewDistance().setValue(6));
            // Le monde de test coupe l'apparition des créatures ; on la réactive pour vérifier les apparitions naturelles.
            sp.getServer().runOnServer(server -> {
                server.getOverworld().getGameRules().setValue(net.minecraft.world.rule.GameRules.DO_MOB_SPAWNING, true, server);
                server.setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
            });
            sp.getServer().runCommand("gamemode creative @a");
            for (String dim : com.forgotten.dimensions.dimension.FdDimensions.IDS) {
                sp.getServer().runOnServer(server -> {
                    var player = server.getPlayerManager().getPlayerList().get(0);
                    if (!com.forgotten.dimensions.dimension.DimensionTravel.travelTo(player, dim)) {
                        throw new AssertionError("dimension absente : " + dim);
                    }
                    var world = player.getEntityWorld();
                    if (!world.getRegistryKey().equals(com.forgotten.dimensions.dimension.FdDimensions.key(dim))) {
                        throw new AssertionError("le joueur n'est pas arrivé dans " + dim);
                    }
                    var pos = player.getBlockPos();
                    if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), net.minecraft.util.math.Direction.UP)) {
                        throw new AssertionError(dim + " : pas de sol sous le joueur en " + pos);
                    }
                    int modBlocks = 0;
                    // Plateformes sur l'eau (océan, lacs gelés) : le fond peut être loin dessous.
                    int below = 220;
                    for (net.minecraft.util.math.BlockPos q : net.minecraft.util.math.BlockPos.iterate(pos.add(-12, -below, -12), pos.add(12, 8, 12))) {
                        if (net.minecraft.registry.Registries.BLOCK.getId(world.getBlockState(q).getBlock()).getNamespace()
                                .equals(com.forgotten.dimensions.ForgottenDimensions.MOD_ID)) modBlocks++;
                    }
                    com.forgotten.dimensions.ForgottenDimensions.LOGGER.info("[client-test] {} : arrivée {} biome {}, {} blocs du mod autour",
                            dim, pos, world.getBiome(pos).getKey().map(k -> k.getValue().toString()).orElse("?"), modBlocks);
                    if (modBlocks < 50) throw new AssertionError(dim + " : terrain sans blocs du mod (" + modBlocks + ")");
                    player.setPitch(8F);
                    player.setYaw(180F);
                    player.setHeadYaw(180F);
                });
                // Rendu logiciel (sans carte graphique) : attente fixe plutôt que « tous les chunks rendus ».
                context.waitTicks(120);
                context.takeScreenshot(TestScreenshotOptions.of("fd_dim_" + dim));
                sp.getServer().runOnServer(server -> {
                    var player = server.getPlayerManager().getPlayerList().get(0);
                    var found = player.getEntityWorld().getEntitiesByClass(net.minecraft.entity.mob.MobEntity.class,
                            player.getBoundingBox().expand(96), e -> e instanceof com.forgotten.dimensions.entity.creature.FdCreature);
                    java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
                    found.forEach(e -> counts.merge(((com.forgotten.dimensions.entity.creature.FdCreature) e).definition().id(), 1, Integer::sum));
                    com.forgotten.dimensions.ForgottenDimensions.LOGGER.info("[client-test] {} : créatures apparues naturellement {}", dim, counts);
                    SPAWNED.addAndGet(found.size());
                });
            }
            if (SPAWNED.get() == 0) throw new AssertionError("aucune créature n'est apparue naturellement dans les dimensions");
            sp.getServer().runOnServer(server -> {
                var player = server.getPlayerManager().getPlayerList().get(0);
                com.forgotten.dimensions.dimension.DimensionTravel.returnHome(player);
                if (com.forgotten.dimensions.dimension.DimensionTravel.isModDimension(player.getEntityWorld())) {
                    throw new AssertionError("retour à la Surface échoué");
                }
            });
            context.waitTicks(20);
        }
    }
}
