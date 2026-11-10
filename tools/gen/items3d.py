"""Modèles d'objets 3D (armes et armures) : chaque objet est composé de segments en relief.

Les armes sont construites en diagonale, comme les sprites vanilla, pour réutiliser exactement les
transformations « en main » de Minecraft. Chaque segment est un cuboïde orienté par pas de 22,5°
(contrainte du format des modèles). Les textures sont de petites matières 16×16 (métal, manche,
garde, gemme, corde) générées par matériau.
"""
import math
import os

import textures as X

ALLOWED = (-45.0, -22.5, 0.0, 22.5, 45.0)

HANDHELD = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
    "fixed": {"rotation": [0, 180, 0], "scale": [1, 1, 1]},
    "gui": {"rotation": [-18, 28, 0], "translation": [0, 0, 0], "scale": [0.92, 0.92, 0.92]},
}
BOW_HANDS = {
    "thirdperson_righthand": {"rotation": [-80, 260, -40], "translation": [-1, -2, 2.5], "scale": [0.9, 0.9, 0.9]},
    "thirdperson_lefthand": {"rotation": [-80, -280, 40], "translation": [-1, -2, 2.5], "scale": [0.9, 0.9, 0.9]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
}
BLOCKLIKE = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.8, 0.8, 0.8]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.35, 0.35, 0.35]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.7, 0.7, 0.7]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.45, 0.45, 0.45]},
    "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.45, 0.45, 0.45]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
}


def _uv(a, b):
    """Rectangle UV de la taille de la face (au plus 16×16), décalé selon la position."""
    w, h = min(16.0, max(0.5, a)), min(16.0, max(0.5, b))
    return [0, 0, round(w, 2), round(h, 2)]


def box(frm, to, tex, rotation=None):
    dx, dy, dz = (to[i] - frm[i] for i in range(3))
    faces = {
        "north": {"uv": _uv(dx, dy), "texture": "#" + tex}, "south": {"uv": _uv(dx, dy), "texture": "#" + tex},
        "east": {"uv": _uv(dz, dy), "texture": "#" + tex}, "west": {"uv": _uv(dz, dy), "texture": "#" + tex},
        "up": {"uv": _uv(dx, dz), "texture": "#" + tex}, "down": {"uv": _uv(dx, dz), "texture": "#" + tex},
    }
    el = {"from": [round(v, 3) for v in frm], "to": [round(v, 3) for v in to], "faces": faces}
    if rotation:
        el["rotation"] = rotation
    return el


def stick(cx, cy, length, width, angle, depth, tex, z=8.0):
    """Segment centré en (cx, cy), orienté selon `angle` (degrés, multiple de 22,5)."""
    a = angle % 180
    rot = a - 90
    vertical = rot in ALLOWED
    if not vertical:
        rot = a if a <= 45 else a - 180
        if rot not in ALLOWED:
            raise ValueError(f"angle non représentable : {angle}")
    if vertical:
        frm = [cx - width / 2, cy - length / 2, z - depth / 2]
        to = [cx + width / 2, cy + length / 2, z + depth / 2]
    else:
        frm = [cx - length / 2, cy - width / 2, z - depth / 2]
        to = [cx + length / 2, cy + width / 2, z + depth / 2]
    rotation = {"origin": [cx, cy, z], "axis": "z", "angle": rot} if rot else None
    return box(frm, to, tex, rotation)


D = (math.sqrt(0.5), math.sqrt(0.5))


def along(t, total=22.0):
    """Point sur la diagonale bas-gauche → haut-droite ; t de 0 (manche) à 1 (pointe)."""
    s = (t - 0.5) * total
    return 8 + D[0] * s, 8 + D[1] * s


def piece(t0, t1, width, depth, tex, total=22.0, angle=45):
    (x0, y0), (x1, y1) = along(t0, total), along(t1, total)
    return stick((x0 + x1) / 2, (y0 + y1) / 2, math.hypot(x1 - x0, y1 - y0), width, angle, depth, tex)


def cross(t, length, width, depth, tex, total=22.0):
    x, y = along(t, total)
    return stick(x, y, length, width, 135, depth, tex)


def weapon_elements(category):
    T = 22.0
    if category == "sword":
        return [piece(0.0, 0.07, 2.2, 2.2, "accent"), piece(0.07, 0.27, 1.2, 1.2, "handle"), cross(0.29, 7.5, 1.4, 2.6, "accent"),
                piece(0.31, 0.82, 2.6, 0.9, "metal"), piece(0.82, 0.94, 1.8, 0.8, "metal"), piece(0.94, 1.0, 0.9, 0.7, "metal"),
                piece(0.33, 0.8, 0.6, 1.0, "accent")]
    if category == "greatsword":
        T = 24.0
        return [piece(0.0, 0.06, 2.4, 2.4, "accent", T), piece(0.06, 0.25, 1.4, 1.4, "handle", T), cross(0.27, 10.0, 1.8, 3.0, "accent", T),
                piece(0.29, 0.84, 3.8, 1.1, "metal", T), piece(0.84, 0.95, 2.6, 1.0, "metal", T), piece(0.95, 1.0, 1.2, 0.9, "metal", T),
                piece(0.31, 0.82, 0.8, 1.2, "accent", T)]
    if category == "dagger":
        T = 16.0
        return [piece(0.0, 0.08, 2.0, 2.0, "accent", T), piece(0.08, 0.36, 1.2, 1.2, "handle", T), cross(0.38, 5.5, 1.2, 2.2, "accent", T),
                piece(0.4, 0.86, 2.0, 0.7, "metal", T), piece(0.86, 1.0, 1.0, 0.6, "metal", T)]
    if category == "battleaxe":
        x, y = along(0.82)
        return [piece(0.0, 1.0, 1.2, 1.2, "handle"), stick(x, y, 3.0, 3.0, 45, 2.6, "accent"),
                stick(x - 3.2, y + 3.2, 8.0, 3.2, 135, 1.2, "metal"), stick(x + 3.2, y - 3.2, 8.0, 3.2, 135, 1.2, "metal"),
                stick(x - 5.2, y + 5.2, 6.0, 1.0, 135, 0.8, "metal"), stick(x + 5.2, y - 5.2, 6.0, 1.0, 135, 0.8, "metal"),
                piece(0.95, 1.06, 1.0, 1.0, "accent")]
    if category == "spear":
        T = 24.0
        return [piece(0.0, 0.78, 0.9, 0.9, "handle", T), piece(0.74, 0.79, 1.8, 1.8, "accent", T),
                piece(0.79, 0.93, 2.4, 0.8, "metal", T), piece(0.93, 1.0, 1.1, 0.7, "metal", T),
                cross(0.8, 3.6, 0.9, 0.9, "metal", T)]
    if category == "staff":
        T = 24.0
        x, y = along(0.9, T)
        return [piece(0.0, 0.82, 1.1, 1.1, "handle", T), piece(0.78, 0.83, 2.2, 2.2, "accent", T),
                stick(x, y, 3.2, 3.2, 45, 3.2, "gem"),
                stick(x - 1.9, y + 1.9, 2.2, 0.7, 45, 0.7, "accent"), stick(x + 1.9, y - 1.9, 2.2, 0.7, 45, 0.7, "accent"),
                piece(0.97, 1.02, 1.2, 1.2, "accent", T)]
    if category == "wand":
        T = 16.0
        x, y = along(0.92, T)
        return [piece(0.05, 0.84, 0.8, 0.8, "handle", T), piece(0.8, 0.86, 1.4, 1.4, "accent", T), stick(x, y, 1.8, 1.8, 45, 1.8, "gem")]
    raise ValueError(category)


def _dir(deg):
    r = math.radians(deg)
    return math.cos(r), math.sin(r)


def _seg(p, deg, length, width, depth, tex):
    """Segment partant du point p dans la direction deg ; renvoie (élément, point d'arrivée)."""
    dx, dy = _dir(deg)
    end = (p[0] + dx * length, p[1] + dy * length)
    return stick((p[0] + end[0]) / 2, (p[1] + end[1]) / 2, length, width, deg, depth, tex), end


def bow_elements(pull=-1):
    """Arc en diagonale, symétrique par rapport à l'axe perpendiculaire passant par la poignée."""
    g1 = (8 + D[0] * 2, 8 + D[1] * 2)
    g0 = (8 - D[0] * 2, 8 - D[1] * 2)
    els = [stick(8, 8, 4.0, 1.6, 45, 1.6, "handle")]
    e, u1 = _seg(g1, 67.5, 5.0, 1.1, 1.1, "metal"); els.append(e)
    e, up = _seg(u1, 90, 3.0, 0.9, 0.9, "accent"); els.append(e)
    e, l1 = _seg(g0, 202.5, 5.0, 1.1, 1.1, "metal"); els.append(e)
    e, lo = _seg(l1, 180, 3.0, 0.9, 0.9, "accent"); els.append(e)
    # Corde : parallèle à la diagonale, reculée pendant la tension.
    back = 0 if pull < 0 else 0.9 * (pull + 1)
    nx, ny = -D[0], D[1]  # normale vers le haut-gauche
    mx, my = (up[0] + lo[0]) / 2 + nx * back, (up[1] + lo[1]) / 2 + ny * back
    length = math.hypot(up[0] - lo[0], up[1] - lo[1])
    els.append(stick(mx, my, length, 0.3, 45, 0.3, "string"))
    if pull >= 0:
        # Flèche perpendiculaire à la corde, traversant la poignée.
        ax, ay = mx - nx * 5.5, my - ny * 5.5
        els.append(stick(ax, ay, 11.0, 0.5, 135, 0.5, "arrow"))
        els.append(stick(mx - nx * 11.0, my - ny * 11.0, 1.6, 1.2, 135, 1.2, "metal"))
    return els


def crossbow_elements(state="standby"):
    x, y = along(0.78, 18.0)
    els = [piece(0.0, 0.85, 1.6, 1.6, "handle", 18.0), piece(0.0, 0.2, 2.2, 2.2, "handle", 18.0),
           stick(x, y, 13.0, 1.2, 135, 1.4, "metal"), stick(x, y, 2.2, 2.2, 45, 2.2, "accent")]
    pull = {"standby": 0.0, "pulling_0": 0.8, "pulling_1": 1.6, "pulling_2": 2.4, "arrow": 2.4, "firework": 2.4}[state]
    sx, sy = x - D[0] * (1.4 + pull), y - D[1] * (1.4 + pull)
    els.append(stick(sx, sy, 12.0, 0.3, 135, 0.3, "string"))
    if state in ("arrow", "firework"):
        els.append(piece(0.45, 0.98, 0.6, 0.6, "arrow" if state == "arrow" else "accent", 18.0))
    return els


def armor_elements(piece_id):
    if piece_id == "helmet":
        return [box([3, 4, 3], [13, 10, 13], "metal"), box([3.5, 10, 3.5], [12.5, 11, 12.5], "metal"),
                box([2.6, 4, 2.6], [13.4, 5, 13.4], "accent"), box([7, 9.5, 2.4], [9, 12.5, 13.6], "accent"),
                box([4.5, 6, 2.8], [11.5, 7, 3.1], "visor")]
    if piece_id == "chestplate":
        return [box([4, 2, 5.5], [12, 12, 10.5], "metal"), box([1.2, 9, 4.8], [4.2, 13, 11.2], "accent"),
                box([11.8, 9, 4.8], [14.8, 13, 11.2], "accent"), box([7.2, 3, 5.2], [8.8, 11, 10.8], "accent"),
                box([4.5, 2, 5.2], [11.5, 3, 10.8], "accent")]
    if piece_id == "leggings":
        return [box([3.5, 11, 5.5], [12.5, 13.5, 10.5], "accent"), box([3.7, 2, 5.8], [7.8, 11, 10.2], "metal"),
                box([8.2, 2, 5.8], [12.3, 11, 10.2], "metal"), box([3.5, 5.5, 5.6], [8, 6.5, 10.4], "accent"),
                box([8, 5.5, 5.6], [12.5, 6.5, 10.4], "accent")]
    if piece_id == "boots":
        return [box([3, 2, 6], [7.5, 7, 10.5], "metal"), box([8.5, 2, 6], [13, 7, 10.5], "metal"),
                box([3, 2, 3.5], [7.5, 4.5, 6], "metal"), box([8.5, 2, 3.5], [13, 4.5, 6], "metal"),
                box([2.8, 6, 5.8], [7.7, 7.5, 10.7], "accent"), box([8.3, 6, 5.8], [13.2, 7.5, 10.7], "accent")]
    raise ValueError(piece_id)


# ------------------------------------------------------------------ textures de matière
def tex_metal(rgb, key):
    cv = X.Canvas()
    X.noisy_fill(cv, key, rgb, levels=(0.78, 0.88, 0.96, 1.04, 1.12), scale=4.0, speckle=0.08)
    for i in range(16):
        cv.set(i, 7, X.shade(rgb, 1.35))
        cv.set(7, i, X.shade(rgb, 1.25))
    return cv


def tex_handle(key):
    cv = X.Canvas()
    for y in range(16):
        for x in range(16):
            band = (x + y) % 4
            cv.set(x, y, X.HANDLE[1] if band == 0 else X.HANDLE[2] if band < 3 else X.HANDLE[3])
    return cv


def tex_accent(rgb, key):
    cv = X.Canvas()
    X.noisy_fill(cv, key, rgb, levels=(0.85, 0.95, 1.05, 1.2), scale=4.0, speckle=0.1)
    return cv


def tex_gem(rgb, key):
    cv = X.Canvas()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 5.5, y - 5.5) / 11
            cv.set(x, y, X.shade(rgb, 1.5 - d))
    return cv


def tex_solid(rgb):
    cv = X.Canvas()
    for y in range(16):
        for x in range(16):
            cv.set(x, y, X.shade(rgb, 1.0 if (x + y) % 5 else 0.85))
    return cv


def write(G, weapons, armor_sets, tier_color, legendary):
    """Écrit les textures de matière et remplace les modèles plats des armes et armures par des modèles 3D."""
    rid, wjson, ASSETS = G.rid, G.wjson, G.ASSETS
    G.save_tex("item", "3d/handle", tex_handle("handle"))
    G.save_tex("item", "3d/string", tex_solid((0xE8, 0xE8, 0xE0)))
    G.save_tex("item", "3d/arrow", tex_solid((0x9A, 0x70, 0x45)))
    G.save_tex("item", "3d/visor", tex_solid((0x18, 0x18, 0x20)))
    gold = (0xE0, 0xB8, 0x40)

    def model(path, elements, textures, display, gui_light="front"):
        wjson(os.path.join(ASSETS, "models", "item", path + ".json"), {
            "gui_light": gui_light, "textures": dict(textures, particle=textures["metal"]),
            "elements": elements, "display": display})

    done = set()
    for w in weapons:
        wid, cat, tier = w["id"], w["category"].lower(), w["tier"]
        color = legendary.get(wid, tier_color[tier])
        key = wid if wid in legendary else tier
        if key not in done:
            G.save_tex("item", f"3d/{key}_metal", tex_metal(color, key + "m"))
            G.save_tex("item", f"3d/{key}_gem", tex_gem(color, key + "g"))
            G.save_tex("item", f"3d/{key}_accent", tex_accent((0xFF, 0xD0, 0x50) if wid in legendary else gold, key + "a"))
            done.add(key)
        tex = {"metal": rid(f"item/3d/{key}_metal"), "handle": rid("item/3d/handle"), "accent": rid(f"item/3d/{key}_accent"),
               "gem": rid(f"item/3d/{key}_gem"), "string": rid("item/3d/string"), "arrow": rid("item/3d/arrow")}
        if cat == "bow":
            disp = dict(HANDHELD, **BOW_HANDS)
            model(wid, bow_elements(), tex, disp)
            for i in range(3):
                model(f"{wid}_pulling_{i}", bow_elements(i), tex, disp)
        elif cat == "crossbow":
            for st in ("standby", "pulling_0", "pulling_1", "pulling_2", "arrow", "firework"):
                model(wid if st == "standby" else f"{wid}_{st}", crossbow_elements(st), tex, HANDHELD)
        else:
            model(wid, weapon_elements(cat), tex, HANDHELD)
            if cat == "spear":
                model(wid + "_in_hand", weapon_elements(cat), tex, HANDHELD)
    for a in armor_sets:
        G.save_tex("item", f"3d/armor_{a['id']}", tex_metal(a["color"], "armor" + a["id"]))
        G.save_tex("item", f"3d/armor_{a['id']}_trim", tex_accent(X.hue_shift(a["color"], 0.08, 0.1, 0.15), "trim" + a["id"]))
        tex = {"metal": rid(f"item/3d/armor_{a['id']}"), "accent": rid(f"item/3d/armor_{a['id']}_trim"), "visor": rid("item/3d/visor")}
        for p in ("helmet", "chestplate", "leggings", "boots"):
            model(f"{a['id']}_{p}", armor_elements(p), tex, BLOCKLIKE, gui_light="side")
