package model.animal;



import model.trail.TrailHashMap;
import model.trail.TrailMapInterface;

public class AnimalSeasonality {


	  private static final TrailMapInterface<AnimalGroup, int[]> seasonalityMap = new TrailHashMap<>();

    static {
        seasonalityMap.put(AnimalGroup.BIRD,       new int[]{1,1,2,3,3,3,2,2,2,1,1,1});
        seasonalityMap.put(AnimalGroup.INSECT,     new int[]{0,0,1,2,3,3,3,3,2,1,0,0});
        seasonalityMap.put(AnimalGroup.MAMMAL,     new int[]{2,2,2,2,2,2,2,2,2,2,2,2});
        seasonalityMap.put(AnimalGroup.AMPHIBIAN,  new int[]{0,1,2,3,3,2,2,1,0,0,0,0});
        seasonalityMap.put(AnimalGroup.REPTILE,    new int[]{0,0,1,2,3,3,3,2,1,0,0,0});
        seasonalityMap.put(AnimalGroup.FISH,       new int[]{1,1,2,3,3,3,2,2,1,1,1,1});
        seasonalityMap.put(AnimalGroup.MARINE,     new int[]{1,1,1,2,3,3,3,3,2,2,1,1});
        seasonalityMap.put(AnimalGroup.PREDATOR,   new int[]{2,2,2,2,2,2,2,2,2,2,2,2});
    }

    public static int getActivityLevel(AnimalGroup group, int month) {
        if (!seasonalityMap.containsKey(group)) return 0;
        return seasonalityMap.get(group)[month - 1];
    }

}
