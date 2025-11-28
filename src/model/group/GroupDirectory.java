package model.group;

import java.time.LocalDateTime;
import java.util.UUID;
import model.trail.Trail;
import model.trail.TrailHashMap;
import model.trail.TrailList;

public class GroupDirectory {
    private final TrailList<Group> groups = new TrailList<>();
    private final TrailHashMap<Trail, TrailList<Group>> groupsByTrail = new TrailHashMap<>();
    private final TrailHashMap<String, Group> byId = new TrailHashMap<>();

    /** Create a group with capacity only (creator is NOT auto-joined). */
    public Group createFromTrail(Trail trail, String title, LocalDateTime start, int capacity) {
        Group g = new Group(
                UUID.randomUUID().toString(),
                trail,
                (title == null || title.isBlank()) ? trail.getName() : title,
                start,
                Math.max(1, capacity)
        );
        add(g);
        return g;
    }

    /** Create a group and directly add the creator with a given party size (party consumes capacity). */
    public Group createAndJoin(Trail trail, String title, LocalDateTime start,
                               int capacity, UserProfile creator, int partySize) {
        Group g = createFromTrail(trail, title, start, capacity);
        if (!g.canJoin(partySize)) {
            throw new IllegalStateException("Party size exceeds capacity");
        }
        g.join(creator, Math.max(1, partySize));
        return g;
    }

    /** Manually add an existing group (e.g., loaded from file/network). */
    public void add(Group g) {
        // 1. Add to main list
        groups.add(g);

        // 2. Index by id
        byId.put(g.getId(), g);

        // 3. Index by trail
        Trail trail = g.getTrail();
        TrailList<Group> list = groupsByTrail.get(trail);
        if (list == null) {
            list = new TrailList<>();
            groupsByTrail.put(trail, list);
        }
        list.add(g);
    }

    public void addAll(Iterable<Group> gs) {
        for (Group g : gs) {
            add(g);
        }
    }

    public boolean remove(Group g) {
        boolean ok = groups.remove(g);
        if (ok) {
            // 1. Remove from id index
            byId.removeByKey(g.getId());

            // 2. Remove from trail -> list index
            Trail trail = g.getTrail();
            TrailList<Group> list = groupsByTrail.get(trail);
            if (list != null) {
                list.remove(g);
                if (list.size() == 0) {
                    groupsByTrail.removeByKey(trail);
                }
            }
        }
        return ok;
    }

    /** Find by id; return null if not found (Optional not used) */
    public Group findById(String id) {
        return byId.get(id);
    }

    /** Return list of all groups, backed by your TrailList implementation */
    public TrailList<Group> all() {
        return groups;
    }

    /** Return list of groups for a given trail */
    public TrailList<Group> groupsForTrail(Trail t) {
        TrailList<Group> list = groupsByTrail.get(t);
        if (list == null) {
            // If nothing exists, return an empty list
            return new TrailList<>();
        }
        return list;
    }

}

