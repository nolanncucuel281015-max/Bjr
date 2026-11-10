"""Correspondance des blocs et créatures du mod vers Minecraft vanilla (édition data pack).

Le data pack reprend la génération des sept dimensions du mod (fichiers JSON générés par
tools/gen) en remplaçant chaque bloc et chaque créature du mod par un équivalent vanilla.
"""

DIMENSIONS = ["celestial", "corrupted", "ocean", "chaos", "eternal", "depths", "void"]

# Pierres et sols de chaque dimension
BLOCKS = {
    # Royaume Céleste : calcite, quartz, mousse claire
    "cloudstone": "calcite", "deep_cloudstone": "diorite", "polished_cloudstone": "polished_diorite",
    "cloudstone_bricks": "quartz_bricks", "chiseled_cloudstone": "chiseled_quartz_block", "sky_soil": "rooted_dirt",
    "sky_grass": "moss_block", "astral_moss": "moss_block", "astral_sand": "white_concrete_powder",
    "cloudwood_log": "birch_log", "cloudwood_leaves": "cherry_leaves", "starlight_block": "sea_lantern",
    "celestial_crystal_block": "amethyst_block", "astral_crystal_block": "amethyst_block", "luminite_block": "glowstone",
    # Dimension Corrompue : roche noire, nylium cramoisi
    "blightstone": "blackstone", "deep_blightstone": "deepslate", "blight_soil": "coarse_dirt",
    "blight_grass": "crimson_nylium", "crimsonroot_log": "crimson_stem", "crimsonroot_leaves": "mangrove_leaves",
    "corrupted_floor": "blackstone",
    # Océan Oublié : prismarine
    "abyssal_stone": "prismarine", "deep_abyssal_stone": "dark_prismarine", "ocean_floor": "prismarine",
    # Terres du Chaos : netherrack, basalte, magma
    "cinderstone": "netherrack", "deep_cinderstone": "blackstone", "cinderstone_bricks": "nether_bricks",
    "polished_cinderstone": "polished_blackstone", "chaos_floor": "basalt", "chaos_roof": "bedrock",
    "ember_block": "magma_block",
    # Monde Éternel : glace tassée, neige
    "frostrock": "packed_ice", "deep_frostrock": "stone", "frost_soil": "snow_block", "frost_grass": "snow_block",
    "frostrock_tiles": "packed_ice", "polished_frostrock": "blue_ice", "frostpine_log": "spruce_log",
    "frostpine_leaves": "spruce_leaves", "eternal_floor": "packed_ice", "everfrost_gem_block": "blue_ice",
    "rime_sapphire_block": "diamond_block",
    # Les Profondeurs : ardoise, boue, champignons lumineux
    "umbral_rock": "deepslate", "deep_umbral_rock": "tuff", "umbral_soil": "mud", "umbral_moss": "sculk",
    "polished_umbral_rock": "polished_deepslate", "umbral_rock_bricks": "deepslate_bricks", "depths_floor": "deepslate",
    "depths_roof": "bedrock", "glowshroom_block": "shroomlight",
    # Le Néant : pierre de l'End, purpur, obsidienne
    "voidstone": "end_stone", "deep_voidstone": "obsidian", "voidstone_bricks": "end_stone_bricks",
    "polished_voidstone": "purpur_block", "void_shard_block": "crying_obsidian", "nebula_block": "purpur_pillar",
    "cosmic_opal_block": "amethyst_block",
    # Architecture des Anciens
    "architect_bricks": "stone_bricks", "arcane_stone": "chiseled_stone_bricks", "enchanted_obsidian": "crying_obsidian",
    "mana_crystal_block": "amethyst_block",
}

# Minerais : chaque minerai du mod devient un minerai vanilla de valeur comparable
ORES = {
    "astral_silver": "iron_ore", "celestial_crystal": "diamond_ore", "windstone": "emerald_ore", "luminite": "glowstone",
    "corruptium": "redstone_ore", "blood_ruby": "redstone_ore", "blight_amber": "gold_ore",
    "tidal_cobalt": "lapis_ore", "sea_jade": "emerald_ore", "abyssal_pearl": "sea_lantern",
    "magmatite": "nether_gold_ore", "ember_garnet": "redstone_ore", "brimstone": "nether_quartz_ore",
    "frost_steel": "iron_ore", "rime_sapphire": "diamond_ore", "everfrost_gem": "lapis_ore",
    "mithril": "diamond_ore", "deep_mithril": "diamond_ore", "shadow_quartz": "redstone_ore",
    "void_shard": "ancient_debris", "nebulite": "amethyst_block", "cosmic_opal": "emerald_ore",
    "ancient_fragment": "gilded_blackstone",
}
for _d in DIMENSIONS:
    ORES[_d + "_fragment"] = "gilded_blackstone"

DEEP_VARIANT = {"iron_ore": "deepslate_iron_ore", "diamond_ore": "deepslate_diamond_ore", "emerald_ore": "deepslate_emerald_ore",
                "redstone_ore": "deepslate_redstone_ore", "gold_ore": "deepslate_gold_ore", "lapis_ore": "deepslate_lapis_ore"}

KEEP_PROPERTIES = ("_leaves", "_log", "_stem", "basalt", "purpur_pillar")


def block(mod_id):
    """Bloc vanilla pour un bloc du mod (sans espace de noms), ou None si ce n'est pas un bloc connu."""
    if mod_id in BLOCKS:
        return "minecraft:" + BLOCKS[mod_id]
    if mod_id.endswith("_ore"):
        base = mod_id[:-4]
        deep = False
        for prefix in ("deepslate_", "deep_"):
            if base.startswith(prefix) and base[len(prefix):] in ORES:
                base, deep = base[len(prefix):], True
                break
        if base in ORES:
            v = ORES[base]
            return "minecraft:" + (DEEP_VARIANT.get(v, v) if deep else v)
    return None


def creature(cr):
    """Créature vanilla équivalente à une créature du mod (même rôle, même milieu)."""
    a, cat, dim, move = cr["arch"], cr["cat"], cr["dim"], cr["move"]
    if move == "SWIMMING":
        return {"jelly": "glow_squid", "turtle": "turtle", "crab": "tropical_fish", "shark": "guardian",
                "serpent": "elder_guardian" if cat in ("ELITE", "MINIBOSS") else "guardian",
                "fish": "tropical_fish" if cat == "PASSIVE" else "guardian", "biped": "drowned"}.get(a, "drowned")
    if cat in ("PASSIVE", "NEUTRAL"):
        return {"quadruped": {"celestial": "sheep", "eternal": "goat", "corrupted": "pig", "depths": "rabbit"}.get(dim, "sheep"),
                "wisp": "allay", "bird": "parrot", "crab": "armadillo", "turtle": "turtle", "worm": "rabbit",
                "bear": "polar_bear", "plant": "mooshroom", "jelly": "allay", "fish": "allay"}.get(a, "sheep")
    if a == "biped":
        return {"celestial": "stray", "corrupted": "zombie", "ocean": "drowned", "chaos": "wither_skeleton",
                "eternal": "stray", "depths": "skeleton", "void": "enderman"}[dim]
    if a == "biped_horned":
        return "evoker" if cat == "MINIBOSS" else "vindicator"
    if a == "golem":
        return "ravager" if cat in ("ELITE", "MINIBOSS") else "vindicator"
    if a == "wolf":
        return "zoglin"
    if a == "wisp":
        return {"chaos": "blaze", "eternal": "breeze"}.get(dim, "vex")
    if a == "drake":
        return "ghast" if dim == "chaos" else "phantom"
    if a in ("worm",):
        return "silverfish"
    if a == "serpent":
        return "phantom" if move == "FLYING" else "silverfish"
    if a == "spider":
        return "cave_spider" if dim == "depths" else "spider"
    if a == "plant":
        return "creeper"
    if a == "bird":
        return "phantom"
    if a == "crystal":
        return "shulker"
    return "zombie"
