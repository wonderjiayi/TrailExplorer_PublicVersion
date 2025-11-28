package repo;

import model.auth.UserAccount;
import model.group.UserProfile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UserRepository {
    private final File file;

    public UserRepository(String path) { this.file = new File(path); }

    /** Load all users */
    public List<UserAccount> loadAll() {
        List<UserAccount> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            // CSV: id,username,email,passwordHash,passwordSalt,profileId,nickname
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] a = line.split(",", -1); // Simple parsing: fields should not contain commas
                if (a.length < 7) continue;
                var profile = new UserProfile(a[5], a[6]);
                var acc = new UserAccount(a[0], a[1], a[2], a[3], a[4], profile);
                list.add(acc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Find by username (case-sensitive, consistent with AuthService) */
    public Optional<UserAccount> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String u = username.trim();
        for (UserAccount acc : loadAll()) {
            if (u.equals(acc.getUsername())) {
                return Optional.of(acc);
            }
        }
        return Optional.empty();
    }

    /** Public save method with duplicate-username check */
    public void save(UserAccount acc) {
        file.getParentFile().mkdirs();
        if (findByUsername(acc.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists: " + acc.getUsername());
        }
        append(acc);
    }

    /** Low-level append (no duplicate check) */
    public void append(UserAccount acc) {
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    acc.getId(), acc.getUsername(), acc.getEmail(),
                    acc.getPasswordHash(), acc.getPasswordSalt(),
                    acc.getProfile().getId(), acc.getProfile().getNickname());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


