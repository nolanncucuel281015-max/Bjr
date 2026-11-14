# Les Dimensions Oubliées — édition data pack

Version **sans mod** des Dimensions Oubliées pour **Minecraft Java 1.21.11** (vanilla, sans Fabric).
Elle fonctionne en solo, sur un serveur vanilla (Spigot/Paper compris) et sur Realms.

Le mod Fabric reste la version complète. Un data pack ne peut pas contenir de code Java : il n'y a
donc ni touches clavier, ni nouveaux blocs ou créatures, ni modèles 3D sur mesure. Tout est fait avec
les outils de Minecraft : fonctions, objets à composants, entités d'affichage, génération de monde.

## Installation

1. Téléchargez `LesDimensionsOubliees.zip`, sans le décompresser.
2. **Nouveau monde** : Créer un monde → Plus → Packs de données → ouvrir le dossier, glissez-y le zip,
   puis activez-le.
   **Monde existant** : copiez le zip dans `.minecraft/saves/<votre monde>/datapacks/`, puis
   **quittez et rouvrez le monde**. Un simple `/reload` ne charge pas les nouvelles dimensions.
3. **Serveur** : copiez le zip dans `world/datapacks/`, puis redémarrez le serveur.

À la première connexion, chaque joueur reçoit le **Grimoire des Anciens** (le guide) et un
**bâton de feu**.

## Ce que contient le pack

| Contenu | Détail |
|---|---|
| 7 dimensions | Royaume Céleste, Dimension Corrompue, Océan Oublié, Terres du Chaos, Monde Éternel, Les Profondeurs, Le Néant. Ce sont les mêmes biomes et reliefs que le mod, en blocs vanilla. |
| 7 clés | Voyage aller-retour : maintenez clic droit. Utilisée dans sa dimension, la clé vous ramène. |
| 5 bâtons | Feu, glace, foudre, nature, néant ; 4 sorts chacun, soit 20 sorts. |
| 14 armes | Une version légendaire de l'arme vanilla, enchantée. |
| 7 armures | 4 pièces chacune, avec garniture. L'ensemble complet donne un bonus permanent. |
| Progression | Niveaux 1 à 50, expérience, mana. +1 cœur tous les 5 niveaux, +1 dégât tous les 10. |
| Créatures à niveau | Le nom affiche le niveau, la vie dépend de la dimension, la barre de vie apparaît une fois touchée, les dégâts s'affichent. |
| Progrès | Un onglet « Les Dimensions Oubliées » : premier sort, invocation, niveaux, chaque dimension. |

## La magie

- **Clic droit** avec un bâton : lancer le sort.
- **Accroupi + clic droit** : passer au sort suivant.
- La barre au-dessus de l'inventaire affiche le niveau, l'XP, le mana et le sort choisi. Elle montre
  aussi la recharge (⌛) ou l'état prêt (✔).
- `/trigger oubliees.menu` ouvre la **fiche de personnage** dans le chat. On peut y cliquer sur un
  sort pour le choisir.

| École | Matériau du bâton | Sorts (niveau requis) |
|---|---|---|
| Feu | poudre de blaze | Trait de feu (1), Nova de feu (8), Colonne de flammes (15), Météore (25) |
| Glace | glace compactée | Éclat de glace (1), Nova de givre (8), Prison de glace (12), Sentinelle de givre (16) |
| Foudre | lingot de cuivre | Étincelle (1), Éclair (5), Chaîne d'éclairs (15), Tempête (30) |
| Nature | émeraude | Soin naturel (1), Trait d'épines (3), Invocation des loups (8), Gardien de pierre (30) |
| Néant | perle de l'Ender | Téléportation (1), Trait du néant (3), Puits gravitationnel (20), Singularité (40) |

**Recette d'un bâton** : deux bâtons en diagonale et le matériau de l'école au sommet.

**Effets 3D** (entités d'affichage) :
- un cercle runique coloré qui tourne sous le lanceur ;
- des sphères d'énergie qui grossissent à l'impact, des ondes de choc ;
- des colonnes de lumière, des pics de glace qui sortent du sol ;
- un météore de magma qui tombe du ciel ;
- un trou noir en rotation ;
- de vrais éclairs.

**Joueurs (PvP)** : les sorts touchent aussi les autres joueurs. Ils épargnent le lanceur et les joueurs en
créatif ou spectateur. Le réglage PvP du serveur (`pvp` dans `server.properties`) et le tir allié des
équipes (`/team modify <équipe> friendlyFire false`) s'appliquent.

**Invocations** :
- 3 loups spectraux ;
- un golem de fer « Gardien de pierre » ;
- un bonhomme de neige géant qui gèle les monstres proches.

Elles durent 45 secondes et ne laissent pas de butin.

## Les voyages

**Recette d'une clé** : un œil de l'Ender entouré de 4 matériaux de la dimension.

| Clé | Matériau |
|---|---|
| Royaume Céleste | éclat d'améthyste |
| Dimension Corrompue | champignon cramoisi |
| Océan Oublié | cristaux de prismarine |
| Terres du Chaos | poudre de blaze |
| Monde Éternel | glace bleue |
| Les Profondeurs | éclat d'écho |
| Le Néant | sans motif : étoile du Nether, souffle de dragon, éclat d'écho et œil de l'Ender |

**Arrivée** :
- Vous arrivez sur la terre ferme la plus proche, dans un rayon de 200 blocs.
- À défaut, une plateforme est créée au-dessus de la mer, ou sous le plafond pour le Chaos et les
  Profondeurs.
- Vous êtes protégé quelques secondes.

**Retour** : la même clé, utilisée dans la dimension, vous ramène à l'endroit exact du départ.

## Différences avec le mod

| Mod Fabric | Data pack |
|---|---|
| Touches G, V, J, K, B | Clic droit, accroupi + clic droit, `/trigger oubliees.menu` |
| 55 sorts, 7 invocations en 3D sur mesure | 20 sorts, invocations vanilla renommées et agrandies |
| 60 créatures et modèles originaux | Créatures vanilla réparties dans les mêmes biomes |
| Blocs, minerais et matériaux du mod | Blocs vanilla équivalents (calcite, prismarine, netherrack…) |
| HUD dessiné, fiche de personnage en écran | Barre d'action et fiche dans le chat |
| 100 armes, 30 armures, 30 artefacts | 14 armes, 7 armures |

## Pour les créateurs

Le pack est généré par `tools/datapack/build_datapack.py`, à partir des fichiers de génération du
mod (`tools/datapack/worldgen_map.py` fait la correspondance des blocs et des créatures) :

```
python3 tools/datapack/build_datapack.py
```

Espace de noms : `oubliees`. Une fonction de test donne tout le matériel :
`/function oubliees:debug/give_all`.
