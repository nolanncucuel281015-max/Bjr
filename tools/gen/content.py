"""Source unique de vérité pour le contenu « table-driven » du mod Les Dimensions Oubliées.

Ce module décrit les dimensions, matériaux, blocs, armes, armures et artefacts.
`generate.py` le lit pour produire :
  * src/main/java/.../registry/generated/ContentTables.java
  * les modèles, états de blocs, définitions d'items, textures, traductions,
  * les recettes, loot tables et tags.

Les comportements spéciaux (artefacts, armes légendaires, blocs magiques) sont
codés à la main en Java et référencés par identifiant.
"""

# ---------------------------------------------------------------------------
# Raretés RPG (6 niveaux) — couleur utilisée pour la palette des textures et l'UI
# ---------------------------------------------------------------------------
RARITIES = ["COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"]

# ---------------------------------------------------------------------------
# Dimensions : id, noms, pierre de base, couleurs de pierre, couleur d'accent
# ---------------------------------------------------------------------------
DIMENSIONS = [
    dict(id="celestial", en="Celestial Kingdom", fr="Royaume Céleste",
         stone="cloudstone", stone_en="Cloudstone", stone_fr="Pierre des nuages",
         stone_color=(0xD8, 0xE4, 0xF0), accent=(0x8F, 0xD8, 0xFF), sound="CALCITE"),
    dict(id="corrupted", en="Corrupted Dimension", fr="Dimension Corrompue",
         stone="blightstone", stone_en="Blightstone", stone_fr="Pierre corrompue",
         stone_color=(0x5A, 0x2A, 0x2E), accent=(0xE0, 0x20, 0x30), sound="NETHERRACK"),
    dict(id="ocean", en="Forgotten Ocean", fr="Océan Oublié",
         stone="abyssal_stone", stone_en="Abyssal Stone", stone_fr="Pierre abyssale",
         stone_color=(0x2A, 0x4A, 0x5C), accent=(0x30, 0xE0, 0xD0), sound="STONE"),
    dict(id="chaos", en="Lands of Chaos", fr="Terres du Chaos",
         stone="cinderstone", stone_en="Cinderstone", stone_fr="Pierre de cendre",
         stone_color=(0x3C, 0x30, 0x2C), accent=(0xFF, 0x70, 0x10), sound="BASALT"),
    dict(id="eternal", en="Eternal World", fr="Monde Éternel",
         stone="frostrock", stone_en="Frostrock", stone_fr="Roche de givre",
         stone_color=(0xA8, 0xC8, 0xD8), accent=(0xC0, 0xF0, 0xFF), sound="STONE"),
    dict(id="depths", en="The Depths", fr="Les Profondeurs",
         stone="umbral_rock", stone_en="Umbral Rock", stone_fr="Roche ombrale",
         stone_color=(0x30, 0x2C, 0x3A), accent=(0x9A, 0x60, 0xFF), sound="DEEPSLATE"),
    dict(id="void", en="The Void", fr="Le Néant",
         stone="voidstone", stone_en="Voidstone", stone_fr="Pierre du néant",
         stone_color=(0x1C, 0x14, 0x2C), accent=(0xC8, 0x60, 0xFF), sound="TUFF"),
]
DIM_BY_ID = {d["id"]: d for d in DIMENSIONS}

# ---------------------------------------------------------------------------
# Matériaux (21) : 3 par dimension. kind = ingot (raw + lingot) ou gem.
# level = niveau d'outil requis pour le minerai : 1 pierre, 2 fer, 3 diamant.
# ---------------------------------------------------------------------------
MATERIALS = [
    # Royaume céleste
    dict(id="astral_silver", dim="celestial", kind="ingot", level=2, color=(0xC8, 0xD4, 0xE8),
         en="Astral Silver", fr="Argent astral", fr_de="d'argent astral"),
    dict(id="celestial_crystal", dim="celestial", kind="gem", level=2, color=(0x7F, 0xE0, 0xFF),
         en="Celestial Crystal", fr="Cristal céleste", fr_de="de cristal céleste"),
    dict(id="windstone", dim="celestial", kind="gem", level=1, color=(0xB8, 0xF0, 0xD8),
         en="Windstone", fr="Pierre des vents", fr_de="de pierre des vents"),
    # Dimension corrompue
    dict(id="corruptium", dim="corrupted", kind="ingot", level=2, color=(0xA0, 0x18, 0x30),
         en="Corruptium", fr="Corruptium", fr_de="de corruptium"),
    dict(id="blood_ruby", dim="corrupted", kind="gem", level=2, color=(0xE0, 0x10, 0x28),
         en="Blood Ruby", fr="Rubis de sang", fr_de="de rubis de sang"),
    dict(id="blight_amber", dim="corrupted", kind="gem", level=1, color=(0xD0, 0x60, 0x20),
         en="Blight Amber", fr="Ambre corrompu", fr_de="d'ambre corrompu"),
    # Océan oublié
    dict(id="tidal_cobalt", dim="ocean", kind="ingot", level=2, color=(0x30, 0x60, 0xD0),
         en="Tidal Cobalt", fr="Cobalt des marées", fr_de="de cobalt des marées"),
    dict(id="abyssal_pearl", dim="ocean", kind="gem", level=2, color=(0xE0, 0xF0, 0xF0),
         en="Abyssal Pearl", fr="Perle abyssale", fr_de="de perle abyssale"),
    dict(id="sea_jade", dim="ocean", kind="gem", level=1, color=(0x30, 0xC0, 0x90),
         en="Sea Jade", fr="Jade marin", fr_de="de jade marin"),
    # Terres du chaos
    dict(id="magmatite", dim="chaos", kind="ingot", level=3, color=(0xF0, 0x60, 0x10),
         en="Magmatite", fr="Magmatite", fr_de="de magmatite"),
    dict(id="ember_garnet", dim="chaos", kind="gem", level=2, color=(0xC0, 0x20, 0x10),
         en="Ember Garnet", fr="Grenat de braise", fr_de="de grenat de braise"),
    dict(id="brimstone", dim="chaos", kind="gem", level=1, color=(0xF0, 0xD0, 0x30),
         en="Brimstone", fr="Soufre infernal", fr_de="de soufre infernal"),
    # Monde éternel
    dict(id="frost_steel", dim="eternal", kind="ingot", level=3, color=(0x90, 0xC8, 0xE8),
         en="Frost Steel", fr="Acier de givre", fr_de="d'acier de givre"),
    dict(id="everfrost_gem", dim="eternal", kind="gem", level=2, color=(0xC8, 0xF8, 0xFF),
         en="Everfrost Gem", fr="Glace éternelle", fr_de="de glace éternelle"),
    dict(id="rime_sapphire", dim="eternal", kind="gem", level=2, color=(0x40, 0x80, 0xF0),
         en="Rime Sapphire", fr="Saphir de givre", fr_de="de saphir de givre"),
    # Les profondeurs
    dict(id="deep_mithril", dim="depths", kind="ingot", level=3, color=(0x90, 0xA0, 0xC0),
         en="Deep Mithril", fr="Mithril des profondeurs", fr_de="de mithril des profondeurs"),
    dict(id="shadow_quartz", dim="depths", kind="gem", level=2, color=(0x50, 0x40, 0x70),
         en="Shadow Quartz", fr="Quartz d'ombre", fr_de="de quartz d'ombre"),
    dict(id="luminite", dim="depths", kind="gem", level=2, color=(0xA0, 0xFF, 0x80),
         en="Luminite", fr="Luminite", fr_de="de luminite"),
    # Le néant
    dict(id="nebulite", dim="void", kind="ingot", level=3, color=(0x80, 0x40, 0xC0),
         en="Nebulite", fr="Nébulite", fr_de="de nébulite"),
    dict(id="void_shard", dim="void", kind="gem", level=3, color=(0x40, 0x10, 0x60),
         en="Void Shard", fr="Éclat du néant", fr_de="d'éclat du néant"),
    dict(id="cosmic_opal", dim="void", kind="gem", level=3, color=(0xF0, 0xB0, 0xF0),
         en="Cosmic Opal", fr="Opale cosmique", fr_de="d'opale cosmique"),
]
MAT_BY_ID = {m["id"]: m for m in MATERIALS}


def mat_item(mid):
    """Identifiant de l'objet « matière » (lingot ou gemme) d'un matériau."""
    m = MAT_BY_ID[mid]
    return f"{mid}_ingot" if m["kind"] == "ingot" else mid


# ---------------------------------------------------------------------------
# Objets simples (matériaux de quête, fragments, essences)
# ---------------------------------------------------------------------------
EXTRA_ITEMS = [
    dict(id="ancient_fragment", rarity="UNCOMMON", color=(0x40, 0xC0, 0xB0), shape="shard",
         en="Ancient Fragment", fr="Fragment ancien",
         desc_en="A shard of the Ancient Architects' lost technology.",
         desc_fr="Un éclat de la technologie perdue des Anciens Architectes."),
    dict(id="primordial_alloy", rarity="MYTHIC", color=(0xF0, 0xE0, 0x90), shape="ingot",
         en="Primordial Alloy", fr="Alliage primordial",
         desc_en="Forged from the essences of the seven worlds.",
         desc_fr="Forgé à partir des essences des sept mondes."),
    dict(id="architect_core", rarity="EPIC", color=(0x40, 0xE0, 0xD0), shape="orb",
         en="Architect Core", fr="Noyau d'Architecte",
         desc_en="A humming core that still remembers the portals.",
         desc_fr="Un noyau vibrant qui se souvient encore des portails."),
]
for d in DIMENSIONS:
    EXTRA_ITEMS.append(dict(
        id=f"{d['id']}_fragment", rarity="RARE", color=d["accent"], shape="shard",
        en=f"{d['en']} Fragment", fr="",
        desc_en="One of the scattered fragments of the Great Collapse.",
        desc_fr="L'un des fragments dispersés par le Grand Effondrement."))
    EXTRA_ITEMS.append(dict(
        id=f"{d['id']}_essence", rarity="RARE", color=d["accent"], shape="essence",
        en=f"{d['en']} Essence", fr="",
        desc_en="The condensed power of a forgotten dimension.",
        desc_fr="La puissance condensée d'une dimension oubliée."))

# Correction lisible des noms FR des fragments (évite les constructions bancales)
_FR_FRAGMENT = {
    "celestial": "Fragment céleste", "corrupted": "Fragment corrompu", "ocean": "Fragment océanique",
    "chaos": "Fragment du chaos", "eternal": "Fragment éternel", "depths": "Fragment des profondeurs",
    "void": "Fragment du néant",
}
_FR_ESSENCE = {
    "celestial": "Essence céleste", "corrupted": "Essence corrompue", "ocean": "Essence océanique",
    "chaos": "Essence du chaos", "eternal": "Essence éternelle", "depths": "Essence des profondeurs",
    "void": "Essence du néant",
}
for it in EXTRA_ITEMS:
    for k, v in _FR_FRAGMENT.items():
        if it["id"] == f"{k}_fragment":
            it["fr"] = v
    for k, v in _FR_ESSENCE.items():
        if it["id"] == f"{k}_essence":
            it["fr"] = v

# ---------------------------------------------------------------------------
# Paliers d'armes (10) -> raretés et statistiques
# ---------------------------------------------------------------------------
# durability, speed, damage bonus, enchantability, incorrect-for (niveau), école de magie
TIERS = [
    dict(id="astral_silver", rarity="COMMON", dur=320, speed=6.5, dmg=2.0, ench=14, level="IRON", school="LIGHTNING"),
    dict(id="celestial_crystal", rarity="UNCOMMON", dur=420, speed=7.0, dmg=2.5, ench=18, level="IRON", school="LIGHTNING"),
    dict(id="corruptium", rarity="UNCOMMON", dur=520, speed=7.0, dmg=3.0, ench=12, level="IRON", school="NATURE"),
    dict(id="tidal_cobalt", rarity="RARE", dur=700, speed=7.5, dmg=3.5, ench=15, level="DIAMOND", school="ICE"),
    dict(id="magmatite", rarity="RARE", dur=900, speed=8.0, dmg=4.0, ench=12, level="DIAMOND", school="FIRE"),
    dict(id="frost_steel", rarity="EPIC", dur=1150, speed=8.0, dmg=4.5, ench=15, level="DIAMOND", school="ICE"),
    dict(id="deep_mithril", rarity="EPIC", dur=1450, speed=8.5, dmg=5.0, ench=16, level="NETHERITE", school="NATURE"),
    dict(id="nebulite", rarity="LEGENDARY", dur=1800, speed=9.0, dmg=5.5, ench=18, level="NETHERITE", school="VOID"),
    dict(id="void_shard", rarity="LEGENDARY", dur=2200, speed=9.5, dmg=6.0, ench=20, level="NETHERITE", school="VOID"),
    dict(id="primordial", rarity="MYTHIC", dur=3000, speed=10.0, dmg=7.0, ench=22, level="NETHERITE", school="FIRE"),
]
TIER_NAMES = {
    "astral_silver": ("Astral Silver", "d'argent astral"),
    "celestial_crystal": ("Celestial Crystal", "de cristal céleste"),
    "corruptium": ("Corruptium", "de corruptium"),
    "tidal_cobalt": ("Tidal Cobalt", "de cobalt des marées"),
    "magmatite": ("Magmatite", "de magmatite"),
    "frost_steel": ("Frost Steel", "d'acier de givre"),
    "deep_mithril": ("Deep Mithril", "de mithril des profondeurs"),
    "nebulite": ("Nebulite", "de nébulite"),
    "void_shard": ("Void Shard", "d'éclat du néant"),
    "primordial": ("Primordial", "primordial"),
}
TIER_ITEM = {t["id"]: (mat_item(t["id"]) if t["id"] in MAT_BY_ID else "primordial_alloy") for t in TIERS}
TIER_COLOR = {t["id"]: (MAT_BY_ID[t["id"]]["color"] if t["id"] in MAT_BY_ID else (0xF0, 0xE0, 0x90)) for t in TIERS}

# Catégories d'armes (9 x 10 paliers = 90) + 10 légendaires = 100
WEAPON_CATEGORIES = [
    dict(id="sword", en="Sword", fr="Épée", fr_g="f"),
    dict(id="greatsword", en="Greatsword", fr="Grande épée", fr_g="f"),
    dict(id="dagger", en="Dagger", fr="Dague", fr_g="f"),
    dict(id="battleaxe", en="Battle Axe", fr="Hache de combat", fr_g="f"),
    dict(id="spear", en="Spear", fr="Lance", fr_g="f"),
    dict(id="staff", en="Staff", fr="Bâton magique", fr_g="m"),
    dict(id="bow", en="Bow", fr="Arc", fr_g="m"),
    dict(id="crossbow", en="Crossbow", fr="Arbalète", fr_g="f"),
    dict(id="wand", en="Wand", fr="Baguette", fr_g="f"),
]

LEGENDARY_WEAPONS = [
    dict(id="celestial_blade", base="sword", rarity="LEGENDARY", tier="celestial_crystal", color=(0xFF, 0xF4, 0xB0),
         en="Celestial Blade", fr="Épée Céleste",
         desc_en="Deals radiant damage and releases an energy wave on critical hits.",
         desc_fr="Inflige des dégâts lumineux et libère une onde d'énergie sur les coups critiques."),
    dict(id="blade_of_corruption", base="sword", rarity="LEGENDARY", tier="corruptium", color=(0xC0, 0x10, 0x20),
         en="Blade of Corruption", fr="Lame de Corruption",
         desc_en="Withers its victims and drains their life force.",
         desc_fr="Flétrit ses victimes et draine leur force vitale."),
    dict(id="abyssal_trident", base="spear", rarity="LEGENDARY", tier="tidal_cobalt", color=(0x20, 0xD0, 0xC0),
         en="Abyssal Trident", fr="Trident Abyssal",
         desc_en="Grants water breathing and grace; devastating underwater.",
         desc_fr="Accorde la respiration aquatique et la grâce ; dévastateur sous l'eau."),
    dict(id="volcanic_hammer", base="battleaxe", rarity="LEGENDARY", tier="magmatite", color=(0xFF, 0x50, 0x10),
         en="Volcanic Hammer", fr="Marteau Volcanique",
         desc_en="Unleashes a burning shockwave around the target.",
         desc_fr="Déclenche une onde de choc brûlante autour de la cible."),
    dict(id="void_bow", base="bow", rarity="LEGENDARY", tier="void_shard", color=(0xB0, 0x40, 0xFF),
         en="Void Bow", fr="Arc du Néant",
         desc_en="Fires energy arrows that ignore gravity.",
         desc_fr="Tire des projectiles énergétiques insensibles à la gravité."),
    dict(id="frost_queen_scepter", base="staff", rarity="LEGENDARY", tier="frost_steel", color=(0xB0, 0xF0, 0xFF),
         en="Frost Queen's Scepter", fr="Sceptre de la Reine du Givre",
         desc_en="Casts empowered ice lances that freeze enemies solid.",
         desc_fr="Lance des javelots de glace surpuissants qui figent les ennemis."),
    dict(id="sky_emperor_greatsword", base="greatsword", rarity="LEGENDARY", tier="astral_silver", color=(0xF0, 0xF8, 0xFF),
         en="Sky Emperor's Greatsword", fr="Grande Épée de l'Empereur des Cieux",
         desc_en="Calls down lightning on struck enemies.",
         desc_fr="Fait tomber la foudre sur les ennemis frappés."),
    dict(id="depths_fang", base="dagger", rarity="LEGENDARY", tier="deep_mithril", color=(0x80, 0x60, 0xC0),
         en="Fang of the Depths", fr="Croc des Profondeurs",
         desc_en="Poisons and blinds; deals extra damage from behind.",
         desc_fr="Empoisonne et aveugle ; inflige plus de dégâts dans le dos."),
    dict(id="stormcaller_crossbow", base="crossbow", rarity="LEGENDARY", tier="nebulite", color=(0xFF, 0xF0, 0x60),
         en="Stormcaller Crossbow", fr="Arbalète d'Appel-Tempête",
         desc_en="Bolts strike their target with lightning.",
         desc_fr="Les carreaux foudroient leur cible."),
    dict(id="world_ender", base="greatsword", rarity="MYTHIC", tier="primordial", color=(0xFF, 0xE0, 0xFF),
         en="World Ender", fr="Fin des Mondes",
         desc_en="The blade forged to end the World Devourer.",
         desc_fr="La lame forgée pour abattre le Dévoreur de Mondes."),
]

# ---------------------------------------------------------------------------
# Armures (30 ensembles)
# ---------------------------------------------------------------------------
# (id, matériau de réparation/recette, multiplicateur de durabilité, défense [bottes, jambières, plastron, casque],
#  robustesse, résistance au recul, enchantabilité, rareté, bonus d'ensemble)
ARMOR_SETS = []


def _armor(id, item, en, fr, color, dur, defense, tough, kb, ench, rarity, bonus):
    ARMOR_SETS.append(dict(id=id, item=item, en=en, fr=fr, color=color, dur=dur, defense=defense,
                           tough=tough, kb=kb, ench=ench, rarity=rarity, bonus=bonus))


_armor("astral_silver", "astral_silver_ingot", "Astral Silver", "d'argent astral", (0xC8, 0xD4, 0xE8), 16, (2, 5, 6, 2), 0.0, 0.0, 14, "COMMON", "none")
_armor("celestial_crystal", "celestial_crystal", "Celestial Crystal", "de cristal céleste", (0x7F, 0xE0, 0xFF), 18, (2, 5, 6, 2), 0.5, 0.0, 18, "UNCOMMON", "none")
_armor("windstone", "windstone", "Windwalker", "du marcheur des vents", (0xB8, 0xF0, 0xD8), 15, (1, 4, 5, 2), 0.0, 0.0, 16, "UNCOMMON", "slow_falling")
_armor("sky_guardian", "celestial_essence", "Sky Guardian", "du gardien des cieux", (0xF0, 0xF8, 0xFF), 26, (3, 6, 7, 3), 1.5, 0.0, 16, "RARE", "slow_falling")
_armor("corruptium", "corruptium_ingot", "Corruptium", "de corruptium", (0xA0, 0x18, 0x30), 20, (2, 5, 7, 2), 1.0, 0.0, 12, "UNCOMMON", "none")
_armor("blood_ruby", "blood_ruby", "Blood Ruby", "de rubis de sang", (0xE0, 0x10, 0x28), 20, (2, 5, 6, 2), 1.0, 0.0, 15, "UNCOMMON", "none")
_armor("blight_amber", "blight_amber", "Blightward", "anti-fléau", (0xD0, 0x60, 0x20), 18, (2, 4, 6, 2), 0.5, 0.0, 14, "UNCOMMON", "poison_immunity")
_armor("corruption_lord", "corrupted_essence", "Corruption Lord", "du seigneur corrompu", (0x60, 0x08, 0x10), 28, (3, 6, 8, 3), 2.0, 0.0, 14, "RARE", "strength")
_armor("tidal_cobalt", "tidal_cobalt_ingot", "Tidal Cobalt", "de cobalt des marées", (0x30, 0x60, 0xD0), 24, (2, 6, 7, 2), 1.0, 0.0, 15, "RARE", "none")
_armor("abyssal_pearl", "abyssal_pearl", "Pearl Diver", "du plongeur de perles", (0xE0, 0xF0, 0xF0), 22, (2, 5, 6, 2), 1.0, 0.0, 18, "RARE", "water_breathing")
_armor("sea_jade", "sea_jade", "Sea Jade", "de jade marin", (0x30, 0xC0, 0x90), 22, (2, 5, 7, 2), 1.0, 0.0, 15, "RARE", "dolphins_grace")
_armor("abyss_king", "ocean_essence", "Abyss King", "du roi des abysses", (0x10, 0x30, 0x50), 30, (3, 6, 8, 3), 2.0, 0.05, 16, "EPIC", "water_breathing")
_armor("magmatite", "magmatite_ingot", "Magmatite", "de magmatite", (0xF0, 0x60, 0x10), 28, (3, 6, 8, 3), 2.0, 0.05, 12, "RARE", "fire_resistance")
_armor("ember_garnet", "ember_garnet", "Ember Garnet", "de grenat de braise", (0xC0, 0x20, 0x10), 26, (3, 6, 7, 2), 1.5, 0.0, 14, "RARE", "none")
_armor("brimstone", "brimstone", "Brimstone", "de soufre infernal", (0xF0, 0xD0, 0x30), 24, (2, 5, 7, 2), 1.0, 0.0, 14, "RARE", "fire_resistance")
_armor("volcanic_titan", "chaos_essence", "Volcanic Titan", "du titan volcanique", (0x50, 0x20, 0x10), 34, (3, 7, 8, 3), 2.5, 0.1, 14, "EPIC", "fire_resistance")
_armor("frost_steel", "frost_steel_ingot", "Frost Steel", "d'acier de givre", (0x90, 0xC8, 0xE8), 32, (3, 6, 8, 3), 2.5, 0.05, 15, "EPIC", "none")
_armor("everfrost", "everfrost_gem", "Everfrost", "de glace éternelle", (0xC8, 0xF8, 0xFF), 30, (3, 6, 8, 3), 2.0, 0.0, 17, "EPIC", "freeze_immunity")
_armor("rime_sapphire", "rime_sapphire", "Rime Sapphire", "de saphir de givre", (0x40, 0x80, 0xF0), 30, (3, 6, 8, 3), 2.0, 0.0, 16, "EPIC", "none")
_armor("frost_queen", "eternal_essence", "Frost Queen", "de la reine du givre", (0xE0, 0xF8, 0xFF), 36, (3, 7, 9, 3), 3.0, 0.1, 18, "EPIC", "freeze_immunity")
_armor("deep_mithril", "deep_mithril_ingot", "Deep Mithril", "de mithril des profondeurs", (0x90, 0xA0, 0xC0), 36, (3, 7, 8, 3), 3.0, 0.1, 16, "EPIC", "none")
_armor("shadow_quartz", "shadow_quartz", "Shadowstalker", "du rôdeur d'ombre", (0x50, 0x40, 0x70), 32, (3, 6, 8, 3), 2.5, 0.05, 16, "EPIC", "night_vision")
_armor("luminite", "luminite", "Luminite", "de luminite", (0xA0, 0xFF, 0x80), 32, (3, 6, 8, 3), 2.5, 0.05, 18, "EPIC", "night_vision")
_armor("depths_devourer", "depths_essence", "Depths Devourer", "du dévoreur souterrain", (0x30, 0x20, 0x40), 40, (4, 7, 9, 4), 3.0, 0.1, 16, "LEGENDARY", "haste")
_armor("nebulite", "nebulite_ingot", "Nebulite", "de nébulite", (0x80, 0x40, 0xC0), 40, (4, 7, 9, 4), 3.0, 0.1, 18, "LEGENDARY", "none")
_armor("void_shard", "void_shard", "Void Walker", "du marcheur du néant", (0x40, 0x10, 0x60), 42, (4, 7, 9, 4), 3.5, 0.1, 18, "LEGENDARY", "slow_falling")
_armor("cosmic_opal", "cosmic_opal", "Cosmic Opal", "d'opale cosmique", (0xF0, 0xB0, 0xF0), 42, (4, 7, 9, 4), 3.5, 0.1, 20, "LEGENDARY", "regeneration")
_armor("void_sovereign", "void_essence", "Void Sovereign", "du souverain du néant", (0x20, 0x08, 0x30), 46, (4, 8, 10, 4), 4.0, 0.15, 20, "LEGENDARY", "resistance")
_armor("architect", "ancient_fragment", "Architect", "de l'Architecte", (0x40, 0xC0, 0xB0), 22, (2, 5, 7, 2), 1.0, 0.0, 20, "RARE", "mana_regen")
_armor("primordial", "primordial_alloy", "Primordial", "primordial", (0xF0, 0xE0, 0x90), 55, (5, 8, 10, 5), 5.0, 0.2, 25, "MYTHIC", "primordial")

ARMOR_PIECES = [
    dict(id="helmet", slot="HELMET", en="Helmet", fr="Casque", fr_g="m"),
    dict(id="chestplate", slot="CHESTPLATE", en="Chestplate", fr="Plastron", fr_g="m"),
    dict(id="leggings", slot="LEGGINGS", en="Leggings", fr="Jambières", fr_g="fp"),
    dict(id="boots", slot="BOOTS", en="Boots", fr="Bottes", fr_g="fp"),
]

# ---------------------------------------------------------------------------
# Artefacts (30) — effets codés dans ArtifactEffects.java
# ---------------------------------------------------------------------------
ARTIFACTS = [
    ("heart_of_the_ancients", "LEGENDARY", "orb", (0x40, 0xE0, 0xD0), "Heart of the Ancients", "Cœur des Anciens",
     "+50% mana regeneration.", "+50 % de régénération de mana."),
    ("ring_of_time", "LEGENDARY", "ring", (0xF0, 0xD0, 0x60), "Ring of Time", "Anneau du Temps",
     "Use: accelerate your own time (Speed II + Haste II, 10 s). Cooldown 60 s.",
     "Utilisation : accélère votre temps (Vitesse II + Célérité II, 10 s). Recharge 60 s."),
    ("dimensional_eye", "EPIC", "eye", (0xC0, 0x60, 0xFF), "Dimensional Eye", "Œil Dimensionnel",
     "Use: locates the nearest ancient ruin. Cooldown 30 s.",
     "Utilisation : localise la ruine ancienne la plus proche. Recharge 30 s."),
    ("crown_of_seven_worlds", "MYTHIC", "crown", (0xFF, 0xE0, 0x60), "Crown of the Seven Worlds", "Couronne des Sept Mondes",
     "+10 max health, +100 max mana, +2 armor, +10% damage.",
     "+10 vie max, +100 mana max, +2 armure, +10 % de dégâts."),
    ("amulet_of_winds", "RARE", "amulet", (0xB8, 0xF0, 0xD8), "Amulet of the Winds", "Amulette des Vents",
     "Jump Boost I and no fall damage.", "Saut amélioré I et aucun dégât de chute."),
    ("corrupted_heart", "EPIC", "heart", (0xA0, 0x10, 0x20), "Corrupted Heart", "Cœur Corrompu",
     "+3 attack damage, -4 max health.", "+3 dégâts d'attaque, -4 vie max."),
    ("abyssal_amulet", "RARE", "amulet", (0x30, 0xD0, 0xE0), "Abyssal Amulet", "Amulette Abyssale",
     "Water Breathing; Dolphin's Grace while swimming.", "Respiration aquatique ; Grâce du dauphin en nageant."),
    ("ember_core", "RARE", "orb", (0xFF, 0x70, 0x10), "Ember Core", "Noyau de Braise",
     "Fire Resistance.", "Résistance au feu."),
    ("frozen_hourglass", "EPIC", "hourglass", (0xC0, 0xF0, 0xFF), "Frozen Hourglass", "Sablier Gelé",
     "Use: freezes time for nearby enemies (8 blocks, 6 s). Cooldown 45 s.",
     "Utilisation : fige le temps des ennemis proches (8 blocs, 6 s). Recharge 45 s."),
    ("lantern_of_the_lost", "UNCOMMON", "lantern", (0xFF, 0xE0, 0x80), "Lantern of the Lost", "Lanterne des Égarés",
     "Night Vision.", "Vision nocturne."),
    ("void_compass", "EPIC", "compass", (0x80, 0x40, 0xC0), "Void Compass", "Boussole du Néant",
     "Use: returns you to your spawn point. Cooldown 5 min.",
     "Utilisation : vous ramène à votre point d'apparition. Recharge 5 min."),
    ("stellar_charm", "UNCOMMON", "star", (0xFF, 0xF0, 0xA0), "Stellar Charm", "Charme Stellaire",
     "+1 luck.", "+1 chance."),
    ("titan_belt", "RARE", "belt", (0x80, 0x50, 0x30), "Titan's Belt", "Ceinture du Titan",
     "+2 armor, +30% knockback resistance.", "+2 armure, +30 % de résistance au recul."),
    ("sky_feather", "UNCOMMON", "feather", (0xF0, 0xF8, 0xFF), "Sky Feather", "Plume Céleste",
     "+10% movement speed.", "+10 % de vitesse de déplacement."),
    ("guardian_emblem", "RARE", "emblem", (0xC0, 0xC8, 0xD8), "Guardian's Emblem", "Emblème du Gardien",
     "+2 armor toughness.", "+2 robustesse d'armure."),
    ("mana_prism", "RARE", "gem", (0x60, 0x90, 0xFF), "Mana Prism", "Prisme de Mana",
     "+50 max mana.", "+50 mana max."),
    ("archmage_tome", "EPIC", "tome", (0x60, 0x30, 0xC0), "Archmage's Tome", "Grimoire de l'Archimage",
     "-15% spell mana cost.", "-15 % de coût en mana des sorts."),
    ("blood_chalice", "EPIC", "chalice", (0xC0, 0x10, 0x20), "Blood Chalice", "Calice de Sang",
     "Melee hits heal you for 10% of damage dealt.", "Les coups au corps à corps vous soignent de 10 % des dégâts."),
    ("briar_crown", "RARE", "crown", (0x40, 0x80, 0x30), "Briar Crown", "Couronne de Ronces",
     "Melee attackers take 2 damage.", "Les attaquants au corps à corps subissent 2 dégâts."),
    ("storm_orb", "EPIC", "orb", (0xFF, 0xF0, 0x60), "Storm Orb", "Orbe de Tempête",
     "Use: strikes the targeted enemy with lightning. Cooldown 20 s.",
     "Utilisation : foudroie l'ennemi visé. Recharge 20 s."),
    ("tidal_bell", "RARE", "bell", (0x30, 0x90, 0xE0), "Tidal Bell", "Cloche des Marées",
     "Use: heals 6 health and grants Regeneration. Cooldown 45 s.",
     "Utilisation : soigne 6 points de vie et accorde Régénération. Recharge 45 s."),
    ("phoenix_feather", "LEGENDARY", "feather", (0xFF, 0x60, 0x10), "Phoenix Feather", "Plume de Phénix",
     "Prevents a fatal blow. Recharges in 10 min.", "Empêche un coup fatal. Se recharge en 10 min."),
    ("shadow_cloak", "EPIC", "cloak", (0x30, 0x28, 0x40), "Shadow Cloak", "Cape d'Ombre",
     "Invisibility while sneaking.", "Invisibilité en étant accroupi."),
    ("deep_lantern", "UNCOMMON", "lantern", (0xA0, 0xFF, 0x80), "Deep Lantern", "Lanterne des Profondeurs",
     "Haste I.", "Célérité I."),
    ("explorer_astrolabe", "RARE", "compass", (0xD0, 0xA0, 0x40), "Explorer's Astrolabe", "Astrolabe de l'Explorateur",
     "+25% experience from exploration.", "+25 % d'expérience d'exploration."),
    ("sigil_of_war", "EPIC", "emblem", (0xD0, 0x30, 0x20), "Sigil of War", "Sceau de Guerre",
     "+10% attack damage.", "+10 % de dégâts d'attaque."),
    ("eagle_eye_charm", "RARE", "eye", (0xD0, 0xB0, 0x60), "Eagle Eye Charm", "Charme Œil d'Aigle",
     "+25% arrow damage.", "+25 % de dégâts des flèches."),
    ("spirit_bell", "RARE", "bell", (0xC0, 0xF0, 0xE0), "Spirit Bell", "Cloche des Esprits",
     "Use: grants Regeneration I to nearby allies (10 s). Cooldown 40 s.",
     "Utilisation : accorde Régénération I aux alliés proches (10 s). Recharge 40 s."),
    ("architect_cog", "EPIC", "cog", (0x40, 0xC0, 0xB0), "Architect's Cog", "Engrenage de l'Architecte",
     "-20% spell cooldowns.", "-20 % de temps de recharge des sorts."),
    ("seed_of_life", "RARE", "seed", (0x60, 0xE0, 0x60), "Seed of Life", "Graine de Vie",
     "+4 max health and slow regeneration.", "+4 vie max et régénération lente."),
]
ARTIFACTS = [dict(id=a[0], rarity=a[1], shape=a[2], color=a[3], en=a[4], fr=a[5], desc_en=a[6], desc_fr=a[7])
             for a in ARTIFACTS]

# ---------------------------------------------------------------------------
# Blocs lumineux (20), technologiques (15) et magiques (15)
# ---------------------------------------------------------------------------
LIGHT_BLOCKS = [
    # id, en, fr, couleur cadre, couleur lumière, luminance, style
    ("celestial_lamp", "Celestial Lamp", "Lampe céleste", (0xC8, 0xD4, 0xE8), (0xC0, 0xF0, 0xFF), 15, "lamp"),
    ("starlight_block", "Starlight Block", "Bloc de lumière stellaire", (0x20, 0x28, 0x50), (0xFF, 0xF8, 0xD0), 15, "stars"),
    ("cloud_lantern", "Cloud Lantern", "Lanterne des nuages", (0xE8, 0xF0, 0xF8), (0xFF, 0xFF, 0xE0), 14, "lantern"),
    ("blood_lamp", "Blood Lamp", "Lampe de sang", (0x40, 0x10, 0x14), (0xFF, 0x30, 0x40), 13, "lamp"),
    ("glowroot_block", "Glowroot Block", "Bloc de racine luisante", (0x50, 0x18, 0x18), (0xFF, 0x80, 0x60), 12, "veins"),
    ("abyssal_lantern", "Abyssal Lantern", "Lanterne abyssale", (0x20, 0x38, 0x48), (0x40, 0xFF, 0xE0), 15, "lantern"),
    ("coral_glow_block", "Coral Glow Block", "Bloc de corail luisant", (0xD0, 0x50, 0x70), (0xFF, 0xB0, 0xD0), 12, "veins"),
    ("pearl_lamp", "Pearl Lamp", "Lampe de perle", (0x30, 0x60, 0xD0), (0xF0, 0xFF, 0xFF), 15, "lamp"),
    ("magma_lamp", "Magma Lamp", "Lampe de magma", (0x30, 0x20, 0x1C), (0xFF, 0x90, 0x20), 15, "lamp"),
    ("ember_block", "Ember Block", "Bloc de braises", (0x40, 0x18, 0x10), (0xFF, 0x60, 0x10), 13, "veins"),
    ("frost_lamp", "Frost Lamp", "Lampe de givre", (0x90, 0xC8, 0xE8), (0xE0, 0xFF, 0xFF), 15, "lamp"),
    ("aurora_block", "Aurora Block", "Bloc d'aurore", (0x20, 0x40, 0x50), (0x60, 0xFF, 0xB0), 14, "stars"),
    ("mithril_lamp", "Mithril Lamp", "Lampe de mithril", (0x90, 0xA0, 0xC0), (0xD0, 0xE0, 0xFF), 15, "lamp"),
    ("glowshroom_block", "Glowshroom Block", "Bloc de champignon luisant", (0x30, 0x2C, 0x3A), (0x90, 0xFF, 0x70), 13, "veins"),
    ("umbral_lantern", "Umbral Lantern", "Lanterne ombrale", (0x30, 0x2C, 0x3A), (0xB0, 0x80, 0xFF), 12, "lantern"),
    ("void_lamp", "Void Lamp", "Lampe du néant", (0x1C, 0x14, 0x2C), (0xD0, 0x80, 0xFF), 15, "lamp"),
    ("star_core", "Star Core", "Cœur d'étoile", (0x40, 0x30, 0x10), (0xFF, 0xFF, 0xA0), 15, "core"),
    ("nebula_block", "Nebula Block", "Bloc de nébuleuse", (0x20, 0x10, 0x40), (0xF0, 0x90, 0xFF), 14, "stars"),
    ("ancient_lamp", "Ancient Lamp", "Lampe ancienne", (0x50, 0x50, 0x48), (0x50, 0xFF, 0xE0), 15, "lamp"),
    ("rune_light", "Rune Light", "Lumière runique", (0x38, 0x38, 0x40), (0x80, 0xC0, 0xFF), 12, "rune"),
]
LIGHT_BLOCKS = [dict(id=l[0], en=l[1], fr=l[2], frame=l[3], glow=l[4], light=l[5], style=l[6]) for l in LIGHT_BLOCKS]

TECH_BLOCKS = [
    # id, en, fr, comportement, style
    ("architect_stone", "Architect Stone", "Pierre d'Architecte", "none", "plain"),
    ("architect_bricks", "Architect Bricks", "Briques d'Architecte", "none", "bricks"),
    ("architect_pillar", "Architect Pillar", "Pilier d'Architecte", "none", "pillar"),
    ("architect_circuit", "Architect Circuit", "Circuit d'Architecte", "glow_low", "circuit"),
    ("architect_panel", "Architect Panel", "Panneau d'Architecte", "none", "panel"),
    ("architect_power_core", "Architect Power Core", "Noyau énergétique d'Architecte", "redstone_power", "core"),
    ("resonance_conduit", "Resonance Conduit", "Conduit de résonance", "glow_low", "conduit"),
    ("ancient_gear_block", "Ancient Gear Block", "Bloc d'engrenages anciens", "none", "gear"),
    ("energy_conduit", "Energy Conduit", "Conduit d'énergie", "glow_low", "conduit"),
    ("glyph_panel", "Glyph Panel", "Panneau de glyphes", "glow_low", "glyph"),
    ("mechanism_block", "Mechanism Block", "Bloc de mécanisme", "none", "gear"),
    ("rune_terminal", "Rune Terminal", "Terminal runique", "lore_terminal", "terminal"),
    ("ancient_vent", "Ancient Vent", "Aération ancienne", "none", "vent"),
    ("archive_shelf", "Archive Shelf", "Étagère d'archives", "none", "shelf"),
    ("dimensional_stabilizer", "Dimensional Stabilizer", "Stabilisateur dimensionnel", "glow_high", "core"),
]
TECH_BLOCKS = [dict(id=t[0], en=t[1], fr=t[2], behavior=t[3], style=t[4]) for t in TECH_BLOCKS]

MAGIC_BLOCKS = [
    # id, en, fr, comportement, couleur
    ("mana_crystal_block", "Mana Crystal Block", "Bloc de cristal de mana", "mana_restore", (0x60, 0x90, 0xFF)),
    ("arcane_stone", "Arcane Stone", "Pierre arcanique", "particles", (0x90, 0x60, 0xE0)),
    ("fire_rune_stone", "Fire Rune Stone", "Pierre runique de feu", "rune_fire", (0xFF, 0x60, 0x10)),
    ("ice_rune_stone", "Ice Rune Stone", "Pierre runique de glace", "rune_ice", (0x90, 0xE0, 0xFF)),
    ("lightning_rune_stone", "Lightning Rune Stone", "Pierre runique de foudre", "rune_lightning", (0xFF, 0xF0, 0x40)),
    ("nature_rune_stone", "Nature Rune Stone", "Pierre runique de nature", "rune_nature", (0x50, 0xE0, 0x50)),
    ("void_rune_stone", "Void Rune Stone", "Pierre runique du néant", "rune_void", (0xB0, 0x50, 0xFF)),
    ("ley_line_block", "Ley Line Block", "Bloc de ligne tellurique", "mana_regen_zone", (0x40, 0xE0, 0xD0)),
    ("enchanted_obsidian", "Enchanted Obsidian", "Obsidienne enchantée", "particles", (0x50, 0x20, 0x80)),
    ("astral_crystal_block", "Astral Crystal Block", "Bloc de cristal astral", "particles", (0xC0, 0xE8, 0xFF)),
    ("healing_spring_stone", "Healing Spring Stone", "Pierre de source guérisseuse", "heal", (0x60, 0xF0, 0xA0)),
    ("warding_stone", "Warding Stone", "Pierre de protection", "ward", (0xE0, 0xD0, 0x80)),
    ("ether_block", "Ether Block", "Bloc d'éther", "particles", (0xE0, 0xC0, 0xFF)),
    ("wisp_block", "Wisp Block", "Bloc de feu follet", "particles", (0x80, 0xFF, 0xF0)),
    ("spell_altar", "Spell Altar", "Autel des sorts", "altar", (0xA0, 0x80, 0xFF)),
]
MAGIC_BLOCKS = [dict(id=m[0], en=m[1], fr=m[2], behavior=m[3], color=m[4]) for m in MAGIC_BLOCKS]
