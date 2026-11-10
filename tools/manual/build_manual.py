#!/usr/bin/env python3
"""Génère le manuel illustré de Les Dimensions Oubliées.

Entrées : les tables de contenu (tools/gen), les textures du mod, les traductions françaises
générées et les captures d'écran prises dans le vrai jeu par le test client
(docs/manuel/img/shots). Sorties : docs/manuel/manuel.html, puis le PDF via export_pdf.mjs.

Toutes les données (sorts, compétences, armes, armures, artefacts, créatures, recettes) sont lues
dans les mêmes sources que le mod : le manuel ne peut pas diverger du jeu.
"""
import html
import json
import math
import os
import re
import sys

from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(ROOT, "tools", "gen"))

import content as C  # noqa: E402
import creatures as CR  # noqa: E402
import lang_ui as L  # noqa: E402

NS = "forgotten_dimensions"
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", NS)
DATA = os.path.join(ROOT, "src", "main", "resources", "data", NS)
OUT = os.path.join(ROOT, "docs", "manuel")
IMG = os.path.join(OUT, "img")
SHOTS = os.path.join(IMG, "shots")
os.makedirs(os.path.join(IMG, "icons"), exist_ok=True)
os.makedirs(os.path.join(IMG, "recipes"), exist_ok=True)
os.makedirs(os.path.join(IMG, "gen"), exist_ok=True)

FR = {k: v.replace("%%", "%") for k, v in json.load(open(os.path.join(ASSETS, "lang", "fr_fr.json"), encoding="utf-8")).items()}
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not os.path.exists(FONT):
    FONT = None

VANILLA_FR = {
    "amethyst_block": "Bloc d'améthyste", "amethyst_shard": "Éclat d'améthyste", "bell": "Cloche", "book": "Livre",
    "bookshelf": "Bibliothèque", "clock": "Horloge", "compass": "Boussole", "copper_ingot": "Lingot de cuivre",
    "crying_obsidian": "Obsidienne pleureuse", "diamond": "Diamant", "enchanted_book": "Livre enchanté",
    "enchanting_table": "Table d'enchant.", "ender_eye": "Œil de l'Ender", "feather": "Plume",
    "fermented_spider_eye": "Œil d'araignée fermenté", "glistering_melon_slice": "Pastèque scintillante",
    "glow_berries": "Baies lumineuses", "glow_ink_sac": "Poche d'encre lumineuse", "glowstone": "Pierre lumineuse",
    "gold_ingot": "Lingot d'or", "golden_apple": "Pomme dorée", "heart_of_the_sea": "Cœur de la mer",
    "iron_ingot": "Lingot de fer", "iron_sword": "Épée en fer", "lantern": "Lanterne", "lapis_lazuli": "Lapis-lazuli",
    "leather": "Cuir", "lightning_rod": "Paratonnerre", "magma_cream": "Crème de magma", "nautilus_shell": "Coquille de nautile",
    "nether_star": "Étoile du Nether", "obsidian": "Obsidienne", "phantom_membrane": "Membrane de phantom",
    "piston": "Piston", "rabbit_foot": "Patte de lapin", "recovery_compass": "Boussole de récupération",
    "redstone": "Redstone", "redstone_block": "Bloc de redstone", "shield": "Bouclier", "spyglass": "Longue-vue",
    "stick": "Bâton", "stone": "Roche", "stone_bricks": "Pierres taillées", "string": "Ficelle",
    "sweet_berries": "Baies sucrées", "totem_of_undying": "Totem d'immortalité", "tripwire_hook": "Crochet",
}


def esc(s):
    return html.escape(str(s))


def name(item_id):
    """Nom français d'un objet du mod ou vanilla."""
    if item_id.startswith("minecraft:"):
        return VANILLA_FR.get(item_id[10:], item_id[10:].replace("_", " "))
    item_id = item_id.replace(NS + ":", "")
    return FR.get(f"item.{NS}.{item_id}") or FR.get(f"block.{NS}.{item_id}") or item_id


# ---------------------------------------------------------------------------- icônes
def _texture_for(item_id):
    for p in (f"textures/item/{item_id}.png", f"textures/block/{item_id}.png", f"textures/item/{item_id}_standby.png"):
        path = os.path.join(ASSETS, p)
        if os.path.exists(path):
            return path, p.startswith("textures/block")
    blockstate = os.path.join(ASSETS, "blockstates", item_id + ".json")
    if os.path.exists(blockstate):
        txt = open(blockstate).read()
        m = re.search(NS + r":block/([a-z0-9_]+)", txt)
        if m:
            model = os.path.join(ASSETS, "models", "block", m.group(1) + ".json")
            if os.path.exists(model):
                tm = re.search(NS + r":block/([a-z0-9_]+)", open(model).read())
                if tm:
                    return os.path.join(ASSETS, "textures", "block", tm.group(1) + ".png"), True
    return None, False


def iso_block(tex_path, size=64):
    """Rendu isométrique simple (dessus + deux faces) d'une texture de bloc."""
    t = Image.open(tex_path).convert("RGBA").resize((16, 16), Image.NEAREST)
    top_path = tex_path.replace(".png", "_top.png")
    top = Image.open(top_path).convert("RGBA") if os.path.exists(top_path) else t
    s = size
    out = Image.new("RGBA", (s, s), (0, 0, 0, 0))

    def face(img, coeffs, shade):
        f = img.resize((s, s), Image.NEAREST)
        px = f.load()
        for y in range(s):
            for x in range(s):
                r, g, b, a = px[x, y]
                px[x, y] = (int(r * shade), int(g * shade), int(b * shade), a)
        return f.transform((s, s), Image.AFFINE, coeffs, Image.NEAREST)

    h = s / 4
    # Coordonnées : dessus (losange), gauche, droite.
    top_img = face(top, _affine((s / 2, 0), (s, h), (0, h)), 1.0)
    left_img = face(t, _affine((0, h), (s / 2, 2 * h), (0, 3 * h)), 0.78)
    right_img = face(t, _affine((s / 2, 2 * h), (s, h), (s / 2, 4 * h)), 0.62)
    for im in (left_img, right_img, top_img):
        out.alpha_composite(im)
    return out


def _affine(p_origin, p_x, p_y):
    """Coefficients inverses pour Image.transform : la texture (0..s) est envoyée sur le parallélogramme."""
    s = 64.0
    ox, oy = p_origin
    ax, ay = (p_x[0] - ox) / s, (p_x[1] - oy) / s
    bx, by = (p_y[0] - ox) / s, (p_y[1] - oy) / s
    det = ax * by - ay * bx
    ia, ib, ic, id_ = by / det, -bx / det, -ay / det, ax / det
    return (ia, ib, -(ia * ox + ib * oy), ic, id_, -(ic * ox + id_ * oy))


ICON_CACHE = {}


def icon(item_id, size=64):
    """Chemin (relatif au manuel) de l'icône d'un objet du mod, ou None."""
    item_id = item_id.replace(NS + ":", "")
    if item_id in ICON_CACHE:
        return ICON_CACHE[item_id]
    tex, is_block = _texture_for(item_id)
    rel = None
    if tex:
        dst = os.path.join(IMG, "icons", item_id + ".png")
        if is_block:
            iso_block(tex, size).save(dst)
        else:
            Image.open(tex).convert("RGBA").resize((size, size), Image.NEAREST).save(dst)
        rel = f"img/icons/{item_id}.png"
    ICON_CACHE[item_id] = rel
    return rel


def icon_html(item_id, cls="ic"):
    p = icon(item_id)
    return f'<img class="{cls}" src="{p}" alt="">' if p else ""


# ---------------------------------------------------------------------------- recettes
def _font(sz):
    return ImageFont.truetype(FONT, sz) if FONT else ImageFont.load_default()


def recipe_image(rid):
    path = os.path.join(DATA, "recipe", rid + ".json")
    r = json.load(open(path))
    S, PAD = 72, 14
    W, H = PAD * 2 + S * 3 + 110 + S, PAD * 2 + S * 3
    img = Image.new("RGBA", (W, H), (198, 198, 198, 255))
    d = ImageDraw.Draw(img)
    grid = [[None] * 3 for _ in range(3)]
    if r["type"].endswith("crafting_shaped"):
        for y, row in enumerate(r["pattern"]):
            for x, ch in enumerate(row):
                if ch != " ":
                    grid[y][x] = r["key"][ch]
    else:
        for i, ing in enumerate(r["ingredients"]):
            grid[i // 3][i % 3] = ing

    def slot(x, y, item):
        d.rectangle([x, y, x + S - 4, y + S - 4], fill=(139, 139, 139, 255))
        d.line([x, y, x + S - 4, y], fill=(55, 55, 55, 255), width=3)
        d.line([x, y, x, y + S - 4], fill=(55, 55, 55, 255), width=3)
        d.line([x, y + S - 4, x + S - 4, y + S - 4], fill=(255, 255, 255, 255), width=3)
        d.line([x + S - 4, y, x + S - 4, y + S - 4], fill=(255, 255, 255, 255), width=3)
        if not item:
            return
        p = icon(item) if not item.startswith("minecraft:") else None
        if p:
            ic = Image.open(os.path.join(OUT, p)).convert("RGBA").resize((S - 16, S - 16), Image.NEAREST)
            img.alpha_composite(ic, (x + 6, y + 6))
        else:
            label = name(item)
            words, lines, cur = label.split(), [], ""
            for w in words:
                if len(cur + " " + w) > 9 and cur:
                    lines.append(cur)
                    cur = w
                else:
                    cur = (cur + " " + w).strip()
            lines.append(cur)
            f = _font(11)
            d.rectangle([x + 4, y + 4, x + S - 8, y + S - 8], fill=(225, 214, 184, 255))
            for i, ln in enumerate(lines[:4]):
                tw = d.textlength(ln, font=f)
                d.text((x + (S - 4 - tw) / 2, y + 10 + i * 13), ln, fill=(70, 50, 20, 255), font=f)

    for y in range(3):
        for x in range(3):
            slot(PAD + x * S, PAD + y * S, grid[y][x])
    ax, ay = PAD + S * 3 + 20, PAD + S * 1.5
    d.polygon([(ax, ay - 12), (ax + 50, ay - 12), (ax + 50, ay - 26), (ax + 76, ay), (ax + 50, ay + 26), (ax + 50, ay + 12), (ax, ay + 12)],
              fill=(120, 120, 120, 255))
    rx, ry = PAD + S * 3 + 104, PAD + S
    slot(rx, ry, r["result"]["id"])
    cnt = r["result"].get("count", 1)
    if cnt > 1:
        f = _font(20)
        d.text((rx + S - 30, ry + S - 32), str(cnt), fill=(255, 255, 255, 255), font=f, stroke_width=2, stroke_fill=(40, 40, 40, 255))
    dst = os.path.join(IMG, "recipes", rid + ".png")
    img.save(dst)
    return f"img/recipes/{rid}.png"


def recipe_html(rid, caption=None):
    if not os.path.exists(os.path.join(DATA, "recipe", rid + ".json")):
        return ""
    cap = caption or name(json.load(open(os.path.join(DATA, "recipe", rid + ".json")))["result"]["id"])
    return f'<figure class="recipe"><img src="{recipe_image(rid)}" alt=""><figcaption>{esc(cap)}</figcaption></figure>'


# ---------------------------------------------------------------------------- données Java
def parse_spells():
    src = open(os.path.join(ROOT, "src/main/java/com/forgotten/dimensions/magic/Spells.java"), encoding="utf-8").read()
    out = []
    for m in re.finditer(r'add\("(\w+)", (\w+), (\w+), (\d+), ([\d.]+F?), (\d+), ([\d.]+F?), ([\d.]+F?), (\d+)(?:, "(\w+)")?\);', src):
        sid, school, kind, lvl, mana, cd, power, radius, dur, skill = m.groups()
        out.append(dict(id=sid, school=school, kind=kind, level=int(lvl), mana=float(mana.rstrip("F")),
                        cooldown=int(cd) / 20, power=float(power.rstrip("F")), skill=skill))
    return out


def parse_skills():
    src = open(os.path.join(ROOT, "src/main/java/com/forgotten/dimensions/progression/Skills.java"), encoding="utf-8").read()
    out = []
    for m in re.finditer(r'add\("(\w+)", SkillTree\.(\w+), (\d+), (\d+), "(\w*)"', src):
        out.append(dict(id=m.group(1), tree=m.group(2), ranks=int(m.group(3)), level=int(m.group(4)), prereq=m.group(5)))
    return out


SPELLS = parse_spells()
SKILLS = parse_skills()
SCHOOL_FR = {"FIRE": "Feu", "ICE": "Glace", "LIGHTNING": "Foudre", "NATURE": "Nature", "VOID": "Néant"}
SCHOOL_COLOR = {"FIRE": "#e8641e", "ICE": "#3aa6d8", "LIGHTNING": "#d8b31e", "NATURE": "#3fa34d", "VOID": "#8a42d8"}
TREE_FR = {"WARRIOR": "Guerrier", "MAGE": "Mage", "ARCHER": "Archer", "EXPLORER": "Explorateur", "SUMMONER": "Invocateur"}
RAR_FR = {"COMMON": "Commun", "UNCOMMON": "Peu commun", "RARE": "Rare", "EPIC": "Épique", "LEGENDARY": "Légendaire", "MYTHIC": "Mythique"}
RAR_COLOR = {"COMMON": "#6b6b6b", "UNCOMMON": "#2e9a3a", "RARE": "#1d8fb8", "EPIC": "#a23fc4", "LEGENDARY": "#d08a10", "MYTHIC": "#d23030"}
CAT_FR = {"PASSIVE": "Passive", "NEUTRAL": "Neutre", "HOSTILE": "Hostile", "ELITE": "Élite", "MINIBOSS": "Mini-boss"}
ABILITY_FR = {
    "BOLT_FIRE": "projectile de feu", "BOLT_ICE": "projectile de glace", "BOLT_LIGHTNING": "projectile de foudre",
    "BOLT_NATURE": "projectile de nature", "BOLT_VOID": "projectile du néant", "LEAP": "bond", "CHARGE": "charge",
    "SUMMON": "invoque des sbires", "AURA_SLOW": "aura de lenteur", "AURA_POISON": "aura de poison", "AURA_WITHER": "aura de flétrissement",
    "AURA_FATIGUE": "aura de fatigue", "HEAL": "se soigne", "TELEPORT": "téléportation", "LIGHTNING": "foudre",
    "FREEZE_BREATH": "souffle glacé", "FIRE_NOVA": "nova de feu", "ROOT": "racines", "HIT_POISON": "coups empoisonnés",
    "HIT_WITHER": "coups flétrissants", "HIT_FIRE": "coups enflammés", "HIT_FREEZE": "coups gelants", "HIT_BLIND": "coups aveuglants",
    "STALK": "traque invisible", "EXPLODE_ON_DEATH": "explose à sa mort"}
DIM_COLOR = {"celestial": "#4aa8d8", "corrupted": "#b82030", "ocean": "#1f8fa0", "chaos": "#d8561a", "eternal": "#6aa8c8",
             "depths": "#6a48b0", "void": "#7a38c0"}


def shot(name_, caption="", cls="shot"):
    path = os.path.join(SHOTS, name_ + ".png")
    if not os.path.exists(path):
        return f'<div class="missing">[capture manquante : {esc(name_)}]</div>'
    return f'<figure class="{cls}"><img src="img/shots/{name_}.png" alt=""><figcaption>{esc(caption)}</figcaption></figure>'


# ---------------------------------------------------------------------------- HUD annoté
def annotated_hud():
    """HUD agrandi ×3 avec repères numérotés dans une marge, reliés à chaque élément."""
    src = os.path.join(SHOTS, "world_hud.png")
    if not os.path.exists(src):
        return ""
    im = Image.open(src).convert("RGB")
    k = im.size[0] / 854.0
    crop = im.crop((0, 0, int(240 * k), int(96 * k))).resize((720, 288), Image.NEAREST)
    out = Image.new("RGB", (1010, 288), (251, 246, 234))
    out.paste(crop, (0, 0))
    d = ImageDraw.Draw(out)
    f = _font(24)
    # (n, y du repère, cible x, cible y) en coordonnées de l'image agrandie
    marks = [(1, 22, 150, 36), (2, 74, 600, 36), (3, 128, 690, 90), (4, 182, 690, 150), (5, 240, 260, 222)]
    for n, my, tx, ty in marks:
        d.line([(778, my + 18), (tx, ty)], fill=(184, 134, 43), width=4)
        d.ellipse([tx - 7, ty - 7, tx + 7, ty + 7], fill=(184, 134, 43))
        d.ellipse([778, my, 814, my + 36], fill=(184, 134, 43), outline=(90, 60, 15), width=3)
        d.text((790, my + 3), str(n), fill=(255, 255, 255), font=f)
    out.save(os.path.join(IMG, "gen", "hud_annotated.png"))
    return "img/gen/hud_annotated.png"


# ---------------------------------------------------------------------------- page de garde
def cover_image():
    src = os.path.join(SHOTS, "manual_dim_celestial_panorama.png")
    if not os.path.exists(src):
        return ""
    im = Image.open(src).convert("RGB")
    w, h = im.size
    im = im.crop((int(w * 0.1), 0, int(w * 0.9), h)).resize((1240, int(1240 * h / (w * 0.8))))
    im.save(os.path.join(IMG, "gen", "cover.jpg"), quality=90)
    return "img/gen/cover.jpg"


# ============================================================================ CONTENU
def sec(id_, title, body, color=None, kicker=None):
    style = f' style="--accent:{color}"' if color else ""
    k = f'<div class="kicker">{esc(kicker)}</div>' if kicker else ""
    return f'<section class="chapter" id="{id_}"{style}>{k}<h1>{esc(title)}</h1>{body}</section>'


def table(headers, rows, cls=""):
    th = "".join(f"<th>{h}</th>" for h in headers)
    trs = "".join("<tr>" + "".join(f"<td>{c}</td>" for c in r) + "</tr>" for r in rows)
    return f'<table class="{cls}"><thead><tr>{th}</tr></thead><tbody>{trs}</tbody></table>'


def rar(r):
    return f'<span class="rar" style="color:{RAR_COLOR[r]}">{RAR_FR[r]}</span>'


def callout(kind, title, body):
    return f'<div class="callout {kind}"><b>{esc(title)}</b><p>{body}</p></div>'


def build():
    parts = []
    cover = cover_image()
    parts.append(f'''<section class="cover"><img class="cover-img" src="{cover}" alt="">
<div class="cover-veil"></div>
<div class="cover-text"><img class="logo" src="{icon('celestial_key', 64) or ''}" alt="">
<div class="cover-kicker">Mod RPG pour Minecraft Java 1.21.11 · Fabric</div>
<div class="cover-title">Les Dimensions<br>Oubliées</div>
<div class="cover-sub">Manuel du joueur</div>
<div class="cover-meta">Sept dimensions · 60 créatures · 50 sorts · 100 armes · 30 armures · 30 artefacts</div></div></section>''')

    toc = [("histoire", "L'histoire des Anciens Architectes"), ("installation", "Installation"), ("premiers-pas", "Premiers pas"),
           ("voyage", "Voyager dans les dimensions"), ("dimensions", "Les sept dimensions"), ("progression", "Progression RPG"),
           ("magie", "La magie"), ("armes", "Armes"), ("armures", "Armures"), ("artefacts", "Artefacts"), ("blocs", "Blocs"),
           ("creatures", "Bestiaire et capacités"), ("commandes", "Commandes, touches et configuration"), ("faq", "Questions fréquentes")]
    parts.append('<section class="toc"><h1>Sommaire</h1><ol>' + "".join(
        f'<li><a href="#{a}">{esc(t)}</a></li>' for a, t in toc) + "</ol>"
        + '<p class="note">Ce manuel est généré à partir des données du mod : chaque valeur (dégâts, mana, niveau requis, '
          'recette…) est celle du jeu. Les captures ont été prises automatiquement dans Minecraft 1.21.11.</p></section>')

    # ------------------------------------------------------------------ histoire
    lore = "".join(f"<li>{esc(L.UI[f'lore.{NS}.terminal.{i}'][1])}</li>" for i in range(1, 8))
    parts.append(sec("histoire", "L'histoire des Anciens Architectes", f'''
<p class="lead">Il y a très longtemps, une civilisation de bâtisseurs, les <b>Anciens Architectes</b>, découvrit sept mondes
au-delà du ciel, de la mer et de la pierre. Elle les relia au nôtre par des portails. Puis une faim s'éveilla entre les
mondes : le <b>Dévoreur de Mondes</b>. Pour l'affamer, les Architectes brisèrent leurs portails. Ce fut le <b>Grand
Effondrement</b>. Les fragments de leur pouvoir se dispersèrent dans la terre, et les gardiens des sept mondes sombrèrent
dans la corruption.</p>
<p>Vous retrouverez cette histoire morceau par morceau, en lisant les <b>Terminaux runiques</b> (clic droit) dispersés
dans les ruines, en traversant les portails et en affrontant les gardiens.</p>
<div class="two"><div><h2>Les archives des Terminaux runiques</h2><ol class="lore">{lore}</ol></div>
<div><h2>Les sept chapitres</h2><ol class="chapters">
<li><b>La découverte</b> — trouver les premiers fragments anciens.</li>
<li><b>Les premiers fragments</b> — miner les fragments des sept mondes à la Surface.</li>
<li><b>Les dimensions</b> — rouvrir les portails avec les clés dimensionnelles.</li>
<li><b>Les anciens gardiens</b> — affronter les six gardiens corrompus <i>(à venir)</i>.</li>
<li><b>La vérité</b> — comprendre le Grand Effondrement <i>(à venir)</i>.</li>
<li><b>Le dernier portail</b> — forger la clé du Néant avec les six cœurs des gardiens.</li>
<li><b>Le Dévoreur de Mondes</b> — l'affrontement final dans le Néant <i>(à venir)</i>.</li>
</ol></div></div>
{callout("info", "État de la version 0.1.0", "Les chapitres 1 à 3 et 6 sont jouables. Les gardiens, les boss, les structures, "
         "les quêtes et les PNJ arrivent dans les prochaines versions.")}'''))

    # ------------------------------------------------------------------ installation
    parts.append(sec("installation", "Installation", '''
<div class="steps">
<div class="step"><span>1</span><div><b>Installer Java 21</b><p>Téléchargez le JDK 21 sur adoptium.net (Windows/macOS) ou
<code>sudo apt install openjdk-21-jdk</code> (Linux). Le launcher officiel embarque déjà Java pour jouer : cette étape ne
sert qu'à compiler le mod vous-même.</p></div></div>
<div class="step"><span>2</span><div><b>Installer Fabric</b><p>Lancez l'installateur de fabricmc.net, choisissez
<b>Minecraft 1.21.11</b> et <b>Fabric Loader 0.20.1</b> (ou plus récent), puis « Installer ».</p></div></div>
<div class="step"><span>3</span><div><b>Ajouter les mods</b><p>Placez <code>forgotten_dimensions-0.1.0.jar</code> et
<code>fabric-api-0.141.6+1.21.11.jar</code> dans le dossier <code>mods</code> :<br>Windows <code>%appdata%\\.minecraft\\mods</code> ·
macOS <code>~/Library/Application Support/minecraft/mods</code> · Linux <code>~/.minecraft/mods</code>.</p></div></div>
<div class="step"><span>4</span><div><b>Jouer</b><p>Choisissez le profil <b>fabric-loader-1.21.11</b> dans le launcher.</p></div></div>
</div>
<h2>Sur un serveur</h2>
<p>Installez le serveur Fabric 1.21.11 (fabricmc.net/use/server), lancez-le une fois, passez <code>eula=true</code> dans
<code>eula.txt</code>, puis copiez le mod <b>et</b> Fabric API dans <code>mods/</code>. Chaque joueur doit avoir les mêmes
fichiers. Le serveur fait autorité : sorts, mana, expérience et compétences sont validés par lui, et la progression est
sauvegardée avec chaque joueur.</p>'''))

    # ------------------------------------------------------------------ premiers pas
    hud = annotated_hud()
    ores_rows = [[icon_html("ancient_fragment_ore"), "Minerai de fragment ancien", "Y −48 à 48 (pierre et ardoise des abîmes)",
                  icon_html("ancient_fragment") + " Fragment ancien"]]
    for d in C.DIMENSIONS:
        ores_rows.append([icon_html(f"{d['id']}_fragment_ore"), esc(name(f"{d['id']}_fragment_ore")), "Y −64 à 16, plus rare",
                          icon_html(f"{d['id']}_fragment") + " " + esc(name(f"{d['id']}_fragment"))])
    parts.append(sec("premiers-pas", "Premiers pas", f'''
<div class="two"><div>
<h2>L'interface</h2>
{f'<img class="wide" src="{hud}" alt="">' if hud else ''}
<ol class="legend"><li><b>Niveau RPG</b> (1 à 100), distinct de l'expérience de Minecraft.</li>
<li><b>Points de compétence</b> à dépenser (touche <kbd>K</kbd>).</li>
<li><b>Barre d'expérience</b> vers le niveau suivant.</li>
<li><b>Mana</b> actuel / maximum, régénéré en continu.</li>
<li><b>Sort actif</b> et son temps de recharge, avec le rappel des commandes : <b>clic droit avec un bâton ou une
baguette</b> pour le lancer, <kbd>G</kbd> pour changer.</li></ol>
</div><div>
<h2>Les touches</h2>
{table(["Touche", "Action"], [["<b>Clic droit</b> (bâton ou baguette en main)", "Lancer le sort actif"], ["<kbd>G</kbd>", "Changer de sort (sort débloqué suivant)"],
                             ["<kbd>K</kbd>", "Arbre de compétences"], ["<kbd>B</kbd>", "Livre de magie"]])}
<p class="small">Modifiables dans Options → Commandes → « Dimensions Oubliées ».</p>
<h2>Gagner de l'expérience</h2>
<ul><li>Vaincre des créatures (plus si elles sont fortes, beaucoup pour les boss).</li>
<li>Miner des minerais (8 XP pour ceux du mod, 3 pour les autres).</li>
<li>Découvrir un nouveau biome (25 XP).</li><li>Lancer des sorts.</li></ul>
</div></div>
<h2>Chapitre 2 : les premiers fragments</h2>
<p>Tout commence sous vos pieds. Des fragments de la technologie des Architectes et des sept mondes se cachent dans la roche
de la Surface. Une pioche en fer suffit pour les miner.</p>
{table(["", "Minerai", "Où le trouver", "Donne"], ores_rows, "ores")}
<div class="recipes">{recipe_html("architect_core")}{recipe_html("architect_stone")}</div>
<p>Le <b>Noyau d'Architecte</b> (8 fragments anciens autour d'un diamant) est la pièce maîtresse du portail et des clés.</p>'''))

    # ------------------------------------------------------------------ voyage
    keys_rows = []
    for d in C.DIMENSIONS:
        how = ("6 cœurs des gardiens + fragment du néant + étoile du Nether" if d["id"] == "void"
               else f"8 × {esc(name(d['id'] + '_fragment'))} + noyau d'Architecte")
        keys_rows.append([icon_html(f"{d['id']}_key"), f"<b>{esc(name(d['id'] + '_key'))}</b>", how])
    parts.append(sec("voyage", "Voyager dans les dimensions", f'''
<p class="lead">Les portails des Architectes peuvent être rallumés. Il vous faut un <b>Portail dimensionnel</b> et la
<b>clé</b> de la dimension voulue. Voici le chemin complet, étape par étape.</p>
<div class="steps big">
<div class="step"><span>1</span><div><b>Miner des fragments</b><p>8 fragments anciens pour le noyau d'Architecte, et
8 fragments de la dimension visée (par exemple 8 fragments célestes pour le Royaume Céleste). Voir « Premiers pas ».</p></div></div>
<div class="step"><span>2</span><div><b>Fabriquer un noyau d'Architecte</b><p>Il en faut un pour le portail et un par clé.</p>
<div class="recipes">{recipe_html("architect_core")}</div></div></div>
<div class="step"><span>3</span><div><b>Fabriquer et poser le Portail dimensionnel</b><p>Pierre d'Architecte (8 pierres taillées autour
d'un fragment ancien), obsidienne pleureuse et noyau d'Architecte. Il se pose comme un bloc ordinaire, où vous voulez.</p>
<div class="recipes">{recipe_html("architect_stone")}{recipe_html("dimensional_gate")}</div></div></div>
<div class="step"><span>4</span><div><b>Fabriquer la clé de la dimension</b><p>8 fragments de la dimension autour d'un noyau
d'Architecte. La clé n'est <b>jamais consommée</b> : elle sert pour tous vos voyages.</p>
<div class="recipes">{recipe_html("celestial_key")}</div></div></div>
<div class="step"><span>5</span><div><b>Utiliser la clé sur le portail</b><p>Tenez la clé et faites un <b>clic droit</b> sur le
portail.</p>{shot("manual_portal_1_gate_and_key", "Le portail posé entre deux lampes anciennes, clé céleste en main.")}</div></div>
<div class="step"><span>6</span><div><b>Arrivée</b><p>Vous arrivez sur un sol sûr (jamais dans un mur, dans la lave ou dans
l'eau). Si aucun sol n'est trouvé, une plateforme est construite : à la surface de l'eau dans l'Océan Oublié, dans les airs
dans le Royaume Céleste. Le nom de la dimension s'affiche.</p>{shot("manual_portal_2_arrival_title", "Arrivée dans le Royaume Céleste.")}</div></div>
<div class="step"><span>7</span><div><b>Rentrer</b><p>Un <b>portail de retour</b> apparaît à côté du point d'arrivée. Clic droit
<b>à main vide</b> dessus : vous revenez exactement là où vous étiez parti.</p>
{shot("manual_portal_3_return_gate", "Le portail de retour, placé automatiquement à côté de l'arrivée.")}</div></div>
</div>
<h2>Les sept clés</h2>
{table(["", "Clé", "Recette"], keys_rows)}
{callout("warn", "Le Néant", "La clé du Néant exige les six <b>cœurs des gardiens</b>, lâchés par les gardiens dimensionnels (prochaine "
         "version). Le Néant est donc la dernière étape de l'aventure. En attendant, un opérateur peut utiliser "
         "<code>/fdimension tp void</code>.")}
{callout("info", "Tomber dans le vide", "Dans le Royaume Céleste et le Néant, tomber sous le monde ne tue pas : le portail ancien "
         "vous ramène à votre point de départ.")}
{callout("tip", "Raccourci pour les opérateurs", "<code>/fdimension tp &lt;dimension&gt;</code> fait le même voyage sans clé, et "
         "<code>/fdimension home</code> ramène au point de départ.")}'''))

    # ------------------------------------------------------------------ dimensions
    dim_pages = []
    for d in C.DIMENSIONS:
        did = d["id"]
        dim_json = json.load(open(os.path.join(DATA, "dimension", did + ".json")))
        dim_biomes = [FR[f"biome.{NS}." + e["biome"].split(":")[1]] for e in dim_json["generator"]["biome_source"]["biomes"]]
        mats = [m for m in C.MATERIALS if m["dim"] == did]
        mat_html = "".join(f'<div class="chip">{icon_html(m["id"] + "_ore")}{icon_html(C.mat_item(m["id"]))}<span>{esc(m["fr"])}</span></div>'
                           for m in mats)
        crs = [c for c in CR.CREATURES if c["dim"] == did]
        k = CR.DIM_SCALE[did]
        cr_rows = [[f"<b>{esc(c['fr'])}</b>", f'<span class="cat {c["cat"].lower()}">{CAT_FR[c["cat"]]}</span>',
                    str(round(c["hp"] * k)), str(round(c["dmg"] * (k ** 0.8), 1)),
                    esc(", ".join(ABILITY_FR[a] for a in c["abilities"]) or "—")] for c in crs]
        desc = {
            "celestial": "Des îles flottantes suspendues au-dessus d'un océan de nuages. Prairies turquoise, forêts de bois des nuages, "
                         "jardins d'étoiles et ruines suspendues. Attention au vide : sans le portail ancien, la chute serait mortelle.",
            "corrupted": "Une terre infectée par une énergie rouge. Ciel écarlate, mers de sang, forêts de racine pourpre et déserts de "
                         "cendres rouges. Les créatures y chassent en meute.",
            "ocean": "Un monde presque entièrement englouti : la mer monte jusqu'à la hauteur 150. Récifs lumineux, forêts de corail, "
                     "abysses et tranchées obscures. La respiration aquatique est offerte à l'arrivée ; une amulette abyssale ou "
                     "l'armure du plongeur de perles est vivement conseillée.",
            "chaos": "Un monde volcanique sous un plafond de roche, avec un océan de lave. Forêts de braises, colonnes de basalte, "
                     "déserts de magma et citadelle du chaos. Une résistance au feu est presque indispensable.",
            "eternal": "Un royaume gelé où le temps semble arrêté : glaciers, pics de glace, lacs gelés et forêts de pins de givre. "
                       "Le gel ralentit ; les armures de glace éternelle y rendent insensible.",
            "depths": "Un immense réseau de cavernes sous un plafond de roche : cavernes cristallines, forêts de champignons géants, "
                      "abysses souterrains et ruines enfouies. Les ombres prédatrices y chassent invisibles.",
            "void": "Une dimension cosmique hors de la réalité : une île centrale entourée d'îles stellaires dans le vide. Désert du "
                    "vide, océan astral, ruines dimensionnelles. C'est ici qu'attend le Dévoreur de Mondes.",
        }[did]
        dim_pages.append(f'''<section class="dim" style="--accent:{DIM_COLOR[did]}">
<div class="dim-head"><div class="kicker">Dimension {C.DIMENSIONS.index(d) + 1} sur 7</div><h1>{esc(d["fr"])}</h1></div>
{shot("manual_dim_" + did + "_panorama", "", "hero")}
<p class="lead">{esc(desc)}</p>
<div class="two"><div><h2>Biomes</h2><ul class="biomes">{"".join(f"<li>{esc(b)}</li>" for b in dim_biomes)}</ul>
<h2>Ressources</h2><div class="chips">{mat_html}</div>
<p class="small">Pierre de la dimension : <b>{esc(d["stone_fr"])}</b> (et sa variante profonde, qui contient les minerais profonds).</p></div>
<div><h2>Clé</h2><p>{icon_html(did + "_key")} <b>{esc(name(did + "_key"))}</b></p>
<h2>Musique et ambiance</h2><p class="small">Ciel, brouillard, lumière et particules propres à chaque biome.</p></div></div>
<h2>Créatures</h2>
{shot("manual_dim_" + did + "_creatures", "Les créatures de la dimension (présentées immobiles pour la capture).")}
{table(["Créature", "Catégorie", "Vie", "Dégâts", "Capacités"], cr_rows, "compact")}
</section>''')
    parts.append(sec("dimensions", "Les sept dimensions", '''
<p class="lead">Chaque dimension a son terrain, cinq biomes, ses trois matériaux, ses créatures et son ambiance. Elles sont
présentées dans l'ordre conseillé : la difficulté (vie et dégâts des créatures) augmente de l'une à l'autre.</p>'''
                     + table(["", "Dimension", "Difficulté", "Danger principal"],
                             [[icon_html(d["id"] + "_key"), f"<b>{esc(d['fr'])}</b>", "★" * (i + 1),
                               ["le vide", "les meutes", "la noyade", "la lave et le feu", "le gel", "l'obscurité", "le vide et les élites"][i]]
                              for i, d in enumerate(C.DIMENSIONS)])) + "".join(dim_pages))

    # ------------------------------------------------------------------ progression
    curve = []
    total = 0
    for lvl in range(1, 101):
        need = round(40 + 12 * lvl + 0.6 * lvl * lvl)
        if lvl in (1, 5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 99):
            curve.append([str(lvl), f"{need:,}".replace(",", " "), f"{total:,}".replace(",", " "), str((lvl - 1) + lvl // 10)])
        total += need
    trees_html = ""
    for tree in ("WARRIOR", "MAGE", "ARCHER", "EXPLORER", "SUMMONER"):
        rows = []
        for s in [x for x in SKILLS if x["tree"] == tree]:
            pre = FR.get(f"skill.{NS}.{s['prereq']}", "—") if s["prereq"] else "—"
            rows.append([f"<b>{esc(FR[f'skill.{NS}.' + s['id']])}</b>", str(s["ranks"]), str(s["level"]), esc(pre),
                         esc(FR[f"skill.{NS}.{s['id']}.desc"])])
        trees_html += f"<h3>{TREE_FR[tree]}</h3>" + table(["Compétence", "Rangs", "Niveau", "Prérequis", "Effet"], rows, "compact")
    advs = [[esc(FR[f"advancements.{NS}.{a[0]}.title"]), esc(FR[f"advancements.{NS}.{a[0]}.description"])] for a in L.ADVANCEMENTS]
    advs += [[esc(d["fr"]), esc(f"Rejoindre : {d['fr']}.")] for d in C.DIMENSIONS]
    parts.append(sec("progression", "Progression RPG", f'''
<p class="lead">Votre personnage progresse du niveau 1 au niveau 100. Chaque niveau donne <b>1 point de compétence</b>,
plus <b>1 point bonus tous les 10 niveaux</b> (109 points au total).</p>
<div class="two"><div>{table(["Niveau", "XP pour le suivant", "XP cumulée", "Points cumulés"], curve, "compact")}</div>
<div>{shot("skill_tree", "L'arbre de compétences (touche K).")}</div></div>
<h2>Les cinq arbres de compétences</h2>
<p>Chaque compétence se monte rang par rang (1 point par rang). Certaines exigent un niveau minimum et une compétence
préalable. Un opérateur peut rembourser tous les points avec <code>/fdimension skills reset</code>.</p>
{trees_html}
<h2>Spécialisations (niveau 20)</h2>
{table(["Spécialisation", "Effet"], [[TREE_FR[t], esc(L.UI[f"spec.{NS}.{t.lower()}.desc"][1])] for t in TREE_FR])}
<p class="small">Une seule spécialisation par personnage, choisie dans l'arbre de compétences.</p>
<h2>Succès</h2>{table(["Succès", "Comment l'obtenir"], advs, "compact")}'''))

    # ------------------------------------------------------------------ magie
    spell_tables = ""
    for school in ("FIRE", "ICE", "LIGHTNING", "NATURE", "VOID"):
        rows = []
        for s in [x for x in SPELLS if x["school"] == school]:
            req = f"Niv. {s['level']}" + (f" + {esc(FR[f'skill.{NS}.' + s['skill']])}" if s["skill"] else "")
            rows.append([f"<b>{esc(FR[f'spell.{NS}.' + s['id']])}</b>", req, f"{s['mana']:g}", f"{s['cooldown']:g} s",
                         f"{s['power']:g}" if s["power"] else "—", esc(FR[f"spell.{NS}.{s['id']}.desc"])])
        spell_tables += (f'<h3 style="color:{SCHOOL_COLOR[school]}">{SCHOOL_FR[school]}</h3>'
                         + table(["Sort", "Requis", "Mana", "Recharge", "Puissance", "Effet"], rows, "compact"))
    parts.append(sec("magie", "La magie", f'''
<p class="lead">Cinq écoles de magie, dix sorts chacune. Choisissez le sort actif dans le <b>Livre de magie</b> (touche
<kbd>B</kbd>) ou avec <kbd>G</kbd>, puis lancez-le d'un <b>clic droit avec un bâton magique ou une baguette</b> en main.
Chaque sort a ses effets en 3D : cercles runiques, ondes de choc, explosions, piliers d'énergie, météores, pics de glace,
rayons, dômes protecteurs, trou noir…</p>
<div class="two"><div>{shot("spell_book", "Le livre de magie : sorts débloqués et verrouillés.")}</div>
<div>{shot("manual_spell_meteor", "Météore : la roche enflammée tombe sur le cercle d'alerte.")}
{shot("manual_spell_meteor_impact", "Impact du météore.")}</div></div>
<h2>Bâtons et baguettes</h2>
<p>Le clic droit lance le <b>sort actif</b> (celui choisi avec <kbd>G</kbd>), quelle que soit son école. L'arme le renforce :</p>
{table(["Arme", "Bonus"], [["Bâton magique", "+15 % de puissance (+8 % par rareté), et +20 % de plus pour les sorts de son école"],
                           ["Baguette", "+0 à +25 % de puissance selon la rareté, -25 % de coût en mana, -20 % de recharge, +20 % pour son école"],
                           ["Sceptre de la Reine du Givre", "Bonus de bâton, +50 % pour les sorts de glace, ralentit fortement la cible visée"]])}
<p class="small">Une touche « Lancer le sort actif sans bâton » existe dans Options → Commandes ; elle n'est pas assignée par défaut.</p>
<h2>Galerie des effets</h2>
<div class="gallery">{shot("manual_spell_fireball", "Boule de feu (projectile 3D).")}{shot("manual_spell_fire_nova", "Nova de feu.")}
{shot("manual_spell_volcanic_eruption", "Explosion volcanique.")}{shot("manual_spell_frost_lance", "Lance de glace.")}
{shot("manual_spell_glacial_spike", "Pic glaciaire.")}{shot("manual_spell_blizzard", "Blizzard.")}
{shot("manual_spell_chain_lightning", "Chaîne électrique.")}{shot("manual_spell_lightning_strike", "Éclair.")}
{shot("manual_spell_thunder_nova", "Explosion électrique.")}{shot("manual_spell_ball_lightning", "Boule de foudre.")}
{shot("manual_spell_ice_armor", "Armure de glace (dôme).")}{shot("manual_spell_gaia_wrath", "Colère de Gaïa.")}
{shot("manual_spell_natural_heal", "Soin naturel.")}{shot("manual_spell_summon_golem", "Gardien de pierre.")}
{shot("manual_spell_dimensional_ray", "Rayon dimensionnel.")}{shot("manual_spell_singularity", "Singularité.")}
{shot("manual_spell_gravity_well", "Puits gravitationnel.")}{shot("manual_spell_void_bolt", "Trait du néant.")}</div>
<h2>Mana</h2>
<p>100 points au départ, +5 par niveau, +20 par rang de <i>Réserve arcanique</i>, +25 % avec la spécialisation Mage, plus les
artefacts (Prisme de mana +50, Couronne des Sept Mondes +100). La régénération augmente avec le niveau, le <i>Flux de mana</i>,
le Cœur des Anciens, l'ensemble de l'Architecte et les <b>Blocs de ligne tellurique</b> (×2 tant que vous êtes dessus).
Un <b>Bloc de cristal de mana</b> rend 30 points au clic droit (toutes les 30 secondes).</p>
<h2>Puissance et maîtrise</h2>
<p>La puissance d'un sort augmente de 1 % par niveau, de 10 % par rang de <i>Maîtrise des sorts</i>, de 10 % avec la
spécialisation Mage, et de 3 % par point de <b>maîtrise de l'école</b>. La maîtrise (de 0 à 10) grandit avec le nombre de sorts
lancés dans l'école ; l'<b>Autel des sorts</b> l'affiche au clic droit.</p>
<p>Effets des écoles : le <b>feu</b> enflamme, la <b>glace</b> ralentit et gèle, la <b>foudre</b> inflige +50 % aux cibles mouillées,
la <b>nature</b> empoisonne, le <b>néant</b> flétrit. Les sorts offensifs épargnent les autres joueurs, vos animaux et vos
invocations (réglable).</p>
{spell_tables}'''))

    # ------------------------------------------------------------------ armes
    cats = [("sword", "Épée", "équilibrée"), ("greatsword", "Grande épée", "lente, frappe aussi les ennemis autour"),
            ("dagger", "Dague", "très rapide, 15 % de chance de coup supplémentaire"), ("battleaxe", "Hache de combat", "désactive les boucliers, affaiblit"),
            ("spear", "Lance", "attaque chargée de Minecraft 1.21.11"), ("staff", "Bâton magique", "clic droit : projectile de l'école (10 mana)"),
            ("wand", "Baguette", "clic droit : petit projectile rapide (4 mana)"), ("bow", "Arc", "flèches renforcées selon le palier"),
            ("crossbow", "Arbalète", "carreaux renforcés selon le palier")]
    tier_rows = [[icon_html(f"{t['id']}_sword"), f"<b>{esc(C.MAT_BY_ID[t['id']]['fr'] if t['id'] in C.MAT_BY_ID else 'Primordial')}</b>"
                  + f" <span class='small'>({esc(name(C.TIER_ITEM[t['id']]))})</span>", rar(t["rarity"]),
                  f"+{t['dmg']:g}", str(t["dur"]), SCHOOL_FR[t["school"]]] for t in C.TIERS]
    grid = ""
    for cat, cfr, _ in cats:
        grid += f'<div class="catrow"><div class="catname">{cfr}</div>' + "".join(
            f'<div class="cell">{icon_html(f"{t["id"]}_{cat}")}<span>{esc(name(t["id"] + "_" + cat))}</span></div>' for t in C.TIERS) + "</div>"
    leg_rows = []
    leg_recipes = ""
    for lw in C.LEGENDARY_WEAPONS:
        leg_rows.append([icon_html(lw["id"]), f"<b>{esc(lw['fr'])}</b>", rar(lw["rarity"]), esc(lw["desc_fr"])])
    for rid in ("celestial_blade", "world_ender"):
        leg_recipes += recipe_html(rid)
    parts.append(sec("armes", "Armes", f'''
<p class="lead">100 armes : 9 catégories × 10 paliers de matériau, plus 10 armes légendaires. Toutes sont modélisées en 3D.</p>
{shot("manual_items_showcase", "Vitrine : armes 3D dans les cadres, armures en relief sur les supports.", "hero")}
<div class="two"><div>{shot("manual_items_frames_closeup", "Armes 3D (lance, dague, arc, épées).")}</div>
<div>{shot("manual_first_person_blade", "L'Épée Céleste en main.")}</div></div>
<h2>Catégories</h2>{table(["Catégorie", "Particularité"], [[c[1], c[2]] for c in cats])}
<h2>Paliers de matériau</h2>
<p>Le palier fixe la rareté, le bonus de dégâts, la durabilité et l'école de magie des bâtons et baguettes. Les recettes
suivent les formes classiques (épée : 2 matériaux + 1 bâton, etc.).</p>
{table(["", "Matériau", "Rareté", "Dégâts", "Durabilité", "École"], tier_rows)}
<div class="recipes">{recipe_html("astral_silver_sword")}{recipe_html("magmatite_staff")}{recipe_html("nebulite_bow")}</div>
<h2>Catalogue des 90 armes</h2><div class="catalog">{grid}</div>
<h2>Armes légendaires</h2>
<p>Recette : l'arme de base du même palier entourée de 4 essences et de 4 fragments de la dimension. La Fin des Mondes exige
l'alliage primordial, une étoile du Nether et un noyau d'Architecte.</p>
{table(["", "Arme", "Rareté", "Pouvoir"], leg_rows)}
<div class="recipes">{leg_recipes}</div>'''))

    # ------------------------------------------------------------------ armures
    bonus_fr = {k.split(".")[-1]: v[1] for k, v in L.UI.items() if k.startswith(f"armor_bonus.{NS}.")}
    arm_rows = []
    for a in C.ARMOR_SETS:
        icons = "".join(icon_html(f"{a['id']}_{p}", "ic sm") for p in ("helmet", "chestplate", "leggings", "boots"))
        arm_rows.append([icons, f"<b>Ensemble {esc(a['fr'])}</b>", rar(a["rarity"]),
                         str(sum(a["defense"])), f"{a['tough']:g}", esc(name(a["item"])),
                         esc(bonus_fr.get(a["bonus"], "—") if a["bonus"] != "none" else "—")])
    parts.append(sec("armures", "Armures", f'''
<p class="lead">30 ensembles complets (casque, plastron, jambières, bottes). Portées, les armures du mod ajoutent de vraies
pièces en relief : crête et visière, épaulières, plastron bombé, ceinture, genouillères et bottes renforcées.</p>
<div class="two"><div>{shot("manual_armor_stands_closeup", "Ensembles du Gardien des cieux et du Seigneur corrompu.")}</div>
<div>{shot("manual_armor_stands_closeup_2", "Ensembles de la Reine du givre et Primordial.")}</div></div>
<div class="two"><div>{shot("manual_player_primordial_armor", "Le joueur en armure primordiale.")}</div>
<div>{shot("manual_inventory_3d", "Inventaire : armes et armures en 3D.")}</div></div>
<h2>Bonus d'ensemble</h2><p>Les quatre pièces d'un même ensemble donnent un bonus permanent (tableau ci-dessous). La recette
suit les formes classiques avec le matériau indiqué.</p>
{table(["Pièces", "Ensemble", "Rareté", "Défense", "Robustesse", "Matériau", "Bonus complet"], arm_rows, "compact")}
<div class="recipes">{recipe_html("frost_queen_chestplate")}{recipe_html("primordial_helmet")}</div>'''))

    # ------------------------------------------------------------------ artefacts
    sys.path.insert(0, os.path.join(ROOT, "tools", "gen"))
    art_rows = []
    for a in C.ARTIFACTS:
        rec = json.load(open(os.path.join(DATA, "recipe", a["id"] + ".json")))
        k = rec["key"]
        ings = f"4 × {name(k['M'])}, 4 × {name(k['E'])}, 1 × {name(k['C'])}"
        art_rows.append([icon_html(a["id"]), f"<b>{esc(a['fr'])}</b>", rar(a["rarity"]), esc(a["desc_fr"]), esc(ings)])
    parts.append(sec("artefacts", "Artefacts", f'''
<p class="lead">30 objets légendaires. Leur effet <b>passif</b> agit tant qu'ils sont dans votre inventaire ; certains ont un
effet <b>actif</b> au clic droit, avec un temps de recharge. Un même artefact ne compte qu'une fois.</p>
<p>Recette commune : 4 matières (coins), 4 essences ou fragments (côtés) et un objet central.</p>
<div class="recipes">{recipe_html("crown_of_seven_worlds")}{recipe_html("ring_of_time")}{recipe_html("heart_of_the_ancients")}</div>
{table(["", "Artefact", "Rareté", "Effet", "Ingrédients"], art_rows, "compact")}'''))

    # ------------------------------------------------------------------ blocs
    def block_grid(ids):
        return '<div class="blocks">' + "".join(f'<div class="cell">{icon_html(i)}<span>{esc(name(i))}</span></div>' for i in ids) + "</div>"

    stones = [x for d in C.DIMENSIONS for x in (d["stone"], f"{d['stone']}_bricks", f"chiseled_{d['stone']}", f"{d['stone']}_tiles")]
    ores = [x for m in C.MATERIALS for x in (f"{m['id']}_ore",)][:21]
    lights = [b["id"] for b in C.LIGHT_BLOCKS]
    tech = [b["id"] for b in C.TECH_BLOCKS]
    magic = [b["id"] for b in C.MAGIC_BLOCKS]
    woods = ["cloudwood_log", "cloudwood_planks", "cloudwood_leaves", "crimsonroot_log", "crimsonroot_planks", "crimsonroot_leaves",
             "frostpine_log", "frostpine_planks", "frostpine_leaves", "sky_grass", "blight_grass", "frost_grass", "umbral_moss", "astral_moss",
             "dimensional_gate"]
    magic_rows = [[icon_html(b["id"]), f"<b>{esc(b['fr'])}</b>", esc({
        "mana_restore": "Clic droit : +30 mana (toutes les 30 s).", "particles": "Décoratif, particules magiques.",
        "rune_fire": "Marcher dessus : résistance au feu.", "rune_ice": "Marcher dessus : dégèle.", "rune_lightning": "Marcher dessus : vitesse II.",
        "rune_nature": "Marcher dessus : saturation.", "rune_void": "Marcher dessus : chute lente.",
        "mana_regen_zone": "Se tenir dessus double la régénération de mana.", "heal": "Marcher dessus : régénération.",
        "ward": "Marcher dessus repousse les monstres proches.", "altar": "Clic droit : affiche votre maîtrise des cinq écoles."}[b["behavior"]])]
                  for b in C.MAGIC_BLOCKS]
    parts.append(sec("blocs", "Blocs", f'''
<p class="lead">282 blocs : pierres des dimensions et leurs variantes (avec escaliers, dalles et murets), 58 minerais,
blocs de stockage, blocs lumineux, technologie des Architectes, blocs magiques, sols, bois et le portail.</p>
<h2>Pierres des dimensions</h2>{block_grid(stones)}
<h2>Minerais des dimensions</h2>{block_grid(ores)}
<h2>Blocs lumineux</h2>{block_grid(lights)}
<h2>Technologie des Architectes</h2>
<p>Le <b>Noyau énergétique d'Architecte</b> émet un signal de redstone permanent de 15. Le <b>Terminal runique</b> raconte une
des sept archives au clic droit.</p>{block_grid(tech)}
<h2>Blocs magiques</h2>{table(["", "Bloc", "Effet"], magic_rows, "compact")}
<h2>Sols, bois et portail</h2>{block_grid(woods)}'''))

    # ------------------------------------------------------------------ bestiaire
    gallery = "".join(shot(f"creatures_{i}", "") for i in range(6))
    ab_rows = [[esc(ABILITY_FR[a]), esc(", ".join(c["fr"] for c in CR.CREATURES if a in c["abilities"]))] for a in ABILITY_FR]
    parts.append(sec("creatures", "Bestiaire et capacités", f'''
<p class="lead">60 créatures, réparties en cinq catégories :</p>
<ul><li><b>Passives</b> fuient et ne se défendent pas.</li><li><b>Neutres</b> attaquent seulement si on les frappe.</li>
<li><b>Hostiles</b> chassent les joueurs ; certaines patrouillent ou gardent une zone, d'autres appellent des renforts ou
chassent en meute.</li><li><b>Élites</b> : plus grandes et plus résistantes, elles invoquent des sbires.</li>
<li><b>Mini-boss</b> : barre de vie à l'écran, sbires, et butin d'artefacts rares.</li></ul>
<p>Les hostiles ne craignent pas la lumière du soleil, seulement celle des <b>torches et lampes</b> : éclairez votre base.
Chaque créature a un œuf d'apparition (onglet créatif « Créatures »).</p>
<div class="gallery">{gallery}</div>
<h2>Capacités</h2>{table(["Capacité", "Créatures qui l'utilisent"], ab_rows, "compact")}'''))

    # ------------------------------------------------------------------ commandes
    cmds = [["/fdimension list", "Liste les sept dimensions."], ["/fdimension tp &lt;dimension&gt;", "Voyage dans une dimension."],
            ["/fdimension home", "Retour au point de départ."], ["/fdimension give &lt;objet&gt; [nombre]", "Donne un objet du mod."],
            ["/fdimension level set &lt;joueur&gt; &lt;niveau&gt;", "Fixe le niveau RPG."], ["/fdimension level get &lt;joueur&gt;", "Affiche niveau et XP."],
            ["/fdimension xp add &lt;joueur&gt; &lt;quantité&gt;", "Ajoute de l'expérience."], ["/fdimension skills reset &lt;joueur&gt;", "Rembourse les compétences."],
            ["/fdimension reload", "Recharge la configuration."]]
    conf = [["xpMultiplier", "1.0", "Expérience RPG gagnée"], ["biomeDiscoveryXp", "25", "XP par nouveau biome"],
            ["manaRegenMultiplier", "1.0", "Régénération de mana"], ["spellDamageMultiplier", "1.0", "Puissance des sorts"],
            ["spellCooldownMultiplier", "1.0", "Recharge des sorts"], ["particleMultiplier", "1.0", "Densité des particules (0 = aucune)"],
            ["summonDurationSeconds", "30", "Durée des invocations"], ["spellFriendlyFire", "false", "Sorts sur les autres joueurs"],
            ["rewardMultiplier", "1.0", "XP des créatures"], ["creatureSpawnMultiplier", "1.0", "Fréquence des créatures"],
            ["spellVisuals", "true", "Effets 3D des sorts (false = particules seules)"]]
    parts.append(sec("commandes", "Commandes, touches et configuration", f'''
<p>Les commandes sont réservées aux opérateurs (<code>/op &lt;pseudo&gt;</code>).</p>
{table(["Commande", "Effet"], [[f"<code>{c[0]}</code>", c[1]] for c in cmds])}
<h2>Configuration</h2><p>Fichier <code>config/forgotten_dimensions.json</code>, rechargeable avec <code>/fdimension reload</code>.</p>
{table(["Option", "Défaut", "Effet"], [[f"<code>{c[0]}</code>", c[1], c[2]] for c in conf])}'''))

    # ------------------------------------------------------------------ FAQ
    faq = [("Le jeu refuse de démarrer : « requires fabric-api ».", "Ajoutez Fabric API 0.141.6+1.21.11 dans le dossier mods."),
           ("« Incompatible mod set ».", "Vérifiez Minecraft 1.21.11 et Fabric Loader 0.20.1 ou plus récent."),
           ("Le portail ne fait rien.", "Utilisez une clé dimensionnelle en main (clic droit). À main vide, il ne sert qu'au retour depuis une dimension."),
           ("Je ne trouve pas de fragments.", "Ils sont plus rares que le fer : cherchez sous Y 16 dans les grottes. Le fragment ancien est le plus commun."),
           ("Comment aller dans le Néant ?", "Il faut les six cœurs des gardiens (prochaine version). Un opérateur peut utiliser /fdimension tp void."),
           ("Comment lancer un sort ?", "Tenez un bâton magique ou une baguette et faites un clic droit : le sort actif part. Changez de sort avec G."),
           ("Mes sorts ne partent pas.", "Tenez un bâton ou une baguette. Vérifiez aussi le niveau requis, le mana et la recharge : le message s'affiche au-dessus de la barre d'objets."),
           ("Trop de particules.", "Baissez particleMultiplier dans la configuration puis /fdimension reload."),
           ("Trop de monstres.", "Baissez creatureSpawnMultiplier, ou éclairez avec des torches (la lumière des blocs empêche leur apparition)."),
           ("Connexion refusée au serveur.", "Le client et le serveur doivent avoir exactement le même mod et la même Fabric API.")]
    parts.append(sec("faq", "Questions fréquentes", "".join(f'<div class="faq"><b>{esc(q)}</b><p>{esc(a)}</p></div>' for q, a in faq)
                     + '<p class="end">Bonne exploration, Architecte.</p>'))

    css = open(os.path.join(HERE, "manual.css"), encoding="utf-8").read()
    doc = f'''<!DOCTYPE html><html lang="fr"><head><meta charset="utf-8"><title>Les Dimensions Oubliées — Manuel du joueur</title>
<style>{css}</style></head><body>{"".join(parts)}</body></html>'''
    with open(os.path.join(OUT, "manuel.html"), "w", encoding="utf-8") as f:
        f.write(doc)
    print("manuel.html écrit :", len(doc) // 1024, "Ko,", len(os.listdir(os.path.join(IMG, "icons"))), "icônes,",
          len(os.listdir(os.path.join(IMG, "recipes"))), "recettes")


if __name__ == "__main__":
    build()
