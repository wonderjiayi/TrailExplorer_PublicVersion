package service;

import model.auth.UserAccount;
import model.group.UserProfile;
import repo.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple file-based authentication service: uses UserRepository to read/write CSV.
 * Provides: login / register.
 */
public class AuthService {

    private final UserRepository users;
    private static final SecureRandom RNG = new SecureRandom();

    public AuthService(UserRepository users) {
        this.users = users;
    }

    /** Login with username + plain-text password */
    public Optional<UserAccount> login(String username, String plainPassword) {
        if (username == null || username.isBlank()) return Optional.empty();
        return users.findByUsername(username.trim()).flatMap(acc -> {
            String calc = hash(plainPassword, acc.getPasswordSalt());
            return calc.equals(acc.getPasswordHash()) ? Optional.of(acc) : Optional.empty();
        });
    }

    /**
     * Registration rules:
     *  - username must be unique
     *  - password is stored as salted hash
     *  - nickname is stored in UserProfile
     */
    public UserAccount register(String username, String email, String plainPassword, String nickname) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username required");
        }
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password required");
        }
        if (users.findByUsername(username.trim()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }

        String id = "acc-" + UUID.randomUUID();
        String salt = genSalt();
        String hash = hash(plainPassword, salt);
        UserProfile profile = new UserProfile("u-" + UUID.randomUUID(), 
                (nickname == null || nickname.isBlank()) ? username.trim() : nickname.trim());

        UserAccount acc = new UserAccount(id, username.trim(),
                (email == null ? "" : email.trim()), hash, salt, profile);

        users.save(acc);   // Write to CSV
        return acc;
    }

    // ========== Utilities: salt generation + hashing ==========
    private static String genSalt() {
        byte[] buf = new byte[16];
        RNG.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    /** Simple SHA-256( salt + ":" + password ) */
    private static String hash(String plain, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String src = (salt == null ? "" : salt) + ":" + (plain == null ? "" : plain);
            byte[] out = md.digest(src.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

