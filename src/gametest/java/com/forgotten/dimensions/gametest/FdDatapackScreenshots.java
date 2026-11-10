package com.forgotten.dimensions.gametest;

import com.forgotten.dimensions.util.config.FdConfig;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.option.Perspective;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Captures de l'édition data pack (datapack/LesDimensionsOubliees) : le pack est copié dans un monde,
 * le monde est rouvert (les dimensions du pack sont chargées), puis sorts, créatures à niveau et
 * voyages sont photographiés. L'interface du mod est masquée pour ne montrer que le data pack.
 */
public class FdDatapackScreenshots implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext ctx) {
        String only = System.getProperty("fd.test.only");
        if (only != null && !only.equals("datapack")) return;
        FdConfig.get().rpgHud = false;
        FdConfig.get().rpgNameplates = false;
        FdConfig.get().damageNumbers = false;
        TestWorldSave save;
        try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
            save = sp.getWorldSave();
        }
        copyPack(save.getSaveDirectory().resolve("datapacks").resolve("LesDimensionsOubliees"));
        try (TestSingleplayerContext sp = save.open()) {
            sp.getClientWorld().waitForChunksRender();
            sp.getServer().runCommand("time set noon");
            // Le mod est aussi chargé dans ce client de test : sa progression est mise au maximum pour qu'aucun
            // titre « LEVEL » du mod ne recouvre les captures du data pack.
            sp.getServer().runCommand("fdimension level set @p 100");
            sp.getServer().runCommand("gamerule doDaylightCycle false");
            ctx.waitTicks(30);
            shot(ctx, "dp_welcome", false);
            ctx.waitTicks(80);
            // Scène : plateforme, créatures à niveau face au joueur
            cmd(sp, "fill ~-10 ~-1 ~-4 ~10 ~-1 ~18 minecraft:polished_andesite");
            cmd(sp, "fill ~-10 ~ ~-4 ~10 ~10 ~18 minecraft:air");
            cmd(sp, "tp @s ~ ~ ~ 0 8");
            cmd(sp, "scoreboard players set @s oubliees.level 50");
            cmd(sp, "scoreboard players set @s oubliees.maxmana 2000");
            cmd(sp, "scoreboard players set @s oubliees.mana 2000");
            String[] mobs = {"husk ~-3 ~ ~9", "vindicator ~0 ~ ~11", "husk ~3 ~ ~8"};
            for (String m : mobs) cmd(sp, "summon " + m + " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
            ctx.runOnClient(c -> c.options.setPerspective(Perspective.THIRD_PERSON_BACK));
            ctx.waitTicks(20);
            cast(ctx, sp, "fire", 0, 3);
            shot(ctx, "dp_hud_fire_bolt", false);
            ctx.waitTicks(30);
            cast(ctx, sp, "fire", 1, 4);
            shot(ctx, "dp_fire_nova", false);
            ctx.waitTicks(40);
            cast(ctx, sp, "fire", 3, 14);
            shot(ctx, "dp_meteor", true);
            ctx.waitTicks(30);
            reviveMobs(ctx, sp, mobs);
            cast(ctx, sp, "ice", 2, 8);
            shot(ctx, "dp_frozen_prison", true);
            ctx.waitTicks(30);
            reviveMobs(ctx, sp, mobs);
            cast(ctx, sp, "lightning", 2, 3);
            shot(ctx, "dp_chain_lightning", true);
            ctx.waitTicks(30);
            reviveMobs(ctx, sp, mobs);
            cast(ctx, sp, "void", 3, 25);
            shot(ctx, "dp_singularity", true);
            ctx.waitTicks(70);
            cast(ctx, sp, "nature", 2, 12);
            cast(ctx, sp, "ice", 3, 10);
            shot(ctx, "dp_summons", false);
            // Barres de vie et chiffres de dégâts
            sp.getServer().runCommand("kill @e[type=!player]");
            ctx.waitTicks(5);
            for (String m : mobs) cmd(sp, "summon " + m + " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
            ctx.runOnClient(c -> c.options.setPerspective(Perspective.FIRST_PERSON));
            cmd(sp, "tp @s ~ ~ ~ 0 8");
            ctx.waitTicks(20);
            sp.getServer().runCommand("damage @e[type=husk,limit=1,sort=nearest] 6");
            sp.getServer().runCommand("damage @e[type=vindicator,limit=1] 13");
            ctx.waitTicks(3);
            sp.getServer().runCommand("damage @e[type=husk,limit=1,sort=furthest] 4");
            ctx.waitTicks(4);
            shot(ctx, "dp_mob_levels", false);
            // Fiche de personnage dans le chat
            cmd(sp, "function oubliees:menu/sheet");
            ctx.waitTicks(5);
            shot(ctx, "dp_character_sheet", false);
            // Voyages vers les dimensions du pack
            sp.getServer().runCommand("kill @e[type=!player]");
            sp.getServer().runCommand("gamemode creative @a");
            for (String dim : new String[]{"celestial", "corrupted", "ocean", "chaos", "eternal", "depths", "void"}) {
                cmd(sp, "function oubliees:travel/key_" + dim);
                ctx.waitTicks(25);
                if (dim.equals("celestial")) shot(ctx, "dp_travel_arrival", false);
                ctx.waitTicks(100);
                sp.getServer().runOnServer(server -> {
                    var p = server.getPlayerManager().getPlayerList().get(0);
                    p.getAbilities().flying = true;
                    p.sendAbilitiesUpdate();
                });
                boolean ceiling = dim.equals("chaos") || dim.equals("depths");
                cmd(sp, "effect give @s minecraft:night_vision 60 0 true");
                cmd(sp, ceiling ? "tp @s ~ ~1 ~ 30 4" : dim.equals("void") ? "tp @s ~ ~4 ~ 30 45" : "tp @s ~ ~7 ~ 30 16");
                ctx.waitTicks(40);
                sp.getClientWorld().waitForChunksRender();
                ctx.waitTicks(20);
                shot(ctx, "dp_dim_" + dim, true);
            }
            ctx.runOnClient(c -> c.options.setPerspective(Perspective.FIRST_PERSON));
        }
    }

    private static void cast(ClientGameTestContext ctx, TestSingleplayerContext sp, String school, int spell, int wait) {
        cmd(sp, "function oubliees:debug/hold_" + school);
        cmd(sp, "scoreboard players set @s oubliees.spell " + spell);
        cmd(sp, "scoreboard players set @s oubliees.cd 0");
        cmd(sp, "scoreboard players set @s oubliees.mana 2000");
        cmd(sp, "tp @s ~ ~ ~ 0 8");
        ctx.waitTicks(2);
        cmd(sp, "function oubliees:spell/cast");
        ctx.waitTicks(Math.max(1, wait));
    }

    private static void reviveMobs(ClientGameTestContext ctx, TestSingleplayerContext sp, String[] mobs) {
        sp.getServer().runCommand("kill @e[type=husk]");
        sp.getServer().runCommand("kill @e[type=vindicator]");
        sp.getServer().runCommand("kill @e[tag=oubliees.fx]");
        ctx.waitTicks(3);
        for (String m : mobs) cmd(sp, "summon " + m + " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
        ctx.waitTicks(5);
    }

    private static void cmd(TestSingleplayerContext sp, String c) {
        sp.getServer().runCommand("execute as @p at @s run " + c);
    }

    private static void shot(ClientGameTestContext ctx, String name, boolean hideHud) {
        ctx.runOnClient(c -> c.options.hudHidden = hideHud);
        ctx.waitTicks(2);
        ctx.takeScreenshot(TestScreenshotOptions.of(name));
        ctx.runOnClient(c -> c.options.hudHidden = false);
    }

    /** Copie datapack/LesDimensionsOubliees (cherché depuis le dossier de lancement vers la racine du projet). */
    private static void copyPack(Path target) {
        Path dir = Path.of("").toAbsolutePath();
        Path src = null;
        for (int i = 0; i < 6 && dir != null; i++, dir = dir.getParent()) {
            Path candidate = dir.resolve("datapack").resolve("LesDimensionsOubliees");
            if (Files.isDirectory(candidate)) {
                src = candidate;
                break;
            }
        }
        if (src == null) throw new IllegalStateException("Data pack introuvable (lancez tools/datapack/build_datapack.py)");
        Path from = src;
        try (Stream<Path> files = Files.walk(from)) {
            for (Path p : (Iterable<Path>) files::iterator) {
                Path dest = target.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(dest);
                else Files.copy(p, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
