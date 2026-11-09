package com.forgotten.dimensions.util.config;

import com.forgotten.dimensions.ForgottenDimensions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration serveur : config/forgotten_dimensions.json. Rechargeable avec /fdimension reload.
 * Seules les options réellement utilisées par le code sont présentes.
 */
public final class FdConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Values values = new Values();

    public static final class Values {
        /** Multiplicateur global de l'expérience RPG gagnée. */
        public double xpMultiplier = 1.0;
        /** Expérience accordée à la découverte d'un nouveau biome. */
        public int biomeDiscoveryXp = 25;
        /** Multiplicateur de la régénération de mana. */
        public double manaRegenMultiplier = 1.0;
        /** Multiplicateur des dégâts des sorts (difficulté de la magie). */
        public double spellDamageMultiplier = 1.0;
        /** Multiplicateur des temps de recharge des sorts. */
        public double spellCooldownMultiplier = 1.0;
        /** Multiplicateur des particules générées par le serveur (0 = aucune). */
        public double particleMultiplier = 1.0;
        /** Durée de base des invocations, en secondes. */
        public int summonDurationSeconds = 30;
        /** Les sorts offensifs touchent-ils les autres joueurs ? */
        public boolean spellFriendlyFire = false;
        /** Multiplicateur des récompenses (expérience des créatures tuées). */
        public double rewardMultiplier = 1.0;
    }

    private FdConfig() {}

    public static Values get() {
        return values;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(ForgottenDimensions.MOD_ID + ".json");
    }

    /** Charge (ou crée) le fichier. Renvoie false si le fichier était invalide (valeurs par défaut utilisées). */
    public static boolean load() {
        Path p = path();
        try {
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p)) {
                    Values loaded = GSON.fromJson(r, Values.class);
                    values = loaded != null ? loaded : new Values();
                }
            } else {
                values = new Values();
            }
            save();
            return true;
        } catch (Exception e) {
            ForgottenDimensions.LOGGER.error("Configuration invalide ({}), valeurs par défaut utilisées", p, e);
            values = new Values();
            return false;
        }
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());
            try (Writer w = Files.newBufferedWriter(path())) {
                GSON.toJson(values, w);
            }
        } catch (IOException e) {
            ForgottenDimensions.LOGGER.error("Impossible d'écrire la configuration", e);
        }
    }
}
