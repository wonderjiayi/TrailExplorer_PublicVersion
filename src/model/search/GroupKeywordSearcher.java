package model.search;

import model.group.Group;
import model.trail.TrailList;

public final class GroupKeywordSearcher {
    private final TrailNameIndex nameIndex;
    private final GroupByTrail groupIndex;

    public GroupKeywordSearcher(TrailNameIndex nameIndex, GroupByTrail groupIndex) {
        this.nameIndex = nameIndex; this.groupIndex = groupIndex;
    }

    /** Search by keyword: return all groups under matched Trails (sorted by trail relevance) */
    public TrailList<Group> searchGroupsByTrailKeyword(String keyword, int trailLimit) {
        var trails = nameIndex.search(keyword, trailLimit <= 0 ? 50 : trailLimit);

        // Store results in TrailList
        TrailList<Group> out = new TrailList<>();

        for (var sid : trails) {
            // groupIndex.groupsOf(...) was updated earlier to return TrailList<Group>
            var groups = groupIndex.groupsOf(sid.id);
            // Add each group into the result
            for (Group g : groups.toList()) {
                out.add(g);
            }
        }
        return out;
    }
}


