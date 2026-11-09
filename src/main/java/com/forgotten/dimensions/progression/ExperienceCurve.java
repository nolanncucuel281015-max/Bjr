package com.forgotten.dimensions.progression;

/** Courbe d'expérience du niveau 1 au niveau 100 (logique pure, testée unitairement). */
public final class ExperienceCurve {
    private ExperienceCurve() {}

    /** Expérience nécessaire pour passer du niveau {@code level} au suivant. */
    public static int xpToNext(int level) {
        if (level >= PlayerProgress.MAX_LEVEL) return Integer.MAX_VALUE;
        return (int) Math.round(40 + 12.0 * level + 0.6 * level * level);
    }

    /** Points de compétence cumulés obtenus en atteignant un niveau : 1 par niveau + 1 bonus tous les 10 niveaux. */
    public static int totalSkillPoints(int level) {
        return (level - 1) + (level / 10);
    }

    /** Expérience totale cumulée nécessaire pour atteindre un niveau depuis le niveau 1. */
    public static long totalXpForLevel(int level) {
        long total = 0;
        for (int l = 1; l < level; l++) total += xpToNext(l);
        return total;
    }

    /**
     * Ajoute de l'expérience et renvoie {niveau, xp restante}. Le niveau est plafonné à 100,
     * l'expérience excédentaire au niveau maximum est conservée à 0.
     */
    public static int[] addXp(int level, int xp, long amount) {
        long current = (long) xp + Math.max(0, amount);
        while (level < PlayerProgress.MAX_LEVEL && current >= xpToNext(level)) {
            current -= xpToNext(level);
            level++;
        }
        if (level >= PlayerProgress.MAX_LEVEL) {
            return new int[]{PlayerProgress.MAX_LEVEL, 0};
        }
        return new int[]{level, (int) current};
    }
}
