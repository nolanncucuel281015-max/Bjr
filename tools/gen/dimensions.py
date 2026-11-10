"""Phase 4 — les sept dimensions : blocs de surface, bois, portail, clés, biomes, terrain et features.

Appelé par generate.py avec le module generate en paramètre (accès à add_block, add_item, wjson...).
Les « noise routers » de tools/gen/templates/noise_router sont repris de Minecraft 1.21.11 (données
fonctionnelles de génération) ; tout le reste (blocs, surfaces, biomes, features) est propre au mod.
"""
import json
import math
import os

HERE = os.path.dirname(os.path.abspath(__file__))

# Étapes de génération (ordre de GenerationStep.Feature)
RAW, LAKES, LOCAL, UNDER_STRUCT, SURF_STRUCT, STRONG, ORES, UNDER_DECO, SPRINGS, VEG, TOP = range(11)


def run(G):
    C, X, NS, rid, wjson = G.C, G.X, G.NS, G.rid, G.wjson
    DATA, ASSETS = G.DATA, G.ASSETS

    def mc(name):
        return {"Name": name if ":" in name else rid(name)}

    # ------------------------------------------------------------------ textures
    def tex_grass_top(key, rgb):
        cv = X.Canvas()
        X.noisy_fill(cv, key, rgb, levels=(0.72, 0.84, 0.95, 1.08), scale=2.0, speckle=0.2)
        return cv

    def tex_grass_side(key, soil, grass):
        cv = X.Canvas()
        X.noisy_fill(cv, key + "soil", soil, levels=(0.7, 0.82, 0.92, 1.0), scale=4.0, speckle=0.15)
        r = X.rng_for(key)
        for x in range(16):
            depth = 3 + r.randint(0, 2)
            for y in range(depth):
                cv.set(x, y, X.shade(grass, 0.85 if (x + y) % 3 else 1.05))
        return cv

    def tex_log_side(key, bark):
        cv = X.Canvas()
        r = X.rng_for(key)
        for x in range(16):
            stripe = 0.7 + 0.35 * ((x * 7 + r.randint(0, 3)) % 4) / 3
            for y in range(16):
                f = stripe * (0.92 + r.random() * 0.12)
                cv.set(x, y, X.shade(bark, f))
        return cv

    def tex_log_top(key, bark, wood):
        cv = X.Canvas()
        for y in range(16):
            for x in range(16):
                d = max(abs(x - 7.5), abs(y - 7.5))
                if d > 6.5:
                    cv.set(x, y, X.shade(bark, 0.8))
                else:
                    ring = int(d) % 2
                    cv.set(x, y, X.shade(wood, 0.85 if ring else 1.0))
        return cv

    def tex_planks(key, wood):
        cv = X.Canvas()
        X.noisy_fill(cv, key, wood, levels=(0.85, 0.93, 1.0, 1.05), scale=8.0, speckle=0.08)
        for y in (3, 7, 11, 15):
            for x in range(16):
                cv.set(x, y, X.shade(wood, 0.6))
        r = X.rng_for(key + "j")
        for row in range(4):
            jx = r.randint(2, 13)
            for y in range(row * 4, row * 4 + 3):
                cv.set(jx, y, X.shade(wood, 0.65))
        return cv

    def tex_leaves(key, rgb):
        cv = X.Canvas()
        X.noisy_fill(cv, key, rgb, levels=(0.55, 0.7, 0.85, 1.0, 1.12), scale=2.0, speckle=0.3)
        return cv

    def tex_gate():
        cv = X.Canvas()
        frame = (0x5C, 0x5E, 0x58)
        X.noisy_fill(cv, "gate", frame, levels=(0.8, 0.9, 1.0), scale=4.0)
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, y - 7.5)
                a = math.atan2(y - 7.5, x - 7.5)
                if d < 5.8:
                    swirl = (math.sin(a * 3 + d * 1.3) + 1) / 2
                    c = (int(0x60 + 0x60 * swirl), int(0x30 + 0x90 * (1 - swirl)), 0xF0)
                    cv.set(x, y, X.shade(c, 0.7 + 0.5 * (1 - d / 6)))
                elif d < 7.0:
                    cv.set(x, y, X.shade((0xC8, 0xA0, 0x40), 1.0))
        return cv

    def tex_key(rgb):
        pal = X.palette(rgb)
        cv = X.Canvas()
        cv.disc(4.5, 4.5, 3.2, pal)
        cv.set(4, 4, X.T)
        cv.set(5, 4, X.T)
        cv.set(4, 5, X.T)
        cv.set(5, 5, X.T)
        for t in range(0, 9):
            x, y = 6 + t, 6 + t
            cv.set(x, y, X.GOLD[2] if t % 2 else X.GOLD[1])
        for (x, y) in ((11, 13), (12, 14), (13, 10), (14, 11)):
            cv.set(x, y, X.GOLD[2])
        cv.outline(pal[0])
        return cv

    # ------------------------------------------------------------------ blocs de surface
    SURFACES = [
        # id, en, fr, couleur, type (grass = dessus herbeux sur sol), sol associé, son
        ("sky_soil", "Sky Soil", "Terre céleste", (0xB0, 0xA8, 0xC8), "soil", None, "GRAVEL"),
        ("sky_grass", "Sky Grass", "Herbe céleste", (0x80, 0xE8, 0xE0), "grass", "sky_soil", "GRASS"),
        ("blight_soil", "Blight Soil", "Terre corrompue", (0x50, 0x28, 0x24), "soil", None, "GRAVEL"),
        ("blight_grass", "Blight Grass", "Herbe corrompue", (0xB0, 0x20, 0x28), "grass", "blight_soil", "GRASS"),
        ("frost_soil", "Frost Soil", "Terre gelée", (0x88, 0x98, 0xA8), "soil", None, "GRAVEL"),
        ("frost_grass", "Frost Grass", "Herbe givrée", (0xE0, 0xF0, 0xFF), "grass", "frost_soil", "SNOW"),
        ("umbral_soil", "Umbral Soil", "Terre ombrale", (0x38, 0x30, 0x40), "soil", None, "GRAVEL"),
        ("umbral_moss", "Umbral Moss", "Mousse ombrale", (0x60, 0x40, 0xA0), "grass", "umbral_soil", "MOSS"),
        ("astral_sand", "Astral Sand", "Sable astral", (0x50, 0x38, 0x70), "soil", None, "SAND"),
        ("astral_moss", "Astral Moss", "Mousse astrale", (0xB0, 0x80, 0xF0), "grass", "astral_sand", "MOSS"),
    ]
    for sid, en, fr, rgb, kind, soil, snd in SURFACES:
        G.add_block(sid, hardness=0.6 if kind == "grass" else 0.5, resistance=0.6, sound=snd, tool=False,
                    en=en, fr=fr, mineable="shovel", loot="self" if kind == "soil" else (soil, False))
        G.tag_block("minecraft:dirt", rid(sid))
        G.tag_item("minecraft:dirt", rid(sid))
        G.tag_block("minecraft:overworld_carver_replaceables", rid(sid))
        if kind == "soil":
            G.save_tex("block", sid, X.tex_raw_block(sid, rgb))
        else:
            soil_rgb = next(s[3] for s in SURFACES if s[0] == soil)
            G.save_tex("block", sid + "_top", tex_grass_top(sid, rgb))
            G.save_tex("block", sid + "_side", tex_grass_side(sid, soil_rgb, rgb))
            wjson(os.path.join(ASSETS, "models", "block", sid + ".json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
                "top": rid("block/" + sid + "_top"), "side": rid("block/" + sid + "_side"), "bottom": rid("block/" + soil)}})

    # ------------------------------------------------------------------ bois
    WOODS = [
        # id, en, fr, écorce, bois, feuillage, particule
        ("cloudwood", "Cloudwood", "bois des nuages", (0xC8, 0xC8, 0xD8), (0xF0, 0xE8, 0xD8), (0x90, 0xF0, 0xE8), "END_ROD"),
        ("crimsonroot", "Crimsonroot", "racine pourpre", (0x50, 0x18, 0x1C), (0x90, 0x30, 0x30), (0xC0, 0x18, 0x28), "CRIMSON_SPORE"),
        ("frostpine", "Frostpine", "pin de givre", (0x50, 0x60, 0x70), (0xA8, 0xC0, 0xD0), (0x70, 0xA8, 0xB8), "SNOWFLAKE"),
    ]
    for wid, en, fr, bark, wood, leaf, particle in WOODS:
        log, planks, leaves = f"{wid}_log", f"{wid}_planks", f"{wid}_leaves"
        G.save_tex("block", log, tex_log_side(log, bark))
        G.save_tex("block", log + "_top", tex_log_top(log, bark, wood))
        G.save_tex("block", planks, tex_planks(planks, wood))
        G.save_tex("block", leaves, tex_leaves(leaves, leaf))
        G.add_block(log, kind="LOG", hardness=2.0, resistance=2.0, sound="WOOD", tool=False, mineable="axe",
                    en=en + " Log", fr="Bûche de " + fr)
        G.add_block(planks, hardness=2.0, resistance=3.0, sound="WOOD", tool=False, mineable="axe",
                    en=en + " Planks", fr="Planches de " + fr)
        G.add_block(leaves, kind="LEAVES", hardness=0.2, resistance=0.2, sound="GRASS", tool=False, mineable="hoe",
                    behavior=particle, en=en + " Leaves", fr="Feuilles de " + fr, loot="leaves")
        for t in ("minecraft:logs", "minecraft:logs_that_burn"):
            G.tag_block(t, rid(log)); G.tag_item(t, rid(log))
        G.tag_block("minecraft:planks", rid(planks)); G.tag_item("minecraft:planks", rid(planks))
        G.tag_block("minecraft:leaves", rid(leaves)); G.tag_item("minecraft:leaves", rid(leaves))
        G.shapeless(planks, planks, [log], 4, "building")

    # ------------------------------------------------------------------ portail et clés
    G.save_tex("block", "dimensional_gate", tex_gate())
    G.add_block("dimensional_gate", kind="GATE", hardness=50.0, resistance=1200.0, sound="METAL", light=12, level=3,
                en="Dimensional Gate", fr="Portail dimensionnel")
    G.shaped("dimensional_gate", "dimensional_gate", ["SOS", "OCO", "SOS"],
             {"S": "architect_stone", "O": "minecraft:crying_obsidian", "C": "architect_core"}, 1, "misc")
    G.LANG_EN[f"block.{NS}.dimensional_gate.desc"] = "Use a dimensional key on it to travel. Empty hand: return home."
    G.LANG_FR[f"block.{NS}.dimensional_gate.desc"] = "Utilisez une clé dimensionnelle dessus pour voyager. Main vide : retour."

    BOSS_CORES = [
        ("sky_emperor", "Sky Emperor's Core", "Cœur de l'Empereur des Cieux", (0xF0, 0xF8, 0xFF)),
        ("corruption_heart", "Corruption's Core", "Cœur de la Corruption", (0xC0, 0x10, 0x20)),
        ("abyss_king", "Abyss King's Core", "Cœur du Roi des Abysses", (0x20, 0xC0, 0xD0)),
        ("volcanic_titan", "Volcanic Titan's Core", "Cœur du Titan Volcanique", (0xFF, 0x60, 0x10)),
        ("frost_queen", "Frost Queen's Core", "Cœur de la Reine du Givre", (0xC0, 0xF0, 0xFF)),
        ("depths_devourer", "Depths Devourer's Core", "Cœur du Dévoreur Souterrain", (0x80, 0x50, 0xC0)),
    ]
    for cid, en, fr, rgb in BOSS_CORES:
        G.add_item(f"{cid}_core", "LEGENDARY", en, fr,
                   "Dropped by a dimensional guardian. Required to open the way to the Void.",
                   "Lâché par un gardien dimensionnel. Nécessaire pour ouvrir la voie du Néant.",
                   canvas=X.item_symmetric("orb", rgb))
    for d in C.DIMENSIONS:
        kid = f"{d['id']}_key"
        G.add_item(kid, "EPIC" if d["id"] != "void" else "MYTHIC", f"{d['en']} Key", f"Clé — {d['fr']}",
                   "Use on a Dimensional Gate to travel to this dimension.",
                   "Utilisez-la sur un Portail dimensionnel pour rejoindre cette dimension.",
                   canvas=tex_key(d["accent"]))
        if d["id"] == "void":
            G.shaped(kid, kid, ["ABC", "DVE", "FNF"], {
                "A": "sky_emperor_core", "B": "corruption_heart_core", "C": "abyss_king_core",
                "D": "volcanic_titan_core", "E": "frost_queen_core", "F": "depths_devourer_core",
                "V": "void_fragment", "N": "minecraft:nether_star"}, 1, "misc")
        else:
            G.shaped(kid, kid, ["FFF", "FCF", "FFF"], {"F": f"{d['id']}_fragment", "C": "architect_core"}, 1, "misc")
        G.tag_item(f"{NS}:dimension_keys", rid(kid))

    # ------------------------------------------------------------------ pierres : tags de génération
    for d in C.DIMENSIONS:
        for s in (d["stone"], "deep_" + d["stone"]):
            for t in ("minecraft:overworld_carver_replaceables", "minecraft:base_stone_overworld",
                      "minecraft:nether_carver_replaceables", "minecraft:dripstone_replaceable_blocks"):
                G.tag_block(t, rid(s))

    # ------------------------------------------------------------------ features communes
    def configured(name, obj):
        wjson(os.path.join(DATA, "worldgen", "configured_feature", name + ".json"), obj)

    def placed(name, feature, placement):
        wjson(os.path.join(DATA, "worldgen", "placed_feature", name + ".json"), {"feature": feature, "placement": placement})
        return rid(name)

    def surface_placement(count, block=None):
        p = [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
             {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"}]
        if block:
            p.append({"type": "minecraft:block_predicate_filter", "predicate": {
                "type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": rid(block)}})
        p.append({"type": "minecraft:biome"})
        return p

    def layered(count):
        return [{"type": "minecraft:count_on_every_layer", "count": count}, {"type": "minecraft:biome"}]

    def tree_config(wid, soil, style):
        leaves = {"Name": rid(f"{wid}_leaves"), "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}
        if style == "spruce":
            foliage = {"type": "minecraft:spruce_foliage_placer", "offset": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 2},
                       "radius": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3},
                       "trunk_height": {"type": "minecraft:uniform", "min_inclusive": 1, "max_inclusive": 2}}
            trunk = {"type": "minecraft:straight_trunk_placer", "base_height": 7, "height_rand_a": 3, "height_rand_b": 1}
        else:
            r = 3 if style == "big" else 2
            foliage = {"type": "minecraft:blob_foliage_placer", "height": 3, "offset": 0, "radius": r}
            trunk = {"type": "minecraft:straight_trunk_placer", "base_height": 5 if style == "big" else 4, "height_rand_a": 3, "height_rand_b": 0}
        return {"type": "minecraft:tree", "config": {
            "decorators": [], "dirt_provider": {"type": "minecraft:simple_state_provider", "state": mc(soil)},
            "foliage_placer": foliage, "foliage_provider": {"type": "minecraft:simple_state_provider", "state": leaves},
            "force_dirt": False, "ignore_vines": True,
            "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
            "trunk_placer": trunk,
            "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": rid(f"{wid}_log"), "Properties": {"axis": "y"}}}}}

    configured("cloudwood_tree", tree_config("cloudwood", "sky_soil", "oak"))
    configured("crimsonroot_tree", tree_config("crimsonroot", "blight_soil", "big"))
    configured("frostpine_tree", tree_config("frostpine", "frost_soil", "spruce"))
    F = {}
    F["cloudwood_sparse"] = placed("cloudwood_sparse", rid("cloudwood_tree"), surface_placement(2, "sky_grass"))
    F["cloudwood_dense"] = placed("cloudwood_dense", rid("cloudwood_tree"), surface_placement(10, "sky_grass"))
    F["crimsonroot_sparse"] = placed("crimsonroot_sparse", rid("crimsonroot_tree"), surface_placement(2, "blight_grass"))
    F["crimsonroot_dense"] = placed("crimsonroot_dense", rid("crimsonroot_tree"), surface_placement(12, "blight_grass"))
    F["frostpine_sparse"] = placed("frostpine_sparse", rid("frostpine_tree"), surface_placement(2, "frost_grass"))
    F["frostpine_dense"] = placed("frostpine_dense", rid("frostpine_tree"), surface_placement(10, "frost_grass"))

    def simple_block(name, block, placement):
        configured(name, {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider", "state": mc(block)}}})
        return placed(name, rid(name), placement)

    F["star_stones"] = simple_block("star_stones", "starlight_block", surface_placement(3, "sky_grass"))
    F["ruin_pillars"] = simple_block("ruin_pillars", "chiseled_cloudstone", surface_placement(4))
    F["glowshrooms"] = simple_block("glowshrooms", "glowshroom_block", layered(2))
    F["ember_stones"] = simple_block("ember_stones", "ember_block", layered(3))
    F["void_crystals"] = simple_block("void_crystals", "nebula_block", surface_placement(2, "astral_moss"))
    F["ruin_blocks_void"] = simple_block("ruin_blocks_void", "architect_bricks", surface_placement(2))

    def geode(name, inner, alt, middle, outer, chance):
        g = json.load(open(os.path.join(HERE, "templates", "geode.json")))
        b = g["config"]["blocks"]
        b["inner_layer_provider"]["state"] = mc(inner)
        b["alternate_inner_layer_provider"]["state"] = mc(alt)
        b["middle_layer_provider"]["state"] = mc(middle)
        b["outer_layer_provider"]["state"] = mc(outer)
        configured(name, g)
        return placed(name, rid(name), [{"type": "minecraft:rarity_filter", "chance": chance}, {"type": "minecraft:in_square"},
                                        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                                                                                      "min_inclusive": {"above_bottom": 8}, "max_inclusive": {"below_top": 8}}},
                                        {"type": "minecraft:biome"}])

    F["celestial_geode"] = geode("celestial_geode", "astral_crystal_block", "celestial_crystal_block", "arcane_stone", "polished_cloudstone", 6)
    F["luminite_geode"] = geode("luminite_geode", "luminite_block", "mana_crystal_block", "arcane_stone", "polished_umbral_rock", 8)
    F["void_geode"] = geode("void_geode", "cosmic_opal_block", "void_shard_block", "enchanted_obsidian", "polished_voidstone", 10)
    F["frost_geode"] = geode("frost_geode", "everfrost_gem_block", "rime_sapphire_block", "minecraft:packed_ice", "polished_frostrock", 12)

    # Minerais et poches de pierre profonde par dimension
    ORES_BY_DIM = {}
    for d in C.DIMENSIONS:
        s = d["stone"]
        deep_blob = f"{d['id']}_deep_stone_blobs"
        configured(deep_blob, {"type": "minecraft:ore", "config": {"discard_chance_on_air_exposure": 0.0, "size": 48, "targets": [
            {"target": {"predicate_type": "minecraft:block_match", "block": rid(s)}, "state": mc("deep_" + s)}]}})
        lst = [placed(deep_blob, rid(deep_blob), [{"type": "minecraft:count", "count": 10}, {"type": "minecraft:in_square"},
                                                  {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                                                                                                "min_inclusive": {"above_bottom": 0}, "max_inclusive": {"below_top": 0}}},
                                                  {"type": "minecraft:biome"}])]
        mult = 3 if d["id"] in ("celestial", "void") else 1
        for m in C.MATERIALS:
            if m["dim"] != d["id"]:
                continue
            ore = f"{m['id']}_ore"
            size = {1: 10, 2: 8, 3: 5}[m["level"]]
            count = {1: 14, 2: 9, 3: 5}[m["level"]] * mult
            configured(ore, {"type": "minecraft:ore", "config": {"discard_chance_on_air_exposure": 0.0, "size": size, "targets": [
                {"target": {"predicate_type": "minecraft:block_match", "block": rid(s)}, "state": mc(ore)},
                {"target": {"predicate_type": "minecraft:block_match", "block": rid("deep_" + s)}, "state": mc("deep_" + ore)}]}})
            lst.append(placed(ore, rid(ore), [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
                                              {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
                                                                                            "min_inclusive": {"above_bottom": 0}, "max_inclusive": {"below_top": 0}}},
                                              {"type": "minecraft:biome"}]))
        ORES_BY_DIM[d["id"]] = lst

    # ------------------------------------------------------------------ biomes
    V = lambda n: "minecraft:" + n  # noqa: E731  (features vanilla réutilisées)

    def biome(id, en, fr, temp, hum, top, under, feats, grass, water, sky, fog, particle=None, prob=0.0, precip=True,
              temperature=0.7, extra=None):
        return dict(id=id, en=en, fr=fr, t=temp, h=hum, top=top, under=under, feats=feats, grass=grass, water=water,
                    sky=sky, fog=fog, particle=particle, prob=prob, precip=precip, temperature=temperature, extra=extra or {})

    DIMS = {
        "celestial": dict(router="floating_islands", noise={"height": 256, "min_y": 0, "size_horizontal": 2, "size_vertical": 1},
                          sea=-64, fluid="minecraft:water", aquifers=False, legacy=True, floor=False, ceiling=False,
                          type=dict(min_y=0, height=256, skylight=True, ceiling=False, ambient=0.05, timelines="#minecraft:in_overworld",
                                    sky="#8fd8ff", fog="#e6f4ff", cloud_height=56.0, music="minecraft:music.game", beds="normal"),
                          carvers=[V("cave")],
                          biomes=[
                              biome("sky_meadow", "Sky Meadow", "Prairie céleste", 0.3, -0.4, "sky_grass", "sky_soil",
                                    {VEG: [F["cloudwood_sparse"], V("patch_grass_plain"), V("flower_default")]}, "#8fe8d8", "#7fd0ff", "#8fd8ff", "#e6f4ff", "minecraft:end_rod", 0.0008),
                              biome("cloud_forest", "Cloud Forest", "Forêt des nuages", 0.3, 0.6, "sky_grass", "sky_soil",
                                    {UNDER_STRUCT: [F["celestial_geode"]], VEG: [F["cloudwood_dense"], V("patch_grass_forest")]}, "#a0f0e0", "#7fd0ff", "#a8e0ff", "#f0f8ff", "minecraft:white_ash", 0.004),
                              biome("floating_peaks", "Floating Peaks", "Montagnes flottantes", -0.6, -0.5, "cloudstone", "cloudstone",
                                    {UNDER_STRUCT: [F["celestial_geode"]]}, "#c0e0f0", "#7fd0ff", "#9fd0ff", "#e0ecff", None, 0, True, 0.1),
                              biome("star_garden", "Star Garden", "Jardin des étoiles", 0.7, 0.2, "sky_grass", "sky_soil",
                                    {VEG: [F["star_stones"], V("flower_flower_forest"), V("patch_grass_plain")]}, "#b0f8ff", "#9fe8ff", "#6f8fff", "#d8e0ff", "minecraft:end_rod", 0.003),
                              biome("suspended_ruins", "Suspended Ruins", "Ruines suspendues", -0.3, 0.3, "polished_cloudstone", "cloudstone_bricks",
                                    {SURF_STRUCT: [F["ruin_pillars"]]}, "#c8e8e8", "#7fd0ff", "#a0c8f0", "#e0e8f0", "minecraft:white_ash", 0.002),
                          ]),
        "corrupted": dict(router="overworld", noise={"height": 384, "min_y": -64, "size_horizontal": 1, "size_vertical": 2},
                          sea=63, fluid="minecraft:water", aquifers=True, legacy=False, floor=True, ceiling=False,
                          type=dict(min_y=-64, height=384, skylight=True, ceiling=False, ambient=0.0, timelines="#minecraft:in_overworld",
                                    sky="#5a1018", fog="#6a1a1a", cloud_height=192.33, music="minecraft:music.nether.crimson_forest", beds="normal"),
                          carvers=[V("cave"), V("cave_extra_underground"), V("canyon")],
                          biomes=[
                              biome("scarlet_forest", "Scarlet Forest", "Forêt écarlate", 0.4, 0.5, "blight_grass", "blight_soil",
                                    {VEG: [F["crimsonroot_dense"], V("patch_crimson_roots")]}, "#c02030", "#6a0f1a", "#5a1018", "#6a1a1a", "minecraft:crimson_spore", 0.02),
                              biome("corrupted_swamp", "Corrupted Swamp", "Marais corrompu", 0.6, 0.9, "blight_soil", "blight_soil",
                                    {VEG: [F["crimsonroot_sparse"], V("patch_waterlily")]}, "#802028", "#3a0a10", "#40101a", "#3a1010", "minecraft:crimson_spore", 0.03),
                              biome("root_plains", "Root Plains", "Plaine des racines", 0.0, 0.0, "blight_grass", "blight_soil",
                                    {VEG: [F["crimsonroot_sparse"], V("patch_crimson_roots")]}, "#a01828", "#6a0f1a", "#6a1820", "#702020", "minecraft:crimson_spore", 0.008),
                              biome("infected_mountains", "Infected Mountains", "Montagnes infectées", -0.6, -0.4, "blightstone", "blightstone",
                                    {}, "#901828", "#6a0f1a", "#501018", "#602020", "minecraft:crimson_spore", 0.005),
                              biome("red_ash_desert", "Red Ash Desert", "Désert de cendres rouges", 0.9, -0.8, "minecraft:red_sand", "minecraft:red_sandstone",
                                    {VEG: [V("patch_dead_bush_2"), V("patch_cactus_desert")]}, "#a05030", "#6a1a10", "#a04030", "#a05040", "minecraft:ash", 0.03, False, 2.0),
                          ]),
        "ocean": dict(router="overworld", noise={"height": 384, "min_y": -64, "size_horizontal": 1, "size_vertical": 2},
                      sea=150, fluid="minecraft:water", aquifers=True, legacy=False, floor=True, ceiling=False,
                      type=dict(min_y=-64, height=384, skylight=True, ceiling=False, ambient=0.0, timelines="#minecraft:in_overworld",
                                sky="#5ad0e8", fog="#90e0f0", cloud_height=192.33, music="minecraft:music.under_water", beds="normal"),
                      carvers=[V("cave"), V("canyon")],
                      biomes=[
                          biome("luminous_reef", "Luminous Reef", "Récif lumineux", 0.7, 0.3, "minecraft:sand", "abyssal_stone",
                                {VEG: [V("warm_ocean_vegetation"), V("sea_pickle"), V("seagrass_deep")]}, "#40c0a0", "#30e0f0", "#5ad0e8", "#90e0f0", "minecraft:underwater", 0.02, True, 0.8,
                                {"minecraft:visual/water_fog_color": "#0a6a7a"}),
                          biome("abyss", "Abyss", "Abysses", -0.5, -0.5, "abyssal_stone", "deep_abyssal_stone",
                                {VEG: [V("kelp_cold")]}, "#205050", "#103050", "#3a90b0", "#204050", "minecraft:underwater", 0.03, True, 0.5,
                                {"minecraft:visual/water_fog_color": "#020a14"}),
                          biome("coral_forest", "Coral Forest", "Forêt de corail", 0.8, 0.8, "minecraft:sand", "minecraft:sandstone",
                                {VEG: [V("warm_ocean_vegetation"), V("kelp_warm"), V("seagrass_deep")]}, "#40e0b0", "#40d0e0", "#6ae0f0", "#a0f0ff", "minecraft:underwater", 0.02, True, 0.9,
                                {"minecraft:visual/water_fog_color": "#0a5060"}),
                          biome("sunken_plains", "Sunken Plains", "Plaine engloutie", 0.2, -0.2, "minecraft:gravel", "abyssal_stone",
                                {VEG: [V("seagrass_deep"), V("kelp_cold")]}, "#409080", "#2a80b0", "#5ac0e0", "#80d0e0", "minecraft:underwater", 0.01, True, 0.6,
                                {"minecraft:visual/water_fog_color": "#05304a"}),
                          biome("dark_trench", "Dark Trench", "Tranchée obscure", -0.8, 0.6, "deep_abyssal_stone", "deep_abyssal_stone",
                                {}, "#103030", "#081830", "#204060", "#101820", "minecraft:underwater", 0.04, True, 0.4,
                                {"minecraft:visual/water_fog_color": "#000408"}),
                      ]),
        "chaos": dict(router="nether", noise={"height": 128, "min_y": 0, "size_horizontal": 1, "size_vertical": 2},
                      sea=32, fluid="minecraft:lava", aquifers=False, legacy=True, floor=True, ceiling=True,
                      type=dict(min_y=0, height=256, logical=128, skylight=False, ceiling=True, ambient=0.3, timelines="#minecraft:in_nether",
                                sky="#300808", fog="#401008", cloud_height=None, music="minecraft:music.nether.basalt_deltas", beds="explode"),
                      carvers=[V("nether_cave")],
                      biomes=[
                          biome("lava_ocean", "Lava Ocean", "Océan de lave", 0.0, 0.0, "cinderstone", "cinderstone",
                                {LOCAL: [V("delta")]}, "#803010", "#ff6010", "#300808", "#501008", "minecraft:lava", 0.002, False, 2.0),
                          biome("ember_forest", "Ember Forest", "Forêt de braises", 0.6, 0.5, "minecraft:magma_block", "cinderstone",
                                {VEG: [F["ember_stones"], V("patch_fire")]}, "#c04010", "#ff6010", "#401008", "#601808", "minecraft:white_ash", 0.03, False, 2.0),
                          biome("volcanic_mountains", "Volcanic Mountains", "Montagnes volcaniques", -0.6, -0.4, "minecraft:basalt", "cinderstone",
                                {SURF_STRUCT: [V("basalt_pillar")], VEG: [V("small_basalt_columns"), V("large_basalt_columns")]}, "#602010", "#ff6010", "#301010", "#3a1a14", "minecraft:ash", 0.05, False, 2.0),
                          biome("magma_desert", "Magma Desert", "Désert de magma", 0.8, -0.7, "minecraft:magma_block", "cinderstone",
                                {VEG: [V("patch_fire")]}, "#a03010", "#ff6010", "#501008", "#6a2008", "minecraft:white_ash", 0.02, False, 2.0),
                          biome("chaos_citadel", "Chaos Citadel", "Citadelle du chaos", -0.3, 0.6, "polished_cinderstone", "cinderstone_bricks",
                                {VEG: [V("patch_soul_fire")]}, "#401010", "#ff6010", "#200404", "#2a0606", "minecraft:soul_fire_flame", 0.004, False, 2.0),
                      ]),
        "eternal": dict(router="overworld", noise={"height": 384, "min_y": -64, "size_horizontal": 1, "size_vertical": 2},
                        sea=63, fluid="minecraft:water", aquifers=True, legacy=False, floor=True, ceiling=False,
                        type=dict(min_y=-64, height=384, skylight=True, ceiling=False, ambient=0.0, timelines="#minecraft:in_overworld",
                                  sky="#c8e8ff", fog="#e8f4ff", cloud_height=150.0, music="minecraft:music.overworld.frozen_peaks", beds="normal"),
                        carvers=[V("cave"), V("cave_extra_underground"), V("canyon")],
                        biomes=[
                            biome("frozen_forest", "Frozen Forest", "Forêt givrée", 0.3, 0.5, "frost_grass", "frost_soil",
                                  {VEG: [F["frostpine_dense"]], TOP: [V("freeze_top_layer")]}, "#a0c8d8", "#90c8f0", "#c8e8ff", "#e8f4ff", "minecraft:snowflake", 0.01, True, -0.5),
                            biome("eternal_glacier", "Eternal Glacier", "Glacier éternel", -0.6, -0.5, "minecraft:packed_ice", "minecraft:blue_ice",
                                  {SURF_STRUCT: [V("ice_spike")], TOP: [V("freeze_top_layer")]}, "#c0e0f0", "#a0d8ff", "#d8f0ff", "#f0f8ff", "minecraft:snowflake", 0.02, True, -0.8),
                            biome("white_mountains", "White Mountains", "Montagnes blanches", -0.3, 0.0, "minecraft:snow_block", "frostrock",
                                  {UNDER_STRUCT: [F["frost_geode"]], TOP: [V("freeze_top_layer")]}, "#e0f0ff", "#a0d8ff", "#d0e8ff", "#ffffff", "minecraft:snowflake", 0.03, True, -0.7),
                            biome("frozen_lake", "Frozen Lake", "Lac gelé", 0.6, 0.8, "frost_soil", "frost_soil",
                                  {LOCAL: [V("ice_patch")], VEG: [F["frostpine_sparse"]], TOP: [V("freeze_top_layer")]}, "#a0c0d0", "#80b8f0", "#b8e0ff", "#e0f0ff", "minecraft:snowflake", 0.005, True, -0.5),
                            biome("ice_kingdom", "Ice Kingdom", "Royaume de glace", 0.0, -0.8, "minecraft:packed_ice", "frostrock_tiles",
                                  {SURF_STRUCT: [V("ice_spike")], TOP: [V("freeze_top_layer")]}, "#d0f0ff", "#a0e0ff", "#a0c8ff", "#e8f8ff", "minecraft:end_rod", 0.002, True, -0.9),
                        ]),
        "depths": dict(router="caves", noise={"height": 192, "min_y": -64, "size_horizontal": 1, "size_vertical": 2},
                       sea=-32, fluid="minecraft:water", aquifers=False, legacy=True, floor=True, ceiling=True,
                       type=dict(min_y=-64, height=192, logical=192, skylight=False, ceiling=True, ambient=0.25, timelines="#minecraft:in_nether",
                                 sky="#000000", fog="#1a1428", cloud_height=None, music="minecraft:music.overworld.deep_dark", beds="explode"),
                       carvers=[V("cave"), V("cave_extra_underground")],
                       biomes=[
                           biome("crystal_caverns", "Crystal Caverns", "Cavernes cristallines", 0.3, -0.4, "umbral_rock", "umbral_rock",
                                 {UNDER_STRUCT: [F["luminite_geode"], V("amethyst_geode")], UNDER_DECO: [V("large_dripstone"), V("pointed_dripstone")]},
                                 "#6040a0", "#3050a0", "#000000", "#202040", "minecraft:glow", 0.006, False, 0.5),
                           biome("fungal_forest", "Fungal Forest", "Forêt fongique", 0.6, 0.6, "umbral_moss", "umbral_soil",
                                 {VEG: [F["glowshrooms"], V("mushroom_island_vegetation"), V("spore_blossom")]}, "#7050c0", "#304080", "#000000", "#281838", "minecraft:spore_blossom_air", 0.02, False, 0.6),
                           biome("underground_abyss", "Underground Abyss", "Abysses souterrains", -0.6, -0.6, "deep_umbral_rock", "deep_umbral_rock",
                                 {}, "#302040", "#101828", "#000000", "#080610", "minecraft:ash", 0.01, False, 0.4),
                           biome("buried_ruins", "Buried Ruins", "Ruines enfouies", 0.0, 0.3, "polished_umbral_rock", "umbral_rock_bricks",
                                 {VEG: [F["glowshrooms"]], UNDER_DECO: [V("cave_vines")]}, "#504060", "#203040", "#000000", "#1a1820", "minecraft:white_ash", 0.004, False, 0.5),
                           biome("shadow_realm", "Shadow Realm", "Royaume des ombres", -0.3, 0.8, "deep_umbral_rock", "umbral_rock",
                                 {}, "#201830", "#080818", "#000000", "#040208", "minecraft:sculk_soul", 0.003, False, 0.3),
                       ]),
        "void": dict(router="end", noise={"height": 128, "min_y": 0, "size_horizontal": 2, "size_vertical": 1},
                     sea=0, fluid="minecraft:air", aquifers=False, legacy=True, floor=False, ceiling=False,
                     type=dict(min_y=0, height=256, logical=256, skylight=True, ceiling=False, ambient=0.2, timelines="#minecraft:in_end",
                               sky="#000000", fog="#100818", cloud_height=None, music="minecraft:music.end", beds="explode", skybox="end"),
                     carvers=[],
                     biomes=[
                         biome("void_desert", "Void Desert", "Désert du vide", 0.6, -0.6, "astral_sand", "voidstone",
                               {}, "#503070", "#4020a0", "#000000", "#100818", "minecraft:portal", 0.004, False, 0.5),
                         biome("stellar_islands", "Stellar Islands", "Îles stellaires", 0.3, 0.5, "astral_moss", "astral_sand",
                               {UNDER_STRUCT: [F["void_geode"]], VEG: [F["void_crystals"]]}, "#a070e0", "#6040c0", "#000000", "#180c28", "minecraft:end_rod", 0.003, False, 0.5),
                         biome("astral_ocean", "Astral Ocean", "Océan astral", -0.4, 0.6, "astral_moss", "voidstone",
                               {VEG: [V("chorus_plant")]}, "#8050c0", "#6040c0", "#000000", "#0c0820", "minecraft:reverse_portal", 0.01, False, 0.5),
                         biome("dimensional_ruins", "Dimensional Ruins", "Ruines dimensionnelles", 0.0, 0.0, "polished_voidstone", "voidstone_bricks",
                               {SURF_STRUCT: [F["ruin_blocks_void"]]}, "#604080", "#4020a0", "#000000", "#140a20", "minecraft:portal", 0.006, False, 0.5),
                         biome("void_realm", "Void Realm", "Royaume du néant", -0.7, -0.5, "voidstone", "deep_voidstone",
                               {}, "#301848", "#200a50", "#000000", "#04020a", "minecraft:reverse_portal", 0.02, False, 0.5),
                     ]),
    }

    overworld_router = json.load(open(os.path.join(HERE, "templates", "noise_router", "overworld.json")))
    BIOME_IDS = {}
    for dim in C.DIMENSIONS:
        did = dim["id"]
        cfg = DIMS[did]
        s = dim["stone"]
        # Ordre global des features (évite les cycles d'ordre de features entre biomes d'une même dimension).
        global_order = [[] for _ in range(11)]

        def push(step, f):
            if f not in global_order[step]:
                global_order[step].append(f)

        for b in cfg["biomes"]:
            for f in ORES_BY_DIM[did]:
                push(ORES, f)
            for step, fs in b["feats"].items():
                for f in fs:
                    push(step, f)
        surface_top, surface_under = [], []
        bsrc = []
        for b in cfg["biomes"]:
            feats = [[] for _ in range(11)]
            wanted = {ORES: list(ORES_BY_DIM[did])}
            for step, fs in b["feats"].items():
                wanted.setdefault(step, []).extend(fs)
            for step, fs in wanted.items():
                feats[step] = sorted(set(fs), key=global_order[step].index)
            attrs = {"minecraft:visual/sky_color": b["sky"], "minecraft:visual/fog_color": b["fog"]}
            if b["particle"]:
                attrs["minecraft:visual/ambient_particles"] = [{"particle": {"type": b["particle"]}, "probability": b["prob"]}]
            attrs.update(b["extra"])
            bj = {"attributes": attrs, "carvers": cfg["carvers"], "downfall": 0.5 if b["precip"] else 0.0,
                  "effects": {"water_color": b["water"], "grass_color": b["grass"], "foliage_color": b["grass"]},
                  "features": feats, "has_precipitation": b["precip"], "spawn_costs": {},
                  "spawners": {k: [] for k in ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature",
                                               "water_ambient", "water_creature")},
                  "temperature": b["temperature"]}
            wjson(os.path.join(DATA, "worldgen", "biome", b["id"] + ".json"), bj)
            G.LANG_EN[f"biome.{NS}.{b['id']}"] = b["en"]
            G.LANG_FR[f"biome.{NS}.{b['id']}"] = b["fr"]
            BIOME_IDS.setdefault(did, []).append(b["id"])
            cond = {"type": "minecraft:biome", "biome_is": [rid(b["id"])]}
            surface_top.append({"type": "minecraft:condition", "if_true": cond, "then_run": {"type": "minecraft:sequence", "sequence": [
                {"type": "minecraft:condition", "if_true": {"type": "minecraft:water", "offset": -1, "surface_depth_multiplier": 0, "add_stone_depth": False},
                 "then_run": {"type": "minecraft:block", "result_state": mc(b["top"])}},
                {"type": "minecraft:block", "result_state": mc(b["under"])}]}})
            surface_under.append({"type": "minecraft:condition", "if_true": cond,
                                  "then_run": {"type": "minecraft:block", "result_state": mc(b["under"])}})
            point = {"temperature": b["t"], "humidity": b["h"], "continentalness": 0.0, "erosion": 0.0, "weirdness": 0.0,
                     "depth": 0.0, "offset": 0.0}
            if did == "ocean":
                point["continentalness"] = {"abyss": -0.8, "dark_trench": -1.0}.get(b["id"], -0.3)
            if b["id"] in ("infected_mountains", "white_mountains"):
                point["erosion"] = -0.7
                point["continentalness"] = 0.5
            bsrc.append({"biome": rid(b["id"]), "parameters": point})

        rules = []
        bedrock = {"type": "minecraft:block", "result_state": {"Name": "minecraft:bedrock"}}
        if cfg["floor"]:
            rules.append({"type": "minecraft:condition", "if_true": {"type": "minecraft:vertical_gradient", "random_name": f"{NS}:{did}_floor",
                                                                     "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}},
                          "then_run": bedrock})
        if cfg["ceiling"]:
            rules.append({"type": "minecraft:condition", "if_true": {"type": "minecraft:not", "invert": {
                "type": "minecraft:vertical_gradient", "random_name": f"{NS}:{did}_roof",
                "true_at_and_below": {"below_top": 5}, "false_at_and_above": {"below_top": 0}}}, "then_run": bedrock})
        rules.append({"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                                                                 "add_surface_depth": False, "secondary_depth_range": 0},
                      "then_run": {"type": "minecraft:sequence", "sequence": surface_top}})
        rules.append({"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                                                                 "add_surface_depth": True, "secondary_depth_range": 0},
                      "then_run": {"type": "minecraft:sequence", "sequence": surface_under}})
        router = json.load(open(os.path.join(HERE, "templates", "noise_router", cfg["router"] + ".json")))
        for k in ("temperature", "vegetation"):
            if not isinstance(router.get(k), dict):
                router[k] = overworld_router[k]
        fluid = {"Name": cfg["fluid"]}
        if cfg["fluid"] != "minecraft:air":
            fluid["Properties"] = {"level": "0"}
        wjson(os.path.join(DATA, "worldgen", "noise_settings", did + ".json"), {
            "sea_level": cfg["sea"], "disable_mob_generation": False, "aquifers_enabled": cfg["aquifers"], "ore_veins_enabled": False,
            "legacy_random_source": cfg["legacy"], "default_block": mc(s), "default_fluid": fluid, "noise": cfg["noise"],
            "noise_router": router, "spawn_target": [], "surface_rule": {"type": "minecraft:sequence", "sequence": rules}})

        t = cfg["type"]
        attrs = {"minecraft:visual/sky_color": t["sky"], "minecraft:visual/fog_color": t["fog"],
                 "minecraft:audio/background_music": {"default": {"min_delay": 6000, "max_delay": 18000, "replace_current_music": True,
                                                                  "sound": t["music"]}}}
        if t["cloud_height"] is not None:
            attrs["minecraft:visual/cloud_height"] = t["cloud_height"]
            attrs["minecraft:visual/cloud_color"] = "#ccffffff"
        if t["beds"] == "normal":
            attrs["minecraft:gameplay/bed_rule"] = {"can_set_spawn": "always", "can_sleep": "when_dark",
                                                    "error_message": {"translate": "block.minecraft.bed.no_sleep"}}
        else:
            attrs["minecraft:gameplay/bed_rule"] = {"can_set_spawn": "never", "can_sleep": "never", "explodes": True}
        if not t["skylight"]:
            attrs["minecraft:visual/fog_end_distance"] = 96.0
            attrs["minecraft:visual/fog_start_distance"] = 10.0
        dtype = {"ambient_light": t["ambient"], "attributes": attrs, "coordinate_scale": 1.0, "has_ceiling": t["ceiling"],
                 "has_skylight": t["skylight"], "height": t["height"], "infiniburn": "#minecraft:infiniburn_overworld",
                 "logical_height": t.get("logical", t["height"]), "min_y": t["min_y"],
                 "monster_spawn_block_light_limit": 0,
                 "monster_spawn_light_level": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7},
                 "timelines": t["timelines"]}
        if t["timelines"] != "#minecraft:in_overworld":
            dtype["has_fixed_time"] = True
        if "skybox" in t:
            dtype["skybox"] = t["skybox"]
        elif not t["skylight"]:
            dtype["skybox"] = "none"
        wjson(os.path.join(DATA, "dimension_type", did + ".json"), dtype)
        wjson(os.path.join(DATA, "dimension", did + ".json"), {"type": rid(did), "generator": {
            "type": "minecraft:noise", "settings": rid(did), "biome_source": {"type": "minecraft:multi_noise", "biomes": bsrc}}})

        # Succès d'entrée dans la dimension
        adv = {"parent": rid("root"), "display": {"icon": {"id": rid(f"{did}_key"), "count": 1},
                                                  "title": {"translate": f"advancements.{NS}.enter_{did}.title"},
                                                  "description": {"translate": f"advancements.{NS}.enter_{did}.description"},
                                                  "frame": "challenge" if did == "void" else "goal", "show_toast": True, "announce_to_chat": True},
               "criteria": {"entered": {"trigger": "minecraft:changed_dimension", "conditions": {"to": rid(did)}}},
               "requirements": [["entered"]]}
        wjson(os.path.join(DATA, "advancement", f"enter_{did}.json"), adv)
        G.LANG_EN[f"advancements.{NS}.enter_{did}.title"] = dim["en"]
        G.LANG_FR[f"advancements.{NS}.enter_{did}.title"] = dim["fr"]
        G.LANG_EN[f"advancements.{NS}.enter_{did}.description"] = f"Travel to the {dim['en']}."
        G.LANG_FR[f"advancements.{NS}.enter_{did}.description"] = f"Rejoignez : {dim['fr']}."
    return BIOME_IDS
