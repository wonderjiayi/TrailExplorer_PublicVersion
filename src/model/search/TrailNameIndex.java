package model.search;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

import model.trail.Trail;

public final class TrailNameIndex {
    private final List<Trail> trails = new ArrayList<>();        // Main table: id -> Trail
    private final Map<String, Set<Integer>> tokenToIds = new HashMap<>(); // Inverted index
    private final Map<Integer, String> idToNameNorm = new HashMap<>();    // Normalized name cache

    private static final Pattern NON_ALNUM = Pattern.compile("[^\\p{L}\\p{Nd}]+");

    public int add(Trail t) {
        int id = trails.size();
        trails.add(t);
        String norm = normalize(t.getName());
        idToNameNorm.put(id, norm);

        // Simple tokenization: split by whitespace and non-alphanumeric characters
        for (String tok : tokenize(norm)) {
            tokenToIds.computeIfAbsent(tok, k -> new HashSet<>()).add(id);
        }
        return id;
    }

    /** Fuzzy search: returns (trailId, score) sorted by score descending */
    public List<ScoredId> search(String keyword, int limit) {
        String q = normalize(keyword);
        if (q.isBlank()) return List.of();

        // 1) Inverted index hits (token matches + prefix matches)
        Set<Integer> candidate = new HashSet<>();
        for (String tok : tokenize(q)) {
            var set = tokenToIds.get(tok);
            if (set != null) candidate.addAll(set);
            // Prefix expansion: tokens that start with tok
            for (var e : tokenToIds.entrySet()) {
                if (e.getKey().startsWith(tok)) candidate.addAll(e.getValue());
            }
        }

        // 2) Add candidates whose normalized name contains the query substring (to avoid misses)
        for (var entry : idToNameNorm.entrySet()) {
            if (entry.getValue().contains(q)) candidate.add(entry.getKey());
        }

        if (candidate.isEmpty()) return List.of();

        // 3) Simple scoring: exact match > prefix match > substring match > token overlap count
        List<ScoredId> out = new ArrayList<>();
        for (int id : candidate) {
            String name = idToNameNorm.get(id);
            double score = 0;
            if (name.equals(q)) score = 100;
            else if (name.startsWith(q)) score = 80;
            else if (name.contains(q)) score = 60;
            // Token overlap contribution
            score += jaccard(tokenize(name), tokenize(q)) * 20;
            out.add(new ScoredId(id, score));
        }
        out.sort((a, b) -> Double.compare(b.score, a.score));
        if (limit > 0 && out.size() > limit) return out.subList(0, limit);
        return out;
    }

    public Trail get(int trailId) { return trails.get(trailId); }

    // ---------- helpers ----------
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
        int inter = 0;
        for (String x : A) if (B.contains(x)) inter++;
        int union = A.size() + B.size() - inter;
        return union == 0 ? 0 : (double) inter / union;
    }

    public static final class ScoredId {
        public final int id;
        public final double score;
        public ScoredId(int id, double score) { this.id = id; this.score = score; }
    }
}

