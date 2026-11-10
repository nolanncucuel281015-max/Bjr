"""Textures des effets 3D des sorts (256×128 pour un modèle UV 64×32 : 4× plus détaillées).

Les textures sont blanches : le jeu les teinte par école de magie (couleur ARGB par pièce) et gère
la transparence en fondu. Le cercle runique est peint sur les faces dessus/dessous du plan
(UV 16..32 et 32..48 × 0..16, soit 64..128 et 128..192 × 0..64 en pixels).
"""
import math
import os
import random

from PIL import Image, ImageDraw


def _noise_img(seed, alpha_lo, alpha_hi, bright_lo):
    r = random.Random(seed)
    w, h = 256, 128
    img = Image.new("RGBA", (w, h))
    px = img.load()
    # bruit lissé par blocs + stries d'énergie
    grid = [[r.random() for _ in range(17)] for _ in range(9)]
    for y in range(h):
        for x in range(w):
            gx, gy = x / 16, y / 16
            ix, iy = int(gx), int(gy)
            fx, fy = gx - ix, gy - iy
            a = grid[iy % 9][ix % 17] * (1 - fx) + grid[iy % 9][(ix + 1) % 17] * fx
            b = grid[(iy + 1) % 9][ix % 17] * (1 - fx) + grid[(iy + 1) % 9][(ix + 1) % 17] * fx
            n = a * (1 - fy) + b * fy
            streak = 0.5 + 0.5 * math.sin((x + y * 0.6) * 0.22 + n * 6)
            v = 0.55 * n + 0.45 * streak
            bright = int(255 * (bright_lo + (1 - bright_lo) * v))
            alpha = int(alpha_lo + (alpha_hi - alpha_lo) * v)
            px[x, y] = (bright, bright, bright, alpha)
    return img


def _rune_face(size=64):
    img = Image.new("RGBA", (size * 4, size * 4), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    S = size * 4
    c = S / 2
    white = (255, 255, 255, 255)
    soft = (255, 255, 255, 150)
    for rr, wdt, col in ((0.48, 10, white), (0.43, 4, soft), (0.30, 8, white), (0.26, 3, soft), (0.12, 6, white)):
        R = rr * S
        d.ellipse([c - R, c - R, c + R, c + R], outline=col, width=wdt)
    # hexagramme
    for k in (0, 1):
        pts = [(c + 0.30 * S * math.cos(math.radians(90 + 120 * i + 60 * k)),
                c + 0.30 * S * math.sin(math.radians(90 + 120 * i + 60 * k))) for i in range(3)]
        d.polygon(pts, outline=white, width=6)
    # glyphes entre les deux anneaux extérieurs
    rng = random.Random(7)
    for i in range(24):
        a = math.radians(i * 15)
        gx, gy = c + 0.365 * S * math.cos(a), c + 0.365 * S * math.sin(a)
        for _ in range(3):
            x0 = gx + rng.uniform(-9, 9)
            y0 = gy + rng.uniform(-9, 9)
            x1 = gx + rng.uniform(-9, 9)
            y1 = gy + rng.uniform(-9, 9)
            d.line([x0, y0, x1, y1], fill=white, width=4)
    # rayons
    for i in range(12):
        a = math.radians(i * 30 + 15)
        d.line([c + 0.12 * S * math.cos(a), c + 0.12 * S * math.sin(a), c + 0.26 * S * math.cos(a), c + 0.26 * S * math.sin(a)],
               fill=soft, width=3)
    return img.resize((size, size), Image.LANCZOS)


def _rock():
    r = random.Random(42)
    img = _noise_img(5, 255, 255, 0.0).convert("RGB")
    px = img.load()
    for y in range(128):
        for x in range(256):
            v = px[x, y][0] / 255
            px[x, y] = (int(40 + 50 * v), int(30 + 35 * v), int(30 + 30 * v))
    d = ImageDraw.Draw(img)
    for _ in range(40):
        x, y = r.randint(0, 255), r.randint(0, 127)
        for _ in range(8):
            nx, ny = x + r.randint(-10, 10), y + r.randint(-10, 10)
            d.line([x, y, nx, ny], fill=(255, r.randint(90, 160), 20), width=2)
            x, y = nx, ny
    return img.convert("RGBA")


def write(save_dir):
    os.makedirs(save_dir, exist_ok=True)
    _noise_img(11, 90, 210, 0.75).save(os.path.join(save_dir, "energy.png"))
    _noise_img(23, 225, 255, 0.88).save(os.path.join(save_dir, "core.png"))
    rune = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    face = _rune_face(64)
    rune.paste(face, (64, 0))
    rune.paste(face, (128, 0))
    rune.save(os.path.join(save_dir, "rune.png"))
    _rock().save(os.path.join(save_dir, "rock.png"))
    return ["energy", "core", "rune", "rock"]
