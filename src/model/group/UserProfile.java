package model.group;

public class UserProfile {
    private final String id;
    private final String nickname;
    private UserPreference preferences = new UserPreference(); 
    
    public UserProfile(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
    public String getId() { return id; }
    public String getNickname() { return nickname; }
    
    public UserPreference getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreference preferences) {
        this.preferences = preferences;
    }
}

