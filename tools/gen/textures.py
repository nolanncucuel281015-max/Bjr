"""Génération procédurale de textures pixel art 16x16 (et 64x32 pour les armures portées).

Toutes les textures sont originales : elles sont dessinées par code à partir de formes
simples, de bruit déterministe et de palettes dérivées d'une couleur de base.
"""
import colorsys
import math
import random
import zlib

from PIL import Image

T = (0, 0, 0, 0)


# ---------------------------------------------------------------------------
# Couleurs
# ---------------------------------------------------------------------------
def clamp(v):
    return max(0, min(255, int(round(v))))


def shade(rgb, f):
    """f < 1 assombrit, f > 1 éclaircit (vers le blanc)."""
    r, g, b = rgb
    if f <= 1:
        return (clamp(r * f), clamp(g * f), clamp(b * f), 255)
    t = f - 1
    return (clamp(r + (255 - r) * t), clamp(g + (255 - g) * t), clamp(b + (255 - b) * t), 255)


def hue_shift(rgb, dh, ds=0.0, dv=0.0):
    h, s, v = colorsys.rgb_to_hsv(*(c / 255 for c in rgb))
    h = (h + dh) % 1.0
    s = max(0, min(1, s + ds))
    v = max(0, min(1, v + dv))
    r, g, b = colorsys.hsv_to_rgb(h, s, v)
    return (clamp(r * 255), clamp(g * 255), clamp(b * 255))


def palette(rgb):
    """0 contour, 1 sombre, 2 moyen, 3 clair, 4 reflet."""
    return [shade(rgb, 0.30), shade(rgb, 0.62), shade(rgb, 0.85), shade(rgb, 1.0), shade(rgb, 1.45)]


HANDLE = [(0x2A, 0x1A, 0x10, 255), (0x5A, 0x3A, 0x22, 255), (0x7C, 0x55, 0x33, 255), (0x9A, 0x70, 0x45, 255)]
GOLD = [(0x5A, 0x40, 0x10, 255), (0xB0, 0x80, 0x20, 255), (0xE8, 0xC0, 0x40, 255), (0xFF, 0xEE, 0x90, 255)]


def rng_for(key):
    return random.Random(zlib.crc32(key.encode()))


# ---------------------------------------------------------------------------
# Canevas
# ---------------------------------------------------------------------------
class Canvas:
    def __init__(self, w=16, h=16):
        self.w, self.h = w, h
        self.px = [[T for _ in range(w)] for _ in range(h)]

    def set(self, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = c

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y][x]
        return T

    def line(self, x0, y0, x1, y1, c):
        x0, y0, x1, y1 = int(x0), int(y0), int(x1), int(y1)
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def disc(self, cx, cy, r, pal, light=(-0.6, -0.6)):
        for y in range(self.h):
            for x in range(self.w):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                d = math.hypot(dx, dy)
                if d <= r:
                    lv = -(dx * light[0] + dy * light[1]) / max(r, 0.01)
                    idx = 2 + (1 if lv > 0.35 else 0) - (1 if lv < -0.35 else 0)
                    if d < r * 0.35 and lv > 0.2:
                        idx = 4
                    self.set(x, y, pal[idx])

    def outline(self, c, diagonal=False):
        """Ajoute un contour autour des pixels opaques."""
        add = []
        for y in range(self.h):
            for x in range(self.w):
                if self.px[y][x][3] == 0:
                    n = [(1, 0), (-1, 0), (0, 1), (0, -1)]
                    if diagonal:
                        n += [(1, 1), (-1, -1), (1, -1), (-1, 1)]
                    if any(self.get(x + a, y + b)[3] > 0 and self.get(x + a, y + b) != c for a, b in n):
                        add.append((x, y))
        for x, y in add:
            self.px[y][x] = c

    def from_ascii(self, rows, cmap, mirror=False):
        for y, row in enumerate(rows):
            if mirror:
                row = row + row[::-1]
            for x, ch in enumerate(row):
                if ch in cmap and cmap[ch] is not None:
                    self.set(x, y, cmap[ch])
        return self

    def image(self):
        img = Image.new("RGBA", (self.w, self.h))
        img.putdata([p for row in self.px for p in row])
        return img

    def save(self, path):
        self.image().save(path)


# ---------------------------------------------------------------------------
# Bruit
# ---------------------------------------------------------------------------
def value_noise(key, w=16, h=16, scale=4.0, octaves=2):
    r = rng_for(key)
    grid = {}

    def g(ix, iy):
        k = (ix % 64, iy % 64)
        if k not in grid:
            grid[k] = r.random()
        return grid[k]

    out = [[0.0] * w for _ in range(h)]
    amp, total = 1.0, 0.0
    sc = scale
    for _ in range(octaves):
        for y in range(h):
            for x in range(w):
                fx, fy = x / sc, y / sc
                ix, iy = int(fx), int(fy)
                tx, ty = fx - ix, fy - iy
                tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
                # bord périodique de 16 px pour des textures qui se répètent
                p = int(w / sc)
                a = g(ix % p, iy % p)
                b = g((ix + 1) % p, iy % p)
                c = g(ix % p, (iy + 1) % p)
                d = g((ix + 1) % p, (iy + 1) % p)
                out[y][x] += amp * ((a * (1 - tx) + b * tx) * (1 - ty) + (c * (1 - tx) + d * tx) * ty)
        total += amp
        amp *= 0.5
        sc /= 2
    return [[v / total for v in row] for row in out]


def noisy_fill(cv, key, base_rgb, levels=(0.72, 0.84, 0.94, 1.04), scale=4.0, speckle=0.08):
    n = value_noise(key, cv.w, cv.h, scale)
    r = rng_for(key + "#speckle")
    for y in range(cv.h):
        for x in range(cv.w):
            v = n[y][x] + (r.random() - 0.5) * speckle * 2
            idx = min(len(levels) - 1, max(0, int(v * len(levels))))
            cv.set(x, y, shade(base_rgb, levels[idx]))


# ---------------------------------------------------------------------------
# Blocs
# ---------------------------------------------------------------------------
def tex_stone(key, rgb, deep=False):
    cv = Canvas()
    if deep:
        noisy_fill(cv, key, rgb, levels=(0.55, 0.66, 0.76, 0.86), scale=4.0)
        r = rng_for(key + "strata")
        for y in (3, 8, 12):
            for x in range(16):
                if r.random() < 0.75:
                    cv.set(x, y + (1 if r.random() < 0.2 else 0), shade(rgb, 0.48))
    else:
        noisy_fill(cv, key, rgb, scale=4.0)
    return cv


def tex_bricks(key, rgb):
    cv = Canvas()
    noisy_fill(cv, key, rgb, levels=(0.82, 0.92, 1.0, 1.06), scale=2.0, speckle=0.12)
    mortar = shade(rgb, 0.55)
    for y in range(16):
        row = y // 4
        if y % 4 == 3:
            for x in range(16):
                cv.set(x, y, mortar)
        else:
            off = 0 if row % 2 == 0 else 4
            for x in range(16):
                if (x + off) % 8 == 7:
                    cv.set(x, y, mortar)
            if y % 4 == 0:
                for x in range(16):
                    if (x + off) % 8 != 7:
                        cv.set(x, y, shade(rgb, 1.12))
    return cv


def tex_polished(key, rgb):
    cv = Canvas()
    noisy_fill(cv, key, rgb, levels=(0.9, 0.95, 1.0, 1.03), scale=8.0, speckle=0.03)
    for i in range(16):
        cv.set(i, 0, shade(rgb, 1.15))
        cv.set(0, i, shade(rgb, 1.15))
        cv.set(i, 15, shade(rgb, 0.7))
        cv.set(15, i, shade(rgb, 0.7))
    return cv


def tex_chiseled(key, rgb, accent):
    cv = tex_polished(key, rgb)
    for i in range(2, 14):
        cv.set(i, 2, shade(rgb, 0.7))
        cv.set(2, i, shade(rgb, 0.7))
        cv.set(i, 13, shade(rgb, 1.12))
        cv.set(13, i, shade(rgb, 1.12))
    glyph = ["..XX..", ".X..X.", "X.XX.X", "X.XX.X", ".X..X.", "..XX.."]
    for y, row in enumerate(glyph):
        for x, ch in enumerate(row):
            if ch == "X":
                cv.set(5 + x, 5 + y, shade(accent, 0.9))
    return cv


def tex_tiles(key, rgb):
    cv = Canvas()
    noisy_fill(cv, key, rgb, levels=(0.86, 0.94, 1.0), scale=4.0, speckle=0.05)
    for y in range(16):
        for x in range(16):
            lx, ly = x % 8, y % 8
            if lx == 7 or ly == 7:
                cv.set(x, y, shade(rgb, 0.6))
            elif lx == 0 or ly == 0:
                cv.set(x, y, shade(rgb, 1.12))
    return cv


def tex_ore(key, stone_cv, rgb):
    cv = Canvas()
    cv.px = [row[:] for row in stone_cv.px]
    pal = palette(rgb)
    r = rng_for(key)
    blobs = []
    for _ in range(5):
        for _ in range(30):
            bx, by = r.randint(2, 13), r.randint(2, 13)
            if all(abs(bx - ox) + abs(by - oy) > 4 for ox, oy in blobs):
                blobs.append((bx, by))
                break
    for bx, by in blobs:
        cells = [(bx, by), (bx + 1, by), (bx, by + 1)]
        if r.random() < 0.6:
            cells.append((bx + 1, by + 1))
        if r.random() < 0.4:
            cells.append((bx - 1, by))
        for (x, y) in cells:
            cv.set(x, y, pal[2])
        cv.set(bx, by, pal[4])
        cv.set(cells[-1][0], cells[-1][1], pal[1])
        for (x, y) in cells:
            for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if (x + a, y + b) not in cells and r.random() < 0.5:
                    cv.set(x + a, y + b, pal[0])
    return cv


def tex_storage(key, rgb, kind):
    pal = palette(rgb)
    cv = Canvas()
    noisy_fill(cv, key, rgb, levels=(0.85, 0.95, 1.0, 1.08), scale=8.0, speckle=0.04)
    for i in range(16):
        cv.set(i, 0, pal[4]); cv.set(0, i, pal[4])
        cv.set(i, 15, pal[1]); cv.set(15, i, pal[1])
    if kind == "ingot":
        for i in range(1, 15):
            cv.set(i, 7, pal[1]); cv.set(i, 8, pal[3])
            cv.set(7, i, pal[1]); cv.set(8, i, pal[3])
    else:
        for i in range(2, 14):
            cv.set(i, i, pal[4])
            cv.set(15 - i, i, pal[1])
    return cv


def tex_raw_block(key, rgb):
    cv = Canvas()
    noisy_fill(cv, key, rgb, levels=(0.55, 0.72, 0.88, 1.0, 1.15), scale=4.0, speckle=0.15)
    return cv


def tex_light(key, frame, glow, style):
    cv = Canvas()
    noisy_fill(cv, key, frame, levels=(0.8, 0.9, 1.0), scale=4.0, speckle=0.05)
    gp = [shade(glow, 0.75), shade(glow, 0.9), shade(glow, 1.0), shade(glow, 1.3)]
    r = rng_for(key + style)
    if style == "lamp":
        for y in range(3, 13):
            for x in range(3, 13):
                d = max(abs(x - 7.5), abs(y - 7.5))
                cv.set(x, y, gp[3 if d < 2 else 2 if d < 3.5 else 1])
        for i in range(2, 14):
            cv.set(i, 2, shade(frame, 0.6)); cv.set(2, i, shade(frame, 0.6))
            cv.set(i, 13, shade(frame, 1.2)); cv.set(13, i, shade(frame, 1.2))
    elif style == "lantern":
        for y in range(2, 14):
            for x in range(4, 12):
                cv.set(x, y, gp[2] if (x + y) % 3 else gp[3])
        for x in range(3, 13):
            cv.set(x, 1, shade(frame, 0.6)); cv.set(x, 14, shade(frame, 0.6))
        for y in range(1, 15):
            cv.set(3, y, shade(frame, 0.6)); cv.set(12, y, shade(frame, 0.6)); cv.set(7, y, shade(frame, 0.7))
    elif style == "stars":
        for _ in range(14):
            x, y = r.randint(0, 15), r.randint(0, 15)
            cv.set(x, y, gp[3])
            if r.random() < 0.4:
                for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    cv.set(x + a, y + b, gp[1])
    elif style == "veins":
        for _ in range(4):
            x, y = r.randint(0, 15), r.randint(0, 15)
            for _ in range(10):
                cv.set(x % 16, y % 16, gp[r.randint(1, 3)])
                x += r.choice((-1, 0, 1)); y += r.choice((-1, 0, 1))
    elif style == "core":
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, y - 7.5)
                if d < 6.5:
                    cv.set(x, y, gp[3 if d < 2.5 else 2 if d < 4.5 else 1])
    elif style == "rune":
        for (x0, y0, x1, y1) in ((4, 3, 11, 3), (7, 3, 7, 12), (4, 12, 11, 12), (4, 7, 11, 8)):
            cv.line(x0, y0, x1, y1, gp[3])
    return cv


def tex_tech(key, style):
    base = (0x5C, 0x5E, 0x58)
    teal = (0x40, 0xE0, 0xD0)
    gold = (0xC8, 0xA0, 0x40)
    cv = Canvas()
    noisy_fill(cv, key, base, levels=(0.8, 0.88, 0.95, 1.0), scale=4.0, speckle=0.05)
    dark = shade(base, 0.55)
    light = shade(base, 1.2)
    r = rng_for(key + style)
    if style == "bricks":
        return tex_bricks(key, base)
    if style in ("plain", "panel", "terminal", "vent", "shelf", "pillar", "glyph", "circuit", "conduit", "core", "gear"):
        for i in range(16):
            cv.set(i, 0, light); cv.set(0, i, light); cv.set(i, 15, dark); cv.set(15, i, dark)
    if style == "pillar":
        for y in range(16):
            for x in (3, 12):
                cv.set(x, y, dark)
            cv.set(4, y, light)
    elif style == "panel":
        for i in range(3, 13):
            cv.set(i, 3, dark); cv.set(3, i, dark); cv.set(i, 12, light); cv.set(12, i, light)
        cv.set(7, 7, shade(gold, 1.0)); cv.set(8, 8, shade(gold, 1.0))
    elif style == "circuit":
        x, y = 2, 2
        for _ in range(6):
            nx = min(13, max(2, x + r.choice((-6, -3, 3, 6))))
            cv.line(x, y, nx, y, shade(teal, 0.9))
            ny = min(13, max(2, y + r.choice((-6, -3, 3, 6))))
            cv.line(nx, y, nx, ny, shade(teal, 0.9))
            cv.set(nx, ny, shade(teal, 1.3))
            x, y = nx, ny
    elif style == "conduit":
        for y in range(16):
            for x in range(6, 10):
                cv.set(x, y, shade(teal, 0.8 if x in (6, 9) else 1.15))
    elif style == "core":
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, y - 7.5)
                if 3 < d < 5:
                    cv.set(x, y, shade(gold, 1.0))
                elif d <= 3:
                    cv.set(x, y, shade(teal, 1.25 if d < 1.5 else 1.0))
    elif style == "gear":
        for y in range(16):
            for x in range(16):
                a = math.atan2(y - 7.5, x - 7.5)
                d = math.hypot(x - 7.5, y - 7.5)
                tooth = 6.5 if math.cos(a * 8) > 0.3 else 5.2
                if 2 < d < tooth:
                    cv.set(x, y, shade(gold, 0.85 if d > 4.5 else 1.0))
    elif style == "glyph":
        for (x0, y0, x1, y1) in ((4, 4, 11, 4), (4, 4, 4, 11), (8, 6, 11, 11), (6, 11, 11, 11)):
            cv.line(x0, y0, x1, y1, shade(teal, 1.1))
    elif style == "terminal":
        for y in range(3, 11):
            for x in range(3, 13):
                cv.set(x, y, (0x10, 0x28, 0x28, 255))
        for y in (4, 6, 8):
            for x in range(4, 4 + r.randint(4, 8)):
                cv.set(x, y, shade(teal, 1.1))
        cv.set(12, 13, shade(gold, 1.0))
    elif style == "vent":
        for y in range(2, 14, 3):
            for x in range(2, 14):
                cv.set(x, y, dark); cv.set(x, y + 1, (0x20, 0x20, 0x20, 255))
    elif style == "shelf":
        for y in (1, 8):
            for x in range(1, 15):
                cv.set(x, y + 6, dark)
            for x in range(2, 14):
                c = r.choice([(0x70, 0x30, 0x20), (0x30, 0x50, 0x70), (0x50, 0x60, 0x30), (0xA0, 0x80, 0x40)])
                for yy in range(y, y + 6):
                    cv.set(x, yy, shade(c, 0.85 if x % 3 == 0 else 1.0))
    return cv


def tex_magic(key, rgb, behavior):
    base = (0x3A, 0x34, 0x48)
    cv = Canvas()
    if behavior in ("particles",) and "crystal" in key:
        noisy_fill(cv, key, rgb, levels=(0.7, 0.85, 1.0, 1.2), scale=2.0, speckle=0.2)
        for i in range(16):
            cv.set(i, (i * 5) % 16, shade(rgb, 1.4))
        return cv
    noisy_fill(cv, key, base if behavior != "particles" else hue_shift(rgb, 0, -0.3, -0.5),
               levels=(0.8, 0.9, 1.0, 1.08), scale=4.0, speckle=0.06)
    glow = palette(rgb)
    r = rng_for(key)
    if behavior.startswith("rune_") or behavior in ("ward", "altar"):
        for i in range(2, 14):
            cv.set(i, 2, glow[1]); cv.set(2, i, glow[1]); cv.set(i, 13, glow[1]); cv.set(13, i, glow[1])
        shapes = {
            "rune_fire": [(7, 4, 4, 11), (8, 4, 11, 11), (5, 9, 10, 9)],
            "rune_ice": [(7, 4, 7, 11), (4, 7, 11, 7), (5, 5, 10, 10), (10, 5, 5, 10)],
            "rune_lightning": [(9, 3, 6, 8), (6, 8, 10, 8), (10, 8, 6, 12)],
            "rune_nature": [(7, 12, 7, 4), (7, 7, 4, 5), (7, 9, 10, 6)],
            "rune_void": [(4, 4, 11, 11), (11, 4, 4, 11), (7, 3, 7, 12)],
            "ward": [(4, 4, 11, 4), (4, 4, 7, 12), (11, 4, 8, 12)],
            "altar": [(4, 10, 11, 10), (5, 7, 10, 7), (7, 4, 8, 4)],
        }[behavior]
        for s in shapes:
            cv.line(*s, glow[4])
    elif behavior in ("mana_restore", "mana_regen_zone", "heal"):
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, y - 7.5)
                if d < 5.5:
                    cv.set(x, y, glow[4 if d < 1.8 else 3 if d < 3.5 else 2])
        if behavior == "mana_regen_zone":
            for i in range(16):
                cv.set(i, 7, glow[3]); cv.set(7, i, glow[3])
    else:
        for _ in range(10):
            x, y = r.randint(0, 15), r.randint(0, 15)
            cv.set(x, y, glow[4])
            cv.set(x + 1, y, glow[2])
    return cv


# ---------------------------------------------------------------------------
# Objets — armes (diagonales)
# ---------------------------------------------------------------------------
def _diag(t):
    return 1 + t, 14 - t


def _blade(cv, pal, t0, t1, width):
    for t in range(t0, t1 + 1):
        x, y = _diag(t)
        tip = t == t1
        cv.set(x, y, pal[3])
        if not tip:
            if width >= 2:
                cv.set(x + 1, y, pal[2])
            if width >= 3:
                cv.set(x, y - 1, pal[4])
            if width >= 4:
                cv.set(x + 1, y + 1, pal[1])
                cv.set(x - 1, y - 1, pal[3])


def _handle(cv, t0, t1, hp=HANDLE):
    for t in range(t0, t1 + 1):
        x, y = _diag(t)
        cv.set(x, y, hp[2] if t % 2 else hp[1])


def _guard(cv, t, half, gp):
    x, y = _diag(t)
    for k in range(-half, half + 1):
        cv.set(x + k, y + k, gp[2] if k else gp[3])


def item_weapon(category, rgb, accent=None, key=""):
    pal = palette(rgb)
    acc = palette(accent) if accent else GOLD
    if accent:
        acc = [acc[1], acc[2], acc[3], acc[4]]
    cv = Canvas()
    if category == "sword":
        _handle(cv, 0, 2)
        _blade(cv, pal, 4, 13, 3)
        _guard(cv, 3, 2, acc)
    elif category == "greatsword":
        _handle(cv, 0, 2)
        _blade(cv, pal, 4, 13, 4)
        _guard(cv, 3, 3, acc)
    elif category == "dagger":
        _handle(cv, 2, 4)
        _blade(cv, pal, 6, 11, 2)
        _guard(cv, 5, 1, acc)
    elif category == "battleaxe":
        _handle(cv, 0, 12)
        for y in range(16):
            for x in range(16):
                for (cx, cy) in ((8.0, 4.0), (12.0, 8.0)):
                    d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                    if d < 3.4 and abs((x + y) - 16) >= 1:
                        cv.set(x, y, pal[4] if d > 2.6 else pal[3] if d > 1.6 else pal[2])
        _handle(cv, 9, 12)
    elif category == "spear":
        _handle(cv, 0, 10)
        for t in range(11, 14):
            x, y = _diag(t)
            cv.set(x, y, pal[3])
            if t < 13:
                cv.set(x + 1, y, pal[2]); cv.set(x, y - 1, pal[4])
        _guard(cv, 10, 1, acc)
    elif category == "staff":
        _handle(cv, 0, 10)
        _guard(cv, 10, 1, acc)
        cv.disc(12.5, 3.5, 2.6, pal)
    elif category == "wand":
        for t in range(2, 10):
            x, y = _diag(t)
            cv.set(x, y, HANDLE[2] if t % 2 else HANDLE[3])
        cv.disc(11.5, 4.5, 1.8, pal)
        cv.set(11, 4, pal[4])
    cv.outline(pal[0])
    return cv


def item_bow(rgb, pull=-1, accent=None):
    """pull : -1 repos, 0..2 états de tension."""
    pal = palette(rgb)
    cv = Canvas()
    cx, cy, R = 2.0, 13.0, 11.0
    pts = []
    for i in range(0, 91, 3):
        a = math.radians(i)
        x, y = int(round(cx + R * math.cos(a))), int(round(cy - R * math.sin(a)))
        pts.append((x, y))
    for i, (x, y) in enumerate(pts):
        mid = abs(i - len(pts) / 2) < 3
        cv.set(x, y, GOLD[2] if mid else (pal[3] if i % 2 else pal[2]))
    end1, end2 = pts[0], pts[-1]
    string = (0xE8, 0xE8, 0xE8, 255)
    if pull < 0:
        cv.line(end1[0] - 1, end1[1], end2[0], end2[1] + 1, string)
    else:
        d = [1.5, 2.5, 3.5][pull]
        px, py = int(round(7.5 - d)), int(round(7.5 + d))
        cv.line(end1[0] - 1, end1[1], px, py, string)
        cv.line(end2[0], end2[1] + 1, px, py, string)
        # flèche
        cv.line(px, py, px + 7, py - 7, (0x9A, 0x70, 0x45, 255))
        cv.set(px + 7, py - 7, (0xC0, 0xC0, 0xC8, 255))
        cv.set(px + 6, py - 7, (0xC0, 0xC0, 0xC8, 255))
        cv.set(px + 7, py - 6, (0xC0, 0xC0, 0xC8, 255))
    cv.outline(pal[0])
    return cv


def item_crossbow(rgb, state="standby"):
    """state : standby, pulling_0..2, arrow, firework."""
    pal = palette(rgb)
    cv = Canvas()
    # fût
    for t in range(1, 11):
        x, y = _diag(t)
        cv.set(x, y, HANDLE[2] if t % 2 else HANDLE[1])
        if t < 4:
            cv.set(x + 1, y, HANDLE[1])
    # branches perpendiculaires
    limbs = [(6, 3), (7, 3), (8, 4), (9, 5), (10, 6), (11, 7), (12, 8), (12, 9)]
    for i, (x, y) in enumerate(limbs):
        cv.set(x, y, pal[3] if i % 2 else pal[2])
    cv.set(9, 5, GOLD[2])
    string = (0xE8, 0xE8, 0xE8, 255)
    e1, e2 = (6, 4), (11, 9)
    pull = {"standby": 0, "pulling_0": 1, "pulling_1": 2, "pulling_2": 3, "arrow": 3, "firework": 3}[state]
    px, py = 8 - pull, 7 + pull
    cv.line(e1[0], e1[1], px, py, string)
    cv.line(e2[0], e2[1], px, py, string)
    if state == "arrow":
        cv.line(px + 1, py - 1, 13, 2, (0xC0, 0xC0, 0xC8, 255))
    elif state == "firework":
        cv.line(px + 1, py - 1, 12, 3, (0xD0, 0x30, 0x30, 255))
        cv.set(13, 2, (0xFF, 0xE0, 0x40, 255))
    cv.outline(pal[0])
    return cv


# ---------------------------------------------------------------------------
# Objets — symétriques (ASCII + miroir)
# ---------------------------------------------------------------------------
SYM = {
    "helmet": [
        "........", "........", "...ooooo", "..o34444", ".o233333", ".o222222", ".o21oooo", ".o21o...",
        ".o1o....", "..o.....", "........", "........", "........", "........", "........", "........"],
    "chestplate": [
        "........", ".ooo..oo", "o343oo34", "o233o233", "o2223222", ".oo23222", "...o2322", "...o2222",
        "...o2322", "...o2222", "...o1222", "...o1111", "....oooo", "........", "........", "........"],
    "leggings": [
        "........", "........", "..oooooo", "..o34444", "..o22222", "..o23222", "..o232oo", "..o22o..",
        "..o23o..", "..o22o..", "..o23o..", "..o12o..", "..o11o..", "..oooo..", "........", "........"],
    "boots": [
        "........", "........", "........", "........", "........", "........", "..oooo..", "..o34o..",
        "..o23o..", "..o22o..", ".o222o..", "o3222o..", "o1111o..", "oooooo..", "........", "........"],
    "gem": [
        "........", "........", "........", ".....ooo", "....o444", "...o3443", "..o33333", "..o23322",
        "...o2222", "....o122", ".....o11", "......oo", "........", "........", "........", "........"],
    "ring": [
        "........", "........", "......oo", ".....o44", "....o4ga", "....oGgg", ".....ooo", "....oo..",
        "...o3o..", "..o23o..", "..o2o...", "..o2o...", "..o12o..", "...o12oo", "....oooo", "........"],
    "orb": [
        "........", "........", ".....ooo", "....o444", "...o3443", "..o33333", "..o33332", "..o23322",
        "..o22222", "..o12222", "...o1122", "....o111", ".....ooo", "........", "........", "........"],
    "amulet": [
        "........", "....hhhh", "...h....", "..h.....", "..h.....", "...h....", "....h...", ".....ooo",
        "....oggg", "...og433", "...og333", "...og232", "....og22", ".....ooo", "........", "........"],
    "crown": [
        "........", "........", "........", "..o...oo", ".o3o.o43", ".o23oo33", ".o222322", ".oggggga",
        ".oGgGgGg", ".oggggga", "..oooooo", "........", "........", "........", "........", "........"],
    "heart": [
        "........", "........", "..ooo...", ".o443o.o", "o34333oo", "o3333332", "o2333322", "o2223222",
        ".o222222", "..o12222", "...o1122", "....o112", ".....o11", "......oo", "........", "........"],
    "eye": [
        "........", "........", "........", "........", "....oooo", "..oo4444", ".o443333", "o4333o11",
        "o333o1oo", "o333o1oo", "o4333o11", ".o443333", "..oo4444", "....oooo", "........", "........"],
    "hourglass": [
        "........", "..oooooo", "..oggggg", "...o4444", "...o3333", "....o333", ".....o33", "......o3",
        "......o2", ".....o.2", "....o..2", "...o..22", "...o2222", "..oggggg", "..oooooo", "........"],
    "lantern": [
        "........", "......oo", ".....o.g", "....oggg", "...ogggg", "...og444", "...og434", "...og343",
        "...og434", "...og343", "...og333", "...ogggg", "....oooo", "........", "........", "........"],
    "compass": [
        "........", "........", ".....ooo", "...oogggg", "..oggg333", "..og33333", ".og333344", ".og333443",
        ".og334433", ".og344333", "..og33333", "..oggg333", "...oogggg", ".....ooo", "........", "........"],
    "star": [
        "........", ".......o", "......o4", "......o4", ".....o43", "oooooo33", "o4433333", ".o333333",
        "..o33322", "...o3222", "..o32222", "..o222oo", ".o22oo..", ".o2o....", ".oo.....", "........"],
    "belt": [
        "........", "........", "........", "........", "........", "oooooooo", "33333333", "2222oggg",
        "2222ogaa", "1111oggg", "oooooooo", "........", "........", "........", "........", "........"],
    "emblem": [
        "........", "..oooooo", ".o444444", ".o433333", ".o333g33", ".o33ggg3", ".o33gGgg", ".o233ggg",
        "..o2333g", "..o22333", "...o2233", "....o222", ".....o11", "......oo", "........", "........"],
    "tome": [
        "........", "........", "..oooooo", ".o333333", ".o3gggg3", ".o3g44g3", ".o3gggg3", ".o333333",
        ".o333333", ".o333333", ".o222222", ".oHHHHHH", ".ooooooo", "........", "........", "........"],
    "chalice": [
        "........", "........", ".ooooooo", ".ogggggg", ".og44444", "..og3333", "..og3333", "...og333",
        "....oggg", ".....oog", "......og", "......og", "....oogg", "...ogggg", "...ooooo", "........"],
    "bell": [
        "........", "......oo", ".....ogg", "....oooo", "...o4444", "...o3333", "..o33333", "..o23333",
        "..o22333", ".o222222", ".ogggggg", ".ooooooo", "......og", "......oo", "........", "........"],
    "cloak": [
        "........", "....oooo", "...ogggg", "..o34444", "..o33333", ".o233333", ".o223333", ".o222333",
        "o2222233", "o2222223", "o1222222", "o1122222", "o1111222", "oooooooo", "........", "........"],
    "cog": [
        "........", ".....ooo", "..oo.o33", ".o33o333", ".o333333", "..o333oo", "oo333o..", "o3333o..",
        "o3333o..", "oo333o..", "..o333oo", ".o222222", ".o22o222", "..oo.o22", ".....ooo", "........"],
    "seed": [
        "........", "......ga", ".....gga", "......g.", ".....ooo", "....o444", "...o3443", "...o3333",
        "...o2333", "...o2233", "....o222", ".....ooo", "........", "........", "........", "........"],
    "ingot": [
        "........", "........", "........", "........", "........", "....oooo", "...o4444", "..o34333",
        ".o333333", ".o233333", ".o222222", ".o111111", "..oooooo", "........", "........", "........"],
    "essence": [
        "........", "......oo", ".....oHH", "......oo", ".....o..", "....o.33", "...o3344", "...o3333",
        "..o33332", "..o23322", "..o22222", "...o2222", "....oooo", "........", "........", "........"],
}
# Les modèles ci-dessus font 8 colonnes (moitié gauche) ; certains en ont 9 par erreur, on les tronque.
SYM = {k: [r[:8] for r in v] for k, v in SYM.items()}


def item_symmetric(shape, rgb, accent=None, trim=None):
    pal = palette(rgb)
    g = GOLD if trim is None else trim
    cmap = {".": None, "o": pal[0], "1": pal[1], "2": pal[2], "3": pal[3], "4": pal[4],
            "g": g[1], "G": g[3], "a": palette(accent)[4] if accent else g[2],
            "h": HANDLE[1], "H": HANDLE[2]}
    cv = Canvas()
    cv.from_ascii(SYM[shape], cmap, mirror=True)
    return cv


def item_shard(rgb, key):
    pal = palette(rgb)
    cv = Canvas()
    r = rng_for(key)
    pts = [(8, 2), (12, 6), (10, 13), (5, 12), (4, 6)]
    pts = [(x + r.randint(-1, 1), y + r.randint(-1, 1)) for x, y in pts]
    # remplissage par test de point dans le polygone
    for y in range(16):
        for x in range(16):
            inside = False
            j = len(pts) - 1
            for i in range(len(pts)):
                xi, yi = pts[i]
                xj, yj = pts[j]
                if (yi > y) != (yj > y) and x < (xj - xi) * (y - yi) / (yj - yi + 1e-9) + xi:
                    inside = not inside
                j = i
            if inside:
                cv.set(x, y, pal[3] if x + y < 15 else pal[2])
    cv.line(pts[0][0], pts[0][1] + 1, pts[3][0] + 1, pts[3][1] - 1, pal[4])
    cv.outline(pal[0])
    return cv


def item_raw(rgb, key):
    pal = palette(rgb)
    cv = Canvas()
    n = value_noise(key, 16, 16, 2.0)
    for y in range(16):
        for x in range(16):
            d = math.hypot((x - 7.5) / 5.5, (y - 8) / 4.8)
            if d + (n[y][x] - 0.5) * 0.5 < 1.0:
                cv.set(x, y, pal[1 + min(3, int(n[y][x] * 4))])
    cv.outline(pal[0])
    return cv


def item_gem(rgb):
    return item_symmetric("gem", rgb)


def item_ingot(rgb):
    return item_symmetric("ingot", rgb)


def item_feather(rgb):
    pal = palette(rgb)
    cv = Canvas()
    x0, y0, x1, y1 = 2.0, 13.5, 13.5, 2.0
    L = math.hypot(x1 - x0, y1 - y0)
    ux, uy = (x1 - x0) / L, (y1 - y0) / L
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5 - x0, y + 0.5 - y0
            t = (px * ux + py * uy) / L
            side = px * uy - py * ux
            if t < 0 or t > 1:
                continue
            w = 0.0 if t < 0.22 else 2.6 * math.sin(math.pi * (t - 0.18) / 0.84)
            if abs(side) < 0.6:
                cv.set(x, y, HANDLE[3] if t < 0.22 else pal[1])
            elif abs(side) < w:
                cv.set(x, y, pal[4] if side < 0 and abs(side) < 1.5 else pal[3] if side < 0 else pal[2])
    cv.outline(pal[0])
    return cv


def item_icon(shape, rgb, key):
    if shape == "feather":
        return item_feather(rgb)
    if shape == "shard":
        return item_shard(rgb, key)
    if shape in SYM:
        return item_symmetric(shape, rgb)
    raise ValueError(shape)


def item_spell_bolt(rgb):
    cv = Canvas()
    pal = palette(rgb)
    cv.disc(8, 8, 4.5, pal)
    for i in range(4, 12):
        cv.set(i, 8, pal[4]); cv.set(8, i, pal[4])
    return cv


# ---------------------------------------------------------------------------
# Armures portées (64x32)
# ---------------------------------------------------------------------------
def _fill_rect(cv, key, rgb, x0, y0, x1, y1, border=True):
    n = value_noise(key + f"{x0},{y0}", 64, 32, 4.0)
    pal = palette(rgb)
    for y in range(y0, y1):
        for x in range(x0, x1):
            v = n[y][x]
            c = pal[3] if v > 0.62 else pal[2] if v > 0.38 else pal[1]
            if border and (y == y0 or x == x0):
                c = pal[4]
            if border and (y == y1 - 1 or x == x1 - 1):
                c = pal[0] if (x1 - x0) > 2 else c
            cv.set(x, y, c)


def armor_layer(rgb, key, leggings=False, trim=GOLD):
    cv = Canvas(64, 32)
    if not leggings:
        # tête (8x8x8) : UV 0..32 x 0..16
        _fill_rect(cv, key, rgb, 8, 0, 24, 8)        # dessus / dessous
        _fill_rect(cv, key, rgb, 0, 8, 32, 16)       # faces latérales
        # visière sombre sur la face avant
        for x in range(9, 15):
            cv.set(x, 12, shade(rgb, 0.25))
            cv.set(x, 13, shade(rgb, 0.25))
        # corps (8x12x4) : UV 16..40 x 16..32
        _fill_rect(cv, key, rgb, 20, 16, 36, 20)
        _fill_rect(cv, key, rgb, 16, 20, 40, 32)
        for y in range(22, 30):
            cv.set(23, y, trim[2]); cv.set(24, y, trim[2])
        # bras (4x12x4) : UV 40..56 x 16..32
        _fill_rect(cv, key, rgb, 44, 16, 52, 20)
        _fill_rect(cv, key, rgb, 40, 20, 56, 32)
        # bottes : bas des jambes 0..16 x 27..32
        _fill_rect(cv, key, rgb, 8, 16, 12, 20)
        _fill_rect(cv, key, rgb, 0, 26, 16, 32)
    else:
        # ceinture + jambes
        _fill_rect(cv, key, rgb, 20, 16, 36, 20)
        _fill_rect(cv, key, rgb, 16, 26, 40, 32)
        for x in range(16, 40):
            cv.set(x, 26, trim[2])
        _fill_rect(cv, key, rgb, 4, 16, 12, 20)
        _fill_rect(cv, key, rgb, 0, 20, 16, 32)
    return cv


def pack_icon():
    cv = Canvas(64, 64)
    for y in range(64):
        for x in range(64):
            d = math.hypot(x - 31.5, y - 31.5)
            a = math.atan2(y - 31.5, x - 31.5)
            if d < 30:
                ring = int((a + math.pi) / (2 * math.pi) * 7) % 7
                from_c = [(0x8F, 0xD8, 0xFF), (0xE0, 0x20, 0x30), (0x30, 0xE0, 0xD0), (0xFF, 0x70, 0x10),
                          (0xC0, 0xF0, 0xFF), (0x9A, 0x60, 0xFF), (0xC8, 0x60, 0xFF)][ring]
                if d > 24:
                    cv.set(x, y, shade(from_c, 0.9))
                elif d > 21:
                    cv.set(x, y, (0x20, 0x18, 0x30, 255))
                else:
                    f = 1.3 - d / 21 * 0.9
                    cv.set(x, y, shade((0x40, 0x20, 0x70), f))
    return cv
