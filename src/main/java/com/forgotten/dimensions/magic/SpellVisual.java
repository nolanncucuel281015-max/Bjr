package com.forgotten.dimensions.magic;

/** Types d'effets 3D des sorts (rendus côté client par SpellEffectRenderer). */
public enum SpellVisual {
    /** Cercle runique tournant sous le lanceur, anneaux qui s'élèvent. */
    CAST_CIRCLE,
    /** Onde de choc : anneau d'énergie qui s'étend, disque runique, colonnes. */
    NOVA,
    /** Sphère d'énergie qui gonfle, éclats radiaux, anneau horizontal. */
    EXPLOSION,
    /** Colonne d'énergie jaillissant du sol, hélice ascendante. */
    PILLAR,
    /** Rocher enflammé qui tombe du ciel avec une traînée, cercle d'alerte au sol. */
    METEOR,
    /** Pics (glace, épines) qui sortent du sol en cercle. */
    SPIKES,
    /** Zone persistante : cercle runique au sol, muraille d'énergie, lueurs ascendantes. */
    ZONE,
    /** Rayon : faisceau lumineux et hélice du lanceur à la cible. */
    BEAM,
    /** Dôme protecteur : trois anneaux tournants autour du lanceur. */
    BARRIER,
    /** Trou noir : cœur sombre, disque d'accrétion tournoyant. */
    SINGULARITY,
    /** Soin : spirale de lueurs et colonne douce. */
    HEAL,
    /** Téléportation : vortex vertical. */
    BLINK,
    /** Invocation : grand cercle et colonne de lumière. */
    SUMMON
}
