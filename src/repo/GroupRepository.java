package repo;

import model.group.Group;
import model.group.UserProfile;
import model.trail.Trail;
import model.trail.TrailIndex;
import model.trail.TrailList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * CSV persistence format:
 * groupId,trailName,title,startIso,capacity,participants_encoded
 * participants_encoded example: userId|nick|party;userId2|nick2|party2
 */
public class GroupRepository {
    private final File file;

    public GroupRepository(String path) { this.file = new File(path); }

    /** Load all groups at startup (resolve Trail by trailName via index) */
    public TrailList<Group> loadAll(TrailIndex index) {
    	TrailList<Group> list = new TrailList<>();
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] a = line.split(",", -1);
                if (a.length < 6) continue;

                String gid = a[0];
                String trailName = a[1];
                String title = a[2];
                LocalDateTime start = LocalDateTime.parse(a[3]);
                int capacity = Integer.parseInt(a[4]);
                String participantsEnc = a[5];

                Trail t = index.getByName(trailName);
                if (t == null) continue; // Skip inconsistent data

                Group g = new Group(gid, t, title, start, capacity);

                // Restore participants (may be empty)
                if (!participantsEnc.isBlank()) {
                    String[] parts = participantsEnc.split(";");
                    for (String p : parts) {
                        if (p.isBlank()) continue;
                        String[] fields = p.split("\\|", -1);
                        if (fields.length < 3) continue;
                        String uid = fields[0];
                        String nick = fields[1];
                        int party = Integer.parseInt(fields[2]);
                        g.join(new UserProfile(uid, nick), party);
                    }
                }
                list.add(g);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Save all groups (full overwrite, simple and reliable) */
    public void saveAll(TrailList<Group> groups) {
        ensureParent();
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
        	for (int i = 0; i < groups.size(); i++) {
                Group g = groups.get(i);
                if (g == null) continue;
                String participants = encodeParticipants(g);
                pw.printf("%s,%s,%s,%s,%d,%s%n",
                        g.getId(),
                        g.getTrail().getName(),
                        escapeComma(g.getTitle()),
                        g.getStartTime(),
                        g.getCapacity(),
                        participants);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static String encodeParticipants(Group g) {
        StringBuilder sb = new StringBuilder();
        g.getParticipants().forEach(p -> {
            if (sb.length() > 0) sb.append(';');
            sb.append(p.getUser().getId()).append('|')
              .append(p.getUser().getNickname()).append('|')
              .append(p.getPartySize());
        });
        return sb.toString();
    }

    // If titles may contain commas, simply replace them (you are already using split(",", -1) when reading;
    // in general, it's better if titles do not contain commas)
    private static String escapeComma(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    private void ensureParent() {
        File p = file.getParentFile();
        if (p != null) p.mkdirs();
    }
}

