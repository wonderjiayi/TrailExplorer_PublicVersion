package model.animal;

public enum AnimalGroup {
    MAMMAL("FOREST"),
    BIRD("FOREST"),
    REPTILE("FOREST"),
    AMPHIBIAN("RIVER"),
    FISH("LAKE"),
    INSECT("FOREST"),
    MARINE("BEACH"),
    PREDATOR("MOUNTAIN");

    private final String defaultTerrain;

    AnimalGroup(String terrain) {
        this.defaultTerrain = terrain;
    }

    public String getDefaultTerrain() {
        return defaultTerrain;
    }
}
