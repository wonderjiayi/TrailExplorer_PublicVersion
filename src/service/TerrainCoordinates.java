package service;

import java.util.HashMap;
import java.util.Map;

public class TerrainCoordinates {

  
    private static final Map<String, double[]> COORDS = new HashMap<>();

    static {
        COORDS.put("FOREST",   new double[]{42.520, -71.350, 11});
        COORDS.put("LAKE",     new double[]{43.800, -71.200, 11});
        COORDS.put("RIVER",    new double[]{42.450, -71.100, 12});
        COORDS.put("MOUNTAIN", new double[]{44.160, -71.500, 10});
        COORDS.put("BEACH",    new double[]{42.410, -70.990, 12});
    }

    public static double[] get(String terrain) {
        return COORDS.getOrDefault(terrain, new double[]{42.36, -71.06, 10}); 
        //  Boston
    }
}
