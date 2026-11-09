package com.forgotten.dimensions.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Progression RPG d'un joueur. Immuable : chaque modification crée une nouvelle instance,
 * ce qui déclenche la sauvegarde et la synchronisation de l'attachement Fabric.
 *
 * @param skills         rang de chaque compétence débloquée
 * @param specialization arbre choisi comme spécialisation ("" si aucune)
 * @param selectedSpell  sort actif lancé par la touche de sort
 * @param visitedBiomes  biomes déjà découverts (expérience d'exploration)
 * @param schoolCasts    nombre de sorts lancés par école (maîtrise)
 */
public record PlayerProgress(int level, int xp, int skillPoints, Map<String, Integer> skills, String specialization,
                             String selectedSpell, List<String> visitedBiomes, Map<String, Integer> schoolCasts) {

    public static final int MAX_LEVEL = 100;

    public static final PlayerProgress DEFAULT = new PlayerProgress(1, 0, 0, Map.of(), "", "fire_bolt", List.of(), Map.of());

    public static final Codec<PlayerProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, MAX_LEVEL).fieldOf("level").forGetter(PlayerProgress::level),
            Codec.INT.fieldOf("xp").forGetter(PlayerProgress::xp),
            Codec.INT.fieldOf("skill_points").forGetter(PlayerProgress::skillPoints),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("skills", Map.of()).forGetter(PlayerProgress::skills),
            Codec.STRING.optionalFieldOf("specialization", "").forGetter(PlayerProgress::specialization),
            Codec.STRING.optionalFieldOf("selected_spell", "fire_bolt").forGetter(PlayerProgress::selectedSpell),
            Codec.STRING.listOf().optionalFieldOf("visited_biomes", List.of()).forGetter(PlayerProgress::visitedBiomes),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("school_casts", Map.of()).forGetter(PlayerProgress::schoolCasts)
    ).apply(i, PlayerProgress::new));

    public PlayerProgress {
        skills = Map.copyOf(skills);
        visitedBiomes = List.copyOf(visitedBiomes);
        schoolCasts = Map.copyOf(schoolCasts);
    }

    public int skillRank(String id) {
        return skills.getOrDefault(id, 0);
    }

    public int spentPoints() {
        return skills.values().stream().mapToInt(Integer::intValue).sum();
    }

    public PlayerProgress withLevelXp(int newLevel, int newXp, int newPoints) {
        return new PlayerProgress(newLevel, newXp, newPoints, skills, specialization, selectedSpell, visitedBiomes, schoolCasts);
    }

    public PlayerProgress withSkill(String id, int rank, int newPoints) {
        Map<String, Integer> copy = new HashMap<>(skills);
        if (rank <= 0) copy.remove(id);
        else copy.put(id, rank);
        return new PlayerProgress(level, xp, newPoints, copy, specialization, selectedSpell, visitedBiomes, schoolCasts);
    }

    public PlayerProgress withSkillsReset(int newPoints) {
        return new PlayerProgress(level, xp, newPoints, Map.of(), "", selectedSpell, visitedBiomes, schoolCasts);
    }

    public PlayerProgress withSpecialization(String spec) {
        return new PlayerProgress(level, xp, skillPoints, skills, spec, selectedSpell, visitedBiomes, schoolCasts);
    }

    public PlayerProgress withSelectedSpell(String spell) {
        return new PlayerProgress(level, xp, skillPoints, skills, specialization, spell, visitedBiomes, schoolCasts);
    }

    public PlayerProgress withVisitedBiome(String biome) {
        List<String> copy = new ArrayList<>(visitedBiomes);
        copy.add(biome);
        return new PlayerProgress(level, xp, skillPoints, skills, specialization, selectedSpell, copy, schoolCasts);
    }

    public PlayerProgress withSchoolCast(String school) {
        Map<String, Integer> copy = new HashMap<>(schoolCasts);
        copy.merge(school, 1, Integer::sum);
        return new PlayerProgress(level, xp, skillPoints, skills, specialization, selectedSpell, visitedBiomes, copy);
    }

    /** Niveau de maîtrise d'une école (0 à 10), dérivé du nombre de sorts lancés. */
    public int schoolMastery(String school) {
        int casts = schoolCasts.getOrDefault(school, 0);
        return Math.min(10, (int) Math.floor(Math.sqrt(casts / 5.0)));
    }
}
