package service;

import model.animal.TerrainType;
import model.trail.Topic;

public class TerrainFilterService {
	 public static TerrainType mapTopicToTerrain(Topic topic) {
	        switch (topic) {
	            case FOREST:   return TerrainType.FOREST;
	            case RIVER:    return TerrainType.RIVER;
	            case MOUNTAIN: return TerrainType.MOUNTAIN;
	            case LAKE:     return TerrainType.LAKE;
	            case BEACH:    return TerrainType.BEACH;
	            default:       return TerrainType.FOREST;
	        }
	    }

}
