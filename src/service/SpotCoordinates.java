package service;

import java.util.HashMap;
import java.util.Map;

public class SpotCoordinates {

    private static final Map<String, double[]> coords = new HashMap<>();

    static {
        coords.put("Blue Hills Reservation", new double[]{42.216, -71.114});
        coords.put("Middlesex Fells", new double[]{42.466, -71.120});
        coords.put("Horn Pond", new double[]{42.45, -71.15});
        coords.put("Boston Common", new double[]{42.355, -71.065});
        coords.put("Mystic River", new double[]{42.40, -71.10});
    }

    public static double[] get(String spot) {
        return coords.getOrDefault(spot, new double[]{42.36, -71.06}); // fallback Boston
    }
}
