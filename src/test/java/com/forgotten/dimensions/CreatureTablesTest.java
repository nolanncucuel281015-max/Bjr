package com.forgotten.dimensions;

import com.forgotten.dimensions.content.CreatureCategory;
import com.forgotten.dimensions.content.CreatureDef;
import com.forgotten.dimensions.entity.creature.CreatureAbilities;
import com.forgotten.dimensions.registry.generated.ContentTables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Phase 5 : 60 créatures complètes (modèle, texture, butin, apparition, capacités valides). */
class CreatureTablesTest {
    private static final Path RES = Path.of("src/main/resources");
    private static final Path ASSETS = RES.resolve("assets/forgotten_dimensions");
    private static final Path DATA = RES.resolve("data/forgotten_dimensions");

    @Test
    void sixtyCreaturesInEveryCategory() {
        List<CreatureDef> all = ContentTables.creatures();
        assertEquals(60, all.size());
        Map<CreatureCategory, Integer> cats = new EnumMap<>(CreatureCategory.class);
        Map<String, Integer> dims = new HashMap<>();
        for (CreatureDef d : all) {
            cats.merge(d.category(), 1, Integer::sum);
            dims.merge(d.dimension(), 1, Integer::sum);
        }
        for (CreatureCategory c : CreatureCategory.values()) assertTrue(cats.getOrDefault(c, 0) >= 3, "catégorie " + c);
        assertEquals(7, dims.size());
        dims.forEach((d, n) -> assertTrue(n >= 8, d + " : " + n + " créatures"));
    }

    @Test
    void abilitiesBehaviorsAndMinionsAreValid() {
        Set<String> ids = new HashSet<>();
        ContentTables.creatures().forEach(d -> ids.add(d.id()));
        Set<String> behaviors = Set.of("patrol", "guard", "reinforce", "flee_low", "skittish", "pack");
        for (CreatureDef d : ContentTables.creatures()) {
            for (String a : d.abilities()) assertTrue(CreatureAbilities.ALL.contains(a), d.id() + " : capacité inconnue " + a);
            for (String b : d.behaviors()) assertTrue(behaviors.contains(b), d.id() + " : comportement inconnu " + b);
            if (d.has("SUMMON")) assertTrue(ids.contains(d.minion()), d.id() + " : sbire inconnu " + d.minion());
            assertTrue(d.maxHealth() > 0 && d.speed() > 0 && d.width() > 0 && d.height() > 0, d.id());
        }
    }

    @Test
    void modelsTexturesLootAndSpawns() throws IOException {
        Map<String, JsonObject> models = new HashMap<>();
        Map<String, Integer> spawnedIn = new HashMap<>();
        try (Stream<Path> biomes = Files.list(DATA.resolve("worldgen/biome"))) {
            for (Path b : biomes.toList()) {
                JsonObject spawners = JsonParser.parseString(Files.readString(b)).getAsJsonObject().getAsJsonObject("spawners");
                for (var e : spawners.entrySet()) {
                    for (JsonElement entry : e.getValue().getAsJsonArray()) {
                        spawnedIn.merge(entry.getAsJsonObject().get("type").getAsString(), 1, Integer::sum);
                    }
                }
            }
        }
        for (CreatureDef d : ContentTables.creatures()) {
            JsonObject model = models.computeIfAbsent(d.archetype(), a -> {
                try {
                    return JsonParser.parseString(Files.readString(ASSETS.resolve("fd_entity_models/" + a + ".json"))).getAsJsonObject();
                } catch (IOException e) {
                    throw new AssertionError("modèle manquant : " + a);
                }
            });
            Path tex = ASSETS.resolve("textures/entity/creature/" + d.id() + ".png");
            assertTrue(Files.exists(tex), "texture manquante : " + d.id());
            BufferedImage img = ImageIO.read(tex.toFile());
            assertEquals(model.getAsJsonArray("texture_size").get(0).getAsInt(), img.getWidth(), d.id() + " : largeur de texture");
            assertEquals(model.getAsJsonArray("texture_size").get(1).getAsInt(), img.getHeight(), d.id() + " : hauteur de texture");
            assertTrue(Files.exists(DATA.resolve("loot_table/entities/" + d.id() + ".json")), "butin manquant : " + d.id());
            assertTrue(Files.exists(ASSETS.resolve("items/" + d.id() + "_spawn_egg.json")), "œuf manquant : " + d.id());
            assertTrue(spawnedIn.getOrDefault("forgotten_dimensions:" + d.id(), 0) > 0, "n'apparaît dans aucun biome : " + d.id());
        }
        for (JsonObject model : models.values()) {
            boolean hasHead = false;
            for (JsonElement p : model.getAsJsonArray("parts")) {
                hasHead |= p.getAsJsonObject().get("name").getAsString().equals("head");
                for (JsonElement b : p.getAsJsonObject().getAsJsonArray("boxes")) assertTrue(b.getAsJsonObject().has("uv"));
            }
            assertTrue(hasHead || model.toString().contains("\"body\""), "modèle sans tête ni corps");
        }
    }
}
