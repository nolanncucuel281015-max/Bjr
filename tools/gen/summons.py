"""Créatures des sorts d'invocation : forme de modèle (modelgen.ARCHETYPES), couleurs et motif de texture.

Les statistiques et le comportement sont dans le code Java (magic/SummonType.java) ; ce fichier ne
décrit que l'apparence et les noms.
"""

SUMMONS = [
    dict(id="spirit_wolf", en="Spirit Wolf", fr="Loup spectral", arch="summon_wolf",
         colors=((0xB8, 0xF0, 0xE8), (0x40, 0xD8, 0xB0), (0x60, 0xFF, 0xE0)), pattern="glow"),
    dict(id="stone_guardian", en="Stone Guardian", fr="Gardien de pierre", arch="summon_guardian",
         colors=((0x8A, 0x8C, 0x80), (0x58, 0xA8, 0x40), (0x90, 0xFF, 0x70)), pattern="leaves"),
    dict(id="fire_elemental", en="Fire Elemental", fr="Élémentaire de feu", arch="summon_elemental",
         colors=((0x3A, 0x20, 0x18), (0xFF, 0x8A, 0x20), (0xFF, 0xF0, 0x80)), pattern="magma"),
    dict(id="phoenix", en="Phoenix", fr="Phénix", arch="summon_phoenix",
         colors=((0xFF, 0x70, 0x18), (0xFF, 0xD8, 0x40), (0xFF, 0xFF, 0xC0)), pattern="feathers"),
    dict(id="frost_sentinel", en="Frost Sentinel", fr="Sentinelle de givre", arch="summon_sentinel",
         colors=((0xB8, 0xE4, 0xF8), (0x50, 0xA8, 0xF8), (0x20, 0x70, 0xFF)), pattern="crystals"),
    dict(id="storm_spirit", en="Storm Spirit", fr="Esprit de l'orage", arch="summon_storm",
         colors=((0xFF, 0xF0, 0x80), (0x80, 0xB0, 0xFF), (0xFF, 0xFF, 0xFF)), pattern="glow"),
    dict(id="void_wraith", en="Void Wraith", fr="Spectre du vide", arch="summon_wraith",
         colors=((0x2A, 0x14, 0x40), (0xC0, 0x50, 0xFF), (0xFF, 0x80, 0xFF)), pattern="runes"),
]
