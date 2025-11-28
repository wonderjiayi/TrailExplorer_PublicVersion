package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.animal.AnimalGroup;
import model.animal.TerrainType;

public class TerrainAnimalMapping {

    private static final Map<TerrainType, List<AnimalGroup>> terrainMap = new HashMap<>();

    static {
        terrainMap.put(TerrainType.FOREST,   Arrays.asList(AnimalGroup.BIRD, AnimalGroup.MAMMAL, AnimalGroup.INSECT));
        terrainMap.put(TerrainType.MOUNTAIN, Arrays.asList(AnimalGroup.MAMMAL, AnimalGroup.PREDATOR, AnimalGroup.BIRD));
        terrainMap.put(TerrainType.RIVER,    Arrays.asList(AnimalGroup.FISH, AnimalGroup.AMPHIBIAN, AnimalGroup.BIRD, AnimalGroup.MAMMAL, AnimalGroup.INSECT));
        terrainMap.put(TerrainType.LAKE,     Arrays.asList(AnimalGroup.FISH, AnimalGroup.BIRD, AnimalGroup.INSECT, AnimalGroup.AMPHIBIAN));
        terrainMap.put(TerrainType.BEACH,    Arrays.asList(AnimalGroup.MARINE, AnimalGroup.BIRD, AnimalGroup.INSECT));
    }

    public static List<AnimalGroup> getAnimalsByTerrain(TerrainType terrain) {
        return terrainMap.getOrDefault(terrain, new ArrayList<>());
    }

    public static Set<TerrainType> getSupportedTerrains() {
        return terrainMap.keySet();
    }

}
