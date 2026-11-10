package com.forgotten.dimensions;

import com.forgotten.dimensions.content.BlockDef;
import com.forgotten.dimensions.registry.generated.ContentTables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Compatibilité des ressources : aucune référence vers une texture, un modèle ou un objet inexistant,
 * chaque bloc a un état, un modèle d'objet et une loot table, chaque nom est traduit (FR et EN).
 */
class ResourceValidationTest {
    private static final Path RES = Path.of("src/main/resources");
    private static final Path ASSETS = RES.resolve("assets/forgotten_dimensions");
    private static final Path DATA = RES.resolve("data/forgotten_dimensions");
    private static final String NS = "forgotten_dimensions:";

    private static JsonObject read(Path p) throws IOException {
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    private static List<String> allItemIds() {
        List<String> ids = new ArrayList<>();
        ContentTables.blocks().forEach(b -> ids.add(b.id()));
        ContentTables.items().forEach(i -> ids.add(i.id()));
        ContentTables.weapons().forEach(w -> ids.add(w.id()));
        ContentTables.artifacts().forEach(a -> ids.add(a.id()));
        ContentTables.armorSets().forEach(a -> {
            for (String p : new String[]{"helmet", "chestplate", "leggings", "boots"}) ids.add(a.id() + "_" + p);
        });
        return ids;
    }

    @Test
    void everyBlockHasStateLootAndTranslation() throws IOException {
        Map<String, JsonElement> fr = JsonParser.parseString(Files.readString(ASSETS.resolve("lang/fr_fr.json"))).getAsJsonObject().asMap();
        Map<String, JsonElement> en = JsonParser.parseString(Files.readString(ASSETS.resolve("lang/en_us.json"))).getAsJsonObject().asMap();
        for (BlockDef b : ContentTables.blocks()) {
            assertTrue(Files.exists(ASSETS.resolve("blockstates/" + b.id() + ".json")), "état manquant : " + b.id());
            assertTrue(Files.exists(DATA.resolve("loot_table/blocks/" + b.id() + ".json")), "loot table manquante : " + b.id());
            assertTrue(fr.containsKey("block.forgotten_dimensions." + b.id()), "traduction FR manquante : " + b.id());
            assertTrue(en.containsKey("block.forgotten_dimensions." + b.id()), "traduction EN manquante : " + b.id());
        }
        for (String id : allItemIds()) {
            assertTrue(Files.exists(ASSETS.resolve("items/" + id + ".json")), "définition d'objet manquante : " + id);
            boolean isBlock = ContentTables.blocks().stream().anyMatch(b -> b.id().equals(id));
            if (!isBlock) {
                assertTrue(fr.containsKey("item.forgotten_dimensions." + id), "traduction FR manquante : " + id);
                assertTrue(en.containsKey("item.forgotten_dimensions." + id), "traduction EN manquante : " + id);
            }
        }
        assertEquals(fr.keySet(), en.keySet(), "les fichiers FR et EN doivent avoir les mêmes clés");
        // Minecraft rejette tout « % » isolé (TranslationException) : seuls %s, %1$s et %% sont admis.
        java.util.regex.Pattern lone = java.util.regex.Pattern.compile("%(?!%|s|\\d+\\$s)");
        for (var lang : List.of(fr, en)) {
            for (var e : lang.entrySet()) {
                String v = e.getValue().getAsString().replace("%%", "");
                assertFalse(lone.matcher(v).find(), "« % » isolé dans " + e.getKey() + " : " + v);
            }
        }
    }

    @Test
    void everyModelReferenceResolves() throws IOException {
        List<String> errors = new ArrayList<>();
        try (Stream<Path> files = Files.walk(ASSETS.resolve("models"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                JsonObject model = read(p);
                if (model.has("textures")) {
                    for (var e : model.getAsJsonObject("textures").entrySet()) {
                        String ref = e.getValue().getAsString();
                        if (ref.startsWith(NS)) {
                            Path tex = ASSETS.resolve("textures/" + ref.substring(NS.length()) + ".png");
                            if (!Files.exists(tex)) errors.add(p.getFileName() + " -> " + ref);
                        }
                    }
                }
            }
        }
        // Les blockstates et définitions d'objets ne référencent que des modèles existants.
        for (String dir : new String[]{"blockstates", "items"}) {
            try (Stream<Path> files = Files.walk(ASSETS.resolve(dir))) {
                for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                    String text = Files.readString(p);
                    var m = java.util.regex.Pattern.compile("\"" + NS + "((?:block|item)/[a-z0-9_/]+)\"").matcher(text);
                    while (m.find()) {
                        if (!Files.exists(ASSETS.resolve("models/" + m.group(1) + ".json"))) errors.add(p.getFileName() + " -> " + m.group(1));
                    }
                }
            }
        }
        // Équipements : textures portées présentes.
        try (Stream<Path> files = Files.walk(ASSETS.resolve("equipment"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                String id = p.getFileName().toString().replace(".json", "");
                for (String layer : new String[]{"humanoid", "humanoid_leggings"}) {
                    if (!Files.exists(ASSETS.resolve("textures/entity/equipment/" + layer + "/" + id + ".png"))) errors.add(id + " " + layer);
                }
            }
        }
        assertTrue(errors.isEmpty(), "références cassées : " + errors);
    }

    @Test
    void recipesAndLootReferenceExistingItems() throws IOException {
        List<String> known = allItemIds();
        List<String> errors = new ArrayList<>();
        for (String dir : new String[]{"recipe", "loot_table", "advancement"}) {
            try (Stream<Path> files = Files.walk(DATA.resolve(dir))) {
                for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                    var m = java.util.regex.Pattern.compile("\"" + NS + "([a-z0-9_]+)\"").matcher(Files.readString(p));
                    while (m.find()) {
                        String id = m.group(1);
                        if (!known.contains(id) && !Files.exists(DATA.resolve("advancement/" + id + ".json"))
                                && !Files.exists(DATA.resolve("dimension/" + id + ".json"))) {
                            errors.add(p.getFileName() + " -> " + id);
                        }
                    }
                }
            }
        }
        assertTrue(errors.isEmpty(), "objets inconnus : " + errors);
    }

    @Test
    void dimensionsReferenceExistingBlocksBiomesAndFeatures() throws IOException {
        List<String> known = allItemIds();
        List<String> errors = new ArrayList<>();
        Path wg = DATA.resolve("worldgen");
        for (String dir : new String[]{"../dimension", "../dimension_type", "noise_settings", "biome", "configured_feature", "placed_feature"}) {
            try (Stream<Path> files = Files.walk(wg.resolve(dir).normalize())) {
                for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                    var m = java.util.regex.Pattern.compile("\"" + NS + "([a-z0-9_]+)\"").matcher(Files.readString(p));
                    while (m.find()) {
                        String id = m.group(1);
                        boolean ok = known.contains(id) || Files.exists(wg.resolve("biome/" + id + ".json"))
                                || Files.exists(wg.resolve("placed_feature/" + id + ".json"))
                                || Files.exists(wg.resolve("configured_feature/" + id + ".json"))
                                || Files.exists(wg.resolve("noise_settings/" + id + ".json"))
                                || Files.exists(DATA.resolve("dimension_type/" + id + ".json"))
                                || Files.exists(DATA.resolve("loot_table/entities/" + id + ".json"))
                                || id.endsWith("_floor") || id.endsWith("_roof");
                        if (!ok) errors.add(p.getFileName() + " -> " + id);
                    }
                }
            }
        }
        assertEquals(7, Files.list(DATA.resolve("dimension")).count(), "7 dimensions");
        assertEquals(35, Files.list(wg.resolve("biome")).count(), "35 biomes");
        assertTrue(errors.isEmpty(), "références de génération cassées : " + errors);
    }
}
