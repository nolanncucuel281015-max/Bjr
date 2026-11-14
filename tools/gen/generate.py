#!/usr/bin/env python3
"""Générateur de contenu pour Les Dimensions Oubliées.

Usage : python3 tools/gen/generate.py   (depuis la racine du projet)

Produit (et écrase) :
  src/main/java/com/forgotten/dimensions/registry/generated/ContentTables.java
  src/main/resources/assets/forgotten_dimensions/{blockstates,models,items,textures,equipment,lang}
  src/main/resources/data/forgotten_dimensions/{recipe,loot_table,tags}
  src/main/resources/data/minecraft/tags/...
Les fichiers écrits à la main ne sont jamais touchés : le générateur ne travaille que
dans des dossiers qu'il possède entièrement (voir OWNED_DIRS) ou sur des fichiers précis.
"""
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import content as C  # noqa: E402
import textures as X  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
NS = "forgotten_dimensions"
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", NS)
DATA = os.path.join(RES, "data", NS)
MCDATA = os.path.join(RES, "data", "minecraft")
JAVA_OUT = os.path.join(ROOT, "src", "main", "java", "com", "forgotten", "dimensions", "registry", "generated",
                        "ContentTables.java")

OWNED_DIRS = [
    os.path.join(ASSETS, "blockstates"), os.path.join(ASSETS, "models"), os.path.join(ASSETS, "items"),
    os.path.join(ASSETS, "textures", "block"), os.path.join(ASSETS, "textures", "item"),
    os.path.join(ASSETS, "textures", "entity", "equipment"), os.path.join(ASSETS, "equipment"),
    os.path.join(ASSETS, "textures", "entity", "creature"), os.path.join(ASSETS, "textures", "entity", "armor_detail"),
    os.path.join(ASSETS, "textures", "entity", "spell"), os.path.join(ASSETS, "textures", "entity", "summon"),
    os.path.join(DATA, "recipe"), os.path.join(DATA, "loot_table", "blocks"), os.path.join(DATA, "tags"),
    os.path.join(DATA, "loot_table", "entities"), os.path.join(ASSETS, "fd_entity_models"),
    os.path.join(MCDATA, "tags"), os.path.join(DATA, "advancement"), os.path.join(DATA, "worldgen"),
    os.path.join(DATA, "dimension"), os.path.join(DATA, "dimension_type"),
]

LANG_EN, LANG_FR = {}, {}
BLOCKS = []      # dicts : id, kind, hardness, resistance, sound, light, tool, base, xp, behavior, level, tags
ITEMS = []       # objets simples : id, rarity, fireproof
RECIPES = {}
TEXTURES_DONE = set()
ITEM_TAGS = {}   # tag -> set(ids)
ENTITY_TAGS = {}
BLOCK_TAGS = {}


def wjson(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def rid(path):
    return f"{NS}:{path}"


def tag_item(tag, value):
    ITEM_TAGS.setdefault(tag, set()).add(value)


def tag_block(tag, value):
    BLOCK_TAGS.setdefault(tag, set()).add(value)


def save_tex(kind, name, canvas):
    path = os.path.join(ASSETS, "textures", kind, name + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    canvas.save(path)
    TEXTURES_DONE.add(f"{kind}/{name}")


def clean_owned():
    for d in OWNED_DIRS:
        if os.path.isdir(d):
            shutil.rmtree(d)


clean_owned()


# ---------------------------------------------------------------------------
# Blocs
# ---------------------------------------------------------------------------
def add_block(id, kind="CUBE", hardness=1.5, resistance=6.0, sound="STONE", light=0, tool=True, base="",
              xp=(0, 0), behavior="none", level=0, en="", fr="", texture=None, loot="self", cutout=False,
              mineable="pickaxe"):
    BLOCKS.append(dict(id=id, kind=kind, hardness=hardness, resistance=resistance, sound=sound, light=light,
                       tool=tool, base=base, xp=xp, behavior=behavior, level=level, loot=loot, cutout=cutout))
    LANG_EN[f"block.{NS}.{id}"] = en
    LANG_FR[f"block.{NS}.{id}"] = fr
    tag_block("minecraft:mineable/" + mineable, rid(id))
    if level == 1:
        tag_block("minecraft:needs_stone_tool", rid(id))
    elif level == 2:
        tag_block("minecraft:needs_iron_tool", rid(id))
    elif level == 3:
        tag_block("minecraft:needs_diamond_tool", rid(id))
    tex = texture or id
    if kind == "LOG":
        for suffix, parent in (("", "cube_column"), ("_horizontal", "cube_column_horizontal")):
            wjson(os.path.join(ASSETS, "models", "block", id + suffix + ".json"),
                  {"parent": "minecraft:block/" + parent, "textures": {"end": rid("block/" + id + "_top"), "side": rid("block/" + id)}})
        wjson(os.path.join(ASSETS, "blockstates", id + ".json"), {"variants": {
            "axis=x": {"model": rid("block/" + id + "_horizontal"), "x": 90, "y": 90},
            "axis=y": {"model": rid("block/" + id)},
            "axis=z": {"model": rid("block/" + id + "_horizontal"), "x": 90}}})
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {"type": "minecraft:model", "model": rid("block/" + id)}})
    if kind in ("CUBE", "ORE", "TECH", "MAGIC", "LEAVES", "GATE"):
        wjson(os.path.join(ASSETS, "blockstates", id + ".json"), {"variants": {"": {"model": rid("block/" + id)}}})
        wjson(os.path.join(ASSETS, "models", "block", id + ".json"),
              {"parent": "minecraft:block/cube_all", "textures": {"all": rid("block/" + tex)}})
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {"type": "minecraft:model", "model": rid("block/" + id)}})
    elif kind == "STAIRS":
        t = rid("block/" + tex)
        for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
            wjson(os.path.join(ASSETS, "models", "block", id + suffix + ".json"),
                  {"parent": "minecraft:block/" + parent, "textures": {"bottom": t, "side": t, "top": t}})
        tmpl = open(os.path.join(HERE, "templates", "stairs_blockstate.json")).read().replace("{MODEL}", rid("block/" + id))
        wjson(os.path.join(ASSETS, "blockstates", id + ".json"), json.loads(tmpl))
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {"type": "minecraft:model", "model": rid("block/" + id)}})
        tag_block("minecraft:stairs", rid(id)); tag_item("minecraft:stairs", rid(id))
    elif kind == "SLAB":
        t = rid("block/" + tex)
        wjson(os.path.join(ASSETS, "models", "block", id + ".json"),
              {"parent": "minecraft:block/slab", "textures": {"bottom": t, "side": t, "top": t}})
        wjson(os.path.join(ASSETS, "models", "block", id + "_top.json"),
              {"parent": "minecraft:block/slab_top", "textures": {"bottom": t, "side": t, "top": t}})
        wjson(os.path.join(ASSETS, "blockstates", id + ".json"), {"variants": {
            "type=bottom": {"model": rid("block/" + id)},
            "type=double": {"model": rid("block/" + base)},
            "type=top": {"model": rid("block/" + id + "_top")}}})
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {"type": "minecraft:model", "model": rid("block/" + id)}})
        tag_block("minecraft:slabs", rid(id)); tag_item("minecraft:slabs", rid(id))
    elif kind == "WALL":
        t = rid("block/" + tex)
        for suffix, parent in (("_post", "template_wall_post"), ("_side", "template_wall_side"),
                               ("_side_tall", "template_wall_side_tall"), ("_inventory", "wall_inventory")):
            wjson(os.path.join(ASSETS, "models", "block", id + suffix + ".json"),
                  {"parent": "minecraft:block/" + parent, "textures": {"wall": t}})
        tmpl = open(os.path.join(HERE, "templates", "wall_blockstate.json")).read().replace("{MODEL}", rid("block/" + id))
        wjson(os.path.join(ASSETS, "blockstates", id + ".json"), json.loads(tmpl))
        wjson(os.path.join(ASSETS, "items", id + ".json"),
              {"model": {"type": "minecraft:model", "model": rid("block/" + id + "_inventory")}})
        tag_block("minecraft:walls", rid(id)); tag_item("minecraft:walls", rid(id))
    # Loot table
    if loot == "leaves":
        shears_or_silk = {"condition": "minecraft:any_of", "terms": [
            {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
            {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
                {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}
        lt = {"type": "minecraft:block", "pools": [
            {"bonus_rolls": 0.0, "rolls": 1.0, "entries": [{"type": "minecraft:item", "name": rid(id), "conditions": [shears_or_silk]}]},
            {"bonus_rolls": 0.0, "rolls": 1.0, "conditions": [{"condition": "minecraft:inverted", "term": shears_or_silk},
                                                               {"condition": "minecraft:random_chance", "chance": 0.05}],
             "entries": [{"type": "minecraft:item", "name": "minecraft:stick"}]}],
              "random_sequence": rid("blocks/" + id)}
    elif loot == "self":
        lt = {"type": "minecraft:block", "pools": [{"bonus_rolls": 0.0, "rolls": 1.0, "conditions": [
            {"condition": "minecraft:survives_explosion"}], "entries": [{"type": "minecraft:item", "name": rid(id)}]}],
              "random_sequence": rid("blocks/" + id)}
        if kind == "SLAB":
            lt = {"type": "minecraft:block", "pools": [{"bonus_rolls": 0.0, "rolls": 1.0, "entries": [
                {"type": "minecraft:item", "name": rid(id), "functions": [
                    {"function": "minecraft:set_count", "add": False, "count": 2.0, "conditions": [
                        {"condition": "minecraft:block_state_property", "block": rid(id), "properties": {"type": "double"}}]},
                    {"function": "minecraft:explosion_decay"}]}]}], "random_sequence": rid("blocks/" + id)}
    else:
        drop, ore_drops = loot
        fn = [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
              {"function": "minecraft:explosion_decay"}] if ore_drops else [{"function": "minecraft:explosion_decay"}]
        lt = {"type": "minecraft:block", "pools": [{"bonus_rolls": 0.0, "rolls": 1.0, "entries": [{
            "type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": rid(id), "conditions": [{"condition": "minecraft:match_tool", "predicate": {
                    "predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
                {"type": "minecraft:item", "name": rid(drop), "functions": fn}]}]}],
              "random_sequence": rid("blocks/" + id)}
    wjson(os.path.join(DATA, "loot_table", "blocks", id + ".json"), lt)


# ---------------------------------------------------------------------------
# Objets
# ---------------------------------------------------------------------------
def add_item(id, rarity="COMMON", en="", fr="", desc_en=None, desc_fr=None, fireproof=False, canvas=None,
             model="generated", register=True):
    if register:
        ITEMS.append(dict(id=id, rarity=rarity, fireproof=fireproof))
    LANG_EN[f"item.{NS}.{id}"] = en
    LANG_FR[f"item.{NS}.{id}"] = fr
    if desc_en:
        LANG_EN[f"item.{NS}.{id}.desc"] = desc_en
        LANG_FR[f"item.{NS}.{id}.desc"] = desc_fr
    if canvas is not None:
        save_tex("item", id, canvas)
    if model == "generated":
        item_model(id, "minecraft:item/generated")
    elif model == "handheld":
        item_model(id, "minecraft:item/handheld")


def item_model(id, parent, texture=None):
    wjson(os.path.join(ASSETS, "models", "item", id + ".json"),
          {"parent": parent, "textures": {"layer0": rid("item/" + (texture or id))}})
    wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {"type": "minecraft:model", "model": rid("item/" + id)}})


# ---------------------------------------------------------------------------
# Recettes
# ---------------------------------------------------------------------------
def ing(x):
    if x.startswith("#") or ":" in x:
        return x
    return rid(x)


def shaped(name, result, pattern, key, count=1, category="misc"):
    RECIPES[name] = {"type": "minecraft:crafting_shaped", "category": category,
                     "key": {k: ing(v) for k, v in key.items()}, "pattern": pattern,
                     "result": {"count": count, "id": ing(result)}}


def shapeless(name, result, ingredients, count=1, category="misc"):
    RECIPES[name] = {"type": "minecraft:crafting_shapeless", "category": category,
                     "ingredients": [ing(i) for i in ingredients], "result": {"count": count, "id": ing(result)}}


def cooking(name, result, ingredient, xp=0.7, blasting=True):
    RECIPES[name + "_from_smelting"] = {"type": "minecraft:smelting", "category": "misc", "cookingtime": 200,
                                        "experience": xp, "ingredient": ing(ingredient), "result": {"id": ing(result)}}
    if blasting:
        RECIPES[name + "_from_blasting"] = {"type": "minecraft:blasting", "category": "misc", "cookingtime": 100,
                                            "experience": xp, "ingredient": ing(ingredient), "result": {"id": ing(result)}}


def stonecut(result, ingredient, count=1):
    RECIPES[f"{result}_from_{ingredient}_stonecutting"] = {"type": "minecraft:stonecutting", "ingredient": ing(ingredient),
                                                            "result": {"count": count, "id": ing(result)}}


# ===========================================================================
# 1. Pierres dimensionnelles et blocs décoratifs
# ===========================================================================
STONE_TEX = {}
for d in C.DIMENSIONS:
    s, rgb, acc, snd = d["stone"], d["stone_color"], d["accent"], d["sound"]
    en, fr = d["stone_en"], d["stone_fr"]
    lvl_h = 1.5
    cv = X.tex_stone(s, rgb); save_tex("block", s, cv); STONE_TEX[s] = cv
    dcv = X.tex_stone("deep_" + s, rgb, deep=True); save_tex("block", "deep_" + s, dcv); STONE_TEX["deep_" + s] = dcv
    save_tex("block", f"{s}_bricks", X.tex_bricks(s + "_bricks", rgb))
    save_tex("block", f"polished_{s}", X.tex_polished("polished_" + s, rgb))
    save_tex("block", f"chiseled_{s}", X.tex_chiseled("chiseled_" + s, rgb, acc))
    save_tex("block", f"{s}_tiles", X.tex_tiles(s + "_tiles", rgb))
    add_block(s, sound=snd, en=en, fr=fr)
    add_block("deep_" + s, hardness=3.0, sound="DEEPSLATE", en="Deep " + en, fr=fr + " profonde")
    variants = [(f"{s}_bricks", en + " Bricks", "Briques de " + fr.lower()),
                (f"polished_{s}", "Polished " + en, fr + " polie"),
                (f"{s}_tiles", en + " Tiles", "Dalles de " + fr.lower())]
    add_block(f"chiseled_{s}", sound=snd, en="Chiseled " + en, fr=fr + " sculptée")
    for vid, ven, vfr in variants:
        add_block(vid, sound=snd, en=ven, fr=vfr)
    for vid, ven, vfr in [(s, en, fr)] + variants:
        stem = vid[:-1] if vid.endswith("s") and (vid.endswith("bricks") or vid.endswith("tiles")) else vid
        add_block(f"{stem}_stairs", kind="STAIRS", sound=snd, base=vid, texture=vid, en=ven.rstrip("s") + " Stairs" if ven.endswith("s") else ven + " Stairs",
                  fr="Escalier — " + vfr.lower())
        add_block(f"{stem}_slab", kind="SLAB", sound=snd, base=vid, texture=vid, en=(ven[:-1] if ven.endswith("s") else ven) + " Slab",
                  fr="Dalle — " + vfr.lower())
        add_block(f"{stem}_wall", kind="WALL", sound=snd, base=vid, texture=vid, en=(ven[:-1] if ven.endswith("s") else ven) + " Wall",
                  fr="Muret — " + vfr.lower())
        shaped(f"{stem}_stairs", f"{stem}_stairs", ["X  ", "XX ", "XXX"], {"X": vid}, 4, "building")
        shaped(f"{stem}_slab", f"{stem}_slab", ["XXX"], {"X": vid}, 6, "building")
        shaped(f"{stem}_wall", f"{stem}_wall", ["XXX", "XXX"], {"X": vid}, 6, "building")
        stonecut(f"{stem}_stairs", vid); stonecut(f"{stem}_slab", vid, 2); stonecut(f"{stem}_wall", vid)
    shaped(f"polished_{s}", f"polished_{s}", ["XX", "XX"], {"X": s}, 4, "building")
    shaped(f"{s}_bricks", f"{s}_bricks", ["XX", "XX"], {"X": f"polished_{s}"}, 4, "building")
    shaped(f"{s}_tiles", f"{s}_tiles", ["XX", "XX"], {"X": f"{s}_bricks"}, 4, "building")
    shaped(f"chiseled_{s}", f"chiseled_{s}", ["X", "X"], {"X": f"{s}_brick_slab"}, 1, "building")
    for v in (f"polished_{s}", f"{s}_bricks", f"{s}_tiles", f"chiseled_{s}"):
        stonecut(v, s)
    cooking(s, s, "deep_" + s, xp=0.1, blasting=False)

# ===========================================================================
# 2. Matériaux, minerais, blocs de stockage
# ===========================================================================
for m in C.MATERIALS:
    d = C.DIM_BY_ID[m["dim"]]
    s = d["stone"]
    mid, rgb, lvl = m["id"], m["color"], m["level"]
    rarity = {1: "COMMON", 2: "UNCOMMON", 3: "RARE"}[lvl]
    out = C.mat_item(mid)
    if m["kind"] == "ingot":
        add_item(f"raw_{mid}", rarity, "Raw " + m["en"], "Minerai brut " + m["fr_de"],
                 canvas=X.item_raw(rgb, "raw_" + mid))
        add_item(out, rarity, m["en"] + " Ingot", "Lingot " + m["fr_de"], canvas=X.item_ingot(rgb))
        drop = f"raw_{mid}"
        add_block(f"raw_{mid}_block", hardness=5.0, sound="STONE", level=lvl, en="Block of Raw " + m["en"],
                  fr="Bloc de minerai brut " + m["fr_de"])
        save_tex("block", f"raw_{mid}_block", X.tex_raw_block("raw_block_" + mid, rgb))
        shaped(f"raw_{mid}_block", f"raw_{mid}_block", ["XXX", "XXX", "XXX"], {"X": drop}, 1, "building")
        shapeless(f"raw_{mid}_from_block", drop, [f"raw_{mid}_block"], 9)
        cooking(out, out, drop)
        xp = (0, 0)
    else:
        add_item(out, rarity, m["en"], m["fr"], canvas=X.item_gem(rgb))
        drop = out
        xp = (2, 5) if lvl < 3 else (3, 7)
    # 2 minerais : pierre dimensionnelle et pierre profonde
    for prefix, stone_key, hard, snd in (("", s, 3.0, "STONE"), ("deep_", "deep_" + s, 4.5, "DEEPSLATE")):
        oid = f"{prefix}{mid}_ore"
        save_tex("block", oid, X.tex_ore(oid, STONE_TEX[stone_key], rgb))
        add_block(oid, kind="ORE", hardness=hard, resistance=3.0, sound=snd, level=lvl, xp=xp,
                  en=("Deep " if prefix else "") + m["en"] + " Ore",
                  fr="Minerai " + m["fr_de"] + (" profond" if prefix else ""), loot=(drop, True))
        tag_item(f"{NS}:ores", rid(oid)); tag_block(f"{NS}:ores", rid(oid))
        if m["kind"] == "ingot":
            cooking(f"{out}_from_{oid}", out, oid, xp=1.0)
    add_block(f"{mid}_block", hardness=5.0, resistance=6.0, sound="METAL" if m["kind"] == "ingot" else "AMETHYST_BLOCK",
              level=lvl, en="Block of " + m["en"], fr="Bloc " + m["fr_de"])
    save_tex("block", f"{mid}_block", X.tex_storage(mid + "_block", rgb, m["kind"]))
    shaped(f"{mid}_block", f"{mid}_block", ["XXX", "XXX", "XXX"], {"X": out}, 1, "building")
    shapeless(f"{out}_from_block", out, [f"{mid}_block"], 9)
    tag_item(f"{NS}:repair/{out}", rid(out))

# Minerais de la Surface : fragment ancien + fragments dimensionnels (Chapitre 2)
STONE_RGB = (0x80, 0x80, 0x80)
DEEPSLATE_RGB = (0x50, 0x50, 0x56)
vstone = X.tex_stone("overworld_stone", STONE_RGB)
vdeep = X.tex_stone("overworld_deepslate", DEEPSLATE_RGB, deep=True)
for it in C.EXTRA_ITEMS:
    canvas = X.item_icon(it["shape"], it["color"], it["id"])
    add_item(it["id"], it["rarity"], it["en"], it["fr"], it["desc_en"], it["desc_fr"], canvas=canvas)
surface_ores = [("ancient_fragment", "ancient", (0x40, 0xC0, 0xB0), "Ancient", "ancien")] + \
               [(f"{d['id']}_fragment", d["id"], d["accent"], d["en"], None) for d in C.DIMENSIONS]
for drop, key, rgb, en, fr in surface_ores:
    for prefix, base_cv, hard, snd in (("", vstone, 3.0, "STONE"), ("deepslate_", vdeep, 4.5, "DEEPSLATE")):
        oid = f"{prefix}{key}_fragment_ore" if key != "ancient" else f"{prefix}ancient_fragment_ore"
        save_tex("block", oid, X.tex_ore(oid, base_cv, rgb))
        frname = next(i["fr"] for i in C.EXTRA_ITEMS if i["id"] == drop)
        add_block(oid, kind="ORE", hardness=hard, resistance=3.0, sound=snd, level=2, xp=(3, 6),
                  en=("Deepslate " if prefix else "") + en + " Fragment Ore",
                  fr="Minerai — " + frname.lower() + (" (ardoise des abîmes)" if prefix else ""), loot=(drop, key == "ancient"))
        tag_item(f"{NS}:ores", rid(oid)); tag_block(f"{NS}:ores", rid(oid))
        tag_block(f"{NS}:surface_ores", rid(oid))

# Génération naturelle à la Surface (Chapitre 2) : fragment ancien commun, fragments dimensionnels rares et profonds.
SURFACE_GEN = []
for drop, key, rgb, en, fr in surface_ores:
    base = "ancient_fragment_ore" if key == "ancient" else f"{key}_fragment_ore"
    deep = "deepslate_" + base
    size, count, lo, hi = (6, 6, -48, 48) if key == "ancient" else (3, 2, -64, 16)
    wjson(os.path.join(DATA, "worldgen", "configured_feature", base + ".json"), {"type": "minecraft:ore", "config": {
        "discard_chance_on_air_exposure": 0.3, "size": size, "targets": [
            {"state": {"Name": rid(base)}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
            {"state": {"Name": rid(deep)}, "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]}})
    wjson(os.path.join(DATA, "worldgen", "placed_feature", base + ".json"), {"feature": rid(base), "placement": [
        {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid",
                                                     "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}},
        {"type": "minecraft:biome"}]})
    SURFACE_GEN.append(base)

# Essences, alliage, noyau
for d in C.DIMENSIONS:
    mats = [C.mat_item(m["id"]) for m in C.MATERIALS if m["dim"] == d["id"]]
    shapeless(f"{d['id']}_essence", f"{d['id']}_essence", mats + [f"{d['id']}_fragment"], 1)
shaped("architect_core", "architect_core", ["FFF", "FDF", "FFF"], {"F": "ancient_fragment", "D": "minecraft:diamond"})
shapeless("primordial_alloy", "primordial_alloy", [f"{d['id']}_essence" for d in C.DIMENSIONS] + ["architect_core"], 2)
tag_item(f"{NS}:repair/primordial_alloy", rid("primordial_alloy"))
for d in C.DIMENSIONS:
    tag_item(f"{NS}:repair/{d['id']}_essence", rid(f"{d['id']}_essence"))
tag_item(f"{NS}:repair/ancient_fragment", rid("ancient_fragment"))

# ===========================================================================
# 3. Blocs lumineux, technologiques, magiques
# ===========================================================================
LIGHT_DIM = ["celestial"] * 3 + ["corrupted"] * 2 + ["ocean"] * 3 + ["chaos"] * 2 + ["eternal"] * 2 + \
            ["depths"] * 3 + ["void"] * 3 + [None] * 2
for lb, dim in zip(C.LIGHT_BLOCKS, LIGHT_DIM):
    save_tex("block", lb["id"], X.tex_light(lb["id"], lb["frame"], lb["glow"], lb["style"]))
    add_block(lb["id"], hardness=1.5, resistance=4.0, sound="GLASS" if lb["style"] in ("lamp", "lantern") else "AMETHYST_BLOCK",
              light=lb["light"], tool=False, en=lb["en"], fr=lb["fr"])
    frame = C.DIM_BY_ID[dim]["stone"] + "_bricks" if dim else "architect_stone"
    shaped(lb["id"], lb["id"], [" S ", "SGS", " S "], {"S": frame, "G": "minecraft:glowstone"}, 4, "building")

for tb in C.TECH_BLOCKS:
    save_tex("block", tb["id"], X.tex_tech(tb["id"], tb["style"]))
    light = {"glow_low": 6, "glow_high": 12}.get(tb["behavior"], 0)
    if tb["behavior"] == "redstone_power":
        light = 9
    add_block(tb["id"], kind="TECH", hardness=3.0, resistance=9.0, sound="METAL" if tb["style"] in ("gear", "core", "conduit") else "STONE",
              light=light, behavior=tb["behavior"], level=1, en=tb["en"], fr=tb["fr"])
shaped("architect_stone", "architect_stone", ["SSS", "SFS", "SSS"], {"S": "minecraft:stone_bricks", "F": "ancient_fragment"}, 8, "building")
for v in ("architect_bricks", "architect_pillar", "architect_panel", "ancient_vent"):
    stonecut(v, "architect_stone")
shapeless("architect_circuit", "architect_circuit", ["architect_stone", "minecraft:redstone", "ancient_fragment"], 1, "building")
shaped("architect_power_core", "architect_power_core", ["SFS", "FCF", "SFS"],
       {"S": "architect_stone", "F": "ancient_fragment", "C": "minecraft:redstone_block"}, 1, "redstone")
shaped("resonance_conduit", "resonance_conduit", ["CFC"], {"C": "minecraft:copper_ingot", "F": "ancient_fragment"}, 3, "building")
shaped("ancient_gear_block", "ancient_gear_block", ["ICI", "CFC", "ICI"],
       {"I": "minecraft:iron_ingot", "C": "minecraft:copper_ingot", "F": "ancient_fragment"}, 2, "building")
shaped("energy_conduit", "energy_conduit", ["CRC"], {"C": "minecraft:copper_ingot", "R": "minecraft:redstone"}, 3, "building")
shapeless("glyph_panel", "glyph_panel", ["architect_panel", "minecraft:glow_ink_sac"], 1, "building")
shapeless("mechanism_block", "mechanism_block", ["ancient_gear_block", "minecraft:piston"], 1, "building")
shapeless("rune_terminal", "rune_terminal", ["architect_panel", "minecraft:book", "ancient_fragment"], 1, "building")
shapeless("archive_shelf", "archive_shelf", ["minecraft:bookshelf", "architect_stone"], 1, "building")
shaped("dimensional_stabilizer", "dimensional_stabilizer", ["FOF", "OCO", "FOF"],
       {"F": "ancient_fragment", "O": "minecraft:obsidian", "C": "architect_core"}, 1, "building")

MAGIC_RECIPES = {
    "mana_crystal_block": (["CCC", "CAC", "CCC"], {"C": "celestial_crystal", "A": "minecraft:amethyst_block"}, 1),
    "arcane_stone": (["SSS", "SAS", "SSS"], {"S": "minecraft:stone", "A": "minecraft:amethyst_shard"}, 8),
    "fire_rune_stone": (["M", "A"], {"M": "ember_garnet", "A": "arcane_stone"}, 1),
    "ice_rune_stone": (["M", "A"], {"M": "everfrost_gem", "A": "arcane_stone"}, 1),
    "lightning_rune_stone": (["M", "A"], {"M": "windstone", "A": "arcane_stone"}, 1),
    "nature_rune_stone": (["M", "A"], {"M": "sea_jade", "A": "arcane_stone"}, 1),
    "void_rune_stone": (["M", "A"], {"M": "void_shard", "A": "arcane_stone"}, 1),
    "ley_line_block": (["FAF", "ACA", "FAF"], {"F": "ancient_fragment", "A": "arcane_stone", "C": "celestial_crystal"}, 2),
    "enchanted_obsidian": (["OOO", "OLO", "OOO"], {"O": "minecraft:obsidian", "L": "minecraft:lapis_lazuli"}, 8),
    "astral_crystal_block": (["CCC", "CCC", "CCC"], {"C": "minecraft:amethyst_shard"}, 1),
    "healing_spring_stone": (["G", "A"], {"G": "minecraft:glistering_melon_slice", "A": "arcane_stone"}, 1),
    "warding_stone": (["T", "A"], {"T": "minecraft:shield", "A": "arcane_stone"}, 1),
    "ether_block": (["PAP"], {"P": "minecraft:phantom_membrane", "A": "arcane_stone"}, 2),
    "wisp_block": (["GAG"], {"G": "minecraft:glow_berries", "A": "arcane_stone"}, 2),
    "spell_altar": (["BBB", "AEA", "AAA"], {"B": "minecraft:book", "A": "arcane_stone", "E": "minecraft:enchanting_table"}, 1),
}
for mb in C.MAGIC_BLOCKS:
    save_tex("block", mb["id"], X.tex_magic(mb["id"], mb["color"], mb["behavior"]))
    light = 10 if mb["behavior"] in ("mana_restore", "mana_regen_zone", "heal", "particles") else 7
    add_block(mb["id"], kind="MAGIC", hardness=2.0, resistance=8.0, sound="AMETHYST_BLOCK", light=light,
              behavior=mb["behavior"], level=1, en=mb["en"], fr=mb["fr"])
    p, k, n = MAGIC_RECIPES[mb["id"]]
    shaped(mb["id"], mb["id"], p, k, n, "building")

# ===========================================================================
# 4. Armes
# ===========================================================================
WEAPONS = []
for t in C.TIERS:
    tag_item(f"{NS}:repair/{C.TIER_ITEM[t['id']]}", C.tier_item_id(t["id"]))
WEAPON_RECIPES = {
    "sword": (["X", "X", "#"], {}),
    "greatsword": ([" X ", " X ", "X#X"], {}),
    "dagger": (["X", "#"], {}),
    "battleaxe": (["XXX", "X#X", " # "], {}),
    "spear": (["  X", " # ", "#  "], {}),
    "staff": (["AXA", " # ", " # "], {"A": "minecraft:amethyst_shard"}),
    "bow": ([" #S", "X S", " #S"], {"S": "minecraft:string"}),
    "crossbow": (["#X#", "SHS", " # "], {"S": "minecraft:string", "H": "minecraft:tripwire_hook"}),
    "wand": ([" X", "# "], {}),
}
CAT_TAGS = {
    "sword": ["minecraft:swords"], "greatsword": ["minecraft:swords"], "dagger": ["minecraft:swords"],
    "battleaxe": ["minecraft:axes"], "spear": ["minecraft:spears"],
    "bow": ["minecraft:enchantable/bow", "minecraft:enchantable/durability"],
    "crossbow": ["minecraft:enchantable/crossbow", "minecraft:enchantable/durability"],
    "staff": ["minecraft:enchantable/durability"], "wand": ["minecraft:enchantable/durability"],
}


def weapon_assets(id, category, rgb, accent=None):
    if category == "bow":
        save_tex("item", id, X.item_bow(rgb))
        for i in range(3):
            save_tex("item", f"{id}_pulling_{i}", X.item_bow(rgb, i))
            wjson(os.path.join(ASSETS, "models", "item", f"{id}_pulling_{i}.json"),
                  {"parent": "minecraft:item/bow", "textures": {"layer0": rid(f"item/{id}_pulling_{i}")}})
        wjson(os.path.join(ASSETS, "models", "item", id + ".json"),
              {"parent": "minecraft:item/bow", "textures": {"layer0": rid("item/" + id)}})
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {
            "type": "minecraft:condition", "property": "minecraft:using_item",
            "on_false": {"type": "minecraft:model", "model": rid("item/" + id)},
            "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": 0.05,
                        "entries": [{"threshold": 0.65, "model": {"type": "minecraft:model", "model": rid(f"item/{id}_pulling_1")}},
                                    {"threshold": 0.9, "model": {"type": "minecraft:model", "model": rid(f"item/{id}_pulling_2")}}],
                        "fallback": {"type": "minecraft:model", "model": rid(f"item/{id}_pulling_0")}}}})
    elif category == "crossbow":
        save_tex("item", id + "_standby", X.item_crossbow(rgb, "standby"))
        states = ["pulling_0", "pulling_1", "pulling_2", "arrow", "firework"]
        for st in states:
            save_tex("item", f"{id}_{st}", X.item_crossbow(rgb, st))
            wjson(os.path.join(ASSETS, "models", "item", f"{id}_{st}.json"),
                  {"parent": "minecraft:item/crossbow", "textures": {"layer0": rid(f"item/{id}_{st}")}})
        wjson(os.path.join(ASSETS, "models", "item", id + ".json"),
              {"parent": "minecraft:item/crossbow", "textures": {"layer0": rid(f"item/{id}_standby")}})
        m = lambda n: {"type": "minecraft:model", "model": rid("item/" + n)}  # noqa: E731
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {
            "type": "minecraft:select", "property": "minecraft:charge_type",
            "cases": [{"when": "arrow", "model": m(f"{id}_arrow")}, {"when": "rocket", "model": m(f"{id}_firework")}],
            "fallback": {"type": "minecraft:condition", "property": "minecraft:using_item", "on_false": m(id),
                         "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:crossbow/pull",
                                     "entries": [{"threshold": 0.58, "model": m(f"{id}_pulling_1")},
                                                 {"threshold": 1.0, "model": m(f"{id}_pulling_2")}],
                                     "fallback": m(f"{id}_pulling_0")}}}})
    elif category == "spear":
        save_tex("item", id, X.item_weapon("spear", rgb, accent))
        wjson(os.path.join(ASSETS, "models", "item", id + ".json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": rid("item/" + id)}})
        wjson(os.path.join(ASSETS, "models", "item", id + "_in_hand.json"),
              {"parent": "minecraft:item/spear_in_hand", "textures": {"layer0": rid("item/" + id)}})
        wjson(os.path.join(ASSETS, "items", id + ".json"), {"model": {
            "type": "minecraft:select", "property": "minecraft:display_context",
            "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": {"type": "minecraft:model", "model": rid("item/" + id)}}],
            "fallback": {"type": "minecraft:model", "model": rid("item/" + id + "_in_hand")}}, "swap_animation_scale": 1.95})
    else:
        save_tex("item", id, X.item_weapon(category, rgb, accent))
        item_model(id, "minecraft:item/handheld")


for cat in C.WEAPON_CATEGORIES:
    for t in C.TIERS:
        if not C.has_weapon(t["id"], cat["id"]):
            continue
        wid = f"{t['id']}_{cat['id']}"
        en_m, fr_m = C.TIER_NAMES[t["id"]]
        rgb = C.TIER_COLOR[t["id"]]
        weapon_assets(wid, cat["id"], rgb)
        fr_name = f"{cat['fr']} {fr_m}"
        if fr_m == "primordial" and cat["fr_g"] == "f":
            fr_name = f"{cat['fr']} primordiale"
        LANG_EN[f"item.{NS}.{wid}"] = f"{en_m} {cat['en']}"
        LANG_FR[f"item.{NS}.{wid}"] = fr_name
        WEAPONS.append(dict(id=wid, category=cat["id"].upper(), tier=t["id"], rarity=t["rarity"], legendary=""))
        pattern, extra = WEAPON_RECIPES[cat["id"]]
        key = {"X": C.tier_item_id(t["id"]), "#": "minecraft:stick"}
        key.update(extra)
        shaped(wid, wid, pattern, {k: v for k, v in key.items() if any(k in row for row in pattern)}, 1, "combat")
        for tg in CAT_TAGS[cat["id"]]:
            tag_item(tg, rid(wid))
        tag_item(f"{NS}:weapons", rid(wid))
        tag_item(f"{NS}:weapons/{cat['id']}", rid(wid))

LEGENDARY_DIM = {"celestial_blade": "celestial", "blade_of_corruption": "corrupted", "abyssal_trident": "ocean",
                 "volcanic_hammer": "chaos", "void_bow": "void", "frost_queen_scepter": "eternal",
                 "sky_emperor_greatsword": "celestial", "depths_fang": "depths", "stormcaller_crossbow": "void",
                 "world_ender": None}
for lw in C.LEGENDARY_WEAPONS:
    weapon_assets(lw["id"], lw["base"], lw["color"], accent=(0xFF, 0xD0, 0x40))
    LANG_EN[f"item.{NS}.{lw['id']}"] = lw["en"]
    LANG_FR[f"item.{NS}.{lw['id']}"] = lw["fr"]
    LANG_EN[f"item.{NS}.{lw['id']}.desc"] = lw["desc_en"]
    LANG_FR[f"item.{NS}.{lw['id']}.desc"] = lw["desc_fr"]
    WEAPONS.append(dict(id=lw["id"], category=lw["base"].upper(), tier=lw["tier"], rarity=lw["rarity"], legendary=lw["id"]))
    dim = LEGENDARY_DIM[lw["id"]]
    base_weapon = f"{lw['tier']}_{lw['base']}"
    if dim:
        shaped(lw["id"], lw["id"], ["EFE", "FWF", "EFE"], {"E": f"{dim}_essence", "F": f"{dim}_fragment", "W": base_weapon}, 1, "combat")
    else:
        shaped(lw["id"], lw["id"], ["PNP", "PWP", "PCP"], {"P": "primordial_alloy", "N": "minecraft:nether_star",
                                                          "W": base_weapon, "C": "architect_core"}, 1, "combat")
    for tg in CAT_TAGS[lw["base"]]:
        tag_item(tg, rid(lw["id"]))
    tag_item(f"{NS}:weapons", rid(lw["id"]))
    tag_item(f"{NS}:weapons/legendary", rid(lw["id"]))

# ===========================================================================
# 5. Armures
# ===========================================================================
for a in C.ARMOR_SETS:
    rgb = a["color"]
    save_tex(os.path.join("entity", "equipment", "humanoid"), a["id"], X.armor_layer(rgb, a["id"]))
    save_tex(os.path.join("entity", "equipment", "humanoid_leggings"), a["id"], X.armor_layer(rgb, a["id"], leggings=True))
    wjson(os.path.join(ASSETS, "equipment", a["id"] + ".json"), {"layers": {
        "humanoid": [{"texture": rid(a["id"])}], "humanoid_leggings": [{"texture": rid(a["id"])}]}})
    tag_item(f"{NS}:repair/{a['item']}", rid(a["item"]))
    for p in C.ARMOR_PIECES:
        pid = f"{a['id']}_{p['id']}"
        save_tex("item", pid, X.item_symmetric(p["id"], rgb))
        item_model(pid, "minecraft:item/generated")
        LANG_EN[f"item.{NS}.{pid}"] = f"{a['en']} {p['en']}"
        fr_suffix = a["fr"]
        if a["fr"] == "primordial":
            fr_suffix = {"m": "primordial", "fp": "primordiales"}[p["fr_g"]]
        LANG_FR[f"item.{NS}.{pid}"] = f"{p['fr']} {fr_suffix}"
        tag_item({"helmet": "minecraft:head_armor", "chestplate": "minecraft:chest_armor",
                  "leggings": "minecraft:leg_armor", "boots": "minecraft:foot_armor"}[p["id"]], rid(pid))
        tag_item(f"{NS}:armor", rid(pid))
    X_ = a["item"]
    shaped(f"{a['id']}_helmet", f"{a['id']}_helmet", ["XXX", "X X"], {"X": X_}, 1, "combat")
    shaped(f"{a['id']}_chestplate", f"{a['id']}_chestplate", ["X X", "XXX", "XXX"], {"X": X_}, 1, "combat")
    shaped(f"{a['id']}_leggings", f"{a['id']}_leggings", ["XXX", "X X", "X X"], {"X": X_}, 1, "combat")
    shaped(f"{a['id']}_boots", f"{a['id']}_boots", ["X X", "X X"], {"X": X_}, 1, "combat")
    if a["bonus"] != "none":
        LANG_EN[f"armor_set.{NS}.{a['id']}"] = a["en"]
        LANG_FR[f"armor_set.{NS}.{a['id']}"] = a["fr"]

# ===========================================================================
# 6. Artefacts
# ===========================================================================
ARTIFACT_RECIPES = {
    "heart_of_the_ancients": ("architect_core", "ancient_fragment", "minecraft:heart_of_the_sea"),
    "ring_of_time": ("astral_silver_ingot", "celestial_essence", "minecraft:clock"),
    "dimensional_eye": ("void_shard", "void_essence", "minecraft:ender_eye"),
    "crown_of_seven_worlds": ("primordial_alloy", "architect_core", "minecraft:nether_star"),
    "amulet_of_winds": ("windstone", "celestial_essence", "minecraft:feather"),
    "corrupted_heart": ("blood_ruby", "corrupted_essence", "minecraft:fermented_spider_eye"),
    "abyssal_amulet": ("abyssal_pearl", "ocean_essence", "minecraft:nautilus_shell"),
    "ember_core": ("ember_garnet", "chaos_essence", "minecraft:magma_cream"),
    "frozen_hourglass": ("everfrost_gem", "eternal_essence", "minecraft:clock"),
    "lantern_of_the_lost": ("luminite", "ancient_fragment", "minecraft:lantern"),
    "void_compass": ("nebulite_ingot", "void_essence", "minecraft:recovery_compass"),
    "stellar_charm": ("celestial_crystal", "ancient_fragment", "minecraft:rabbit_foot"),
    "titan_belt": ("magmatite_ingot", "chaos_essence", "minecraft:leather"),
    "sky_feather": ("windstone", "celestial_fragment", "minecraft:phantom_membrane"),
    "guardian_emblem": ("astral_silver_ingot", "ancient_fragment", "minecraft:shield"),
    "mana_prism": ("celestial_crystal", "ancient_fragment", "minecraft:amethyst_shard"),
    "archmage_tome": ("shadow_quartz", "depths_essence", "minecraft:enchanted_book"),
    "blood_chalice": ("blood_ruby", "corrupted_essence", "minecraft:gold_ingot"),
    "briar_crown": ("blight_amber", "corrupted_fragment", "minecraft:sweet_berries"),
    "storm_orb": ("celestial_crystal", "celestial_essence", "minecraft:lightning_rod"),
    "tidal_bell": ("tidal_cobalt_ingot", "ocean_essence", "minecraft:bell"),
    "phoenix_feather": ("ember_garnet", "chaos_essence", "minecraft:totem_of_undying"),
    "shadow_cloak": ("shadow_quartz", "depths_essence", "minecraft:phantom_membrane"),
    "deep_lantern": ("deep_mithril_ingot", "depths_fragment", "minecraft:lantern"),
    "explorer_astrolabe": ("astral_silver_ingot", "ancient_fragment", "minecraft:compass"),
    "sigil_of_war": ("corruptium_ingot", "chaos_fragment", "minecraft:iron_sword"),
    "eagle_eye_charm": ("windstone", "celestial_fragment", "minecraft:spyglass"),
    "spirit_bell": ("sea_jade", "ocean_fragment", "minecraft:bell"),
    "architect_cog": ("ancient_fragment", "architect_core", "minecraft:clock"),
    "seed_of_life": ("sea_jade", "depths_fragment", "minecraft:golden_apple"),
}
for a in C.ARTIFACTS:
    add_item(a["id"], a["rarity"], a["en"], a["fr"], a["desc_en"], a["desc_fr"],
             canvas=X.item_icon(a["shape"], a["color"], a["id"]), register=False)
    m, e, c = ARTIFACT_RECIPES[a["id"]]
    shaped(a["id"], a["id"], ["MEM", "ECE", "MEM"], {"M": m, "E": e, "C": c}, 1, "misc")
    tag_item(f"{NS}:artifacts", rid(a["id"]))

# ===========================================================================
# 6b. Modèles 3D des armes et armures (remplacent les modèles plats)
# ===========================================================================
import items3d as I3  # noqa: E402
I3.write(sys.modules[__name__], WEAPONS, C.ARMOR_SETS, C.TIER_COLOR,
         {lw["id"]: lw["color"] for lw in C.LEGENDARY_WEAPONS})

# ===========================================================================
# 7. Phase 4 : dimensions, biomes, surfaces, bois, portail et clés
# ===========================================================================
import dimensions as D  # noqa: E402
# ===========================================================================
# 8. Phase 5 : créatures (modèles, textures, butin, œufs, apparitions)
# ===========================================================================
import creatures as CR  # noqa: E402
import modelgen as MG  # noqa: E402

CREATURE_SPAWNS = {}
CREATURE_ROWS = []
ARCH_LAYOUTS = {}
for arch in MG.ARCHETYPES:
    model, tw, th = MG.layout(arch)
    ARCH_LAYOUTS[arch] = (model, tw, th)
    wjson(os.path.join(ASSETS, "fd_entity_models", arch + ".json"), model)
# Textures des pièces d'armure en relief (une par ensemble).
_dm, _dw, _dh = ARCH_LAYOUTS["armor_details"]
for a in C.ARMOR_SETS:
    fake = {"id": "detail_" + a["id"], "colors": (a["color"], X.hue_shift(a["color"], 0.08, 0.15, 0.2), a["color"]),
            "pattern": "plates"}
    save_tex(os.path.join("entity", "armor_detail"), a["id"], MG.paint(_dm, _dw, _dh, fake))
for cr in CR.CREATURES:
    cid = cr["id"]
    k = CR.DIM_SCALE[cr["dim"]]
    cat = cr["cat"]
    hp = round(cr["hp"] * k)
    dmg = round(cr["dmg"] * (k ** 0.8), 1)
    model, tw, th = ARCH_LAYOUTS[cr["arch"]]
    save_tex(os.path.join("entity", "creature"), cid, MG.paint(model, tw, th, cr))
    bw, bh = CR.BASE_SIZE[cr["arch"]]
    width, height = round(bw * cr["scale"], 2), round(bh * cr["scale"], 2)
    xp = {"PASSIVE": 2, "NEUTRAL": 5, "HOSTILE": 5 + hp // 10, "ELITE": 30 + hp // 10, "MINIBOSS": 120}[cat]
    if cat in ("HOSTILE", "ELITE", "MINIBOSS"):
        group = "monster"
    elif cr["move"] == "SWIMMING":
        group = "water_creature"
    else:
        group = "creature"
    CREATURE_ROWS.append(dict(cr, hp_final=hp, dmg_final=dmg, width=width, height=height, xp=xp, group=group))
    for biome_id in cr["biomes"]:
        CREATURE_SPAWNS.setdefault(biome_id, {}).setdefault(group, []).append(
            {"type": rid(cid), "weight": cr["weight"], "minCount": cr["group"][0], "maxCount": cr["group"][1]})
    # Butin
    entries = []
    for item, lo, hi, chance in cr["drops"]:
        fn = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}, "add": False},
              {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
               "count": {"type": "minecraft:uniform", "min": 0.0, "max": 1.0}}]
        pool = {"bonus_rolls": 0.0, "rolls": 1.0, "entries": [{"type": "minecraft:item", "name": ing(item), "functions": fn}]}
        if chance < 1.0:
            pool["conditions"] = [{"condition": "minecraft:random_chance_with_enchanted_bonus", "enchantment": "minecraft:looting",
                                   "unenchanted_chance": chance, "enchanted_chance": {"type": "minecraft:linear",
                                                                                     "base": min(1.0, chance + 0.05), "per_level_above_first": 0.05}}]
        entries.append(pool)
    wjson(os.path.join(DATA, "loot_table", "entities", cid + ".json"),
          {"type": "minecraft:entity", "pools": entries, "random_sequence": rid("entities/" + cid)})
    LANG_EN[f"entity.{NS}.{cid}"] = cr["en"]
    LANG_FR[f"entity.{NS}.{cid}"] = cr["fr"]
    egg = cid + "_spawn_egg"
    save_tex("item", egg, MG.spawn_egg(cr["colors"][0], cr["colors"][1], cid))
    item_model(egg, "minecraft:item/generated")
    LANG_EN[f"item.{NS}.{egg}"] = cr["en"] + " Spawn Egg"
    LANG_FR[f"item.{NS}.{egg}"] = "Œuf d'apparition — " + cr["fr"]
    tag_item(f"{NS}:spawn_eggs", rid(egg))
    tag_entity = f"{NS}:creatures/{cr['dim']}"
    ENTITY_TAGS.setdefault(tag_entity, set()).add(rid(cid))
    ENTITY_TAGS.setdefault(f"{NS}:creatures", set()).add(rid(cid))
    if cat == "MINIBOSS":
        ENTITY_TAGS.setdefault(f"{NS}:minibosses", set()).add(rid(cid))

# Créatures invoquées par les sorts d'invocation (modèle 3D + texture + nom)
import summons as SU  # noqa: E402
for su in SU.SUMMONS:
    model, tw, th = ARCH_LAYOUTS[su["arch"]]
    cv = MG.paint(model, tw, th, su)
    save_tex(os.path.join("entity", "summon"), su["id"], cv)
    save_tex(os.path.join("entity", "summon"), su["id"] + "_glow", MG.glow_mask(cv, su))
    LANG_EN[f"entity.{NS}.{su['id']}"] = su["en"]
    LANG_FR[f"entity.{NS}.{su['id']}"] = su["fr"]

BIOMES = D.run(sys.modules[__name__])

# Projectile magique (texture utilisée par le rendu des sorts)
for school, rgb in (("fire", (0xFF, 0x70, 0x20)), ("ice", (0xA0, 0xE8, 0xFF)), ("lightning", (0xFF, 0xF0, 0x60)),
                    ("nature", (0x60, 0xE0, 0x60)), ("void", (0xB0, 0x50, 0xFF))):
    add_item(f"{school}_spell_bolt", "COMMON", f"{school.title()} Spell Bolt", f"Projectile magique ({school})",
             canvas=X.item_spell_bolt(rgb), register=True)

# ===========================================================================
# Écriture : textures diverses, langues, recettes, tags, Java
# ===========================================================================
X.pack_icon().save(os.path.join(ASSETS, "icon.png"))

# Textures des effets 3D des sorts
import spellfx as SFX  # noqa: E402
for _n in SFX.write(os.path.join(ASSETS, "textures", "entity", "spell")):
    TEXTURES_DONE.add("entity/spell/" + _n)

# Tags divers utilisés par le code
for d in C.DIMENSIONS:
    tag_item(f"{NS}:ores_drops", rid(f"{d['id']}_fragment"))
tag_item(f"{NS}:ores_drops", rid("ancient_fragment"))
wjson(os.path.join(DATA, "tags", "worldgen", "structure", "ancient_ruins.json"), {"replace": False, "values": [
    "minecraft:trail_ruins", "minecraft:ancient_city", "minecraft:desert_pyramid", "minecraft:jungle_pyramid",
    "minecraft:ocean_ruin_cold", "minecraft:ocean_ruin_warm"]})
wjson(os.path.join(DATA, "tags", "entity_type", "bosses.json"), {"replace": False, "values": [
    "minecraft:ender_dragon", "minecraft:wither", "minecraft:warden", "minecraft:elder_guardian"]})

# Succès
import lang_ui as L  # noqa: E402
for adv_id, icon, parent, crit, title, desc, frame in L.ADVANCEMENTS:
    if crit[0] == "granted":
        criteria = {"granted": {"trigger": "minecraft:impossible"}}
    else:
        criteria = {"granted": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": crit[1]}]}}}
    display = {"icon": {"id": icon, "count": 1}, "title": {"translate": f"advancements.{NS}.{adv_id}.title"},
               "description": {"translate": f"advancements.{NS}.{adv_id}.description"}, "frame": frame,
               "show_toast": True, "announce_to_chat": adv_id != "root"}
    adv = {"display": display, "criteria": criteria, "requirements": [["granted"]]}
    if parent:
        adv["parent"] = rid(parent)
    else:
        display["background"] = "minecraft:gui/advancements/backgrounds/stone"
    wjson(os.path.join(DATA, "advancement", adv_id + ".json"), adv)
    LANG_EN[f"advancements.{NS}.{adv_id}.title"], LANG_FR[f"advancements.{NS}.{adv_id}.title"] = title
    LANG_EN[f"advancements.{NS}.{adv_id}.description"], LANG_FR[f"advancements.{NS}.{adv_id}.description"] = desc

# Traductions des tags d'objets (convention Fabric tag.item.<ns>.<chemin>)
TAG_NAMES = {"ores": ("Forgotten Ores", "Minerais oubliés"), "ores_drops": ("Ancient Fragments", "Fragments anciens"),
             "weapons": ("Forgotten Weapons", "Armes oubliées"), "armor": ("Forgotten Armor", "Armures oubliées"),
             "artifacts": ("Artifacts", "Artefacts")}
for tag in ITEM_TAGS:
    ns, path = tag.split(":")
    if ns != NS:
        continue
    key = f"tag.item.{NS}." + path.replace("/", ".")
    if path in TAG_NAMES:
        en, fr = TAG_NAMES[path]
    elif path.startswith("repair/"):
        item = path.split("/", 1)[1]
        en, fr = "Repairs with " + LANG_EN.get(f"item.{NS}.{item}", item), "Répare avec : " + LANG_FR.get(f"item.{NS}.{item}", item)
    elif path.startswith("weapons/"):
        cat = path.split("/", 1)[1]
        en, fr = "Weapons: " + cat, "Armes : " + cat
    else:
        en, fr = path, path
    LANG_EN[key], LANG_FR[key] = en, fr

# Langues : entrées générées + entrées manuelles (lang_ui.py)
for key, (en, fr) in L.UI.items():
    LANG_EN[key] = en
    LANG_FR[key] = fr
import re as _re  # noqa: E402

_PCT = _re.compile(r"%%|%(?:\d+\$)?s|%")


def mc_escape(text):
    """Minecraft refuse tout « % » isolé dans une traduction (il affiche alors la clé brute) : on le double."""
    return _PCT.sub(lambda m: m.group(0) if m.group(0) != "%" else "%%", text)


for code, table in (("en_us", LANG_EN), ("fr_fr", LANG_FR)):
    wjson(os.path.join(ASSETS, "lang", code + ".json"), {k: mc_escape(v) for k, v in sorted(table.items())})

for name, r in RECIPES.items():
    wjson(os.path.join(DATA, "recipe", name + ".json"), r)


def write_tags(tags, kind):
    for tag, values in tags.items():
        ns, path = tag.split(":")
        base = MCDATA if ns == "minecraft" else DATA
        wjson(os.path.join(base, "tags", kind, path + ".json"), {"replace": False, "values": sorted(values)})


write_tags(ITEM_TAGS, "item")
write_tags(ENTITY_TAGS, "entity_type")
write_tags(BLOCK_TAGS, "block")


def jstr(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def jf(v):
    return f"{float(v)}F"


lines = []
lines.append("// FICHIER GÉNÉRÉ PAR tools/gen/generate.py — NE PAS MODIFIER À LA MAIN.")
lines.append("package com.forgotten.dimensions.registry.generated;")
lines.append("")
lines.append("import com.forgotten.dimensions.content.*;")
lines.append("import com.forgotten.dimensions.magic.SpellSchool;")
lines.append("import java.util.List;")
lines.append("")
lines.append("public final class ContentTables {")
lines.append("    private ContentTables() {}")
lines.append("")
lines.append("    public static List<BlockDef> blocks() {")
lines.append("        return List.of(")
rows = []
for b in BLOCKS:
    rows.append(f"            new BlockDef({jstr(b['id'])}, BlockKind.{b['kind']}, {jf(b['hardness'])}, {jf(b['resistance'])}, "
                f"{jstr(b['sound'])}, {b['light']}, {str(b['tool']).lower()}, {jstr(b['base'])}, {b['xp'][0]}, {b['xp'][1]}, "
                f"{jstr(b['behavior'])})")
lines.append(",\n".join(rows))
lines.append("        );")
lines.append("    }")
lines.append("")
lines.append("    public static List<SimpleItemDef> items() {")
lines.append("        return List.of(")
lines.append(",\n".join(f"            new SimpleItemDef({jstr(i['id'])}, FdRarity.{i['rarity']}, {str(i['fireproof']).lower()})" for i in ITEMS))
lines.append("        );")
lines.append("    }")
lines.append("")
lines.append("    public static List<TierDef> tiers() {")
lines.append("        return List.of(")
lines.append(",\n".join(
    f"            new TierDef({jstr(t['id'])}, FdRarity.{t['rarity']}, {t['dur']}, {jf(t['speed'])}, {jf(t['dmg'])}, {t['ench']}, "
    f"{jstr(t['level'])}, SpellSchool.{t['school']}, {jstr(C.TIER_ITEM[t['id']])})" for t in C.TIERS))
lines.append("        );")
lines.append("    }")
lines.append("")
lines.append("    public static List<WeaponDef> weapons() {")
lines.append("        return List.of(")
lines.append(",\n".join(
    f"            new WeaponDef({jstr(w['id'])}, WeaponCategory.{w['category']}, {jstr(w['tier'])}, FdRarity.{w['rarity']}, {jstr(w['legendary'])})"
    for w in WEAPONS))
lines.append("        );")
lines.append("    }")
lines.append("")
lines.append("    public static List<ArmorSetDef> armorSets() {")
lines.append("        return List.of(")
lines.append(",\n".join(
    f"            new ArmorSetDef({jstr(a['id'])}, {jstr(a['item'])}, {a['dur']}, {a['defense'][0]}, {a['defense'][1]}, "
    f"{a['defense'][2]}, {a['defense'][3]}, {jf(a['tough'])}, {jf(a['kb'])}, {a['ench']}, FdRarity.{a['rarity']}, {jstr(a['bonus'])})"
    for a in C.ARMOR_SETS))
lines.append("        );")
lines.append("    }")
lines.append("")
lines.append("    /** Minerais générés naturellement dans la Surface (placed_feature du même nom). */")
lines.append("    public static List<String> overworldOres() {")
lines.append("        return List.of(" + ", ".join(jstr(o) for o in SURFACE_GEN) + ");")
lines.append("    }")
lines.append("")
lines.append("    public static List<CreatureDef> creatures() {")
lines.append("        return List.of(")
lines.append(",\n".join(
    f"            new CreatureDef({jstr(r['id'])}, {jstr(r['dim'])}, CreatureCategory.{r['cat']}, {jstr(r['arch'])}, "
    f"CreatureMovement.{r['move']}, {r['hp_final']}, {jf(r['dmg_final'])}, {jf(r['speed'])}, {jf(r['armor'])}, {jf(r['scale'])}, "
    f"{jf(r['width'])}, {jf(r['height'])}, {r['xp']}, List.of({', '.join(jstr(a) for a in r['abilities'])}), "
    f"List.of({', '.join(jstr(b) for b in r['behaviors'])}), {jstr(r['sounds'])}, {jstr(r['minion'])}, {str(r['fire']).lower()}, "
    f"{jstr(r['group'])}, {r['colors'][0][0] << 16 | r['colors'][0][1] << 8 | r['colors'][0][2]}, "
    f"{r['colors'][1][0] << 16 | r['colors'][1][1] << 8 | r['colors'][1][2]})"
    for r in CREATURE_ROWS))
lines.append("        );")
lines.append("    }")
lines.append("")
lines.append("    public static List<ArtifactDef> artifacts() {")
lines.append("        return List.of(")
lines.append(",\n".join(f"            new ArtifactDef({jstr(a['id'])}, FdRarity.{a['rarity']})" for a in C.ARTIFACTS))
lines.append("        );")
lines.append("    }")
lines.append("}")
os.makedirs(os.path.dirname(JAVA_OUT), exist_ok=True)
with open(JAVA_OUT, "w", encoding="utf-8") as f:
    f.write("\n".join(lines) + "\n")

# Résumé
ores = sum(1 for b in BLOCKS if b["kind"] == "ORE")
print(f"Blocs : {len(BLOCKS)} (minerais : {ores}, lumineux : {len(C.LIGHT_BLOCKS)}, techno : {len(C.TECH_BLOCKS)}, "
      f"magiques : {len(C.MAGIC_BLOCKS)})")
print(f"Objets simples : {len(ITEMS)} | Armes : {len(WEAPONS)} | Armures : {len(C.ARMOR_SETS) * 4} pièces "
      f"({len(C.ARMOR_SETS)} ensembles) | Artefacts : {len(C.ARTIFACTS)}")
print(f"Recettes : {len(RECIPES)} | Textures : {len(TEXTURES_DONE)} | Entrées de langue : {len(LANG_EN)}")

