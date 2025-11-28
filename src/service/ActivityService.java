package service;

import controller.GlobalData;
import model.group.Group;
import model.trail.TrailList;
import repo.ActivityRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityService {
    private final ActivityRepository repo;

    public ActivityService(ActivityRepository repo) {
        this.repo = repo;
    }

    // ─────────────────── Record activities ───────────────────
    public void markViewed(String username, String groupId) {
        repo.append(username, ActivityRepository.Type.VIEWED, groupId);
    }

    public void markCreated(String username, String groupId) {
        repo.append(username, ActivityRepository.Type.CREATED, groupId);
    }

    public void markJoined(String username, String groupId) {
        repo.append(username, ActivityRepository.Type.JOINED, groupId);
    }

    // ─────────────────── Query activities: return TrailList<Group> ───────────────────
    public TrailList<Group> getViewed(String username) {
        List<String> ids = repo.listGroupIds(username, ActivityRepository.Type.VIEWED);
        return resolve(ids);
    }

    public TrailList<Group> getCreated(String username) {
        List<String> ids = repo.listGroupIds(username, ActivityRepository.Type.CREATED);
        return resolve(ids);
    }

    /**
     * Joined groups: for robustness, compute in real time (scan group participants for the current user).
     */
    public TrailList<Group> getJoined(String username) {
        TrailList<Group> result = new TrailList<>();

        var allGroups = GlobalData.getAllGroups(); // TrailList<Group>
        for (int i = 0; i < allGroups.size(); i++) {
            Group g = allGroups.get(i);
            if (g == null) continue;

            boolean joined = false;
            // participants is still a List<Participant>; a regular for-each loop is sufficient here
            var participants = g.getParticipants();
            for (var p : participants) {
                var u = p.getUser();
                String id   = u.getId();
                String nick = u.getNickname();
                if (username.equalsIgnoreCase(id)
                        || username.equalsIgnoreCase(nick)) {
                    joined = true;
                    break;
                }
            }
            if (joined) {
                result.add(g);
            }
        }

        return result;
    }

    // ─────────────────── Helper: id list -> TrailList<Group> ───────────────────
    private TrailList<Group> resolve(List<String> ids) {
        TrailList<Group> out = new TrailList<>();
        if (ids == null || ids.isEmpty()) return out;

        // First, build a global mapping from group id -> Group
        Map<String, Group> map = new HashMap<>();
        var allGroups = GlobalData.getAllGroups(); // TrailList<Group>
        for (int i = 0; i < allGroups.size(); i++) {
            Group g = allGroups.get(i);
            if (g == null) continue;
            map.putIfAbsent(g.getId(), g);
        }

        // Then, restore the Group list in the same order as the ids returned by ActivityRepository
        for (String id : ids) {
            Group g = map.get(id);
            if (g != null) {
                out.add(g);
            }
        }

        return out;
    }
}



