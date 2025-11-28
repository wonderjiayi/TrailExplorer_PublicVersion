package service;

import model.group.Group;
import model.group.GroupSearchCriteria;
import model.group.UserProfile;
import model.trail.Difficulty;
import model.trail.Topic;
import model.trail.Trail;
import model.trail.TrailHashMap;
import model.trail.TrailHeap;
import model.trail.TrailList;
import model.trail.TrailTreeMap;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Search & grouping service implementation:
 * - Keyword index (fuzzy search on Trail name)
 * - Advanced search (Hash index for equality, Tree index for ranges, intersection, Heap for Top-K)
 * If you replace Map/Tree/Heap with your own TrailHashTable/TrailTreeMap/TrailHeap, just swap them in this class.
 */
public class GroupSearchServiceImpl implements GroupSearchService {

    // ========= Indexes =========
    private final TrailNameIndex nameIndex = new TrailNameIndex(); // Fuzzy name search
    private final TrailIndex trailIndex = new TrailIndex();        // Topic/Difficulty/Pet/Hours
    private final GroupIndex groupIndex = new GroupIndex();        // trailId -> groups / targetSize -> groups

    // ========= Initialization =========
    @Override
    public void indexTrails(TrailList<Trail> trails) {
        if (trails == null || trails.size() == 0) return;
        for (int i = 0; i < trails.size(); i++) {
            Trail t = trails.get(i);
            int id = trailIndex.add(t);
            nameIndex.add(t, id); // Use the same trailId
        }
    }

    @Override
    public void indexGroups(TrailList<Group> groups) {
        if (groups == null || groups.size() == 0) return;
        for (int i = 0; i < groups.size(); i++) {
            Group g = groups.get(i);
            int trailId = trailIndex.idOf(g.getTrail());
            if (trailId >= 0) groupIndex.add(trailId, g);
        }
    }

    // ========= Keyword search =========
    @Override
    public TrailList<Group> searchGroupsByTrailKeyword(String keyword, int trailLimit) {
        List<Integer> trailIds = nameIndex.searchIds(
                keyword,
                trailLimit <= 0 ? 50 : trailLimit
        );
        TrailList<Group> out = new TrailList<>();
        for (int tid : trailIds) {
            TrailList<Group> gs = groupIndex.groupsForTrail(tid);
            for (Group g : gs.toList()) {
                out.add(g);
            }
        }
        return out;
    }

    // ========= Advanced search =========
    @Override
    public TrailList<Group> advancedSearch(GroupSearchCriteria c, int topK) {
        // 1) First, use equality conditions to get the most constrained candidate set
        Set<Integer> cand = trailIndex.candidates(
                c.topic(),
                c.maxDifficulty(),
                c.needPetFriendly()
        );
        // 2) Range filtering (maximum visit duration)
        trailIndex.applyMaxHours(cand, c.maxVisitHours());

        // 3) Group-level filtering + Top-K (using a min-heap; can be replaced with your own TrailHeap)
        TrailHeap<GroupScore> heap = new TrailHeap<>(
                (a, b) -> Double.compare(a.score, b.score) // Smaller score means higher priority (top of heap)
        );

        for (int tid : cand) {
            TrailList<Group> list = groupIndex.groupsForTrail(tid);
            for (Group g : list.toList()) {
                if (c.joinAsPartySize() != null && !g.canJoin(c.joinAsPartySize())) {
                    continue;
                }

                double s = score(g, c);

                if (topK <= 0) {
                    heap.add(new GroupScore(g, s)); // No K limit: collect all first
                } else {
                    if (heap.getCurrentSize() < topK) {
                        heap.add(new GroupScore(g, s));
                    } else if (s > heap.peekTop().score) {
                        // Current candidate is better than heap top → replace it
                        heap.removeTop();
                        heap.add(new GroupScore(g, s));
                    }
                }
            }
        }

        // 4) Output in descending order of score
        List<GroupScore> tmp = new ArrayList<>();
        while (!heap.isEmpty()) {
            tmp.add(heap.removeTop()); // Popped from small to large
        }
        tmp.sort((a, b) -> Double.compare(b.score, a.score)); // Convert to descending

        TrailList<Group> out = new TrailList<>();
        for (GroupScore gs : tmp) {
            out.add(gs.g);
        }
        return out;
    }

    @Override public boolean canJoin(Group g, int partySize) { return g.canJoin(partySize); }
    @Override public void join(Group g, UserProfile user, int partySize) { g.join(user, partySize); }

    // ========= Scoring function (weights can be adjusted) =========
    private static double score(Group g, GroupSearchCriteria c) {
        // More remaining slots is better (but with diminishing returns)
        double s = Math.log(1 + g.getRemainingSlots()) * 20;

        // Lower difficulty is better
        s += 30.0 / (1 + rank(g.getTrail().getDifficulty()));

        // Shorter visit duration is better
        s += 30.0 / (1 + g.getTrail().getVisitHours());

        // If joinAs can be fully accommodated, give a small bonus (optional)
        if (c.joinAsPartySize() != null && g.canJoin(c.joinAsPartySize())) s += 10;

        return s;
    }

    private static int rank(Difficulty d) {
        try { return d.rank(); } catch (Throwable ignored) { return d.ordinal(); }
    }

    // =========================================================================
    // ======================== Internal index implementations =================
    // =========================================================================

    /** Trail name fuzzy index (inverted index + substring + simple scoring). */
    private static final class TrailNameIndex {
        private final Map<Integer, String> idToNormName = new HashMap<>();
        private final Map<String, Set<Integer>> tokenToIds = new HashMap<>();
        private static final Pattern NON_ALNUM = Pattern.compile("[^\\p{L}\\p{Nd}]+");

        void add(Trail t, int trailId) {
            String norm = normalize(t.getName());
            idToNormName.put(trailId, norm);
            for (String tok : tokenize(norm)) {
                tokenToIds.computeIfAbsent(tok, k -> new HashSet<>()).add(trailId);
            }
        }

        List<Integer> searchIds(String keyword, int limit) {
            String q = normalize(keyword);
            if (q.isBlank()) return List.of();

            Set<Integer> cand = new HashSet<>();
            // Token matches + prefix expansions
            for (String tok : tokenize(q)) {
                var s = tokenToIds.get(tok);
                if (s != null) cand.addAll(s);
                for (var e : tokenToIds.entrySet()) {
                    if (e.getKey().startsWith(tok)) cand.addAll(e.getValue());
                }
            }
            // Name contains query as substring
            for (var e : idToNormName.entrySet()) {
                if (e.getValue().contains(q)) cand.add(e.getKey());
            }
            if (cand.isEmpty()) return List.of();

            // Simple scoring
            List<Map.Entry<Integer, Double>> scored = new ArrayList<>();
            for (int id : cand) {
                String name = idToNormName.get(id);
                double s = name.equals(q) ? 100 : name.startsWith(q) ? 80 :
                           name.contains(q) ? 60 : 0;
                s += jaccard(tokenize(name), tokenize(q)) * 20;
                scored.add(new AbstractMap.SimpleEntry<>(id, s));
            }
            scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            int n = (limit <= 0 || limit > scored.size()) ? scored.size() : limit;
            List<Integer> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) out.add(scored.get(i).getKey());
            return out;
        }

        private static String normalize(String s) {
            String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFKC);
            n = n.toLowerCase(Locale.ROOT);
            n = NON_ALNUM.matcher(n).replaceAll(" ").trim();
            return n.replaceAll("\\s+", " ");
        }
        private static List<String> tokenize(String s) {
            if (s.isBlank()) return List.of();
            return Arrays.asList(s.split("\\s+"));
        }
        private static double jaccard(List<String> a, List<String> b) {
            if (a.isEmpty() || b.isEmpty()) return 0;
            Set<String> A = new HashSet<>(a), B = new HashSet<>(b);
            int inter = 0; for (String x : A) if (B.contains(x)) inter++;
            int union = A.size() + B.size() - inter;
            return union == 0 ? 0 : (double) inter / union;
        }
    }

    /** Trail condition index (Topic/Difficulty/Pet as equality; VisitHours as range). */
    private static final class TrailIndex {
    	private final TrailList<Trail> trails = new TrailList<>();
        private final TrailHashMap<Trail, Integer> trailToId = new TrailHashMap<>();

        private final Map<Topic, Set<Integer>> byTopic = new HashMap<>();
        private final Map<Difficulty, Set<Integer>> byDifficulty = new HashMap<>();
        private final Set<Integer> petTrue = new HashSet<>();
        private final TrailTreeMap<Double, Integer> byVisitHours = new TrailTreeMap<>();

        int add(Trail t) {
        	Integer old = trailToId.get(t);          // Use Trail.equals to deduplicate
            if (old != null) return old;             // Already added; return existing id
        	
            int id = trails.size();
            trails.add(t);
            trailToId.put(t, id);
            byTopic.computeIfAbsent(t.getTopic(), k -> new HashSet<>()).add(id);
            byDifficulty.computeIfAbsent(t.getDifficulty(), k -> new HashSet<>()).add(id);
            if (t.isPetFriendly()) petTrue.add(id);
            byVisitHours.put(t.getVisitHours(), id);
            return id;
        }

        int idOf(Trail t) { return trailToId.getOrDefault(t, -1); }

        Set<Integer> candidates(Topic topic, Difficulty maxDiff, boolean needPet) {
            List<Set<Integer>> pools = new ArrayList<>();
            if (topic != null && byTopic.containsKey(topic)) pools.add(byTopic.get(topic));

            if (maxDiff != null) {
                Set<Integer> s = new HashSet<>();
                for (Difficulty d : Difficulty.values()) {
                    if (rank(d) <= rank(maxDiff) && byDifficulty.containsKey(d)) {
                        s.addAll(byDifficulty.get(d));
                    }
                }
                pools.add(s);
            }
            if (needPet) pools.add(petTrue);

            if (pools.isEmpty()) {
                Set<Integer> all = new HashSet<>();
                for (int i = 0; i < trails.size(); i++) all.add(i);
                return all;
            }
            pools.sort(Comparator.comparingInt(Set::size));
            return new HashSet<>(pools.get(0));
        }

        void applyMaxHours(Set<Integer> base, Double maxHours) {
            if (maxHours == null) return;
            List<Integer> idsInRange = byVisitHours.getRange(0.0, maxHours);
            Set<Integer> ok = new HashSet<>(idsInRange);
            base.retainAll(ok);
        }
    }

    /** Group index (trailId -> groups; targetSize -> groups for ≥ queries) */
    private static final class GroupIndex {
    	private final TrailHashMap<Integer, TrailList<Group>> groupsByTrail = new TrailHashMap<>();
    	void add(int trailId, Group g) {
            TrailList<Group> list = groupsByTrail.get(trailId);
            if (list == null) {
                list = new TrailList<>();
                groupsByTrail.put(trailId, list);
            }
            if (!list.contains(g)) {
                list.add(g);
            }
        }
    	TrailList<Group> groupsForTrail(int trailId) {
            TrailList<Group> list = groupsByTrail.get(trailId);
            if (list == null) {
                return new TrailList<>();
            }
            return list;
        }
    }

    private static final class GroupScore {
        final Group g; final double score;
        GroupScore(Group g, double score) { this.g = g; this.score = score; }
    }
}

