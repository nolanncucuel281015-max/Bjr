package com.forgotten.dimensions;

import com.forgotten.dimensions.content.BlockDef;
import com.forgotten.dimensions.content.BlockKind;
import com.forgotten.dimensions.content.WeaponDef;
import com.forgotten.dimensions.magic.Spell;
import com.forgotten.dimensions.magic.SpellSchool;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.registry.generated.ContentTables;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Vérifie les quantités exigées par le cahier des charges et la cohérence des tables. */
class ContentTablesTest {
    @Test
    void requiredQuantities() {
        List<BlockDef> blocks = ContentTables.blocks();
        assertTrue(blocks.stream().filter(b -> b.kind() == BlockKind.ORE).count() >= 50, "au moins 50 minerais");
        assertEquals(15, blocks.stream().filter(b -> b.kind() == BlockKind.TECH).count(), "15 blocs technologiques");
        assertEquals(15, blocks.stream().filter(b -> b.kind() == BlockKind.MAGIC).count(), "15 blocs magiques");
        assertTrue(blocks.stream().filter(b -> b.kind() == BlockKind.CUBE && b.luminance() > 0).count() >= 20, "au moins 20 blocs lumineux");
        assertTrue(blocks.stream().filter(b -> b.kind() == BlockKind.STAIRS).count() > 0);
        assertTrue(ContentTables.weapons().size() >= 100, "au moins 100 armes");
        assertEquals(30, ContentTables.armorSets().size(), "30 armures complètes");
        assertEquals(30, ContentTables.artifacts().size(), "30 artefacts");
    }

    @Test
    void idsAreUnique() {
        Set<String> ids = new HashSet<>();
        ContentTables.blocks().forEach(b -> assertTrue(ids.add(b.id()), "doublon : " + b.id()));
        ContentTables.items().forEach(i -> assertTrue(ids.add(i.id()), "doublon : " + i.id()));
        ContentTables.weapons().forEach(w -> assertTrue(ids.add(w.id()), "doublon : " + w.id()));
        ContentTables.artifacts().forEach(a -> assertTrue(ids.add(a.id()), "doublon : " + a.id()));
        ContentTables.armorSets().forEach(a -> {
            for (String p : new String[]{"helmet", "chestplate", "leggings", "boots"}) assertTrue(ids.add(a.id() + "_" + p));
        });
    }

    @Test
    void shapesReferenceEarlierBases() {
        Set<String> seen = new HashSet<>();
        for (BlockDef b : ContentTables.blocks()) {
            if (b.kind() == BlockKind.STAIRS || b.kind() == BlockKind.SLAB || b.kind() == BlockKind.WALL) {
                assertTrue(seen.contains(b.base()), b.id() + " référence " + b.base() + " avant son enregistrement");
            }
            seen.add(b.id());
        }
    }

    @Test
    void weaponsReferenceKnownTiers() {
        Set<String> tiers = new HashSet<>();
        ContentTables.tiers().forEach(t -> tiers.add(t.id()));
        for (WeaponDef w : ContentTables.weapons()) assertTrue(tiers.contains(w.tier()), w.id());
        assertEquals(10, ContentTables.weapons().stream().filter(WeaponDef::isLegendary).count());
    }

    @Test
    void fiftySpellsTenPerSchool() {
        assertEquals(50, Spells.all().size());
        for (SpellSchool s : SpellSchool.values()) assertEquals(10, Spells.ofSchool(s).size(), s.name());
        for (Spell s : Spells.all().values()) {
            assertTrue(s.mana() > 0 && s.cooldown() > 0 && s.level() >= 1 && s.level() <= 100, s.id());
        }
    }

    @Test
    void progressCodecRoundTrip() {
        PlayerProgress p = new PlayerProgress(42, 1234, 7, Map.of("vigor", 3, "strength", 1), "mage", "meteor",
                List.of("minecraft:plains"), Map.of("fire", 25));
        JsonElement json = PlayerProgress.CODEC.encodeStart(JsonOps.INSTANCE, p).getOrThrow();
        PlayerProgress back = PlayerProgress.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(p, back);
        assertEquals(2, back.schoolMastery("fire"));
    }
}
