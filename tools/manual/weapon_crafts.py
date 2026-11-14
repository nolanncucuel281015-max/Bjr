#!/usr/bin/env python3
"""Planches illustrées des crafts des 100 armes du mod (grilles d'établi avec icônes).

Sortie : docs/crafts/armes_*.png (une planche par matériau, une pour les types, une pour les
légendaires) et docs/crafts/Crafts des armes.pdf (toutes les planches).
Les recettes sont lues dans les fichiers JSON du mod ; les icônes du mod viennent de
tools/manual/build_manual.py, celles de Minecraft du jar client du cache Gradle.
"""
import io
import json
import os
import sys
import zipfile

from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(ROOT, "tools", "gen"))

import build_manual as BM  # noqa: E402
import content as C  # noqa: E402

NS = "forgotten_dimensions"
DATA = os.path.join(ROOT, "src", "main", "resources", "data", NS, "recipe")
OUT = os.path.join(ROOT, "docs", "crafts")
CLIENT_JAR = "/root/.gradle/caches/fabric-loom/1.21.11/minecraft-client.jar"
VANILLA_TEX = {"minecraft:stick": "item/stick", "minecraft:string": "item/string", "minecraft:amethyst_shard": "item/amethyst_shard",
               "minecraft:tripwire_hook": "block/tripwire_hook", "minecraft:nether_star": "item/nether_star"}
FR_VANILLA = {"minecraft:stick": "Bâton", "minecraft:string": "Ficelle", "minecraft:amethyst_shard": "Éclat d'améthyste",
              "minecraft:tripwire_hook": "Crochet", "minecraft:nether_star": "Étoile du Nether"}
FR = json.load(open(os.path.join(ROOT, "src", "main", "resources", "assets", NS, "lang", "fr_fr.json"), encoding="utf-8"))

BG = (40, 32, 48)
GOLD = (216, 176, 74)
RARITY_COLOR = {"COMMON": (230, 230, 230), "UNCOMMON": (120, 230, 120), "RARE": (90, 200, 255), "EPIC": (200, 120, 255),
                "LEGENDARY": (255, 190, 60), "MYTHIC": (255, 90, 160)}
RARITY_FR = {"COMMON": "Commun", "UNCOMMON": "Peu commun", "RARE": "Rare", "EPIC": "Épique", "LEGENDARY": "Légendaire",
             "MYTHIC": "Mythique"}
_ICONS = {}
_jar = zipfile.ZipFile(CLIENT_JAR) if os.path.exists(CLIENT_JAR) else None


def name(item):
    if item.startswith("minecraft:"):
        return FR_VANILLA.get(item, item.split(":")[1])
    local = item.split(":")[-1]
    return FR.get(f"item.{NS}.{local}") or FR.get(f"block.{NS}.{local}") or local


def icon_img(item, size):
    key = (item, size)
    if key in _ICONS:
        return _ICONS[key]
    im = None
    if item.startswith("minecraft:"):
        if _jar and item in VANILLA_TEX:
            im = Image.open(io.BytesIO(_jar.read(f"assets/minecraft/textures/{VANILLA_TEX[item]}.png"))).convert("RGBA")
    else:
        rel = BM.icon(item)
        if rel:
            im = Image.open(os.path.join(BM.OUT, rel)).convert("RGBA")
    if im is not None:
        im = im.resize((size, size), Image.NEAREST)
    _ICONS[key] = im
    return im


def recipe_card(rid):
    """Carte : nom de l'arme, grille 3×3, flèche, résultat, et légende des ingrédients."""
    r = json.load(open(os.path.join(DATA, rid + ".json"), encoding="utf-8"))
    S, PAD = 64, 16
    grid = [[None] * 3 for _ in range(3)]
    for y, row in enumerate(r["pattern"]):
        for x, ch in enumerate(row):
            if ch != " ":
                grid[y][x] = r["key"][ch]
    ingredients = []
    for row in grid:
        for it in row:
            if it and it not in ingredients:
                ingredients.append(it)
    W = PAD * 2 + S * 3 + 90 + S + 8
    H = 44 + S * 3 + 12 + 22 * len(ingredients) + PAD
    img = Image.new("RGBA", (W, H), (58, 48, 68, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W - 1, H - 1], outline=GOLD, width=2)
    result = r["result"]["id"]
    d.text((PAD, 12), name(result), fill=(255, 236, 170), font=BM._font(17))

    def slot(x, y, item, big=False):
        s = S + (8 if big else 0)
        d.rectangle([x, y, x + s - 4, y + s - 4], fill=(139, 139, 139))
        d.line([x, y, x + s - 4, y], fill=(55, 55, 55), width=3)
        d.line([x, y, x, y + s - 4], fill=(55, 55, 55), width=3)
        d.line([x, y + s - 4, x + s - 4, y + s - 4], fill=(255, 255, 255), width=3)
        d.line([x + s - 4, y, x + s - 4, y + s - 4], fill=(255, 255, 255), width=3)
        if item:
            ic = icon_img(item, s - 16)
            if ic:
                img.alpha_composite(ic, (x + 6, y + 6))

    top = 44
    for y in range(3):
        for x in range(3):
            slot(PAD + x * S, top + y * S, grid[y][x])
    ax, ay = PAD + S * 3 + 14, top + S * 1.5
    d.polygon([(ax, ay - 10), (ax + 40, ay - 10), (ax + 40, ay - 22), (ax + 62, ay), (ax + 40, ay + 22), (ax + 40, ay + 10),
               (ax, ay + 10)], fill=(200, 200, 200))
    slot(PAD + S * 3 + 86, top + S - 4, result, big=True)
    ly = top + S * 3 + 10
    for it in ingredients:
        ic = icon_img(it, 18)
        if ic:
            img.alpha_composite(ic, (PAD, ly))
        d.text((PAD + 26, ly + 1), name(it), fill=(225, 225, 225), font=BM._font(14))
        ly += 22
    return img


def sheet(title, subtitle, rids, cols=3, color=GOLD):
    cards = [recipe_card(r) for r in rids]
    cw = max(c.width for c in cards)
    ch = max(c.height for c in cards)
    gap = 18
    rows = (len(cards) + cols - 1) // cols
    W = gap + cols * (cw + gap)
    H = 110 + rows * (ch + gap) + 10
    img = Image.new("RGBA", (W, H), BG + (255,))
    d = ImageDraw.Draw(img)
    d.rectangle([6, 6, W - 7, H - 7], outline=color, width=3)
    d.text((gap + 4, 22), title, fill=color, font=BM._font(34))
    d.text((gap + 6, 70), subtitle, fill=(200, 190, 210), font=BM._font(17))
    for i, c in enumerate(cards):
        x = gap + (i % cols) * (cw + gap)
        y = 110 + (i // cols) * (ch + gap)
        img.alpha_composite(c, (x, y))
    return img.convert("RGB")


def main():
    os.makedirs(OUT, exist_ok=True)
    pages = []
    # 1. Les 9 types d'armes (forme du craft), illustrés avec la magmatite
    cats = [c["id"] for c in C.WEAPON_CATEGORIES]
    p = sheet("Les 9 types d'armes", "Même craft pour les 10 matériaux : seul le matériau change (ici : magmatite).",
              [f"magmatite_{c}" for c in cats])
    p.save(os.path.join(OUT, "armes_0_types.png"))
    pages.append(p)
    # 2. Une planche par matériau (9 armes chacune)
    for i, t in enumerate(C.TIERS, 1):
        en, fr = C.TIER_NAMES[t["id"]]
        mat = name(f"{NS}:{C.TIER_ITEM[t['id']]}")
        col = RARITY_COLOR[t["rarity"]]
        p = sheet(f"Armes {fr}", f"Matériau : {mat}  ·  rareté : {RARITY_FR[t['rarity']]}",
                  [f"{t['id']}_{c}" for c in cats], color=col)
        p.save(os.path.join(OUT, f"armes_{i:02d}_{t['id']}.png"))
        pages.append(p)
    # 3. Les 10 légendaires
    p = sheet("Les 10 armes légendaires", "Une arme du mod au centre, entourée de l'essence et du fragment de sa dimension.",
              [lw["id"] for lw in C.LEGENDARY_WEAPONS], color=(255, 190, 60))
    p.save(os.path.join(OUT, "armes_11_legendaires.png"))
    pages.append(p)
    pdf = os.path.join(OUT, "Crafts des armes.pdf")
    pages[0].save(pdf, save_all=True, append_images=pages[1:], resolution=110)
    print(f"{len(pages)} planches, {9 * 11 + 1} recettes -> {OUT}")


if __name__ == "__main__":
    main()
