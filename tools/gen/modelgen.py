"""Modèles d'entités originaux (19 formes) et textures des créatures.

Chaque forme est décrite en parties (cuboïdes). Le placement UV est calculé automatiquement
(disposition « box UV » standard de Minecraft), écrit dans
assets/forgotten_dimensions/fd_entity_models/<forme>.json et relu par le client Java
(CreatureModels.java). Les textures de créatures sont peintes face par face sur ce même placement.

Conventions de noms (utilisées par l'animation Java) : head, body, arm_*, leg_*, wing_*, tail*, seg*,
tentacle*, orb*, fin*, claw*.
"""
import math

import textures as X

PI = math.pi


def B(origin, size):
    return {"origin": list(origin), "size": [int(s) for s in size]}


def P(name, pivot, boxes, rot=(0, 0, 0), parent=None):
    return {"name": name, "pivot": list(pivot), "rot": list(rot), "boxes": boxes, "parent": parent}


def _biped(horned=False):
    head = [B((-4, -8, -4), (8, 8, 8))]
    if horned:
        head += [B((-5, -12, -1), (2, 5, 2)), B((3, -12, -1), (2, 5, 2))]
    return [
        P("head", (0, 0, 0), head),
        P("body", (0, 0, 0), [B((-4, 0, -2), (8, 12, 4))]),
        P("arm_r", (-5, 2, 0), [B((-3, -2, -2), (4, 12, 4))]),
        P("arm_l", (5, 2, 0), [B((-1, -2, -2), (4, 12, 4))]),
        P("leg_r", (-1.9, 12, 0), [B((-2, 0, -2), (4, 12, 4))]),
        P("leg_l", (1.9, 12, 0), [B((-2, 0, -2), (4, 12, 4))]),
    ]


def _spider_legs(y, xoff, zs, length, thick):
    parts = []
    for i, z in enumerate(zs):
        yaw = (0.75 - 0.5 * i) * (PI / 4 * 1.1)
        parts.append(P(f"leg_r{i}", (-xoff, y, z), [B((-length + 1, -thick / 2, -thick / 2), (length, thick, thick))], (0, yaw, -PI / 4)))
        parts.append(P(f"leg_l{i}", (xoff, y, z), [B((-1, -thick / 2, -thick / 2), (length, thick, thick))], (0, -yaw, PI / 4)))
    return parts


def _segments(prefix, y, z0, sizes, start_parent=None):
    parts = []
    parent = start_parent
    for i, (w, h, d) in enumerate(sizes):
        name = f"{prefix}{i}"
        pivot = (0, y, z0) if i == 0 else (0, 0, sizes[i - 1][2])
        parts.append(P(name, pivot, [B((-w / 2, -h / 2, 0), (w, h, d))], parent=parent))
        parent = name
    return parts


ARCHETYPES = {
    "biped": _biped(),
    "biped_horned": _biped(True),
    "plant": [
        P("head", (0, 0, 0), [B((-6, -10, -6), (12, 6, 12)), B((-3, -4, -3), (6, 4, 6))]),
        P("body", (0, 0, 0), [B((-3, 0, -2), (6, 12, 4))]),
        P("arm_r", (-4, 2, 0), [B((-2, -1, -1), (2, 12, 2))]),
        P("arm_l", (4, 2, 0), [B((0, -1, -1), (2, 12, 2))]),
        P("leg_r", (-1.5, 12, 0), [B((-1.5, 0, -1.5), (3, 12, 3))]),
        P("leg_l", (1.5, 12, 0), [B((-1.5, 0, -1.5), (3, 12, 3))]),
    ],
    "golem": [
        P("head", (0, -7, -2), [B((-4, -12, -5), (8, 10, 8)), B((-1, -5, -7), (2, 4, 2))]),
        P("body", (0, -7, 0), [B((-9, -2, -6), (18, 12, 11)), B((-4.5, 10, -3), (9, 5, 6))]),
        P("arm_r", (0, -7, 0), [B((-13, -2.5, -3), (4, 30, 6))]),
        P("arm_l", (0, -7, 0), [B((9, -2.5, -3), (4, 30, 6))]),
        P("leg_r", (-4, 11, 0), [B((-3.5, -3, -3), (6, 16, 5))]),
        P("leg_l", (5, 11, 0), [B((-3.5, -3, -3), (6, 16, 5))]),
    ],
    "quadruped": [
        P("body", (0, 0, 0), [B((-4, 6, -7), (8, 8, 14))]),
        P("head", (0, 8, -7), [B((-3, -4, -6), (6, 6, 6)), B((-2, 0, -8), (4, 3, 2))]),
        P("leg_fr", (-2.5, 14, -5), [B((-1.5, 0, -1.5), (3, 10, 3))]),
        P("leg_fl", (2.5, 14, -5), [B((-1.5, 0, -1.5), (3, 10, 3))]),
        P("leg_br", (-2.5, 14, 5), [B((-1.5, 0, -1.5), (3, 10, 3))]),
        P("leg_bl", (2.5, 14, 5), [B((-1.5, 0, -1.5), (3, 10, 3))]),
        P("tail", (0, 7, 7), [B((-1, 0, 0), (2, 2, 4))], (0.4, 0, 0)),
    ],
    "wolf": [
        P("body", (0, 0, 0), [B((-3, 9, -6), (6, 6, 12))]),
        P("head", (0, 11, -6), [B((-3, -3, -4), (6, 6, 4)), B((-1.5, 0, -7), (3, 3, 3)),
                                B((-3, -5, -2), (2, 2, 1)), B((1, -5, -2), (2, 2, 1))]),
        P("leg_fr", (-1.5, 15, -4), [B((-1, 0, -1), (2, 9, 2))]),
        P("leg_fl", (1.5, 15, -4), [B((-1, 0, -1), (2, 9, 2))]),
        P("leg_br", (-1.5, 15, 4), [B((-1, 0, -1), (2, 9, 2))]),
        P("leg_bl", (1.5, 15, 4), [B((-1, 0, -1), (2, 9, 2))]),
        P("tail", (0, 10, 6), [B((-1, 0, 0), (2, 2, 8))], (0.6, 0, 0)),
    ],
    "bear": [
        P("body", (0, 0, 0), [B((-6, 4, -9), (12, 11, 18))]),
        P("head", (0, 7, -9), [B((-4, -4, -7), (8, 8, 7)), B((-2, 0, -10), (4, 3, 3)),
                               B((-4, -6, -4), (2, 2, 1)), B((2, -6, -4), (2, 2, 1))]),
        P("leg_fr", (-3.5, 15, -6), [B((-2.5, 0, -2.5), (5, 9, 5))]),
        P("leg_fl", (3.5, 15, -6), [B((-2.5, 0, -2.5), (5, 9, 5))]),
        P("leg_br", (-3.5, 15, 6), [B((-2.5, 0, -2.5), (5, 9, 5))]),
        P("leg_bl", (3.5, 15, 6), [B((-2.5, 0, -2.5), (5, 9, 5))]),
    ],
    "spider": [
        P("body", (0, 15, 0), [B((-3, -3, -3), (6, 6, 6))]),
        P("head", (0, 15, -3), [B((-4, -4, -8), (8, 8, 8))]),
        P("abdomen", (0, 15, 9), [B((-5, -4, -6), (10, 8, 12))]),
    ] + _spider_legs(15, 4, (2, 1, 0, -1), 16, 2),
    "crab": [
        P("body", (0, 0, 0), [B((-6, 16, -5), (12, 5, 10))]),
        P("head", (0, 16, -5), [B((-3, -3, -1), (2, 3, 2)), B((1, -3, -1), (2, 3, 2))]),
        P("claw_r", (-6, 18, -5), [B((-4, -2, -4), (4, 3, 4))]),
        P("claw_l", (6, 18, -5), [B((0, -2, -4), (4, 3, 4))]),
    ] + _spider_legs(19, 5, (2, 0, -2), 8, 2)[:6],
    "bird": [
        P("body", (0, 16, 0), [B((-3, -3, -5), (6, 6, 10))]),
        P("head", (0, 14, -5), [B((-2.5, -4, -4), (5, 5, 5)), B((-1, -2, -6), (2, 2, 2))]),
        P("wing_r", (-3, 14, -1), [B((-12, 0, -3), (12, 1, 7))]),
        P("wing_l", (3, 14, -1), [B((0, 0, -3), (12, 1, 7))]),
        P("tail", (0, 15, 5), [B((-3, 0, 0), (6, 1, 5))]),
        P("leg_r", (-1.5, 19, 0), [B((-0.5, 0, -0.5), (1, 5, 1))]),
        P("leg_l", (1.5, 19, 0), [B((-0.5, 0, -0.5), (1, 5, 1))]),
    ],
    "drake": [
        P("body", (0, 12, 0), [B((-5, -4, -8), (10, 9, 16))]),
        P("head", (0, 10, -8), [B((-2.5, -3, -6), (5, 5, 6)), B((-3.5, -6, -13), (7, 6, 7)),
                                B((-3, -9, -10), (1, 3, 1)), B((2, -9, -10), (1, 3, 1))]),
        P("wing_r", (-5, 9, -3), [B((-20, 0, -6), (20, 1, 12))]),
        P("wing_l", (5, 9, -3), [B((0, 0, -6), (20, 1, 12))]),
        P("leg_fr", (-3.5, 17, -5), [B((-1.5, 0, -1.5), (3, 7, 3))]),
        P("leg_fl", (3.5, 17, -5), [B((-1.5, 0, -1.5), (3, 7, 3))]),
        P("leg_br", (-3.5, 17, 5), [B((-1.5, 0, -1.5), (3, 7, 3))]),
        P("leg_bl", (3.5, 17, 5), [B((-1.5, 0, -1.5), (3, 7, 3))]),
    ] + _segments("tail", 11, 8, [(4, 4, 8), (3, 3, 8), (2, 2, 6)]),
    "serpent": [
        P("head", (0, 18, -8), [B((-3, -3, -8), (6, 6, 8)), B((-1, -5, -6), (2, 2, 3))]),
    ] + _segments("seg", 18, -8, [(6, 6, 8), (5, 5, 8), (5, 5, 8), (4, 4, 8), (3, 3, 8), (1, 6, 6)]),
    "worm": [
        P("head", (0, 21, -6), [B((-2.5, -2.5, -5), (5, 5, 5))]),
    ] + _segments("seg", 21, -6, [(5, 5, 5), (4, 4, 5), (4, 4, 5), (3, 3, 5), (2, 2, 4)]),
    "fish": [
        P("body", (0, 18, 0), [B((-2, -4, -6), (4, 8, 12))]),
        P("head", (0, 18, -6), [B((-2, -3, -4), (4, 6, 4))]),
        P("tail", (0, 18, 6), [B((-0.5, -4, 0), (1, 8, 5))]),
        P("fin_top", (0, 14, -2), [B((-0.5, -4, 0), (1, 4, 6))]),
        P("fin_r", (-2, 20, -2), [B((-4, 0, 0), (4, 1, 3))]),
        P("fin_l", (2, 20, -2), [B((0, 0, 0), (4, 1, 3))]),
    ],
    "shark": [
        P("body", (0, 17, 0), [B((-3, -4, -8), (6, 8, 18))]),
        P("head", (0, 17, -8), [B((-3, -3, -6), (6, 6, 6))]),
        P("tail", (0, 17, 10), [B((-0.5, -5, 0), (1, 10, 6))]),
        P("fin_top", (0, 13, -2), [B((-0.5, -7, 0), (1, 7, 6))]),
        P("fin_r", (-3, 20, -4), [B((-6, 0, 0), (6, 1, 4))]),
        P("fin_l", (3, 20, -4), [B((0, 0, 0), (6, 1, 4))]),
    ],
    "jelly": [
        P("head", (0, 10, 0), [B((-6, -8, -6), (12, 8, 12)), B((-5, 0, -5), (10, 2, 10))]),
    ] + [P(f"tentacle{i}", (round(4 * math.cos(i * PI / 4), 1), 12, round(4 * math.sin(i * PI / 4), 1)),
           [B((-0.5, 0, -0.5), (1, 12, 1))]) for i in range(8)],
    "wisp": [
        P("head", (0, 14, 0), [B((-3, -3, -3), (6, 6, 6))]),
        P("tail", (0, 17, 0), [B((-2, 0, -2), (4, 5, 4))]),
    ] + [P(f"orb{i}", (0, 14, 0), [B((-1, -1, -1), (2, 2, 2))]) for i in range(4)],
    "crystal": [
        P("head", (0, 14, 0), [B((-3, -7, -3), (6, 14, 6))]),
    ] + [P(f"orb{i}", (0, 14, 0), [B((-1, -2.5, -1), (2, 5, 2))]) for i in range(4)],
    "turtle": [
        P("body", (0, 0, 0), [B((-7, 15, -8), (14, 6, 16)), B((-6, 13, -7), (12, 2, 14))]),
        P("head", (0, 18, -8), [B((-2.5, -2, -5), (5, 4, 5))]),
        P("leg_fr", (-7, 19, -5), [B((-6, 0, -2), (6, 1, 4))]),
        P("leg_fl", (7, 19, -5), [B((0, 0, -2), (6, 1, 4))]),
        P("leg_br", (-6, 20, 6), [B((-4, 0, -1.5), (4, 1, 3))]),
        P("leg_bl", (6, 20, 6), [B((0, 0, -1.5), (4, 1, 3))]),
    ],
}


def layout(arch):
    """Attribue les coordonnées UV (rangement en étagères) et renvoie (modèle JSON, largeur, hauteur)."""
    parts = [dict(p, boxes=[dict(b) for b in p["boxes"]]) for p in ARCHETYPES[arch]]
    boxes = [b for p in parts for b in p["boxes"]]
    for width in (64, 128, 256):
        x = y = shelf = 0
        ok = True
        for b in sorted(boxes, key=lambda b: -(b["size"][1] + b["size"][2])):
            w, h, d = b["size"]
            bw, bh = 2 * d + 2 * w, d + h
            if bw > width:
                ok = False
                break
            if x + bw > width:
                x, y, shelf = 0, y + shelf, 0
            b["uv"] = [x, y]
            x += bw
            shelf = max(shelf, bh)
        if ok:
            height = y + shelf
            th = 32 if height <= 32 else 64 if height <= 64 else 128 if height <= 128 else 256
            if th <= width * 2:
                return {"texture_size": [width, th], "parts": parts}, width, th
    raise ValueError("UV trop grand : " + arch)


def faces(box):
    """Rectangles (x, y, w, h) des six faces d'un cuboïde : top, bottom, east, north(front), west, south."""
    u, v = box["uv"]
    w, h, d = box["size"]
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "side_a": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "side_b": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def _pattern_color(pattern, x, y, base, second, noise, r):
    n = noise[y % len(noise)][x % len(noise[0])]
    col = X.shade(base, 0.82 + 0.3 * n)
    if pattern == "spots" and (n > 0.72):
        col = X.shade(second, 1.0)
    elif pattern == "stripes" and (x // 2) % 3 == 0:
        col = X.shade(second, 0.9)
    elif pattern == "veins" and abs(math.sin(x * 0.7 + y * 0.45 + n * 4)) < 0.12:
        col = X.shade(second, 1.15)
    elif pattern in ("scales", "shell") and ((x + (y // 2) % 2 * 2) % 4 == 0 or y % 2 == 0 and n > 0.6):
        col = X.shade(base, 0.7) if pattern == "scales" else X.shade(second, 0.85)
    elif pattern == "plates" and (x % 6 == 0 or y % 5 == 0):
        col = X.shade(second, 0.75)
    elif pattern == "feathers" and (y % 3 == 0 and (x + y) % 2 == 0):
        col = X.shade(base, 0.72)
    elif pattern == "fluffy":
        col = X.shade(base, 0.86 + 0.24 * ((x * 7 + y * 13) % 5) / 4)
    elif pattern == "glow" and n > 0.65:
        col = X.shade(second, 1.25)
    elif pattern == "leaves" and n > 0.55:
        col = X.shade(second, 0.9 + 0.2 * n)
    elif pattern == "rings" and y % 3 == 0:
        col = X.shade(second, 1.0)
    elif pattern == "runes" and ((x * 3 + y * 5) % 11 == 0):
        col = X.shade(second, 1.3)
    elif pattern == "crystals" and ((x + y) % 5 == 0 or n > 0.8):
        col = X.shade(second, 1.2)
    elif pattern == "stars" and r.random() < 0.06:
        col = (255, 255, 255, 255)
    return col


def paint(arch_model, tw, th, creature):
    base, second, eye = creature["colors"]
    pattern = creature["pattern"]
    cv = X.Canvas(tw, th)
    noise = X.value_noise(creature["id"], 32, 32, 4.0)
    r = X.rng_for(creature["id"] + "px")
    shade_by_face = {"top": 1.12, "bottom": 0.7, "side_a": 0.93, "side_b": 0.93, "front": 1.0, "back": 0.88}
    for part in arch_model["parts"]:
        accent_part = part["name"].startswith(("wing", "fin", "tail", "claw", "tentacle", "orb", "abdomen"))
        for bi, box in enumerate(part["boxes"]):
            for fname, (fx, fy, fw, fh) in faces(box).items():
                for y in range(fh):
                    for x in range(fw):
                        col = _pattern_color(pattern, fx + x, fy + y, second if accent_part and pattern not in ("plates",) else base,
                                             base if accent_part else second, noise, r)
                        col = X.shade(col[:3], shade_by_face[fname])
                        cv.set(fx + x, fy + y, col)
                # contour discret
                for x in range(fw):
                    if fh > 2:
                        cv.set(fx + x, fy + fh - 1, X.shade(cv.get(fx + x, fy + fh - 1)[:3], 0.8))
            # Yeux sur la face avant du premier cuboïde de la tête.
            if part["name"] == "head" and bi == 0:
                fx, fy, fw, fh = faces(box)["front"]
                if fw >= 4 and fh >= 3:
                    ey = fy + max(1, fh // 3)
                    ex1, ex2 = fx + fw // 4, fx + fw - 1 - fw // 4
                    for ex in (ex1, ex2):
                        cv.set(ex, ey, eye + (255,) if len(eye) == 3 else eye)
                        if fw >= 8:
                            cv.set(ex + (1 if ex == ex1 else -1), ey, X.shade(eye, 0.8))
                    if fh >= 6 and fw >= 6:
                        for mx in range(fx + fw // 3, fx + fw - fw // 3):
                            cv.set(mx, fy + fh - 2, X.shade(base, 0.35))
    return cv


def spawn_egg(base, second, key):
    cv = X.Canvas()
    r = X.rng_for(key)
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 5.2, (y - 8.5) / 6.6
            if dy < 0:
                dy *= 1.15
            if dx * dx + dy * dy <= 1.0:
                col = X.shade(base, 1.15 - 0.3 * (x + y) / 30)
                if r.random() < 0.18:
                    col = X.shade(second, 1.0)
                cv.set(x, y, col)
    cv.outline(X.shade(base, 0.3))
    return cv
