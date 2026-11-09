package com.forgotten.dimensions;

import com.forgotten.dimensions.event.CombatEvents;
import com.forgotten.dimensions.magic.ManaManager;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.SpellCooldowns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormulasTest {
    @Test
    void maxManaFormula() {
        assertEquals(100F, ManaManager.maxMana(1, 0, false, 0), 1e-4);
        assertEquals(100F + 5F * 9 + 60F + 50F, ManaManager.maxMana(10, 3, false, 50), 1e-4);
        assertEquals((100F + 5F * 99) * 1.25F, ManaManager.maxMana(100, 0, true, 0), 1e-3);
    }

    @Test
    void spellPowerScalesWithProgression() {
        float base = SpellCaster.powerMultiplier(1, 0, 0, false, 1.0);
        assertEquals(1.0F, base, 1e-5);
        float strong = SpellCaster.powerMultiplier(50, 3, 10, true, 1.0);
        assertTrue(strong > 2.5F && strong < 3.5F, "puissance obtenue : " + strong);
        assertEquals(0.5F, SpellCaster.powerMultiplier(1, 0, 0, false, 0.5), 1e-5);
    }

    @Test
    void killXp() {
        assertEquals(1, CombatEvents.killXp(10F, false, false));
        assertEquals(4 + 7, CombatEvents.killXp(20F, true, false));
        assertEquals(200 + 300, CombatEvents.killXp(300F, true, true));
    }

    @Test
    void cooldownsPurgeExpiredEntries() {
        SpellCooldowns cds = SpellCooldowns.EMPTY.with("a", 100, 0).with("b", 50, 0);
        assertEquals(100, cds.remaining("a", 0));
        SpellCooldowns later = cds.with("c", 300, 60);
        assertFalse(later.until().containsKey("b"), "entrée expirée conservée");
        assertTrue(later.until().containsKey("a"));
        assertEquals(0, later.remaining("b", 60));
    }
}
