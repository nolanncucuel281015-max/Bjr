package com.forgotten.dimensions.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.forgotten.dimensions.magic.SpellKind.*;
import static com.forgotten.dimensions.magic.SpellSchool.*;

/** Les 55 sorts : 48 sorts d'attaque et de soutien et 7 sorts d'invocation (kind SUMMON). */
public final class Spells {
    private static final Map<String, Spell> SPELLS = new LinkedHashMap<>();

    static {
        // FEU
        add("fire_bolt", FIRE, BOLT, 1, 10, 15, 5, 0, 0);
        add("ignite", FIRE, RAY, 3, 12, 40, 4, 0, 160);
        add("fireball", FIRE, BOLT, 6, 20, 40, 7, 2.5F, 0);
        add("flame_wall", FIRE, ZONE_TARGET, 10, 30, 200, 3, 3, 100);
        add("fire_nova", FIRE, NOVA, 15, 30, 160, 8, 5, 0);
        add("phoenix_blessing", FIRE, BUFF_SELF, 20, 35, 600, 0, 0, 400);
        add("volcanic_eruption", FIRE, AREA, 25, 40, 200, 12, 4, 0);
        add("meteor", FIRE, DELAYED_AREA, 40, 60, 400, 22, 5, 30);
        add("inferno", FIRE, ZONE_SELF, 50, 70, 600, 5, 6, 160);
        add("sun_strike", FIRE, DELAYED_AREA, 70, 100, 900, 40, 7, 40);
        add("summon_fire_elemental", FIRE, SUMMON, 12, 40, 900, 5, 0, 0);
        add("summon_phoenix", FIRE, SUMMON, 50, 90, 1800, 10, 0, 0);
        // GLACE
        add("ice_shard", ICE, BOLT, 1, 10, 15, 5, 0, 0);
        add("frost_ward", ICE, BUFF_SELF, 6, 20, 600, 0, 0, 600);
        add("frost_lance", ICE, RAY, 8, 18, 40, 8, 0, 60);
        add("frozen_prison", ICE, ROOT, 12, 25, 200, 4, 0, 80);
        add("frost_nova", ICE, NOVA, 15, 30, 160, 7, 5, 0);
        add("ice_armor", ICE, BUFF_SELF, 18, 30, 600, 0, 0, 400);
        add("snowstorm", ICE, ZONE_TARGET, 22, 40, 300, 3, 4, 120);
        add("glacial_spike", ICE, AREA, 30, 45, 200, 15, 3, 0);
        add("blizzard", ICE, ZONE_TARGET, 45, 70, 600, 5, 7, 160);
        add("absolute_zero", ICE, NOVA, 65, 100, 900, 25, 9, 0);
        add("summon_frost_sentinel", ICE, SUMMON, 16, 45, 1000, 9, 0, 0);
        // FOUDRE
        add("spark", LIGHTNING, BOLT, 1, 8, 12, 4, 0, 0);
        add("lightning_strike", LIGHTNING, AREA, 5, 20, 60, 10, 1.5F, 0);
        add("overcharge", LIGHTNING, BUFF_SELF, 10, 25, 600, 0, 0, 300);
        add("chain_lightning", LIGHTNING, CHAIN, 12, 30, 100, 8, 6, 4);
        add("static_dash", LIGHTNING, DASH, 16, 25, 120, 6, 10, 0);
        add("thunder_nova", LIGHTNING, NOVA, 20, 35, 160, 9, 5, 0);
        add("ball_lightning", LIGHTNING, BOLT, 25, 40, 160, 14, 3.5F, 0);
        add("storm", LIGHTNING, ZONE_TARGET, 30, 60, 500, 8, 6, 160);
        add("thunderclap", LIGHTNING, NOVA, 40, 50, 300, 14, 6, 0);
        add("tempest", LIGHTNING, ZONE_SELF, 60, 90, 900, 12, 9, 200);
        add("summon_storm_spirit", LIGHTNING, SUMMON, 20, 45, 1000, 6, 0, 0);
        // NATURE
        add("natural_heal", NATURE, HEAL, 1, 15, 100, 6, 0, 0);
        add("entangling_roots", NATURE, ROOT, 1, 15, 120, 2, 0, 60);
        add("thorn_bolt", NATURE, BOLT, 3, 10, 15, 5, 0, 0);
        add("summon_wolves", NATURE, SUMMON, 8, 40, 900, 4, 0, 0, "call_of_the_pack");
        add("poison_cloud", NATURE, ZONE_TARGET, 10, 30, 240, 2, 3.5F, 120);
        add("regrowth", NATURE, HEAL, 15, 35, 300, 10, 8, 0);
        add("barkskin", NATURE, BUFF_SELF, 20, 30, 600, 0, 0, 400);
        add("leaf_storm", NATURE, ZONE_SELF, 25, 50, 400, 4, 5, 120);
        add("summon_golem", NATURE, SUMMON, 30, 80, 1800, 12, 0, 0, "stone_guardian");
        add("gaia_wrath", NATURE, NOVA, 55, 90, 700, 20, 8, 0);
        // NÉANT
        add("blink", VOID, BLINK, 1, 15, 60, 0, 12, 0);
        add("void_bolt", VOID, BOLT, 3, 12, 18, 6, 0, 0);
        add("dimensional_ray", VOID, RAY, 10, 25, 60, 11, 0, 0);
        add("distortion", VOID, DISTORTION, 15, 35, 240, 5, 6, 100);
        add("phase_shift", VOID, BUFF_SELF, 20, 40, 600, 0, 0, 160);
        add("gravity_well", VOID, PULL, 25, 50, 400, 3, 6, 100);
        add("rift", VOID, ZONE_TARGET, 30, 55, 400, 7, 4, 100);
        add("void_explosion", VOID, AREA, 35, 60, 400, 20, 5, 0);
        add("entropy", VOID, RAY, 45, 50, 200, 10, 0, 100);
        add("singularity", VOID, DELAYED_AREA, 75, 120, 1200, 45, 8, 60);
        add("summon_void_wraith", VOID, SUMMON, 35, 60, 1200, 8, 0, 0);
    }

    private Spells() {}

    private static void add(String id, SpellSchool school, SpellKind kind, int level, float mana, int cooldown,
                            float power, float radius, int duration) {
        add(id, school, kind, level, mana, cooldown, power, radius, duration, "");
    }

    private static void add(String id, SpellSchool school, SpellKind kind, int level, float mana, int cooldown,
                            float power, float radius, int duration, String skill) {
        SPELLS.put(id, new Spell(id, school, kind, level, mana, cooldown, power, radius, duration, skill));
    }

    public static Spell get(String id) {
        return SPELLS.get(id);
    }

    public static Map<String, Spell> all() {
        return Collections.unmodifiableMap(SPELLS);
    }

    /** Sorts d'un grimoire : invocations ({@code summon = true}) ou attaque et soutien. */
    public static List<Spell> ofMode(boolean summon) {
        List<Spell> list = new ArrayList<>();
        for (Spell s : SPELLS.values()) if (s.isSummon() == summon) list.add(s);
        return list;
    }

    public static List<Spell> ofSchool(SpellSchool school) {
        List<Spell> list = new ArrayList<>();
        for (Spell s : SPELLS.values()) if (s.school() == school) list.add(s);
        return list;
    }

    /** Sort de base (projectile) de chaque école, utilisé par les bâtons et baguettes. */
    public static Spell basicBolt(SpellSchool school) {
        return switch (school) {
            case FIRE -> get("fire_bolt");
            case ICE -> get("ice_shard");
            case LIGHTNING -> get("spark");
            case NATURE -> get("thorn_bolt");
            case VOID -> get("void_bolt");
        };
    }
}
