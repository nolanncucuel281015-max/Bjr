#!/usr/bin/env python3
"""Génère l'édition data pack des Dimensions Oubliées (Minecraft Java 1.21.11, sans mod).

Sortie : datapack/LesDimensionsOubliees/ (dossier du pack) et build/datapack/LesDimensionsOubliees.zip.

Ce que contient le pack (tout en vanilla) :
  - les 7 dimensions du mod, régénérées avec des blocs et des créatures de Minecraft ;
  - 7 clés dimensionnelles (voyage aller-retour), 5 bâtons magiques, 14 armes, 7 armures de 4 pièces ;
  - 20 sorts (4 par école) lancés au clic droit, effets 3D en entités d'affichage ;
  - progression RPG (niveaux 1 à 50, expérience, mana), barre d'action, fiche de personnage ;
  - créatures à niveau (nom, vie selon la dimension), barres de vie et chiffres de dégâts ;
  - un onglet de progrès et un guide illustré (livre).
"""
import json
import os
import shutil
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(ROOT, "tools", "gen"))
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import creatures as CR  # noqa: E402
import worldgen_map as WM  # noqa: E402

NS = "oubliees"
MOD = "forgotten_dimensions"
OUT = os.path.join(ROOT, "datapack", "LesDimensionsOubliees")
DATA = os.path.join(OUT, "data", NS)
MCDATA = os.path.join(OUT, "data", "minecraft")
MOD_DATA = os.path.join(ROOT, "src", "main", "resources", "data", MOD)

FILES = 0


def write(path, text):
    global FILES
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)
    FILES += 1


def wjson(path, obj):
    write(path, json.dumps(obj, ensure_ascii=False, indent=2) + "\n")


def fn(name, lines):
    """Écrit la fonction oubliees:<name>."""
    if isinstance(lines, str):
        lines = lines.strip("\n").split("\n")
    write(os.path.join(DATA, "function", name + ".mcfunction"), "\n".join(l.rstrip() for l in lines) + "\n")


def snbt(obj):
    """JSON compact, aussi valable en SNBT (composants de texte dans les commandes)."""
    return json.dumps(obj, ensure_ascii=False, separators=(",", ":"))


def T(text, color=None, **kw):
    d = {"text": text}
    if color:
        d["color"] = color
    d.update(kw)
    return d


# ============================================================================ données
DIM_FR = {"celestial": "Royaume Céleste", "corrupted": "Dimension Corrompue", "ocean": "Océan Oublié",
          "chaos": "Terres du Chaos", "eternal": "Monde Éternel", "depths": "Les Profondeurs", "void": "Le Néant"}
DIM_COLOR = {"celestial": "#bfeaff", "corrupted": "#e0405a", "ocean": "#3fd0c8", "chaos": "#ff7a20",
             "eternal": "#a8e4ff", "depths": "#8a7aff", "void": "#c070ff"}
# Niveau de base des créatures et multiplicateur de vie (même échelle que le mod)
DIM_LEVEL = {"overworld": (1, 1.0), "the_nether": (15, 1.3), "the_end": (30, 1.6), "celestial": (6, 1.0),
             "corrupted": (16, 1.15), "ocean": (26, 1.3), "chaos": (40, 1.5), "eternal": (54, 1.7),
             "depths": (68, 1.9), "void": (82, 2.2)}
DIM_KEY_ID = {"overworld": "minecraft:overworld", "the_nether": "minecraft:the_nether", "the_end": "minecraft:the_end"}
for _d in WM.DIMENSIONS:
    DIM_KEY_ID[_d] = f"{NS}:{_d}"
DIM_CODE = {d: i for i, d in enumerate(["overworld", "the_nether", "the_end"] + WM.DIMENSIONS)}
CEILING = {"chaos", "depths"}
# Hauteur de la plateforme de secours (au-dessus de la mer, sous les plafonds)
PLATFORM_Y = {"celestial": 120, "corrupted": 100, "ocean": 152, "chaos": 45, "eternal": 100, "depths": 20, "void": 70}
ARRIVAL_STONE = {"celestial": "calcite", "corrupted": "blackstone", "ocean": "prismarine", "chaos": "basalt",
                 "eternal": "packed_ice", "depths": "deepslate", "void": "end_stone"}

KEYS = {  # matériau du motif, modèle d'objet, niveau conseillé
    "celestial": ("minecraft:amethyst_shard", "minecraft:amethyst_shard", 1),
    "corrupted": ("minecraft:crimson_fungus", "minecraft:nether_wart", 10),
    "ocean": ("minecraft:prismarine_crystals", "minecraft:prismarine_crystals", 18),
    "chaos": ("minecraft:blaze_powder", "minecraft:blaze_powder", 26),
    "eternal": ("minecraft:blue_ice", "minecraft:blue_ice", 32),
    "depths": ("minecraft:echo_shard", "minecraft:echo_shard", 40),
    "void": ("minecraft:dragon_breath", "minecraft:ender_eye", 45),
}

SCHOOLS = [  # id, code, nom, couleur, matériau du bâton, modèle, particule, verre, damage_type
    ("fire", 1, "Feu", "#ff7a20", "minecraft:blaze_powder", "minecraft:blaze_rod", "minecraft:flame",
     "orange_stained_glass", "minecraft:in_fire"),
    ("ice", 2, "Glace", "#8fe0ff", "minecraft:packed_ice", "minecraft:breeze_rod", "minecraft:snowflake",
     "light_blue_stained_glass", "minecraft:freeze"),
    ("lightning", 3, "Foudre", "#ffe24a", "minecraft:copper_ingot", "minecraft:lightning_rod", "minecraft:electric_spark",
     "yellow_stained_glass", "minecraft:lightning_bolt"),
    ("nature", 4, "Nature", "#6ce06c", "minecraft:emerald", "minecraft:bamboo", "minecraft:composter",
     "lime_stained_glass", "minecraft:magic"),
    ("void", 5, "Néant", "#b066ff", "minecraft:ender_pearl", "minecraft:end_rod", "minecraft:reverse_portal",
     "purple_stained_glass", "minecraft:magic"),
]
SCHOOL = {s[0]: s for s in SCHOOLS}
RGB = {"fire": "[1.0,0.48,0.12]", "ice": "[0.56,0.88,1.0]", "lightning": "[1.0,0.9,0.3]", "nature": "[0.42,0.88,0.42]",
       "void": "[0.69,0.4,1.0]"}

# Sorts : école, index (ordre du cycle), id, nom, niveau, mana, recharge (ticks), dégâts de base, type, description
SPELLS = [
    ("fire", 0, "fire_bolt", "Trait de feu", 1, 10, 12, 6, "ray", "Un trait enflammé qui embrase la cible."),
    ("fire", 1, "fire_nova", "Nova de feu", 8, 30, 80, 7, "nova", "Une onde de flammes autour de vous."),
    ("fire", 2, "flame_pillar", "Colonne de flammes", 15, 40, 120, 10, "pillar", "Une colonne de feu jaillit à l'endroit visé."),
    ("fire", 3, "meteor", "Météore", 25, 60, 240, 16, "meteor", "Un rocher en fusion s'écrase sur la zone visée."),
    ("ice", 0, "ice_shard", "Éclat de glace", 1, 10, 12, 5, "ray", "Un éclat qui ralentit la cible."),
    ("ice", 1, "frost_nova", "Nova de givre", 8, 30, 80, 5, "nova", "Gèle et ralentit les ennemis autour de vous."),
    ("ice", 2, "frozen_prison", "Prison de glace", 12, 30, 160, 4, "prison", "Des pics de glace emprisonnent la zone visée."),
    ("ice", 3, "frost_sentinel", "Sentinelle de givre", 16, 45, 600, 0, "summon_sentinel", "Invoque un colosse de neige qui gèle les monstres."),
    ("lightning", 0, "spark", "Étincelle", 1, 8, 10, 4, "ray", "Un arc électrique rapide."),
    ("lightning", 1, "lightning_strike", "Éclair", 5, 20, 60, 6, "strike", "La foudre frappe l'endroit visé."),
    ("lightning", 2, "chain_lightning", "Chaîne d'éclairs", 15, 35, 120, 6, "chain", "La foudre frappe la cible et deux ennemis proches."),
    ("lightning", 3, "tempest", "Tempête", 30, 70, 400, 5, "tempest", "Un orage foudroie la zone pendant cinq secondes."),
    ("nature", 0, "natural_heal", "Soin naturel", 1, 15, 100, 0, "heal", "Vous soigne, vous et vos alliés proches."),
    ("nature", 1, "thorn_bolt", "Trait d'épines", 3, 10, 12, 5, "ray", "Une épine empoisonnée."),
    ("nature", 2, "summon_wolves", "Invocation des loups", 8, 40, 600, 0, "summon_wolves", "Invoque trois loups spectraux."),
    ("nature", 3, "stone_guardian", "Gardien de pierre", 30, 80, 1200, 0, "summon_golem", "Invoque un colosse de pierre protecteur."),
    ("void", 0, "blink", "Téléportation", 1, 15, 60, 0, "blink", "Vous téléporte jusqu'à 12 blocs devant vous."),
    ("void", 1, "void_bolt", "Trait du néant", 3, 12, 14, 6, "ray", "Un trait qui flétrit la cible."),
    ("void", 2, "gravity_well", "Puits gravitationnel", 20, 50, 300, 2, "well", "Attire les ennemis vers un point qui les écrase."),
    ("void", 3, "singularity", "Singularité", 40, 100, 600, 18, "singularity", "Un trou noir aspire les ennemis puis explose."),
]

MAX_LEVEL = 50

# Créatures qui reçoivent un niveau (nom affiché avec leur barre de vie)
LEVELED = ["zombie", "husk", "drowned", "zombie_villager", "skeleton", "stray", "bogged", "wither_skeleton", "spider",
           "cave_spider", "creeper", "enderman", "witch", "slime", "magma_cube", "blaze", "ghast", "phantom", "pillager",
           "vindicator", "evoker", "ravager", "piglin", "piglin_brute", "zombified_piglin", "hoglin", "zoglin", "guardian",
           "elder_guardian", "shulker", "silverfish", "endermite", "vex", "breeze", "creaking", "warden", "wolf",
           "polar_bear", "goat", "llama", "panda", "bee", "iron_golem", "cow", "pig", "sheep", "chicken", "rabbit",
           "horse", "fox", "mooshroom", "armadillo", "allay", "glow_squid", "turtle", "parrot", "camel", "frog", "sniffer"]


# ============================================================================ 1. pack
def build_pack():
    if os.path.exists(OUT):
        shutil.rmtree(OUT)
    wjson(os.path.join(OUT, "pack.mcmeta"), {"pack": {
        "description": [T("Les Dimensions Oubliées", "gold", bold=True), T("\nÉdition data pack : 7 dimensions, magie, RPG", "gray")],
        "min_format": [94, 1], "max_format": 94}})
    wjson(os.path.join(MCDATA, "tags", "function", "load.json"), {"values": [f"{NS}:load"]})
    wjson(os.path.join(MCDATA, "tags", "function", "tick.json"), {"values": [f"{NS}:tick"]})


# ============================================================================ 2. dimensions
def convert_ids(obj, unknown):
    """Remplace récursivement les identifiants du mod : ressources de génération → espace oubliees,
    blocs → blocs vanilla, créatures → créatures vanilla."""
    worldgen_names = set()
    for sub in ("worldgen/biome", "worldgen/configured_feature", "worldgen/placed_feature", "worldgen/noise_settings",
                "dimension", "dimension_type"):
        d = os.path.join(MOD_DATA, sub)
        worldgen_names |= {f[:-5] for f in os.listdir(d)}
    creatures = {c["id"]: c for c in CR.CREATURES}

    block_keys = {"Name", "block", "blocks", "state"}

    def conv(v, key=None):
        if isinstance(v, dict):
            out = {k: conv(x, k) for k, x in v.items()}
            name = out.get("Name")
            if isinstance(name, str) and name.startswith("minecraft:") and "Properties" in out:
                if not name.endswith(WM.KEEP_PROPERTIES):
                    del out["Properties"]
            return out
        if isinstance(v, list):
            return [conv(x, key) for x in v]
        if isinstance(v, str):
            tag = v.startswith("#")
            s = v[1:] if tag else v
            if s.startswith(MOD + ":"):
                local = s.split(":", 1)[1]
                if local in creatures and key == "type":
                    return "minecraft:" + WM.creature(creatures[local])
                b = WM.block(local)
                if b and not tag and (key in block_keys or local not in worldgen_names):
                    return b
                if local in worldgen_names or tag:
                    return ("#" if tag else "") + f"{NS}:{local}"
                unknown.add(local)
            return v
        return v

    return conv(obj)


def build_dimensions():
    unknown = set()
    for sub in ("worldgen/biome", "worldgen/configured_feature", "worldgen/placed_feature", "worldgen/noise_settings",
                "dimension", "dimension_type"):
        src = os.path.join(MOD_DATA, sub)
        for f in sorted(os.listdir(src)):
            with open(os.path.join(src, f), encoding="utf-8") as fh:
                obj = json.load(fh)
            wjson(os.path.join(DATA, sub, f), convert_ids(obj, unknown))
    if unknown:
        raise SystemExit("Identifiants du mod sans équivalent vanilla : " + ", ".join(sorted(unknown)))


# ============================================================================ 3. objets
def item_text(name, color, bold=False):
    return {"text": name, "color": color, "italic": False, "bold": bold}


def lore(*lines):
    return [{"text": l, "color": c, "italic": False} for l, c in lines]


def staff_components(school):
    sid, code, sname, color, _mat, model, *_ = SCHOOL[school]
    spells = [s for s in SPELLS if s[0] == school]
    lines = [(f"École : {sname}", color), ("", "gray")]
    for s in spells:
        lines.append((f"  {s[3]} — niv. {s[4]}", "gray"))
    lines += [("", "gray"), ("Clic droit : lancer le sort", "aqua"), ("Accroupi + clic droit : sort suivant", "aqua")]
    return {
        "minecraft:custom_name": item_text(f"Bâton de {sname.lower()}" if school != "void" else "Bâton du néant", color, True),
        "minecraft:item_model": model,
        "minecraft:lore": lore(*lines),
        "minecraft:custom_data": {NS: {"staff": school}},
        "minecraft:enchantment_glint_override": True,
        "minecraft:rarity": "rare",
        "minecraft:unbreakable": {},
    }


def key_components(dim):
    mat, model, lvl = KEYS[dim]
    return {
        "minecraft:custom_name": item_text(f"Clé — {DIM_FR[dim]}", DIM_COLOR[dim], True),
        "minecraft:item_model": model,
        "minecraft:lore": lore((f"Ouvre un passage vers : {DIM_FR[dim]}", "gray"), (f"Niveau conseillé : {lvl}", "yellow"),
                               ("", "gray"), ("Maintenez clic droit pour voyager.", "aqua"),
                               ("Utilisée dans sa dimension, elle vous ramène.", "aqua")),
        "minecraft:custom_data": {NS: {"key": dim}},
        "minecraft:consumable": {"consume_seconds": 1.6, "animation": "bow", "sound": "minecraft:block.portal.trigger",
                                 "has_consume_particles": False},
        "minecraft:enchantment_glint_override": True,
        "minecraft:rarity": "epic",
        "minecraft:max_stack_size": 1,
    }


WEAPONS = [  # id, nom, dimension, objet de base, matériau, dégâts, vitesse, enchantements, modèle
    ("celestial_blade", "Lame céleste", "celestial", "minecraft:diamond_sword", "minecraft:amethyst_shard", 8, -2.2,
     {"minecraft:sharpness": 3, "minecraft:sweeping_edge": 2}, "minecraft:golden_sword"),
    ("cloud_bow", "Arc des nuées", "celestial", "minecraft:bow", "minecraft:phantom_membrane", None, None,
     {"minecraft:power": 3, "minecraft:punch": 1}, None),
    ("blight_scythe", "Faux corrompue", "corrupted", "minecraft:netherite_hoe", "minecraft:crimson_fungus", 9, -2.6,
     {"minecraft:sharpness": 3}, None),
    ("blood_dagger", "Dague de sang", "corrupted", "minecraft:iron_sword", "minecraft:redstone_block", 5, -1.6,
     {"minecraft:looting": 3, "minecraft:sharpness": 2}, None),
    ("abyssal_trident", "Trident des abysses", "ocean", "minecraft:trident", "minecraft:heart_of_the_sea", None, None,
     {"minecraft:impaling": 4, "minecraft:loyalty": 3}, None),
    ("coral_blade", "Lame corallienne", "ocean", "minecraft:diamond_sword", "minecraft:prismarine_crystals", 7, -2.2,
     {"minecraft:sharpness": 3, "minecraft:knockback": 1}, None),
    ("volcanic_hammer", "Marteau volcanique", "chaos", "minecraft:netherite_axe", "minecraft:magma_block", 13, -3.2,
     {"minecraft:fire_aspect": 2, "minecraft:sharpness": 2}, None),
    ("ember_sword", "Épée de braise", "chaos", "minecraft:golden_sword", "minecraft:blaze_rod", 8, -2.2,
     {"minecraft:fire_aspect": 2}, None),
    ("frost_axe", "Hache du givre", "eternal", "minecraft:diamond_axe", "minecraft:blue_ice", 11, -3.0,
     {"minecraft:sharpness": 3, "minecraft:efficiency": 4}, None),
    ("glacial_crossbow", "Arbalète glaciaire", "eternal", "minecraft:crossbow", "minecraft:packed_ice", None, None,
     {"minecraft:quick_charge": 3, "minecraft:piercing": 4}, None),
    ("depths_pickaxe", "Pioche des profondeurs", "depths", "minecraft:netherite_pickaxe", "minecraft:echo_shard", None, None,
     {"minecraft:efficiency": 5, "minecraft:fortune": 3}, None),
    ("shadow_blade", "Épée de l'ombre", "depths", "minecraft:netherite_sword", "minecraft:sculk", 9, -2.0,
     {"minecraft:sharpness": 4}, None),
    ("world_ender", "Fin des Mondes", "void", "minecraft:netherite_sword", "minecraft:nether_star", 14, -2.4,
     {"minecraft:sharpness": 5, "minecraft:looting": 3, "minecraft:sweeping_edge": 3}, None),
    ("void_bow", "Arc du néant", "void", "minecraft:bow", "minecraft:dragon_breath", None, None,
     {"minecraft:power": 5, "minecraft:flame": 1, "minecraft:infinity": 1}, None),
]

ARMORS = [  # id, nom, dimension, matière, matériau de recette, garniture (matériau, motif), effets du bonus
    ("celestial", "céleste", "celestial", "diamond", "minecraft:amethyst_shard", ("minecraft:quartz", "minecraft:spire"),
     [("slow_falling", 0), ("speed", 0)], "Chute lente et vitesse"),
    ("corrupted", "corrompue", "corrupted", "netherite", "minecraft:crimson_fungus", ("minecraft:redstone", "minecraft:rib"),
     [("strength", 0)], "Force"),
    ("ocean", "des abysses", "ocean", "diamond", "minecraft:prismarine_crystals", ("minecraft:lapis", "minecraft:tide"),
     [("water_breathing", 0), ("dolphins_grace", 0)], "Respiration et grâce du dauphin"),
    ("chaos", "volcanique", "chaos", "netherite", "minecraft:magma_block", ("minecraft:gold", "minecraft:snout"),
     [("fire_resistance", 0)], "Résistance au feu"),
    ("eternal", "du givre", "eternal", "diamond", "minecraft:blue_ice", ("minecraft:iron", "minecraft:ward"),
     [("resistance", 0)], "Résistance"),
    ("depths", "des profondeurs", "depths", "netherite", "minecraft:echo_shard", ("minecraft:amethyst", "minecraft:silence"),
     [("night_vision", 0), ("haste", 1)], "Vision nocturne et célérité II"),
    ("void", "du néant", "void", "netherite", "minecraft:dragon_breath", ("minecraft:emerald", "minecraft:eye"),
     [("regeneration", 0), ("resistance", 0)], "Régénération et résistance"),
]
PIECES = [("helmet", "Casque", "head"), ("chestplate", "Plastron", "chest"), ("leggings", "Jambières", "legs"),
          ("boots", "Bottes", "feet")]


def recipe_shaped(name, pattern, key, result):
    wjson(os.path.join(DATA, "recipe", name + ".json"),
          {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern, "key": key, "result": result})


def build_items():
    for s in SCHOOLS:
        recipe_shaped(f"staff_{s[0]}", ["  M", " S ", "S  "], {"M": s[4], "S": "minecraft:stick"},
                      {"id": "minecraft:carrot_on_a_stick", "count": 1, "components": staff_components(s[0])})
    for d, (mat, _model, _lvl) in KEYS.items():
        if d == "void":
            wjson(os.path.join(DATA, "recipe", "key_void.json"), {
                "type": "minecraft:crafting_shapeless", "category": "misc",
                "ingredients": ["minecraft:nether_star", "minecraft:dragon_breath", "minecraft:echo_shard", "minecraft:ender_eye"],
                "result": {"id": "minecraft:echo_shard", "count": 1, "components": key_components(d)}})
        else:
            recipe_shaped(f"key_{d}", [" M ", "MEM", " M "], {"M": mat, "E": "minecraft:ender_eye"},
                          {"id": "minecraft:echo_shard", "count": 1, "components": key_components(d)})
    for wid, name, dim, base, mat, dmg, speed, ench, model in WEAPONS:
        comps = {"minecraft:custom_name": item_text(name, DIM_COLOR[dim], True),
                 "minecraft:lore": lore((f"Arme légendaire — {DIM_FR[dim]}", "gray")),
                 "minecraft:enchantments": ench, "minecraft:rarity": "epic", "minecraft:custom_data": {NS: {"weapon": wid}}}
        if dmg is not None:
            comps["minecraft:attribute_modifiers"] = [
                {"type": "minecraft:attack_damage", "id": "minecraft:base_attack_damage", "amount": dmg, "operation": "add_value", "slot": "mainhand"},
                {"type": "minecraft:attack_speed", "id": "minecraft:base_attack_speed", "amount": speed, "operation": "add_value", "slot": "mainhand"}]
        if model:
            comps["minecraft:item_model"] = model
        if wid in ("world_ender", "ember_sword"):
            comps["minecraft:unbreakable"] = {}
        recipe_shaped(f"weapon_{wid}", [" M ", "MBM", " M "], {"M": mat, "B": base},
                      {"id": base, "count": 1, "components": comps})
    for aid, adj, dim, material, mat, (trim_mat, trim_pat), effects, bonus in ARMORS:
        for piece, pname, _slot in PIECES:
            base = f"minecraft:{material}_{piece}"
            comps = {"minecraft:custom_name": item_text(f"{pname} {adj}", DIM_COLOR[dim], True),
                     "minecraft:lore": lore((f"Armure — {DIM_FR[dim]}", "gray"), ("Ensemble complet :", "gold"), (f"  {bonus}", "yellow")),
                     "minecraft:trim": {"material": trim_mat, "pattern": trim_pat},
                     "minecraft:enchantments": {"minecraft:protection": 3, "minecraft:unbreaking": 2},
                     "minecraft:rarity": "rare", "minecraft:custom_data": {NS: {"set": aid}}}
            recipe_shaped(f"armor_{aid}_{piece}", [" M ", "MBM", " M "], {"M": mat, "B": base},
                          {"id": base, "count": 1, "components": comps})


# ============================================================================ 4. fonctions de base
def build_core():
    objectives = [("level", "dummy"), ("xp", "dummy"), ("xpnext", "dummy"), ("mana", "dummy"), ("maxmana", "dummy"),
                  ("spell", "dummy"), ("school", "dummy"), ("cd", "dummy"), ("cds", "dummy"),
                  ("use", "minecraft.used:minecraft.carrot_on_a_stick"), ("kills", "minecraft.custom:minecraft.mob_kills"),
                  ("killsl", "dummy"), ("menu", "trigger"), ("life", "dummy"), ("hp", "dummy"), ("hpl", "dummy"),
                  ("hpmax", "dummy"), ("rx", "dummy"), ("ry", "dummy"), ("rz", "dummy"), ("rdim", "dummy"), ("tmp", "dummy"),
                  ("sneak", "dummy"), ("dmgi", "dummy")]
    lines = [f"scoreboard objectives add {NS}.{n} {c}" for n, c in objectives]
    lines += [f"scoreboard players set #{n} {NS}.tmp {n}" for n in (2, 3, 5, 10, 20, 100)]
    bars_full = [("■" * i) for i in range(11)]
    bars_empty = [("■" * (10 - i)) for i in range(11)]
    lines += [f"data modify storage {NS}:ui full set value {snbt(bars_full)}",
              f"data modify storage {NS}:ui empty set value {snbt(bars_empty)}",
              f'tellraw @a[tag={NS}.debug] {snbt(T("[Dimensions Oubliées] data pack chargé", "gold"))}']
    fn("load", lines)

    fn("tick", f"""
scoreboard players add #t {NS}.tmp 1
scoreboard players operation #t5 {NS}.tmp = #t {NS}.tmp
scoreboard players operation #t5 {NS}.tmp %= #5 {NS}.tmp
scoreboard players operation #t20 {NS}.tmp = #t {NS}.tmp
scoreboard players operation #t20 {NS}.tmp %= #20 {NS}.tmp
scoreboard players enable @a {NS}.menu
execute as @a at @s run function {NS}:player/tick
execute as @e[tag={NS}.fx] at @s run function {NS}:fx/tick
execute as @e[type=marker,tag={NS}.spell] at @s run function {NS}:spell/marker/tick
execute if score #t20 {NS}.tmp matches 0 as @e[tag={NS}.summon] at @s run function {NS}:summon/tick
execute as @a at @s as @e[distance=..40,tag=!{NS}.lv,type=#{NS}:leveled] at @s run function {NS}:mob/init
execute as @e[tag={NS}.lv] at @s if entity @a[distance=..24] run function {NS}:mob/track
""")

    # --- joueur
    fn("player/tick", f"""
execute unless score @s {NS}.level matches 1.. run function {NS}:player/first_join
execute unless entity @s[tag={NS}.recipes] run function {NS}:player/recipes
scoreboard players remove @s[scores={{{NS}.cd=1..}}] {NS}.cd 1
function {NS}:player/school
execute if score @s {NS}.use matches 1.. run function {NS}:player/use
scoreboard players set @s {NS}.use 0
execute if score @s {NS}.kills > @s {NS}.killsl run function {NS}:player/kill_xp
execute if score #t20 {NS}.tmp matches 0 run function {NS}:player/second
execute if score #t5 {NS}.tmp matches 0 if score @s {NS}.school matches 1.. run function {NS}:hud/show
execute if score @s {NS}.menu matches 1.. run function {NS}:menu/dispatch
""")
    school_lines = [f"scoreboard players set @s {NS}.school 0"]
    for sid, code, *_ in SCHOOLS:
        school_lines.append(f'execute if items entity @s weapon.mainhand *[minecraft:custom_data~{{{NS}:{{staff:"{sid}"}}}}] '
                            f"run scoreboard players set @s {NS}.school {code}")
    fn("player/school", school_lines)

    starter = staff_components("fire")
    fn("player/first_join", f"""
scoreboard players set @s {NS}.level 1
scoreboard players set @s {NS}.xp 0
scoreboard players set @s {NS}.xpnext 60
scoreboard players set @s {NS}.maxmana 106
scoreboard players set @s {NS}.mana 106
scoreboard players set @s {NS}.spell 0
scoreboard players set @s {NS}.cd 0
scoreboard players operation @s {NS}.killsl = @s {NS}.kills
function {NS}:guide/give
give @s carrot_on_a_stick[{comp_snbt(starter)}]
title @s times 10 70 20
title @s subtitle {snbt(T("Clic droit avec le bâton pour lancer un sort", "yellow"))}
title @s title {snbt(T("Les Dimensions Oubliées", "gold", bold=True))}
tellraw @s [{snbt(T("✦ Bienvenue ! ", "gold", bold=True))},{snbt(T("Lisez le Grimoire des Anciens, ou tapez ", "gray"))},{snbt(T("/trigger oubliees.menu", "aqua", click_event={"action": "suggest_command", "command": "/trigger oubliees.menu"}))},{snbt(T(" pour votre fiche de personnage.", "gray"))}]
playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 0.6 1.2
advancement grant @s only {NS}:root
""")

    # Toutes les recettes du pack dans le livre de recettes de l'établi
    names = sorted(f[:-5] for f in os.listdir(os.path.join(DATA, "recipe")))
    fn("player/recipes", [f"recipe give @s {NS}:{n}" for n in names] + [f"tag @s add {NS}.recipes"])

    fn("player/second", f"""
# Régénération du mana : 2 + niveau / 10 par seconde
scoreboard players operation #r {NS}.tmp = @s {NS}.level
scoreboard players operation #r {NS}.tmp /= #10 {NS}.tmp
scoreboard players add #r {NS}.tmp 2
scoreboard players operation @s {NS}.mana += #r {NS}.tmp
scoreboard players operation @s {NS}.mana < @s {NS}.maxmana
function {NS}:armor/check
""")

    fn("player/kill_xp", f"""
scoreboard players operation #k {NS}.tmp = @s {NS}.kills
scoreboard players operation #k {NS}.tmp -= @s {NS}.killsl
scoreboard players operation @s {NS}.killsl = @s {NS}.kills
scoreboard players operation #k {NS}.tmp *= #10 {NS}.tmp
scoreboard players add #k {NS}.tmp 4
scoreboard players operation @s {NS}.xp += #k {NS}.tmp
function {NS}:player/check_level
""")
    fn("player/check_level", f"""
execute if score @s {NS}.level matches {MAX_LEVEL}.. run return 0
execute if score @s {NS}.xp >= @s {NS}.xpnext run function {NS}:player/level_up
""")
    milestones = []
    for lv in range(5, MAX_LEVEL + 1, 5):
        milestones.append(f"execute if score @s {NS}.level matches {lv} run attribute @s minecraft:max_health modifier add {NS}:level_{lv} 2 add_value")
    for lv in range(10, MAX_LEVEL + 1, 10):
        milestones.append(f"execute if score @s {NS}.level matches {lv} run attribute @s minecraft:attack_damage modifier add {NS}:level_{lv} 1 add_value")
    for lv in (10, 25, 50):
        milestones.append(f"execute if score @s {NS}.level matches {lv} run advancement grant @s only {NS}:level_{lv}")
    fn("player/level_up", f"""
scoreboard players operation @s {NS}.xp -= @s {NS}.xpnext
scoreboard players add @s {NS}.level 1
# expérience requise : 40 + 20 × niveau ; mana max : 100 + 6 × niveau
scoreboard players operation @s {NS}.xpnext = @s {NS}.level
scoreboard players operation @s {NS}.xpnext *= #20 {NS}.tmp
scoreboard players add @s {NS}.xpnext 40
scoreboard players operation @s {NS}.maxmana = @s {NS}.level
scoreboard players operation @s {NS}.maxmana *= #2 {NS}.tmp
scoreboard players operation @s {NS}.maxmana *= #3 {NS}.tmp
scoreboard players add @s {NS}.maxmana 100
scoreboard players operation @s {NS}.mana = @s {NS}.maxmana
{chr(10).join(milestones)}
title @s times 8 50 20
title @s subtitle [{snbt(T("Mana et vie augmentés — ", "yellow"))},{snbt(T("/trigger oubliees.menu", "aqua"))}]
title @s title [{snbt(T("NIVEAU ", "gold", bold=True))},{snbt({"score": {"name": "@s", "objective": f"{NS}.level"}, "color": "gold", "bold": True})}]
playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 0.8 1
playsound minecraft:entity.player.levelup player @a ~ ~ ~ 1 0.8
particle minecraft:totem_of_undying ~ ~1 ~ 0.5 0.8 0.5 0.3 60
function {NS}:fx/pillar {{block:"yellow_stained_glass",h:140}}
function {NS}:fx/circle {{color:"#ffd84a",glyph:"۞"}}
function {NS}:player/check_level
""")

    # --- dégâts de base des sorts selon le niveau (dixièmes) → storage oubliees:cast dmg
    fn("spell/power", f"""
# $(base) en dixièmes ; puissance = base × (100 + 3 × niveau) / 100
$scoreboard players set #b {NS}.tmp $(base)
scoreboard players operation #l {NS}.tmp = @s {NS}.level
scoreboard players operation #l {NS}.tmp *= #3 {NS}.tmp
scoreboard players add #l {NS}.tmp 100
scoreboard players operation #b {NS}.tmp *= #l {NS}.tmp
scoreboard players operation #b {NS}.tmp /= #100 {NS}.tmp
execute store result storage {NS}:cast dmg float 0.1 run scoreboard players get #b {NS}.tmp
""")


def comp_snbt(comps):
    """Composants d'objet pour /give : clé=valeur séparés par des virgules (valeurs en SNBT)."""
    return ",".join(f"{k.split(':', 1)[1]}={snbt(v)}" for k, v in comps.items())


# ============================================================================ 5. magie
def spell_line(s):
    return s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8]


def build_magic():
    # Utilisation du bâton : accroupi = sort suivant, sinon lancer.
    fn("player/use", f"""
execute if score @s {NS}.school matches 0 run return 0
execute if predicate {NS}:sneaking run return run function {NS}:spell/cycle
function {NS}:spell/cast
""")
    wjson(os.path.join(DATA, "predicate", "sneaking.json"),
          {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"flags": {"is_sneaking": True}}})

    # Cycle : sort suivant débloqué de l'école du bâton tenu
    cyc = [f"scoreboard players add @s {NS}.spell 1", f"execute if score @s {NS}.spell matches 4.. run scoreboard players set @s {NS}.spell 0"]
    for sid, code, *_ in SCHOOLS:
        for s in [x for x in SPELLS if x[0] == sid]:
            if s[4] > 1:
                cyc.append(f"execute if score @s {NS}.school matches {code} if score @s {NS}.spell matches {s[1]} "
                           f"if score @s {NS}.level matches ..{s[4] - 1} run scoreboard players set @s {NS}.spell 0")
    cyc += [f"playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 1.4", f"function {NS}:hud/show"]
    fn("spell/cycle", cyc)

    cast = [f"# sort verrouillé (niveau trop bas) : repli sur le premier sort de l'école"]
    for sid, code, *_ in SCHOOLS:
        for s in [x for x in SPELLS if x[0] == sid]:
            if s[4] > 1:
                cast.append(f"execute if score @s {NS}.school matches {code} if score @s {NS}.spell matches {s[1]} "
                            f"if score @s {NS}.level matches ..{s[4] - 1} run scoreboard players set @s {NS}.spell 0")
    for s in SPELLS:
        cast.append(f"execute if score @s {NS}.school matches {SCHOOL[s[0]][1]} if score @s {NS}.spell matches {s[1]} "
                    f"run return run function {NS}:spell/try/{s[2]}")
    fn("spell/cast", cast)

    fn("spell/feedback/cooldown", f"""
title @s actionbar {snbt(T("⌛ Sort en recharge", "red"))}
playsound minecraft:block.note_block.bass player @s ~ ~ ~ 0.6 0.6
""")
    fn("spell/feedback/no_mana", f"""
title @s actionbar {snbt(T("❖ Mana insuffisant", "blue"))}
playsound minecraft:block.note_block.bass player @s ~ ~ ~ 0.6 0.5
""")

    for school, idx, sid, name, lvl, mana, cd, base, kind, desc in SPELLS:
        sc = SCHOOL[school]
        fn(f"spell/try/{sid}", f"""
execute if score @s {NS}.level matches ..{lvl - 1} run return run title @s actionbar {snbt(T(f"🔒 {name} : niveau {lvl} requis", "red"))}
execute if score @s {NS}.cd matches 1.. run return run function {NS}:spell/feedback/cooldown
execute unless score @s {NS}.mana matches {mana}.. run return run function {NS}:spell/feedback/no_mana
scoreboard players remove @s {NS}.mana {mana}
scoreboard players set @s {NS}.cd {cd}
scoreboard players add @s {NS}.xp 1
function {NS}:spell/power {{base:{base * 10}}}
data modify storage {NS}:cast school set value "{school}"
tag @s add {NS}.caster
function {NS}:fx/circle {{color:"{sc[3]}",glyph:"۞"}}
function {NS}:spell/cast/{sid}
tag @s remove {NS}.caster
advancement grant @s only {NS}:first_spell
function {NS}:player/check_level
""")
        fn(f"spell/cast/{sid}", cast_body(school, sid, kind, base))

    # Coup porté par un sort : dégâts attribués au lanceur, effet de l'école
    effects = {
        "fire": ["data modify entity @s[type=!player] Fire set value 100s", "particle minecraft:lava ~ ~1 ~ 0.3 0.4 0.3 0 6"],
        "ice": ["effect give @s minecraft:slowness 3 2", "data modify entity @s[type=!player] TicksFrozen set value 220",
                "particle minecraft:snowflake ~ ~1 ~ 0.3 0.5 0.3 0.02 14"],
        "lightning": ["effect give @s minecraft:glowing 2 0", "particle minecraft:electric_spark ~ ~1 ~ 0.3 0.5 0.3 0.1 20"],
        "nature": ["effect give @s minecraft:poison 4 1", "particle minecraft:composter ~ ~1 ~ 0.3 0.5 0.3 0 14"],
        "void": ["effect give @s minecraft:wither 4 1", "particle minecraft:reverse_portal ~ ~1 ~ 0.3 0.5 0.3 0.05 20"],
    }
    for sid, *_ in SCHOOLS:
        fn(f"spell/hit/{sid}", [
            "# Joueurs : jamais en créatif ou spectateur ; le PvP du serveur et le tir allié des équipes sont",
            "# appliqués par Minecraft (damage ... by <joueur>). Les effets ne suivent que si le coup a porté.",
            "execute if entity @s[type=player,gamemode=creative] run return 0",
            "execute if entity @s[type=player,gamemode=spectator] run return 0",
            f"$execute store success score #ok {NS}.tmp run damage @s $(dmg) {SCHOOL[sid][8]} by @p[tag={NS}.caster]",
            f"execute if score #ok {NS}.tmp matches 0 run return 0"] + effects[sid])


    # Rayon instantané (projectile magique)
    for sid, *_ in SCHOOLS:
        part = SCHOOL[sid][6]
        fn(f"spell/ray/{sid}_step", f"""
particle {part} ~ ~ ~ 0.04 0.04 0.04 0.005 2 force
particle minecraft:dust{{color:{RGB[sid]},scale:1.4}} ~ ~ ~ 0.06 0.06 0.06 0 2 force
scoreboard players add #steps {NS}.tmp 1
execute positioned ~-0.5 ~-0.5 ~-0.5 as @e[dx=0,dy=0,dz=0,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster,limit=1] positioned ~0.5 ~0.5 ~0.5 run function {NS}:spell/ray/hit_{sid}
execute if score #hit {NS}.tmp matches 1 run return 0
execute if score #steps {NS}.tmp matches 60.. run return run function {NS}:fx/burst {{block:"{SCHOOL[sid][7]}",size:14}}
execute unless block ^ ^ ^0.5 #minecraft:replaceable run return run function {NS}:fx/burst {{block:"{SCHOOL[sid][7]}",size:14}}
execute positioned ^ ^ ^0.5 run function {NS}:spell/ray/{sid}_step
""")
        fn(f"spell/ray/hit_{sid}", f"""
scoreboard players set #hit {NS}.tmp 1
function {NS}:spell/hit/{sid} with storage {NS}:cast
execute at @s positioned ~ ~1 ~ run function {NS}:fx/burst {{block:"{SCHOOL[sid][7]}",size:18}}
""")
        fn(f"spell/ray/{sid}", f"""
scoreboard players set #steps {NS}.tmp 0
scoreboard players set #hit {NS}.tmp 0
execute anchored eyes positioned ^ ^ ^0.8 run function {NS}:spell/ray/{sid}_step
playsound {cast_sound(sid)} player @a ~ ~ ~ 1 1.3
""")

    # Visée : premier ennemi ou bloc dans l'axe du regard (32 blocs), puis appel de la zone $(area)
    fn("spell/aim/step", f"""
scoreboard players add #steps {NS}.tmp 1
$execute if score #steps {NS}.tmp matches 64.. run return run function {NS}:spell/area/$(area)
$execute positioned ~-0.5 ~-0.5 ~-0.5 if entity @e[dx=0,dy=0,dz=0,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster] positioned ~0.5 ~0.5 ~0.5 run return run function {NS}:spell/area/$(area)
$execute unless block ^ ^ ^0.5 #minecraft:replaceable run return run function {NS}:spell/area/$(area)
execute positioned ^ ^ ^0.5 run function {NS}:spell/aim/step with storage {NS}:cast
""")
    fn("spell/aim/start", f"""
scoreboard players set #steps {NS}.tmp 0
$data modify storage {NS}:cast area set value "$(area)"
execute anchored eyes positioned ^ ^ ^0.6 run function {NS}:spell/aim/step with storage {NS}:cast
""")
    # Zones (exécutées au point visé, dans le contexte du lanceur pour l'attribution)
    for name, body in AREAS.items():
        fn(f"spell/area/{name}", body)

    # Marqueurs de sorts persistants (météore, tempête, puits, singularité)
    fn("spell/marker/tick", f"""
scoreboard players remove @s {NS}.life 1
function {NS}:util/tag_owner with entity @s data
execute if entity @s[tag={NS}.meteor] run function {NS}:spell/marker/meteor
execute if entity @s[tag={NS}.tempest] run function {NS}:spell/marker/tempest
execute if entity @s[tag={NS}.well] run function {NS}:spell/marker/well
tag @a remove {NS}.caster
execute if score @s {NS}.life matches ..0 run kill @s
""")
    fn("util/tag_owner", f"$tag @a[nbt={{UUID:$(owner)}}] add {NS}.caster")
    fn("spell/marker/meteor", f"""
execute if score @s {NS}.life matches 1.. run particle minecraft:flame ~ ~0.2 ~ 2 0 2 0 6
execute if score @s {NS}.life matches 1.. run particle minecraft:dust{{color:{RGB['fire']},scale:2}} ~ ~0.1 ~ 2.5 0 2.5 0 6
execute if score @s {NS}.life matches 0 run function {NS}:spell/marker/meteor_impact
""")
    fn("spell/marker/meteor_impact", f"""
execute as @e[distance=..4.5,type=!#{NS}:non_target,tag=!{NS}.summon] run function {NS}:spell/hit/fire with entity @e[type=marker,tag={NS}.meteor,limit=1,sort=nearest] data
particle minecraft:explosion_emitter ~ ~1 ~ 0 0 0 0 1 force
particle minecraft:lava ~ ~1 ~ 2 1 2 0 40 force
playsound minecraft:entity.generic.explode master @a ~ ~ ~ 2 0.8
kill @e[type=item_display,tag={NS}.meteor_rock,distance=..30,limit=1,sort=nearest]
function {NS}:fx/burst {{block:"orange_stained_glass",size:50}}
function {NS}:fx/nova {{block:"orange_stained_glass",size:110}}
""")
    fn("spell/marker/tempest", f"""
execute store result score #m {NS}.tmp run scoreboard players get @s {NS}.life
scoreboard players operation #m {NS}.tmp %= #10 {NS}.tmp
execute if score #m {NS}.tmp matches 0 run function {NS}:spell/marker/tempest_bolt
particle minecraft:electric_spark ~ ~6 ~ 4 0.5 4 0 10
particle minecraft:cloud ~ ~7 ~ 4 0.3 4 0 6
""")
    fn("spell/marker/tempest_bolt", f"""
execute store result storage {NS}:tmp x int 1 run random value -5..5
execute store result storage {NS}:tmp z int 1 run random value -5..5
function {NS}:spell/marker/tempest_strike with storage {NS}:tmp
""")
    fn("spell/marker/tempest_strike", f"""
$execute positioned ~$(x) ~ ~$(z) run summon minecraft:lightning_bolt ~ ~ ~
$execute positioned ~$(x) ~ ~$(z) as @e[distance=..3,type=!#{NS}:non_target,tag=!{NS}.summon] run function {NS}:spell/hit/lightning with entity @e[type=marker,tag={NS}.tempest,limit=1,sort=nearest] data
""")
    fn("spell/marker/well", f"""
tag @s add {NS}.current
execute as @e[distance=1.2..7,type=!#{NS}:non_target,tag=!{NS}.summon] at @s facing entity @e[type=marker,tag={NS}.current,limit=1] feet run tp @s ^ ^ ^0.3
particle minecraft:reverse_portal ~ ~1 ~ 3 1 3 0.3 20
particle minecraft:dust{{color:{RGB['void']},scale:2}} ~ ~1 ~ 0.4 0.4 0.4 0 6
execute store result score #m {NS}.tmp run scoreboard players get @s {NS}.life
scoreboard players operation #m {NS}.tmp %= #20 {NS}.tmp
execute if score #m {NS}.tmp matches 0 as @e[distance=..2.5,type=!#{NS}:non_target,tag=!{NS}.summon] run function {NS}:spell/hit/void with entity @e[type=marker,tag={NS}.current,limit=1] data
execute if score @s {NS}.life matches 0 if entity @s[tag={NS}.collapse] run function {NS}:spell/marker/collapse
tag @s remove {NS}.current
""")
    fn("spell/marker/collapse", f"""
data modify entity @s data.dmg set from entity @s data.big
execute as @e[distance=..7,type=!#{NS}:non_target,tag=!{NS}.summon] run function {NS}:spell/hit/void with entity @e[type=marker,tag={NS}.current,limit=1] data
particle minecraft:explosion_emitter ~ ~1 ~ 0 0 0 0 1 force
particle minecraft:reverse_portal ~ ~1 ~ 1 1 1 1.5 200 force
playsound minecraft:entity.warden.sonic_boom master @a ~ ~ ~ 1.5 0.7
function {NS}:fx/burst {{block:"purple_stained_glass",size:60}}
function {NS}:fx/nova {{block:"purple_stained_glass",size:140}}
""")

    # Invocations : durée de vie (secondes) et comportements périodiques
    fn("summon/tick", f"""
scoreboard players remove @s {NS}.life 1
execute if entity @s[tag={NS}.sentinel] as @e[distance=..8,type=#{NS}:hostile,limit=3,sort=nearest] run function {NS}:summon/sentinel_freeze
particle minecraft:soul_fire_flame ~ ~1 ~ 0.3 0.5 0.3 0.01 3
execute if score @s {NS}.life matches ..0 run function {NS}:summon/expire
""")
    fn("summon/sentinel_freeze", f"""
effect give @s minecraft:slowness 2 2
data modify entity @s TicksFrozen set value 200
damage @s 3 minecraft:freeze
particle minecraft:snowflake ~ ~1 ~ 0.3 0.5 0.3 0.02 12
""")
    fn("summon/expire", f"""
particle minecraft:poof ~ ~1 ~ 0.4 0.6 0.4 0.02 20
particle minecraft:soul ~ ~1 ~ 0.3 0.5 0.3 0.02 8
tp @s ~ -1000 ~
kill @s
""")


def cast_sound(school):
    return {"fire": "minecraft:entity.blaze.shoot", "ice": "minecraft:block.glass.break",
            "lightning": "minecraft:entity.lightning_bolt.thunder", "nature": "minecraft:block.grass.break",
            "void": "minecraft:entity.enderman.teleport"}[school]


AREAS = {}


def cast_body(school, sid, kind, base):
    glass = SCHOOL[school][7]
    if kind == "ray":
        return f"function {NS}:spell/ray/{school}"
    if kind == "nova":
        return f"""
execute as @e[distance=..5.5,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster] run function {NS}:spell/hit/{school} with storage {NS}:cast
function {NS}:fx/nova {{block:"{glass}",size:110}}
function {NS}:fx/burst {{block:"{glass}",size:30}}
particle {SCHOOL[school][6]} ~ ~1 ~ 2.5 0.5 2.5 0.15 120
playsound {cast_sound(school)} player @a ~ ~ ~ 1.2 0.8
playsound minecraft:entity.generic.explode player @a ~ ~ ~ 0.6 1.4"""
    if kind == "pillar":
        AREAS["pillar"] = f"""
execute positioned ~ ~-0.5 ~ as @e[distance=..3,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster] run function {NS}:spell/hit/fire with storage {NS}:cast
execute align y run function {NS}:fx/pillar {{block:"orange_stained_glass",h:120}}
function {NS}:fx/nova {{block:"orange_stained_glass",size:60}}
particle minecraft:flame ~ ~2 ~ 0.6 3 0.6 0.05 160 force
particle minecraft:lava ~ ~1 ~ 1 1 1 0 20 force
playsound minecraft:item.firecharge.use player @a ~ ~ ~ 1.5 0.7"""
        return f"function {NS}:spell/aim/start {{area:\"pillar\"}}"
    if kind == "meteor":
        AREAS["meteor"] = f"""
summon minecraft:marker ~ ~ ~ {{Tags:["{NS}.spell","{NS}.meteor","{NS}.new"]}}
data modify entity @e[type=marker,tag={NS}.new,limit=1] data.dmg set from storage {NS}:cast dmg
data modify entity @e[type=marker,tag={NS}.new,limit=1] data.owner set from entity @p[tag={NS}.caster] UUID
scoreboard players set @e[type=marker,tag={NS}.new,limit=1] {NS}.life 20
tag @e[type=marker,tag={NS}.new] remove {NS}.new
summon minecraft:item_display ~ ~16 ~ {{Tags:["{NS}.fx","{NS}.meteor_rock","{NS}.fall"],item:{{id:"minecraft:magma_block",count:1}},teleport_duration:1,brightness:{{sky:15,block:15}},transformation:{{left_rotation:[0.2f,0.3f,0.1f,0.92f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[3f,3f,3f]}}}}
scoreboard players set @e[type=item_display,tag={NS}.meteor_rock,distance=..20,limit=1,sort=nearest] {NS}.life 24
function {NS}:fx/circle {{color:"#ff5020",glyph:"✺"}}
playsound minecraft:entity.ghast.shoot player @a ~ ~ ~ 1.5 0.6"""
        return f"function {NS}:spell/aim/start {{area:\"meteor\"}}"
    if kind == "prison":
        AREAS["prison"] = f"""
execute as @e[distance=..3.5,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster] run function {NS}:spell/area/prison_hit
execute align y run function {NS}:fx/spikes
function {NS}:fx/nova {{block:"light_blue_stained_glass",size:70}}
particle minecraft:snowflake ~ ~1 ~ 2 1 2 0.05 120 force
playsound minecraft:block.glass.break player @a ~ ~ ~ 1.5 0.6"""
        AREAS["prison_hit"] = f"""
function {NS}:spell/hit/ice with storage {NS}:cast
effect give @s minecraft:slowness 6 5
effect give @s minecraft:mining_fatigue 6 2"""
        return f"function {NS}:spell/aim/start {{area:\"prison\"}}"
    if kind == "strike":
        AREAS["strike"] = f"""
summon minecraft:lightning_bolt ~ ~ ~
execute as @e[distance=..2.5,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster] run function {NS}:spell/hit/lightning with storage {NS}:cast
function {NS}:fx/burst {{block:"yellow_stained_glass",size:25}}"""
        return f"function {NS}:spell/aim/start {{area:\"strike\"}}"
    if kind == "chain":
        AREAS["chain"] = f"""
execute as @e[distance=..8,type=!#{NS}:non_target,tag=!{NS}.summon,tag=!{NS}.caster,limit=3,sort=nearest] at @s run function {NS}:spell/area/chain_hit"""
        AREAS["chain_hit"] = f"""
summon minecraft:lightning_bolt ~ ~ ~
function {NS}:spell/hit/lightning with storage {NS}:cast
function {NS}:fx/burst {{block:"yellow_stained_glass",size:20}}"""
        return f"function {NS}:spell/aim/start {{area:\"chain\"}}"
    if kind == "tempest":
        AREAS["tempest"] = marker_area("tempest", 100, "")
        return f"function {NS}:spell/aim/start {{area:\"tempest\"}}\nplaysound minecraft:entity.lightning_bolt.thunder player @a ~ ~ ~ 1.5 0.6"
    if kind == "well":
        AREAS["well"] = marker_area("well", 100, "")
        return f"function {NS}:spell/aim/start {{area:\"well\"}}\nplaysound minecraft:block.portal.trigger player @a ~ ~ ~ 1 1.6"
    if kind == "singularity":
        AREAS["singularity"] = marker_area("well", 70, f"tag @e[type=marker,tag={NS}.new,limit=1] add {NS}.collapse\n"
                                           f"data modify entity @e[type=marker,tag={NS}.new,limit=1] data.big set from storage {NS}:cast dmg\n"
                                           f"data modify entity @e[type=marker,tag={NS}.new,limit=1] data.dmg set value 2.0f", big=True)
        return f"function {NS}:spell/aim/start {{area:\"singularity\"}}\nplaysound minecraft:block.portal.trigger player @a ~ ~ ~ 1 0.6"
    if kind == "heal":
        return f"""
effect give @a[distance=..6] minecraft:instant_health 1 0
effect give @a[distance=..6] minecraft:regeneration 6 1
execute if score @s {NS}.level matches 20.. run effect give @a[distance=..6] minecraft:instant_health 1 1
particle minecraft:heart ~ ~1.2 ~ 1.5 0.6 1.5 0 16
particle minecraft:happy_villager ~ ~1 ~ 2 1 2 0 40
function {NS}:fx/pillar {{block:"lime_stained_glass",h:50}}
playsound minecraft:block.amethyst_block.chime player @a ~ ~ ~ 1.5 1.2"""
    if kind == "blink":
        return f"""
particle minecraft:reverse_portal ~ ~1 ~ 0.3 0.6 0.3 0.2 60
function {NS}:fx/burst {{block:"purple_stained_glass",size:16}}
scoreboard players set #steps {NS}.tmp 0
execute anchored eyes positioned ^ ^ ^0.5 run function {NS}:spell/blink_step
playsound minecraft:entity.enderman.teleport player @a ~ ~ ~ 1 1.2"""
    if kind == "summon_wolves":
        return summon_cmds("wolf", 3, "Loup spectral", "aqua", 30, 6, 0.32, 1.0, extra=",CollarColor:3b") + \
            f"\nfunction {NS}:fx/nova {{block:\"lime_stained_glass\",size:60}}\nadvancement grant @s only {NS}:summon"
    if kind == "summon_golem":
        return summon_cmds("iron_golem", 1, "Gardien de pierre", "green", 120, 18, 0.25, 1.15, extra=",PlayerCreated:1b") + \
            f"\nfunction {NS}:fx/pillar {{block:\"lime_stained_glass\",h:60}}\nadvancement grant @s only {NS}:summon"
    if kind == "summon_sentinel":
        return summon_cmds("snow_golem", 1, "Sentinelle de givre", "aqua", 60, 0, 0.25, 2.4, extra=",Pumpkin:1b", sentinel=True) + \
            f"\nfunction {NS}:fx/pillar {{block:\"light_blue_stained_glass\",h:70}}\nadvancement grant @s only {NS}:summon"
    raise ValueError(kind)


def marker_area(kind, life, extra, big=False):
    rune = {"tempest": ("#ffe24a", "✵"), "well": ("#b066ff", "۞")}[kind]
    return f"""
summon minecraft:marker ~ ~ ~ {{Tags:["{NS}.spell","{NS}.{kind}","{NS}.new"]}}
data modify entity @e[type=marker,tag={NS}.new,limit=1] data.dmg set from storage {NS}:cast dmg
data modify entity @e[type=marker,tag={NS}.new,limit=1] data.owner set from entity @p[tag={NS}.caster] UUID
scoreboard players set @e[type=marker,tag={NS}.new,limit=1] {NS}.life {life}
{extra}
tag @e[type=marker,tag={NS}.new] remove {NS}.new
function {NS}:fx/circle_long {{color:"{rune[0]}",glyph:"{rune[1]}",life:{life}}}
{f'function {NS}:fx/core {{life:{life}}}' if kind == "well" else ''}"""


def summon_cmds(etype, count, name, color, hp, dmg, speed, scale, extra="", sentinel=False):
    attrs = [f'{{id:"minecraft:max_health",base:{hp}d}}', f'{{id:"minecraft:scale",base:{scale}d}}',
             f'{{id:"minecraft:movement_speed",base:{speed}d}}']
    if dmg:
        attrs.append(f'{{id:"minecraft:attack_damage",base:{dmg}d}}')
    tags = f'"{NS}.summon","{NS}.new"' + (f',"{NS}.sentinel"' if sentinel else "")
    lines = []
    offsets = ["^1.5 ^ ^1.5", "^-1.5 ^ ^1.5", "^ ^ ^2.5"][:count] if count > 1 else ["^ ^ ^2.5"]
    for off in offsets:
        lines.append(f'summon minecraft:{etype} {off} {{Tags:[{tags}],CustomName:{snbt(T("✦ " + name, color))},'
                     f'CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"minecraft:empty",attributes:[{",".join(attrs)}],Health:{hp}f{extra}}}')
    if etype == "wolf":
        lines.append(f"execute as @e[type=wolf,tag={NS}.new] run data modify entity @s Owner set from entity @p[tag={NS}.caster] UUID")
    lines.append(f"scoreboard players set @e[tag={NS}.new] {NS}.life 45")
    lines.append(f"execute as @e[tag={NS}.new] at @s run particle minecraft:soul ~ ~1 ~ 0.4 0.8 0.4 0.05 30")
    lines.append(f"execute as @e[tag={NS}.new] at @s run function {NS}:fx/circle {{color:\"#60ffd0\",glyph:\"۞\"}}")
    lines.append(f"tag @e[tag={NS}.new] remove {NS}.new")
    lines.append("playsound minecraft:entity.evoker.prepare_summon player @a ~ ~ ~ 1 1")
    return "\n".join(lines)


def build_blink():
    fn("spell/blink_step", f"""
scoreboard players add #steps {NS}.tmp 1
particle minecraft:portal ~ ~ ~ 0.1 0.1 0.1 0.5 3
execute if score #steps {NS}.tmp matches 24.. run return run function {NS}:spell/blink_land
execute unless block ^ ^ ^0.5 #minecraft:replaceable run return run function {NS}:spell/blink_land
execute positioned ^ ^ ^0.5 run function {NS}:spell/blink_step
""")
    fn("spell/blink_land", f"""
execute positioned ~ ~-1.6 ~ if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run tp @s ~ ~ ~
execute at @s run particle minecraft:reverse_portal ~ ~1 ~ 0.3 0.6 0.3 0.2 60
execute at @s run function {NS}:fx/burst {{block:"purple_stained_glass",size:16}}
""")


# ============================================================================ 6. effets 3D (entités d'affichage)
def build_fx():
    fn("fx/tick", f"""
scoreboard players remove @s {NS}.life 1
execute if entity @s[tag={NS}.spin] run tp @s ~ ~ ~ ~9 ~
execute if entity @s[tag={NS}.spin_fast] run tp @s ~ ~ ~ ~-16 ~
execute if entity @s[tag={NS}.fall] run tp @s ~ ~-0.7 ~ ~8 ~
execute if entity @s[tag={NS}.rise] run tp @s ~ ~0.06 ~
execute if entity @s[tag={NS}.grow] run function {NS}:fx/grow_apply
execute if score @s {NS}.life matches 3 if entity @s[tag={NS}.shrink] run data merge entity @s {{start_interpolation:0,interpolation_duration:3,transformation:{{scale:[0f,0f,0f],translation:[0f,0f,0f]}}}}
execute if score @s {NS}.life matches ..0 run kill @s
""")
    new = f"tag @e[tag={NS}.new_fx] remove {NS}.new_fx"
    # Cercle runique au sol (glyphe géant couché, en rotation)
    fn("fx/circle", f"""
$summon minecraft:text_display ~ ~0.06 ~ {{Tags:["{NS}.fx","{NS}.spin","{NS}.shrink","{NS}.new_fx"],text:{{text:"$(glyph)",color:"$(color)"}},background:0,billboard:"fixed",teleport_duration:2,brightness:{{sky:15,block:15}},Rotation:[0f,0f],transformation:{{left_rotation:[-0.7071f,0f,0f,0.7071f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,1.1f],scale:[9f,9f,9f]}}}}
scoreboard players set @e[type=text_display,tag={NS}.new_fx] {NS}.life 26
{new}
""")
    fn("fx/circle_long", f"""
$summon minecraft:text_display ~ ~0.1 ~ {{Tags:["{NS}.fx","{NS}.spin","{NS}.shrink","{NS}.new_fx"],text:{{text:"$(glyph)",color:"$(color)"}},background:0,billboard:"fixed",teleport_duration:2,brightness:{{sky:15,block:15}},Rotation:[0f,0f],transformation:{{left_rotation:[-0.7071f,0f,0f,0.7071f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,2.3f],scale:[18f,18f,18f]}}}}
$scoreboard players set @e[type=text_display,tag={NS}.new_fx] {NS}.life $(life)
{new}
""")
    # Cœur sombre en rotation entouré d'une sphère violette (puits, singularité)
    fn("fx/core", f"""
summon minecraft:block_display ~ ~1 ~ {{Tags:["{NS}.fx","{NS}.spin_fast","{NS}.shrink","{NS}.new_fx"],block_state:{{Name:"minecraft:black_concrete"}},teleport_duration:2,transformation:{{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.6f,-0.6f,-0.6f],scale:[1.2f,1.2f,1.2f]}}}}
summon minecraft:block_display ~ ~1 ~ {{Tags:["{NS}.fx","{NS}.spin","{NS}.shrink","{NS}.new_fx"],block_state:{{Name:"minecraft:purple_stained_glass"}},teleport_duration:2,brightness:{{sky:15,block:15}},transformation:{{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-1.1f,-1.1f,-1.1f],scale:[2.2f,2.2f,2.2f]}}}}
$scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.life $(life)
{new}
""")
    # Sphère d'énergie qui grossit (explosion), disque d'onde de choc, colonne d'énergie.
    # Taille cible en dixièmes dans le score dmgi ; croissance interpolée au tick suivant.
    fn("fx/burst", f"""
$summon minecraft:block_display ~ ~ ~ {{Tags:["{NS}.fx","{NS}.grow","{NS}.spin","{NS}.shrink","{NS}.new_fx"],block_state:{{Name:"minecraft:$(block)"}},teleport_duration:2,brightness:{{sky:15,block:15}},transformation:{{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.05f,-0.05f,-0.05f],scale:[0.1f,0.1f,0.1f]}}}}
$scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.dmgi $(size)
scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.life 9
{new}
""")
    fn("fx/nova", f"""
$summon minecraft:block_display ~ ~0.1 ~ {{Tags:["{NS}.fx","{NS}.grow","{NS}.flat","{NS}.spin","{NS}.shrink","{NS}.new_fx"],block_state:{{Name:"minecraft:$(block)"}},teleport_duration:2,brightness:{{sky:15,block:15}},transformation:{{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.1f,0f,-0.1f],scale:[0.2f,0.08f,0.2f]}}}}
$scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.dmgi $(size)
scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.life 10
{new}
""")
    fn("fx/pillar", f"""
$summon minecraft:block_display ~ ~ ~ {{Tags:["{NS}.fx","{NS}.grow","{NS}.tall","{NS}.spin","{NS}.shrink","{NS}.new_fx"],block_state:{{Name:"minecraft:$(block)"}},teleport_duration:2,brightness:{{sky:15,block:15}},transformation:{{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[-0.6f,0f,-0.6f],scale:[1.2f,0.1f,1.2f]}}}}
$scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.dmgi $(h)
scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.life 22
{new}
""")
    fn("fx/grow_apply", f"""
tag @s remove {NS}.grow
execute store result storage {NS}:tmp s float 0.1 run scoreboard players get @s {NS}.dmgi
scoreboard players operation #h {NS}.tmp = @s {NS}.dmgi
scoreboard players operation #h {NS}.tmp *= #5 {NS}.tmp
execute store result storage {NS}:tmp h float -0.01 run scoreboard players get #h {NS}.tmp
function {NS}:fx/grow_macro with storage {NS}:tmp
""")
    # Croissance centrée : translation = -taille/2 (h) ; les colonnes et pics gardent leur base au sol
    fn("fx/grow_macro", f"""
$execute if entity @s[tag=!{NS}.flat,tag=!{NS}.tall] run data merge entity @s {{start_interpolation:0,interpolation_duration:6,transformation:{{scale:[$(s)f,$(s)f,$(s)f],translation:[$(h)f,$(h)f,$(h)f]}}}}
$execute if entity @s[tag={NS}.flat] run data merge entity @s {{start_interpolation:0,interpolation_duration:7,transformation:{{scale:[$(s)f,0.08f,$(s)f],translation:[$(h)f,0f,$(h)f]}}}}
$execute if entity @s[tag={NS}.tall,tag=!{NS}.thin] run data merge entity @s {{start_interpolation:0,interpolation_duration:5,transformation:{{scale:[1.2f,$(s)f,1.2f],translation:[-0.6f,0f,-0.6f]}}}}
$execute if entity @s[tag={NS}.thin] run data merge entity @s {{start_interpolation:0,interpolation_duration:5,transformation:{{scale:[0.55f,$(s)f,0.55f],translation:[-0.27f,0f,-0.27f]}}}}
""")
    # Pics de glace qui sortent du sol
    spikes = []
    for x, z, h in [(0, 0, 32), (1.4, 0.6, 22), (-1.2, 1.0, 26), (0.6, -1.5, 20), (-1.5, -0.8, 18), (2.0, -0.6, 15), (-0.4, 2.0, 17)]:
        spikes.append(f'summon minecraft:block_display ~{x} ~ ~{z} {{Tags:["{NS}.fx","{NS}.grow","{NS}.tall","{NS}.thin","{NS}.shrink","{NS}.new_fx"],'
                      f'block_state:{{Name:"minecraft:packed_ice"}},transformation:{{left_rotation:[0f,0f,0f,1f],'
                      f'right_rotation:[0f,0f,0f,1f],translation:[-0.27f,0f,-0.27f],scale:[0.55f,0.1f,0.55f]}}}}')
        spikes.append(f"scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.dmgi {h}")
        spikes.append(f"scoreboard players set @e[type=block_display,tag={NS}.new_fx] {NS}.life 70")
        spikes.append(new)
    fn("fx/spikes", spikes)


# ============================================================================ 7. barre d'action et menu
def build_hud():
    lines = []
    for school, idx, sid, name, lvl, mana, cd, base, kind, desc in SPELLS:
        sc = SCHOOL[school]
        head = [T("✦ Niv. ", "gold"), {"score": {"name": "@s", "objective": f"{NS}.level"}, "color": "yellow"},
                T("  XP ", "dark_gray"), {"score": {"name": "@s", "objective": f"{NS}.xp"}, "color": "gray"}, T("/", "dark_gray"),
                {"score": {"name": "@s", "objective": f"{NS}.xpnext"}, "color": "gray"},
                T("  ❖ ", "aqua"), {"score": {"name": "@s", "objective": f"{NS}.mana"}, "color": "aqua"}, T("/", "dark_aqua"),
                {"score": {"name": "@s", "objective": f"{NS}.maxmana"}, "color": "dark_aqua"}, T("   ")]
        cond = f"execute if score @s {NS}.school matches {sc[1]} if score @s {NS}.spell matches {idx}"
        locked = head + [T(f"🔒 {name} (niv. {lvl})", "dark_gray")]
        cooling = head + [T(f"⚔ {name} ", sc[3]), T("⌛", "red"), {"score": {"name": "@s", "objective": f"{NS}.cds"}, "color": "red"}, T("s", "red")]
        ready = head + [T(f"⚔ {name} ", sc[3], bold=True), T("✔", "green")]
        if lvl > 1:
            lines.append(f"{cond} if score @s {NS}.level matches ..{lvl - 1} run return run title @s actionbar {snbt(locked)}")
        lines.append(f"{cond} if score @s {NS}.cd matches 1.. run return run title @s actionbar {snbt(cooling)}")
        lines.append(f"{cond} run return run title @s actionbar {snbt(ready)}")
    fn("hud/show", [f"scoreboard players operation @s {NS}.cds = @s {NS}.cd", f"scoreboard players add @s {NS}.cds 19",
                    f"scoreboard players operation @s {NS}.cds /= #20 {NS}.tmp"] + lines)

    # Menu (/trigger oubliees.menu) : 1 = fiche, 2 = guide, 10..13 = choisir un sort de l'école tenue
    fn("menu/dispatch", f"""
execute if score @s {NS}.menu matches 1 run function {NS}:menu/sheet
execute if score @s {NS}.menu matches 2 run function {NS}:guide/give
execute if score @s {NS}.menu matches 10..13 run function {NS}:menu/select
scoreboard players set @s {NS}.menu 0
""")
    fn("menu/select", f"""
scoreboard players operation @s {NS}.spell = @s {NS}.menu
scoreboard players remove @s {NS}.spell 10
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 1.4
function {NS}:hud/show
""")
    sc = lambda o, c="yellow": {"score": {"name": "@s", "objective": f"{NS}.{o}"}, "color": c}  # noqa: E731
    sheet = [f'tellraw @s {snbt(T("══════ Fiche de personnage ══════", "gold", bold=True))}',
             f'tellraw @s [{snbt(T(" Niveau ", "gray"))},{snbt(sc("level", "gold"))},{snbt(T("  —  expérience ", "gray"))},'
             f'{snbt(sc("xp"))},{snbt(T(" / ", "gray"))},{snbt(sc("xpnext"))}]',
             f'tellraw @s [{snbt(T(" Mana ", "gray"))},{snbt(sc("mana", "aqua"))},{snbt(T(" / ", "gray"))},{snbt(sc("maxmana", "aqua"))},'
             f'{snbt(T("   régénération : 2 + niveau ÷ 10 par seconde", "dark_gray"))}]',
             f'tellraw @s {snbt(T(" Puissance des sorts : +3 % par niveau. Vie : +1 cœur tous les 5 niveaux.", "dark_gray"))}',
             f'tellraw @s {snbt(T(" Sorts (cliquez pour choisir, bâton de l'école en main) :", "gray"))}']
    for school, code, sname, color, *_ in SCHOOLS:
        parts = [T(f"  {sname} ", color, bold=True)]
        for s in [x for x in SPELLS if x[0] == school]:
            parts.append(T(f"[{s[3]} · {s[4]}]", color, click_event={"action": "run_command", "command": f"/trigger {NS}.menu set {10 + s[1]}"},
                           hover_event={"action": "show_text", "value": T(f"{s[9]}\nNiveau {s[4]} · mana {s[5]} · recharge {s[6] / 20:g} s", "white")}))
            parts.append(T(" "))
        sheet.append(f"tellraw @s {snbt(parts)}")
    sheet.append(f'tellraw @s [{snbt(T(" [Guide]", "aqua", click_event={"action": "run_command", "command": f"/trigger {NS}.menu set 2"}))},'
                 f'{snbt(T("  Clés : faites-les avec un œil de l'Ender entouré du matériau de la dimension.", "dark_gray"))}]')
    fn("menu/sheet", sheet)


# ============================================================================ 8. voyages
def build_travel():
    for d in WM.DIMENSIONS:
        wjson(os.path.join(DATA, "advancement", "key", f"{d}.json"), {
            "criteria": {"use": {"trigger": "minecraft:consume_item", "conditions": {"item": {
                "items": "minecraft:echo_shard", "predicates": {"minecraft:custom_data": {NS: {"key": d}}}}}}},
            "rewards": {"function": f"{NS}:travel/key_{d}"}})
        dim_id = DIM_KEY_ID[d]
        fn(f"travel/key_{d}", f"""
advancement revoke @s only {NS}:key/{d}
give @s echo_shard[{comp_snbt(key_components(d))}]
execute if dimension {dim_id} run return run function {NS}:travel/return
function {NS}:travel/save
execute in {dim_id} run function {NS}:travel/arrive_{d}
""")
        under = "under 110 " if d in CEILING else ""
        fn(f"travel/arrive_{d}", f"""
scoreboard players set #ok {NS}.tmp 0
execute positioned as @s store success score #ok {NS}.tmp run spreadplayers ~ ~ 0 200 {under}false @s
execute if score #ok {NS}.tmp matches 0 run function {NS}:travel/platform {{block:"{ARRIVAL_STONE[d]}",y:{PLATFORM_Y[d]}}}
execute at @s run function {NS}:travel/welcome {{name:"{DIM_FR[d]}",color:"{DIM_COLOR[d]}"}}
advancement grant @s only {NS}:dim_{d}
""")
    fn("travel/save", f"""
execute store result score @s {NS}.rx run data get entity @s Pos[0]
execute store result score @s {NS}.ry run data get entity @s Pos[1]
execute store result score @s {NS}.rz run data get entity @s Pos[2]
{chr(10).join(f'execute if dimension {DIM_KEY_ID[d]} run scoreboard players set @s {NS}.rdim {DIM_CODE[d]}' for d in DIM_CODE)}
""")
    ret = [f"execute store result storage {NS}:tmp x int 1 run scoreboard players get @s {NS}.rx",
           f"execute store result storage {NS}:tmp y int 1 run scoreboard players get @s {NS}.ry",
           f"execute store result storage {NS}:tmp z int 1 run scoreboard players get @s {NS}.rz",
           f'data modify storage {NS}:tmp dim set value "minecraft:overworld"']
    ret += [f'execute if score @s {NS}.rdim matches {code} run data modify storage {NS}:tmp dim set value "{DIM_KEY_ID[d]}"'
            for d, code in DIM_CODE.items()]
    ret += [f"execute unless score @s {NS}.rdim matches 0.. run return run function {NS}:travel/home",
            f"function {NS}:travel/return_macro with storage {NS}:tmp",
            f'execute at @s run function {NS}:travel/welcome {{name:"Retour",color:"gold"}}']
    fn("travel/return", ret)
    fn("travel/return_macro", "$execute in $(dim) run tp @s $(x) $(y) $(z)")
    fn("travel/home", f"""
execute in minecraft:overworld run tp @s 0 200 0
effect give @s minecraft:slow_falling 20 0
""")
    fn("travel/platform", f"""
$execute positioned ~ $(y) ~ run fill ~-3 ~ ~-3 ~3 ~3 ~3 minecraft:air
$execute positioned ~ $(y) ~ run fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:$(block)
$tp @s ~ $(y) ~
effect give @s minecraft:slow_falling 10 0
""")
    fn("travel/welcome", f"""
effect give @s minecraft:resistance 6 4 true
particle minecraft:reverse_portal ~ ~1 ~ 0.5 1 0.5 0.4 120
playsound minecraft:block.end_portal.spawn player @s ~ ~ ~ 0.6 1.4
function {NS}:fx/circle {{color:"#ffd84a",glyph:"۞"}}
title @s times 10 50 20
title @s subtitle {snbt(T("Les Dimensions Oubliées", "gray"))}
$title @s title {{text:"$(name)",color:"$(color)",bold:true}}
""")


# ============================================================================ 9. créatures à niveau, barres de vie, chiffres de dégâts
def build_mobs():
    wjson(os.path.join(DATA, "tags", "entity_type", "leveled.json"), {"values": [f"minecraft:{t}" for t in LEVELED]})
    wjson(os.path.join(DATA, "tags", "entity_type", "hostile.json"), {"values": [
        f"minecraft:{t}" for t in ["zombie", "husk", "drowned", "zombie_villager", "skeleton", "stray", "bogged", "wither_skeleton",
                                   "spider", "cave_spider", "creeper", "enderman", "witch", "slime", "magma_cube", "blaze",
                                   "ghast", "phantom", "pillager", "vindicator", "evoker", "ravager", "piglin_brute", "hoglin",
                                   "zoglin", "guardian", "elder_guardian", "shulker", "silverfish", "endermite", "vex", "breeze",
                                   "creaking", "warden"]]})
    wjson(os.path.join(DATA, "tags", "entity_type", "non_target.json"), {"values": [
        f"minecraft:{t}" for t in ["item", "experience_orb", "arrow", "spectral_arrow", "trident", "marker",
                                   "block_display", "item_display", "text_display", "interaction", "armor_stand",
                                   "area_effect_cloud", "item_frame", "glow_item_frame", "painting", "falling_block", "lightning_bolt",
                                   "snowball", "egg", "ender_pearl", "splash_potion", "lingering_potion", "fireball", "small_fireball", "wither_skull",
                                   "firework_rocket", "fishing_bobber", "leash_knot", "tnt", "end_crystal", "evoker_fangs",
                                   "eye_of_ender", "villager", "wandering_trader", "allay", "iron_golem", "snow_golem"]]})
    init = [f"tag @s add {NS}.lv"]
    for d, (lv, mult) in DIM_LEVEL.items():
        init.append(f"execute if dimension {DIM_KEY_ID[d]} store result storage {NS}:tmp lvl int 1 run random value {lv}..{lv + 3}")
        if mult > 1.0:
            init.append(f"execute if dimension {DIM_KEY_ID[d]} run attribute @s minecraft:max_health modifier add {NS}:dimension {mult - 1:.2f} add_multiplied_base")
            init.append(f"execute if dimension {DIM_KEY_ID[d]} run attribute @s minecraft:attack_damage modifier add {NS}:dimension {(mult - 1) * 0.8:.2f} add_multiplied_base")
    init += [f"execute store result entity @s Health float 1 run attribute @s minecraft:max_health get",
             f"execute store result score @s {NS}.hpl run data get entity @s Health 10",
             f"execute store result score @s {NS}.hpmax run attribute @s minecraft:max_health get 10"]
    for t in LEVELED:
        init.append(f'execute if entity @s[type=minecraft:{t}] run data modify storage {NS}:tmp type set value "{t}"')
    init += [f"execute if entity @s[tag={NS}.summon] run return 0",
             f"execute if data entity @s CustomName run return 0",
             f"function {NS}:mob/name with storage {NS}:tmp"]
    fn("mob/init", init)
    fn("mob/name", f"""
$data modify entity @s CustomName set value {{text:"",extra:[{{text:"Niv. $(lvl) ",color:"gold"}},{{translate:"entity.minecraft.$(type)",color:"white"}},{{text:" ",color:"green"}},{{text:"",color:"dark_gray"}}]}}
tag @s add {NS}.named
""")
    fn("mob/track", f"""
execute store result score @s {NS}.hp run data get entity @s Health 10
scoreboard players operation #d {NS}.tmp = @s {NS}.hpl
scoreboard players operation #d {NS}.tmp -= @s {NS}.hp
execute if score #d {NS}.tmp matches 5.. run function {NS}:mob/damaged
execute if score #d {NS}.tmp matches ..-10 run function {NS}:mob/healed
scoreboard players operation @s {NS}.hpl = @s {NS}.hp
""")
    fn("mob/damaged", f"""
# chiffre de dégâts (une décimale si besoin), orange et plus gros à partir de 8
scoreboard players operation #a {NS}.tmp = #d {NS}.tmp
scoreboard players operation #a {NS}.tmp /= #10 {NS}.tmp
scoreboard players operation #b {NS}.tmp = #d {NS}.tmp
scoreboard players operation #b {NS}.tmp %= #10 {NS}.tmp
execute store result storage {NS}:tmp a int 1 run scoreboard players get #a {NS}.tmp
execute store result storage {NS}:tmp b int 1 run scoreboard players get #b {NS}.tmp
function {NS}:mob/number_text with storage {NS}:tmp
execute if score #b {NS}.tmp matches 0 run function {NS}:mob/number_int with storage {NS}:tmp
data modify storage {NS}:tmp color set value "#fff4d0"
data modify storage {NS}:tmp sc set value 1.6
execute if score #d {NS}.tmp matches 80.. run data modify storage {NS}:tmp color set value "#ff8a2a"
execute if score #d {NS}.tmp matches 80.. run data modify storage {NS}:tmp sc set value 2.3
scoreboard players add #ni {NS}.tmp 1
scoreboard players operation #nm {NS}.tmp = #ni {NS}.tmp
scoreboard players operation #nm {NS}.tmp %= #5 {NS}.tmp
data modify storage {NS}:tmp ox set value 0.0
data modify storage {NS}:tmp oz set value 0.0
execute if score #nm {NS}.tmp matches 1 run data modify storage {NS}:tmp ox set value 0.45
execute if score #nm {NS}.tmp matches 2 run data modify storage {NS}:tmp ox set value -0.45
execute if score #nm {NS}.tmp matches 3 run data modify storage {NS}:tmp oz set value 0.45
execute if score #nm {NS}.tmp matches 4 run data modify storage {NS}:tmp oz set value -0.45
function {NS}:mob/number with storage {NS}:tmp
# barre de vie dans le nom (10 cases)
execute if entity @s[tag={NS}.named] run function {NS}:mob/bar
""")
    fn("mob/healed", f"""
execute if entity @s[tag={NS}.named] run function {NS}:mob/bar
""")
    fn("mob/number_text", f"""
$data modify storage {NS}:tmp t set value "-$(a).$(b)"
""")
    fn("mob/number_int", f"""
$data modify storage {NS}:tmp t set value "-$(a)"
""")
    fn("mob/number", f"""
$summon minecraft:text_display ~$(ox) ~1.9 ~$(oz) {{Tags:["{NS}.fx","{NS}.rise","{NS}.shrink","{NS}.new_fx"],text:{{text:"$(t)",color:"$(color)",bold:true}},background:0,shadow:true,billboard:"center",teleport_duration:1,brightness:{{sky:15,block:15}},transformation:{{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[$(sc)f,$(sc)f,$(sc)f]}}}}
scoreboard players set @e[type=text_display,tag={NS}.new_fx] {NS}.life 22
tag @e[tag={NS}.new_fx] remove {NS}.new_fx
""")
    fn("mob/bar", f"""
scoreboard players operation #n {NS}.tmp = @s {NS}.hp
scoreboard players operation #n {NS}.tmp *= #10 {NS}.tmp
scoreboard players operation #n {NS}.tmp += @s {NS}.hpmax
scoreboard players remove #n {NS}.tmp 1
scoreboard players operation #n {NS}.tmp /= @s {NS}.hpmax
execute if score #n {NS}.tmp matches 11.. run scoreboard players set #n {NS}.tmp 10
execute if score #n {NS}.tmp matches ..-1 run scoreboard players set #n {NS}.tmp 0
execute store result storage {NS}:tmp n int 1 run scoreboard players get #n {NS}.tmp
data modify storage {NS}:tmp bar set value "green"
execute if score #n {NS}.tmp matches ..5 run data modify storage {NS}:tmp bar set value "yellow"
execute if score #n {NS}.tmp matches ..2 run data modify storage {NS}:tmp bar set value "red"
function {NS}:mob/bar_macro with storage {NS}:tmp
data modify entity @s CustomNameVisible set value 1b
""")
    fn("mob/bar_macro", f"""
$data modify entity @s CustomName.extra[2].text set from storage {NS}:ui full[$(n)]
$data modify entity @s CustomName.extra[2].color set value "$(bar)"
$data modify entity @s CustomName.extra[3].text set from storage {NS}:ui empty[$(n)]
""")


# ============================================================================ 10. armures (bonus d'ensemble)
def build_armor():
    lines = []
    for aid, adj, dim, material, mat, trim, effects, bonus in ARMORS:
        cond = "execute " + " ".join(
            f'if items entity @s armor.{slot} *[minecraft:custom_data~{{{NS}:{{set:"{aid}"}}}}]' for _p, _n, slot in PIECES)
        for eff, amp in effects:
            lines.append(f"{cond} run effect give @s minecraft:{eff} 3 {amp} true")
        lines.append(f"{cond} run advancement grant @s only {NS}:full_set")
    fn("armor/check", lines)


# ============================================================================ 11. progrès
def build_advancements():
    def adv(name, parent, icon, title, desc, criteria, frame="task", hidden=False, bg=None):
        disp = {"icon": {"id": icon}, "title": T(title, None), "description": T(desc, "gray"), "frame": frame,
                "show_toast": True, "announce_to_chat": True, "hidden": hidden}
        if bg:
            disp["background"] = bg
            disp["show_toast"] = False
            disp["announce_to_chat"] = False
        obj = {"display": disp, "criteria": criteria}
        if parent:
            obj["parent"] = f"{NS}:{parent}"
        wjson(os.path.join(DATA, "advancement", name + ".json"), obj)

    imp = {"granted": {"trigger": "minecraft:impossible"}}
    adv("root", None, "minecraft:ender_eye", "Les Dimensions Oubliées", "Sept mondes perdus vous attendent.", imp,
        bg="minecraft:gui/advancements/backgrounds/end")
    adv("first_spell", "root", "minecraft:blaze_rod", "Premier sort", "Lancer un sort avec un bâton magique.", imp)
    adv("summon", "first_spell", "minecraft:bone", "Invocateur", "Invoquer une créature alliée.", imp)
    adv("level_10", "first_spell", "minecraft:experience_bottle", "Apprenti", "Atteindre le niveau 10.", imp)
    adv("level_25", "level_10", "minecraft:enchanted_book", "Mage confirmé", "Atteindre le niveau 25.", imp, frame="goal")
    adv("level_50", "level_25", "minecraft:nether_star", "Archimage", "Atteindre le niveau 50.", imp, frame="challenge")
    adv("full_set", "root", "minecraft:diamond_chestplate", "Équipement légendaire", "Porter une armure complète des dimensions.", imp, frame="goal")
    parent = "root"
    icons = {"celestial": "minecraft:amethyst_shard", "corrupted": "minecraft:crimson_fungus", "ocean": "minecraft:prismarine_crystals",
             "chaos": "minecraft:blaze_powder", "eternal": "minecraft:blue_ice", "depths": "minecraft:echo_shard", "void": "minecraft:dragon_egg"}
    for d in WM.DIMENSIONS:
        adv(f"dim_{d}", parent, icons[d], DIM_FR[d], f"Voyager vers {DIM_FR[d]}.",
            {"enter": {"trigger": "minecraft:changed_dimension", "conditions": {"to": DIM_KEY_ID[d]}}},
            frame="challenge" if d == "void" else "goal")
        parent = f"dim_{d}"


# ============================================================================ 12. guide (livre)
def build_guide():
    def page(*parts):
        return list(parts)

    pages = [
        page(T("Les Dimensions\nOubliées\n\n", "dark_purple", bold=True), T("Grimoire des Anciens\n\n", "gold"),
             T("Sept mondes perdus, une magie oubliée et votre propre légende.\n\nTournez la page.", "black")),
        page(T("La magie\n\n", "dark_purple", bold=True),
             T("Fabriquez un bâton : deux bâtons en diagonale et le matériau de l'école au sommet.\n\n", "black"),
             T("Clic droit", "dark_blue", bold=True), T(" : lancer le sort.\n", "black"),
             T("Accroupi + clic droit", "dark_blue", bold=True), T(" : sort suivant.\n\n", "black"),
             T("Le sort, le mana et le niveau s'affichent au-dessus de la barre d'objets.", "dark_gray")),
        page(T("Les écoles\n\n", "dark_purple", bold=True), T("Feu", "gold", bold=True), T(" : poudre de blaze\n", "black"),
             T("Glace", "aqua", bold=True), T(" : glace compactée\n", "black"), T("Foudre", "yellow", bold=True), T(" : lingot de cuivre\n", "black"),
             T("Nature", "dark_green", bold=True), T(" : émeraude\n", "black"), T("Néant", "dark_purple", bold=True), T(" : perle de l'Ender\n\n", "black"),
             T("Quatre sorts par école, débloqués avec les niveaux.", "dark_gray")),
        page(T("Les voyages\n\n", "dark_purple", bold=True),
             T("Une clé = un œil de l'Ender entouré de 4 matériaux de la dimension :\n", "black"),
             T("Céleste : améthyste\nCorrompue : champignon cramoisi\nOcéan : cristaux de prismarine\nChaos : poudre de blaze\nÉternel : glace bleue\nProfondeurs : éclat d'écho\n", "dark_blue"),
             T("Néant : étoile du Nether, souffle de dragon, éclat d'écho et œil de l'Ender.", "dark_purple")),
        page(T("Voyager\n\n", "dark_purple", bold=True),
             T("Maintenez clic droit avec une clé. Vous arrivez dans la dimension, protégé quelques secondes.\n\n", "black"),
             T("Pour rentrer, utilisez la même clé dans cette dimension : elle vous ramène là où vous étiez.", "black")),
        page(T("Le héros\n\n", "dark_purple", bold=True),
             T("Chaque créature vaincue donne de l'expérience. Chaque niveau augmente votre mana et la puissance des sorts ; tous les 5 niveaux, un cœur de plus.\n\n", "black"),
             T("Fiche de personnage :\n", "dark_blue"), T("/trigger oubliees.menu", "dark_red")),
        page(T("Les créatures\n\n", "dark_purple", bold=True),
             T("Les créatures ont un niveau qui dépend du monde : Céleste vers 6, Néant vers 82. Plus le niveau est haut, plus elles sont robustes.\n\n", "black"),
             T("Une fois touchées, leur barre de vie apparaît au-dessus d'elles et les dégâts s'affichent.", "dark_gray")),
        page(T("L'armurerie\n\n", "dark_purple", bold=True),
             T("Entourez une arme ou une pièce d'armure en diamant (ou netherite) de 4 matériaux d'une dimension pour obtenir sa version légendaire.\n\n", "black"),
             T("Une armure complète donne un bonus permanent.", "dark_gray")),
    ]
    comps = {"written_book_content": {"title": "Grimoire des Anciens", "author": "Les Architectes", "pages": pages}}
    fn("guide/give", f"give @s written_book[written_book_content={snbt(comps['written_book_content'])}]")


# ============================================================================ main
def main():
    build_pack()
    build_dimensions()
    build_items()
    build_core()
    build_magic()
    build_blink()
    build_fx()
    build_hud()
    build_travel()
    build_mobs()
    build_armor()
    build_advancements()
    build_guide()
    fn("debug/give_all", [f"give @s carrot_on_a_stick[{comp_snbt(staff_components(s[0]))}]" for s in SCHOOLS] +
       [f"give @s echo_shard[{comp_snbt(key_components(d))}]" for d in WM.DIMENSIONS] +
       [f"scoreboard players set @s {NS}.mana 9999", f"scoreboard players set @s {NS}.maxmana 9999"])
    for sc in SCHOOLS:
        fn(f"debug/hold_{sc[0]}", f"item replace entity @s weapon.mainhand with carrot_on_a_stick[{comp_snbt(staff_components(sc[0]))}]")
    shutil.copy(os.path.join(ROOT, "datapack", "LISEZMOI.md"), os.path.join(OUT, "LISEZMOI.md"))
    # Archive prête à installer
    os.makedirs(os.path.join(ROOT, "build", "datapack"), exist_ok=True)
    archive = os.path.join(ROOT, "build", "datapack", "LesDimensionsOubliees")
    shutil.make_archive(archive, "zip", OUT)
    print(f"Data pack : {FILES} fichiers, {len(SPELLS)} sorts, {len(WEAPONS)} armes, {len(ARMORS) * 4} pièces d'armure")
    print(f"Archive : {archive}.zip")


if __name__ == "__main__":
    main()
