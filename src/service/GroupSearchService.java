package service;

import model.group.Group;
import model.group.GroupSearchCriteria;
import model.group.UserProfile;
import model.trail.Trail;
import model.trail.TrailList;

public interface GroupSearchService {
    // Initialize / incrementally maintain indexes
    void indexTrails(TrailList<Trail> trails);
    void indexGroups(TrailList<Group> groups);

    // Keyword search: fuzzy match on Trail name, return groups under those Trails
    TrailList<Group> searchGroupsByTrailKeyword(String keyword, int trailLimit);

    // Advanced search (using Hash/Tree/Heap, etc.); if topK<=0, return full sorted results
    TrailList<Group> advancedSearch(GroupSearchCriteria criteria, int topK);

    // Group joining actions
    boolean canJoin(Group g, int partySize);
    void join(Group g, UserProfile user, int partySize);
}


