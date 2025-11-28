package service;

import java.util.*;

import model.animal.AnimalGroup;
import model.animal.TerrainType;
import model.trail.Difficulty;
import model.trail.Topic;
import model.trail.Trail;
import model.trail.TrailHashMap;
import model.trail.TrailList;
import model.trail.TrailListInterface;
import model.trail.TrailMapInterface;

/**
 * Intelligent Recommendation System for matching animals to trails.
 * Combines ecology, terrain, difficulty, seasonality, and predator risk.
 */
public class AnimalRecommendationService {

  
    public List<Trail> recommendTrailsForAnimal(AnimalGroup group, int currentMonth, List<Trail> allTrails) {


      
        TrailMapInterface<Trail, Double> scoreMap = new TrailHashMap<>();


        // calculate score
        for (Trail trail : allTrails) {
            double ecoScore = computeEcoScore(trail, group);
            double seasonScore = computeSeasonScore(group, currentMonth);
            double difficultyScore = computeDifficultyScore(trail.getDifficulty(), group);
            double risk = computePredatorRisk(trail, group);

            double total = computeTotalScore(ecoScore, seasonScore, difficultyScore, risk);
            scoreMap.put(trail, total);
        }

       
        List<Trail> sortedTrails = new ArrayList<>();

        // keySet()
        Set<Trail> keys = scoreMap.keySet();
        if (keys != null) {
            sortedTrails.addAll(keys);
        }

      
        sortedTrails.sort((a, b) -> Double.compare(scoreMap.get(b), scoreMap.get(a)));

        // TrailListInterface
        TrailListInterface<Trail> recommended = new TrailList<>();
        for (Trail t : sortedTrails) {
            recommended.add(t);
        }

        return recommended.toList(); // 
    }

   
    private double computeEcoScore(Trail trail, AnimalGroup group) {
        TerrainType terrain = TerrainFilterService.mapTopicToTerrain(trail.getTopic());
        List<AnimalGroup> terrainAnimals = TerrainAnimalMapping.getAnimalsByTerrain(terrain);

        if (terrainAnimals.contains(group)) {
            return 3.0; 
        } else {
            return 0.5; 
        }
    }

    
    private double computeSeasonScore(AnimalGroup group, int month) {
        int activity = AnimalSeasonService.getActivityLevel(group, month);
        //  0-3 →  0.0–3.0
        return activity * 1.0;
    }

    // computeDifficultyScore
    private double computeDifficultyScore(Difficulty difficulty, AnimalGroup group) {
        double base = 1.0;
        switch (difficulty) {
            case EASY: base = 1.0; break;
            case MODERATE: base = 2.0; break;
            case HARD: base = 3.0; break;
        }

        if (group == AnimalGroup.PREDATOR || group == AnimalGroup.MAMMAL) {
            return base * 1.2; 
        } else if (group == AnimalGroup.BIRD || group == AnimalGroup.INSECT) {
            return 4 - base; 
        } else {
            return base;
        }
    }

    // ⚠️computePredatorRisk Predator risk
    private double computePredatorRisk(Trail trail, AnimalGroup group) {
        TerrainType terrain = TerrainFilterService.mapTopicToTerrain(trail.getTopic());

        
        if (group == AnimalGroup.PREDATOR) return 0.0;

        List<AnimalGroup> animals = TerrainAnimalMapping.getAnimalsByTerrain(terrain);
        if (animals.contains(AnimalGroup.PREDATOR)) {
            return 1.5;
        }
        return 0.0;
    }

    
    private double computeTotalScore(double eco, double season, double difficulty, double risk) {
        double total = eco * 1.5 + season * 1.2 + difficulty - risk;
        return total;
    }
}

