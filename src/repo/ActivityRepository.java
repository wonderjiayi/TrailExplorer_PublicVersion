package repo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class ActivityRepository {
    public enum Type { VIEWED, CREATED, JOINED }

    private final Path file;

    public ActivityRepository(Path file) {
        this.file = file;
    }

    public void append(String username, Type type, String groupId) {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter bw = Files.newBufferedWriter(
                    file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                // CSV: ts,username,type,groupId
                bw.write(String.join(",",
                        Long.toString(Instant.now().toEpochMilli()),
                        esc(username), type.name(), esc(groupId)));
                bw.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> listGroupIds(String username, Type type) {
        if (!Files.exists(file)) return List.of();

        try {
            // Read all lines at once
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

            // Traverse from the end to the beginning so the latest records are added first
            LinkedHashSet<String> ordered = new LinkedHashSet<>();
            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i);
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] a = line.split(",", -1);
                if (a.length < 4) continue;

                String u   = unesc(a[1]);
                String t   = a[2];
                String gid = unesc(a[3]);

                if (u.equals(username) && t.equals(type.name())) {
                    // The first occurrence is the latest; LinkedHashSet preserves this order and removes duplicates
                    ordered.add(gid);
                }
            }
            // Now the order is: newest -> oldest
            return new ArrayList<>(ordered);

        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    private static String esc(String s){ return s==null? "" : s.replace(",", "&#44;"); }
    private static String unesc(String s){ return s.replace("&#44;", ","); }
}


