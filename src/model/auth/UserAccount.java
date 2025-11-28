package model.auth;

import model.group.UserProfile;

public class UserAccount {
    private final String id;           // UUID
    private final String username;     // Unique
    private final String email;        // Optional unique
    private final String passwordHash; // PBKDF2
    private final String passwordSalt; // Salt
    private final UserProfile profile; // For display (nickname etc.)

    public UserAccount(String id, String username, String email,
                       String passwordHash, String passwordSalt,
                       UserProfile profile) {
        this.id = id; this.username = username; this.email = email;
        this.passwordHash = passwordHash; this.passwordSalt = passwordSalt;
        this.profile = profile;
    }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPasswordSalt() { return passwordSalt; }
    public UserProfile getProfile() { return profile; }
}


