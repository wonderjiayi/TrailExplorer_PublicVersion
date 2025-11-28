package model.search;

import model.group.Group;
import model.trail.TrailHashMap;
import model.trail.TrailList;


public final class GroupByTrail {
    // trailId -> groups
	private final TrailHashMap<Integer, TrailList<Group>> groupsByTrail = new TrailHashMap<>();

	public void add(int trailId, Group g) {
        // If no list exists for trailId, create a new TrailList<Group> and store it
        TrailList<Group> list = groupsByTrail.computeIfAbsent(trailId, new TrailList<>());
        list.add(g);
    }

    public TrailList<Group> groupsOf(int trailId) {
        // If none exists, return an empty TrailList<Group>
        return groupsByTrail.getOrDefault(trailId, new TrailList<>());
    }
}

