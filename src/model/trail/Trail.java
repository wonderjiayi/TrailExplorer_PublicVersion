package model.trail;

import java.util.List;
import java.util.Objects;

/**
 * Represents a hiking trail with detailed environmental and management attributes.
 * Each Trail is uniquely identified by its name + park + state + zipcode.
 */
public class Trail implements Comparable<Trail> {

    // ─────── Basic Info ───────
    private final String name;          // e.g., "Mount Lafayette Loop"
    private final String park;          // e.g., "Franconia Notch State Park"
    private final String address;       // full address or area description
    private final String state;         // e.g., "New Hampshire"
    private final String zipcode;       // e.g., "03251"
    private double lat;  // Latitude
    private double lon;  // Longitude

    // ─────── Categorization ───────
    private final Topic topic;          // e.g., MOUNTAIN, LAKE, BEACH，
    private final Difficulty difficulty;// EASY, MODERATE, HARD

    // ─────── Physical Attributes ───────
    private final double length;        // in miles
    private final double visitHours;    // average visiting time in hours
    private final double elevationGain; // elevation gain in feet

    // ─────── Environmental & Facilities ───────
    private final boolean petFriendly;
    private final boolean campingAllowed;
    private final boolean birdSpotted;
    private final boolean wildAnimalPossible;
    private final boolean icyTrail;     // whether trail is often icy
    private final String parkArea;      // section or area name

    // ─────── Seasonal & Suitability Info ───────
    private final List<Season> seasons; // recommended seasons
    private final List<Suitability> suitability; // e.g., FAMILY, SOLO, PHOTOGRAPHY

    // ─────── Alerts or Warnings ───────
    private final String alert;         // text description of hazard or maintenance

    // ─────── Constructor ───────
    public Trail(
            String name,
            String park,
            String state,
            String zipcode,
            Topic topic,
            Difficulty difficulty,
            double length,
            double visitHours,
            double elevationGain,
            boolean petFriendly,
            boolean campingAllowed,
            boolean birdSpotted,
            boolean wildAnimalPossible,
            boolean icyTrail
    ) {
        this.name = name;
        this.park = park;
        this.address = "";       // default
        this.state = state;
        this.zipcode = zipcode;
        this.lat = 0;
        this.lon = 0;
        this.topic = topic;
        this.difficulty = difficulty;
        this.length = length;
        this.visitHours = visitHours;
        this.elevationGain = elevationGain;
        this.petFriendly = petFriendly;
        this.campingAllowed = campingAllowed;
        this.birdSpotted = birdSpotted;
        this.wildAnimalPossible = wildAnimalPossible;
        this.icyTrail = icyTrail;

        this.parkArea = "";
        this.seasons = List.of();
        this.suitability = List.of();
        this.alert = "";
    }
    

    // ─────── Comparable: default sort by difficulty then length ───────
    @Override
    public int compareTo(Trail other) {
        int diffCmp = Integer.compare(this.difficulty.rank(), other.difficulty.rank());
        if (diffCmp != 0) return diffCmp;
        return Double.compare(this.length, other.length);
    }

    // ─────── Utility Methods ───────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trail)) return false;
        Trail trail = (Trail) o;
        return Objects.equals(name, trail.name) &&
               Objects.equals(park, trail.park) &&
               Objects.equals(state, trail.state) &&
               Objects.equals(zipcode, trail.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, park, state, zipcode);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %.1f mi, %s, Elev: %.0fft, Visit: %.1f hr, Pet: %s, Camp: %s",
                name, difficulty, length, topic,
                elevationGain, visitHours,
                petFriendly ? "Yes" : "No",
                campingAllowed ? "Yes" : "No",
        		wildAnimalPossible ? "Yes" : "No");
    }
    

    // ─────── Getters ───────
    public String getName() { return name; }
    public String getPark() { return park; }
    public String getAddress() { return address; }
    public String getState() { return state; }
    public String getZipcode() { return zipcode; }
    public Topic getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public double getLength() { return length; }
    public double getVisitHours() { return visitHours; }
    public double getElevationGain() { return elevationGain; }
    public boolean isPetFriendly() { return petFriendly; }
    public boolean isCampingAllowed() { return campingAllowed; }
    public boolean isBirdSpotted() { return birdSpotted; }
    public boolean isWildAnimalPossible() { return wildAnimalPossible; }
    public boolean isIcyTrail() { return icyTrail; }
    public String getParkArea() { return parkArea; }
    public List<Season> getSeasons() { return seasons; }
    public List<Suitability> getSuitability() { return suitability; }
    public String getAlert() { return alert; }
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}