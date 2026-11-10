package com.forgotten.dimensions.client.render;

import com.forgotten.dimensions.ForgottenDimensions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Charge les 19 formes de modèles (assets/forgotten_dimensions/fd_entity_models/*.json, générées par
 * tools/gen/modelgen.py) et les enregistre comme couches de modèle d'entité.
 */
public final class CreatureModels {
    private static final Map<String, EntityModelLayer> LAYERS = new HashMap<>();

    private CreatureModels() {}

    public static EntityModelLayer layer(String archetype) {
        return LAYERS.computeIfAbsent(archetype, a -> {
            EntityModelLayer layer = new EntityModelLayer(ForgottenDimensions.id("creature_" + a), "main");
            EntityModelLayerRegistry.registerModelLayer(layer, () -> load(a));
            return layer;
        });
    }

    static TexturedModelData load(String archetype) {
        String path = "/assets/" + ForgottenDimensions.MOD_ID + "/fd_entity_models/" + archetype + ".json";
        try (InputStream in = CreatureModels.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Modèle introuvable : " + path);
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            ModelData data = new ModelData();
            Map<String, ModelPartData> parts = new HashMap<>();
            for (JsonElement el : json.getAsJsonArray("parts")) {
                JsonObject p = el.getAsJsonObject();
                ModelPartBuilder builder = ModelPartBuilder.create();
                for (JsonElement be : p.getAsJsonArray("boxes")) {
                    JsonObject b = be.getAsJsonObject();
                    JsonArray uv = b.getAsJsonArray("uv"), o = b.getAsJsonArray("origin"), s = b.getAsJsonArray("size");
                    builder.uv(uv.get(0).getAsInt(), uv.get(1).getAsInt())
                            .cuboid(o.get(0).getAsFloat(), o.get(1).getAsFloat(), o.get(2).getAsFloat(),
                                    s.get(0).getAsFloat(), s.get(1).getAsFloat(), s.get(2).getAsFloat());
                }
                JsonArray pv = p.getAsJsonArray("pivot"), rot = p.getAsJsonArray("rot");
                ModelTransform transform = ModelTransform.of(pv.get(0).getAsFloat(), pv.get(1).getAsFloat(), pv.get(2).getAsFloat(),
                        rot.get(0).getAsFloat(), rot.get(1).getAsFloat(), rot.get(2).getAsFloat());
                JsonElement parentEl = p.get("parent");
                ModelPartData parent = parentEl == null || parentEl.isJsonNull() ? data.getRoot() : parts.get(parentEl.getAsString());
                parts.put(p.get("name").getAsString(), parent.addChild(p.get("name").getAsString(), builder, transform));
            }
            JsonArray size = json.getAsJsonArray("texture_size");
            return TexturedModelData.of(data, size.get(0).getAsInt(), size.get(1).getAsInt());
        } catch (Exception e) {
            throw new IllegalStateException("Modèle de créature invalide : " + archetype, e);
        }
    }
}
