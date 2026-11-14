#!/usr/bin/env python3
"""Planches « Aller dans une dimension » : noyau d'Architecte, pierre d'Architecte, portail et clé de chaque dimension.
Sortie : docs/crafts/portail_<dimension>.png."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import weapon_crafts as W  # noqa: E402

NAMES = {"celestial": "le Royaume Céleste", "corrupted": "la Dimension Corrompue", "ocean": "l'Océan Oublié",
         "chaos": "les Terres du Chaos", "eternal": "le Monde Éternel", "depths": "les Profondeurs"}

if __name__ == "__main__":
    os.makedirs(W.OUT, exist_ok=True)
    for dim, nom in NAMES.items():
        p = W.sheet(f"Aller dans {nom}", "1. Noyau  2. Pierre d'Architecte  3. Portail (à poser)  4. Clé : clic droit sur le portail",
                    ["architect_core", "architect_stone", "dimensional_gate", f"{dim}_key"], cols=2)
        p.save(os.path.join(W.OUT, f"portail_{dim}.png"))
    print("planches portail écrites")
