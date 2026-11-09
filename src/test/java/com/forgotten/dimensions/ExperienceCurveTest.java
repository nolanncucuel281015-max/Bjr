package com.forgotten.dimensions;

import com.forgotten.dimensions.progression.ExperienceCurve;
import com.forgotten.dimensions.progression.PlayerProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceCurveTest {
    @Test
    void xpToNextIsStrictlyIncreasing() {
        for (int l = 1; l < PlayerProgress.MAX_LEVEL - 1; l++) {
            assertTrue(ExperienceCurve.xpToNext(l + 1) > ExperienceCurve.xpToNext(l), "niveau " + l);
        }
        assertEquals(Integer.MAX_VALUE, ExperienceCurve.xpToNext(PlayerProgress.MAX_LEVEL));
    }

    @Test
    void addXpLevelsUpAndCarriesRemainder() {
        int need = ExperienceCurve.xpToNext(1);
        int[] r = ExperienceCurve.addXp(1, 0, need + 5);
        assertEquals(2, r[0]);
        assertEquals(5, r[1]);
    }

    @Test
    void addXpCapsAtMaxLevel() {
        int[] r = ExperienceCurve.addXp(1, 0, Long.MAX_VALUE / 4);
        assertEquals(PlayerProgress.MAX_LEVEL, r[0]);
        assertEquals(0, r[1]);
    }

    @Test
    void totalXpMatchesSumOfSteps() {
        long total = ExperienceCurve.totalXpForLevel(PlayerProgress.MAX_LEVEL);
        int[] r = ExperienceCurve.addXp(1, 0, total);
        assertEquals(PlayerProgress.MAX_LEVEL, r[0]);
        int[] almost = ExperienceCurve.addXp(1, 0, total - 1);
        assertEquals(PlayerProgress.MAX_LEVEL - 1, almost[0]);
    }

    @Test
    void skillPointsGrant1PerLevelPlusBonusEvery10() {
        assertEquals(0, ExperienceCurve.totalSkillPoints(1));
        assertEquals(9 + 1, ExperienceCurve.totalSkillPoints(10));
        assertEquals(99 + 10, ExperienceCurve.totalSkillPoints(100));
    }
}
