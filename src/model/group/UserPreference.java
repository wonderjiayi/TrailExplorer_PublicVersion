package model.group;

import model.trail.Difficulty;
import model.trail.Topic;

public class UserPreference {

    private double targetLength;
    private Difficulty difficulty;
    private Topic topic;
    private boolean petFriendly;
    private boolean campingAllowed;
    private boolean preferWildlife;
    private String preferredLocation = "Boston Area";

    // NEW FIELDS: user's fixed location
    private double preferredLat = 42.3601;   // Boston latitude
    private double preferredLon = -71.0589;  // Boston longitude

    public UserPreference() {
        this.targetLength = 5.0;
        this.difficulty = Difficulty.MODERATE;
        this.topic = Topic.MOUNTAIN;
        this.petFriendly = false;
        this.campingAllowed = false;
        this.preferWildlife = false;
    }

    // Getters / Setters
    public double getTargetLength() { return targetLength; }
    public void setTargetLength(double targetLength) { this.targetLength = targetLength; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public boolean isPetFriendly() { return petFriendly; }
    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }

    public boolean isCampingAllowed() { return campingAllowed; }
    public void setCampingAllowed(boolean campingAllowed) { this.campingAllowed = campingAllowed; }

    public boolean isPreferWildlife() { return preferWildlife; }
    public void setPreferWildlife(boolean preferWildlife) { this.preferWildlife = preferWildlife; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String loc) { this.preferredLocation = loc; }

    // NEW GETTERS
    public double getPreferredLat() { return preferredLat; }
    public double getPreferredLon() { return preferredLon; }
}