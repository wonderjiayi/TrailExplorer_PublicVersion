package data;

import model.group.Group;
import model.trail.Trail;
import model.trail.TrailList;

import java.time.LocalDateTime;
import java.util.UUID;

public final class GroupSeeder {
    private GroupSeeder(){}

    /** For each trail, generate 1–2 sample groups and return them */
    public static TrailList<Group> makeSampleGroupsPerTrail(TrailList<Trail> trails) {
        TrailList<Group> out = new TrailList<>();
        if (trails == null || trails.isEmpty()) return out;

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < trails.size(); i++) {
            Trail t = trails.get(i);
            // ① Morning Hike (capacity roughly based on difficulty)
            int cap1 = capacityByDifficulty(t.getDifficulty());
            Group morning = new Group(
                    UUID.randomUUID().toString(),
                    t,
                    t.getName() + " · Morning Hike",
                    now.plusDays(1).withHour(9).withMinute(0),
                    cap1
            );
            out.add(morning);

            // ② If hike duration is not too long, create an additional “Sunset Walk”
            if (t.getVisitHours() <= 4.0) {
                int cap2 = Math.max(6, cap1 - 2);
                Group sunset = new Group(
                        UUID.randomUUID().toString(),
                        t,
                        t.getName() + " · Sunset Walk",
                        now.plusDays(2).withHour(17).withMinute(30),
                        cap2
                );
                out.add(sunset);
            }
        }
        return out;
    }

    private static int capacityByDifficulty(model.trail.Difficulty d) {
        switch (d) {
            case EASY:     return 10;
            case MODERATE: return 8;
            case HARD:     return 6;
            default:       return 8;
        }
    }
}
