package service;

import model.animal.Animal;
import model.animal.AnimalGroup;
import model.animal.TerrainType;
import model.trail.Trail;
import model.trail.Topic;

import java.util.ArrayList;
import java.util.List;

public class TrailAnimalMatcher {

    /**
     * A basic Trail's topic、birdSpotted、wildAnimalPossible
     * 
     */
    public static List<Animal> matchAnimals(Trail trail, List<Animal> allAnimals) {

        List<AnimalGroup> possibleGroups = new ArrayList<>();

     
        Topic topic = trail.getTopic();

        switch (topic) {
            case MOUNTAIN:
                possibleGroups.add(AnimalGroup.MAMMAL);
                possibleGroups.add(AnimalGroup.PREDATOR);
                possibleGroups.add(AnimalGroup.BIRD);
                break;
            case FOREST:
                possibleGroups.add(AnimalGroup.MAMMAL);
                possibleGroups.add(AnimalGroup.BIRD);
                possibleGroups.add(AnimalGroup.INSECT);
                break;
            case LAKE:
                possibleGroups.add(AnimalGroup.FISH);
                possibleGroups.add(AnimalGroup.BIRD);
                break;
            case RIVER:
                possibleGroups.add(AnimalGroup.AMPHIBIAN);
                possibleGroups.add(AnimalGroup.FISH);
                break;
            case BEACH:
                possibleGroups.add(AnimalGroup.MARINE);
                possibleGroups.add(AnimalGroup.BIRD);
                break;
        }

        
        if (trail.isBirdSpotted()) {
            possibleGroups.add(AnimalGroup.BIRD);
        }

       
        if (trail.isWildAnimalPossible()) {
            possibleGroups.add(AnimalGroup.PREDATOR);
            possibleGroups.add(AnimalGroup.MAMMAL);
        }

       
        possibleGroups = possibleGroups.stream().distinct().toList();

        
        List<Animal> matched = new ArrayList<>();
        for (Animal a : allAnimals) {
            if (possibleGroups.contains(a.getGroup())) {
                matched.add(a);
            }
        }

        return matched;
    }
}
