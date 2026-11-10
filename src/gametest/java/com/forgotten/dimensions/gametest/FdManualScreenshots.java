package com.forgotten.dimensions.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.option.Perspective;

/** Captures d'écran du manuel (docs/manuel) prises dans le vrai jeu : objets 3D, portail, dimensions, créatures. */
public class FdManualScreenshots implements FabricClientGameTest {
    private static final String[] WEAPONS = {"astral_silver_sword", "celestial_crystal_greatsword", "corruptium_dagger",
            "tidal_cobalt_battleaxe", "magmatite_spear", "frost_steel_staff", "deep_mithril_wand", "nebulite_bow",
            "void_shard_crossbow", "celestial_blade", "volcanic_hammer", "world_ender"};
    private static final String[][] STANDS = {
            {"sky_guardian", "celestial_blade"}, {"corruption_lord", "blade_of_corruption"},
            {"frost_queen", "frost_queen_scepter"}, {"primordial", "world_ender"}};

    private static net.minecraft.util.math.BlockPos base;

    /** Téléportation absolue relative au point de base de la vitrine. */
    private static void at(TestSingleplayerContext sp, double dx, double dy, double dz, float yaw, float pitch) {
        sp.getServer().runCommand(String.format(java.util.Locale.ROOT, "tp @p %.2f %.2f %.2f %.1f %.1f",
                base.getX() + 0.5 + dx, base.getY() + dy, base.getZ() + 0.5 + dz, yaw, pitch));
    }

    private static void cmd(TestSingleplayerContext sp, String c) {
        sp.getServer().runCommand("execute as @p at @s run " + c);
    }

    private static void shot(ClientGameTestContext ctx, String name, boolean hideHud) {
        ctx.runOnClient(c -> c.options.hudHidden = hideHud);
        ctx.waitTicks(2);
        ctx.takeScreenshot(TestScreenshotOptions.of("manual_" + name));
        ctx.runOnClient(c -> c.options.hudHidden = false);
    }

    @Override
    public void runTest(ClientGameTestContext ctx) {
        try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
            sp.getClientWorld().waitForChunksRender();
            sp.getServer().runCommand("time set noon");
            sp.getServer().runCommand("gamemode creative @a");
            sp.getServer().runCommand("fdimension level set @p 35");
            ctx.runOnClient(c -> {
                c.options.getViewDistance().setValue(8);
                c.options.getGamma().setValue(1.0);
            });

            // ---------------------------------------------------------- vitrine des objets 3D
            cmd(sp, "tp @s ~400 ~ ~ 0 8");
            ctx.waitTicks(60);
            base = sp.getServer().computeOnServer(server -> server.getPlayerManager().getPlayerList().get(0).getBlockPos());
            cmd(sp, "fill ~-7 ~-1 ~-2 ~7 ~-1 ~9 forgotten_dimensions:polished_cloudstone");
            cmd(sp, "fill ~-7 ~ ~7 ~7 ~5 ~7 forgotten_dimensions:architect_bricks");
            cmd(sp, "fill ~-7 ~ ~-2 ~7 ~6 ~6 air");
            for (int i = 0; i < WEAPONS.length; i++) {
                int x = (i % 6) * 2 - 5, y = 1 + (i / 6) * 2;
                cmd(sp, "summon item_frame ~" + x + " ~" + y + " ~6 {Facing:2b,Fixed:1b,Invisible:0b,Item:{id:\"forgotten_dimensions:"
                        + WEAPONS[i] + "\",count:1}}");
            }
            for (int i = 0; i < STANDS.length; i++) {
                String set = STANDS[i][0];
                double x = -4.5 + i * 3;
                cmd(sp, "summon armor_stand ~" + x + " ~ ~3.5 {ShowArms:1b,NoBasePlate:1b,Rotation:[180f,0f],equipment:{"
                        + "head:{id:\"forgotten_dimensions:" + set + "_helmet\"},chest:{id:\"forgotten_dimensions:" + set + "_chestplate\"},"
                        + "legs:{id:\"forgotten_dimensions:" + set + "_leggings\"},feet:{id:\"forgotten_dimensions:" + set + "_boots\"},"
                        + "mainhand:{id:\"forgotten_dimensions:" + STANDS[i][1] + "\"}}}");
            }
            at(sp, 0, 0, -1.5, 0, 10);
            ctx.waitTicks(40);
            shot(ctx, "items_showcase", true);
            at(sp, 0, 1.0, 4.6, 0, 10);
            ctx.waitTicks(20);
            shot(ctx, "items_frames_closeup", true);
            // Gros plan sur les armures portées (pièces en relief).
            at(sp, -3, 0, 0.9, 0, 12);
            ctx.waitTicks(20);
            shot(ctx, "armor_stands_closeup", true);
            at(sp, 3, 0, 0.9, 0, 12);
            ctx.waitTicks(20);
            shot(ctx, "armor_stands_closeup_2", true);

            // Objets en main (première personne) et inventaire 3D.
            sp.getServer().runCommand("clear @a");
            for (String w : new String[]{"celestial_blade", "volcanic_hammer", "void_bow", "frost_queen_scepter", "abyssal_trident",
                    "world_ender", "frost_queen_helmet", "frost_queen_chestplate", "frost_queen_leggings"}) {
                sp.getServer().runCommand("give @p forgotten_dimensions:" + w);
            }
            at(sp, 0, 0, -1.5, 0, -5);
            ctx.waitTicks(20);
            shot(ctx, "first_person_blade", false);
            ctx.runOnClient(c -> c.player.getInventory().setSelectedSlot(1));
            ctx.waitTicks(10);
            shot(ctx, "first_person_hammer", false);
            sp.getServer().runCommand("gamemode survival @a");
            for (String piece : new String[]{"head:helmet", "chest:chestplate", "legs:leggings", "feet:boots"}) {
                String[] p = piece.split(":");
                sp.getServer().runCommand("item replace entity @p armor." + p[0] + " with forgotten_dimensions:primordial_" + p[1]);
            }
            ctx.waitTicks(5);
            ctx.runOnClient(c -> c.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(c.player)));
            ctx.waitTicks(5);
            shot(ctx, "inventory_3d", false);
            ctx.setScreen(() -> null);
            ctx.runOnClient(c -> c.options.setPerspective(Perspective.THIRD_PERSON_FRONT));
            ctx.waitTicks(10);
            shot(ctx, "player_primordial_armor", true);
            ctx.runOnClient(c -> c.options.setPerspective(Perspective.FIRST_PERSON));
            sp.getServer().runCommand("gamemode creative @a");

            // ---------------------------------------------------------- tutoriel du portail
            at(sp, 30, 0, 0, 0, 15);
            ctx.waitTicks(40);
            cmd(sp, "fill ~-2 ~-1 ~1 ~2 ~-1 ~5 forgotten_dimensions:architect_bricks");
            cmd(sp, "setblock ~ ~ ~3 forgotten_dimensions:dimensional_gate");
            cmd(sp, "setblock ~-1 ~ ~3 forgotten_dimensions:ancient_lamp");
            cmd(sp, "setblock ~1 ~ ~3 forgotten_dimensions:ancient_lamp");
            sp.getServer().runCommand("clear @a");
            sp.getServer().runCommand("give @p forgotten_dimensions:celestial_key");
            sp.getServer().runCommand("give @p forgotten_dimensions:dimensional_gate");
            sp.getServer().runCommand("give @p forgotten_dimensions:architect_core");
            sp.getServer().runCommand("give @p forgotten_dimensions:celestial_fragment 8");
            ctx.waitTicks(30);
            shot(ctx, "portal_1_gate_and_key", false);
            sp.getServer().runOnServer(server -> com.forgotten.dimensions.dimension.DimensionTravel.travelTo(
                    server.getPlayerManager().getPlayerList().get(0), "celestial"));
            ctx.waitTicks(25);
            shot(ctx, "portal_2_arrival_title", false);
            ctx.waitTicks(80);
            cmd(sp, "tp @s ~ ~ ~ -90 25");
            ctx.waitTicks(20);
            shot(ctx, "portal_3_return_gate", false);

            // ---------------------------------------------------------- panoramas et créatures de chaque dimension
            java.util.Map<String, String> stones = java.util.Map.of("celestial", "cloudstone", "corrupted", "blightstone",
                    "ocean", "abyssal_stone", "chaos", "cinderstone", "eternal", "frostrock", "depths", "umbral_rock", "void", "voidstone");
            for (String dim : com.forgotten.dimensions.dimension.FdDimensions.IDS) {
                sp.getServer().runOnServer(server -> com.forgotten.dimensions.dimension.DimensionTravel.travelTo(
                        server.getPlayerManager().getPlayerList().get(0), dim));
                ctx.waitTicks(100);
                boolean open = !dim.equals("chaos") && !dim.equals("depths");
                // Le portail de retour est à l'est de l'arrivée : on regarde vers l'ouest (yaw 90).
                sp.getServer().runOnServer(server -> {
                    var p = server.getPlayerManager().getPlayerList().get(0);
                    p.getAbilities().flying = true;
                    p.sendAbilitiesUpdate();
                });
                // La rotation doit passer par /tp : le client garde sa caméra si on ne change que le serveur.
                cmd(sp, open ? "tp @s ~4 ~24 ~ 90 24" : "tp @s ~ ~ ~ 90 8");
                ctx.waitTicks(120);
                shot(ctx, "dim_" + dim + "_panorama", true);
                // Petite scène dégagée devant le joueur, puis les créatures de la dimension, immobiles, face à lui.
                cmd(sp, open ? "tp @s ~-4 ~-24 ~ 90 12" : "tp @s ~ ~ ~ 90 12");
                ctx.waitTicks(10);
                sp.getServer().runCommand("kill @e[type=!player]");
                cmd(sp, "fill ~-24 ~-1 ~-11 ~-2 ~-1 ~11 forgotten_dimensions:polished_" + stones.get(dim));
                cmd(sp, "fill ~-24 ~ ~-11 ~-2 ~8 ~11 air");
                java.util.List<String> list = new java.util.ArrayList<>();
                com.forgotten.dimensions.registry.ModCreatures.definitions().forEach((id, d) -> {
                    if (d.dimension().equals(dim)) list.add(id);
                });
                for (int i = 0; i < list.size(); i++) {
                    int row = i / 5, col = i % 5;
                    cmd(sp, "summon forgotten_dimensions:" + list.get(i) + " ~" + (-(7 + row * 6)) + " ~" + (row * 0.6)
                            + " ~" + ((col - 2) * 3.6) + " {NoAI:1b,PersistenceRequired:1b,Rotation:[-90f,0f]}");
                }
                ctx.waitTicks(40);
                shot(ctx, "dim_" + dim + "_creatures", true);
                sp.getServer().runCommand("kill @e[type=!player]");
            }

            // ---------------------------------------------------------- magie
            sp.getServer().runOnServer(server -> com.forgotten.dimensions.dimension.DimensionTravel.travelTo(
                    server.getPlayerManager().getPlayerList().get(0), "eternal"));
            ctx.waitTicks(100);
            sp.getServer().runCommand("summon forgotten_dimensions:phantom_wolf ~ ~ ~");
            sp.getServer().runOnServer(server -> {
                var p = server.getPlayerManager().getPlayerList().get(0);
                p.setAttached(com.forgotten.dimensions.registry.ModAttachments.MANA, new com.forgotten.dimensions.magic.ManaState(400F, 400F));
                p.setPitch(20F);
                com.forgotten.dimensions.magic.SpellExecutor.execute(p, com.forgotten.dimensions.magic.Spells.get("fire_nova"), 8F);
            });
            ctx.waitTicks(3);
            shot(ctx, "spell_fire_nova", false);
            ctx.waitTicks(60);
            sp.getServer().runOnServer(server -> {
                var p = server.getPlayerManager().getPlayerList().get(0);
                com.forgotten.dimensions.magic.SpellExecutor.execute(p, com.forgotten.dimensions.magic.Spells.get("frost_nova"), 8F);
            });
            ctx.waitTicks(3);
            shot(ctx, "spell_frost_nova", false);
            ctx.runOnClient(c -> c.options.setPerspective(Perspective.FIRST_PERSON));
        }
    }
}
