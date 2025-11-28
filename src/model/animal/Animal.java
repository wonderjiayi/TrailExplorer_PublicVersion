package model.animal;


import model.trail.TrailList;
/**
 * Extended Animal model
 * Supports advanced fields for UI, ecology, and seasonal activity.
 */
public class Animal {
    private int id;
    private String name;
    private AnimalGroup group;
    private String habitat;           
    private TerrainType preferredTerrain; 
    private String icon;             
    private String image;             
    private String description;       
    private TrailList<Integer> activeMonths;
    private TrailList<Integer> activeDays;
    private TrailList<String> recentSpots;

    // ----------------------------
    // ✅ Constructors
    // ----------------------------
    public Animal() {
        //  for JSON Loader
    }

    public Animal(String name, AnimalGroup group, TerrainType terrain, String icon) {
        this.name = name;
        this.group = group;
        this.preferredTerrain = terrain;
        this.icon = icon;
    }

    // ----------------------------
    // ✅ Getters and Setters
    // ----------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AnimalGroup getGroup() { return group; }
    public void setGroup(AnimalGroup group) { this.group = group; }

    public String getHabitat() { return habitat; }
    public void setHabitat(String habitat) { this.habitat = habitat; }

    public TerrainType getPreferredTerrain() { return preferredTerrain; }
    public void setPreferredTerrain(TerrainType preferredTerrain) { this.preferredTerrain = preferredTerrain; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TrailList<Integer> getActiveMonths() { return activeMonths; }

    public void setActiveMonths(TrailList<Integer> activeMonths) {
        this.activeMonths = activeMonths;
    }


    public TrailList<Integer> getActiveDays() { return activeDays; }

    public void setActiveDays(TrailList<Integer> activeDays) {
        this.activeDays = activeDays;
    }

    public TrailList<String> getRecentSpots() { return recentSpots; }

    public void setRecentSpots(TrailList<String> recentSpots) {
        this.recentSpots = recentSpots;
    }


    // ----------------------------
    // ✅ For UI display
    // ----------------------------
    @Override
    public String toString() {
        return name + " (" + group + ")";
    }
}
