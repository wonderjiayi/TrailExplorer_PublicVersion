package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.animal.AnimalGroup;
import model.animal.AnimalSeasonality;
import model.animal.TerrainType;

public class AnimalSeasonService {

    public static int getActivityLevel(AnimalGroup group, int month) {
        return AnimalSeasonality.getActivityLevel(group, month);
    }

    public static List<AnimalGroup> getActiveAnimalsByMonth(TerrainType terrain, int month) {
        List<AnimalGroup> active = new ArrayList<>();
        for (AnimalGroup group : TerrainAnimalMapping.getAnimalsByTerrain(terrain)) {
            int level = getActivityLevel(group, month);
            if (level > 0) active.add(group);
        }
        return active;
    }

    public static Map<AnimalGroup, Integer> getActivityMapByMonth(TerrainType terrain, int month) {
        Map<AnimalGroup, Integer> map = new HashMap<>();
        for (AnimalGroup group : TerrainAnimalMapping.getAnimalsByTerrain(terrain)) {
            int level = getActivityLevel(group, month);
            map.put(group, level);
        }
        return map;
    }

}
