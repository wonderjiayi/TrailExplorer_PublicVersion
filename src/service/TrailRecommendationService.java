package service;

import model.auth.AuthContext;
import model.group.UserPreference;
import model.trail.*;

import java.util.Comparator;
import java.util.List;

/**
 * TrailRecommendationService (ADT version with TrailList / TrailHeap)
 *
 * Responsibilities:
 *  1. Recommend trails based on user preferences (length / difficulty / topic / pet / camping / wildlife).
 *  2. Progressive relaxation: start from strict matching and gradually relax constraints.
 *  3. Use custom ADTs: TrailList (list) and TrailHeap (priority queue) instead of java.util.List.
 *  4. Nearby popular recommendation: 70% distance + 30% preference.
 */
public class TrailRecommendationService {
    private final TrailIndex index;
    
    public TrailRecommendationService(TrailIndex idx) {
        this.index = idx;
    }

    /* ========================================================================
       0. Recommend For Current User (Landing banner entry)
       ======================================================================== */

    /**
     * Entry used by landing page banner.
     * - If topK <= 3: use simple recommend() logic.
     * - If topK  > 3 : upgrade to strong personalized version.
     */
    public TrailList<Trail> recommendForCurrentUser(int topK) {

        if (topK > 3) {
            // For larger K, use stronger personalized logic
            return personalRecommendForCurrentUser(topK);
        }

        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) return new TrailList<>();

        UserPreference pref = sessionOpt.get().getProfile().getPreferences();

        return recommend(
                pref.getTargetLength(),
                pref.getDifficulty(),
                pref.getTopic(),
                pref.isPetFriendly(),
                pref.isCampingAllowed(),
                pref.isPreferWildlife(),
                topK
        );
    }

    /* ========================================================================
       1. Strong Personalized Recommendation
       ======================================================================== */

    /**
     * Stronger personalized recommendation for current user.
     * Steps:
     *  1) Try strict filter using all preferences.
     *  2) If strict result count >= topK → score & return.
     *  3) Otherwise switch to progressiveFilter() to relax conditions gradually.
     */
    public TrailList<Trail> personalRecommendForCurrentUser(int topK) {

        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) return new TrailList<>();

        UserPreference pref = sessionOpt.get().getProfile().getPreferences();

        Double lengthPref = pref.getTargetLength() > 0 ? pref.getTargetLength() : null;

        // Step 1: strict filter based on all preferences
        TrailList<Trail> strict = filter(
                pref.getDifficulty(),
                lengthPref,
                pref.isPetFriendly(),
                pref.isCampingAllowed(),
                pref.getTopic() == null ? null : pref.getTopic().name().toLowerCase(),
                pref.isPreferWildlife(),
                null
        );

        if (strict.size() >= topK) {
            return sortByFinalScore(strict, pref, topK);
        }

        // Step 2: apply progressive relaxation
        TrailList<Trail> relaxed = progressiveFilter(
                lengthPref,
                pref.getDifficulty(),
                pref.getTopic(),
                pref.isPetFriendly(),
                pref.isCampingAllowed(),
                pref.isPreferWildlife(),
                topK
        );

        return sortByFinalScore(relaxed, pref, topK);
    }

    /* ========================================================================
       2. API: recommend() — keep original logic, ADT version
       ======================================================================== */

    /**
     * Core recommend API with explicit preference parameters.
     * Internally still uses progressiveFilter + final scoring.
     */
    public TrailList<Trail> recommend(
            double targetLen,
            Difficulty prefDiff,
            Topic prefTopic,
            boolean preferPet,
            boolean preferCamping,
            boolean preferWildlife,
            int topK
    ) {

        Double lengthPref = targetLen > 0 ? targetLen : null;

        TrailList<Trail> candidates = progressiveFilter(
                lengthPref, prefDiff, prefTopic,
                preferPet, preferCamping, preferWildlife,
                topK
        );

        var sessionOpt = AuthContext.currentUser();
        UserPreference pref = sessionOpt.isPresent()
                ? sessionOpt.get().getProfile().getPreferences()
                : new UserPreference();

        return sortByFinalScore(candidates, pref, topK);
    }

    /* ========================================================================
       3. STRICT FILTER (method name kept as filter)
       ======================================================================== */

    /**
     * Strict filter: all conditions must match (no relaxation).
     * Replaces old Set-based implementation with TrailList.
     */
    public TrailList<Trail> filter(
            Difficulty diff,
            Double length,
            Boolean pet,
            Boolean camp,
            String topicKey,
            Boolean wildlife,
            Object unused // kept for compatibility
    ) {
        TrailList<Trail> out = new TrailList<>();

        // index.getAll() returns a Java List; convert into iteration
        List<Trail> all = index.getAll();

        for (Trail t : all) {

            if (diff != null && t.getDifficulty() != diff)
                continue;

            if (topicKey != null &&
                    !t.getTopic().name().equalsIgnoreCase(topicKey))
                continue;

            if (length != null && Math.abs(t.getLength() - length) > 0.5)
                continue;

            if (pet != null && t.isPetFriendly() != pet)
                continue;

            if (camp != null && t.isCampingAllowed() != camp)
                continue;

            if (wildlife != null && t.isWildAnimalPossible() != wildlife)
                continue;

            out.add(t);
        }

        return out;
    }

    /* ========================================================================
       4. PROGRESSIVE FILTER (method name kept as progressiveFilter)
       ======================================================================== */

    /**
     * Progressive relaxation:
     *  1) Start with wildlife / camping / pet all respected.
     *  2) Relax wildlife → relax camping → relax pet-friendly.
     *  3) Then gradually widen length tolerance.
     *  4) Then loosen difficulty tolerance.
     *  5) Finally relax topic constraint.
     *
     * Stops as soon as we have >= topK candidates.
     */
    public TrailList<Trail> progressiveFilter(
            Double targetLen,
            Difficulty diff,
            Topic topic,
            Boolean pet,
            Boolean camp,
            Boolean wildlife,
            int topK
    ) {
        // Convert all trails into TrailList
        TrailList<Trail> all = fromJavaList(index.getAll());
        TrailList<Trail> result;

        // Step 1 – wildlife respected, others exact
        result = filterRelax(all, targetLen, diff, topic, pet, camp, wildlife, 0.5);
        if (result.size() >= topK) return result;

        // Step 2 – relax wildlife (ignore it)
        result = filterRelax(all, targetLen, diff, topic, pet, camp, null, 0.5);
        if (result.size() >= topK) return result;

        // Step 3 – relax camping
        result = filterRelax(all, targetLen, diff, topic, pet, null, null, 0.5);
        if (result.size() >= topK) return result;

        // Step 4 – relax pet-friendly
        result = filterRelax(all, targetLen, diff, topic, null, null, null, 0.5);
        if (result.size() >= topK) return result;

        // Step 5 – progressively relax length tolerance
        double[] lenBands = {1, 2, 3, 5};
        for (double tol : lenBands) {
            result = filterRelax(all, targetLen, diff, topic, null, null, null, tol);
            if (result.size() >= topK) return result;
        }

        // Step 6 – relax difficulty tolerance
        int[] diffTol = {0, 1, 2};
        for (int dt : diffTol) {
            result = filterRelaxDiff(all, targetLen, diff, topic, dt);
            if (result.size() >= topK) return result;
        }

        // Step 7 – finally return all trails as fallback
        return all;
    }

    /* ========================================================================
       5. RELAX HELPERS (all TrailList based)
       ======================================================================== */

    /**
     * Filter helper with soft length tolerance.
     */
    private TrailList<Trail> filterRelax(
            TrailList<Trail> all,
            Double targetLen,
            Difficulty diff,
            Topic topic,
            Boolean pet,
            Boolean camp,
            Boolean wildlife,
            double lenTolerance
    ) {

        TrailList<Trail> out = new TrailList<>();

        for (Trail t : all.toList()) {

            if (diff != null && t.getDifficulty() != diff) continue;
            if (topic != null && t.getTopic() != topic) continue;
            if (pet != null && t.isPetFriendly() != pet) continue;
            if (camp != null && t.isCampingAllowed() != camp) continue;
            if (wildlife != null && t.isWildAnimalPossible() != wildlife) continue;

            if (targetLen != null &&
                    Math.abs(t.getLength() - targetLen) > lenTolerance)
                continue;

            out.add(t);
        }

        return out;
    }

    /**
     * Filter helper that relaxes difficulty within tolerance band.
     */
    private TrailList<Trail> filterRelaxDiff(
            TrailList<Trail> all,
            Double targetLen,
            Difficulty prefDiff,
            Topic topic,
            int diffTolerance
    ) {

        TrailList<Trail> out = new TrailList<>();

        for (Trail t : all.toList()) {

            if (topic != null && t.getTopic() != topic)
                continue;

            if (prefDiff != null) {
                int d = Math.abs(t.getDifficulty().rank() - prefDiff.rank());
                if (d > diffTolerance) continue;
            }

            if (targetLen != null &&
                    Math.abs(t.getLength() - targetLen) > 5.0)
                continue;

            out.add(t);
        }

        return out;
    }

    /* ========================================================================
       6. FINAL SCORE & SORTING
       ======================================================================== */

    /**
     * Sort given TrailList by finalScore (desc) and cut to topK.
     */
    private TrailList<Trail> sortByFinalScore(TrailList<Trail> list, UserPreference pref, int topK) {

        Comparator<Trail> cmp = Comparator.<Trail>comparingDouble(
                t -> finalScore(t, pref)
        ).reversed();

        // Make a copy to avoid side effects, then sort using TrailList.sort()
        TrailList<Trail> copy = copyOf(list);
        copy.sort(cmp);

        TrailList<Trail> out = new TrailList<>();
        int limit = Math.min(topK, copy.size());
        for (int i = 0; i < limit; i++) {
            out.add(copy.get(i));
        }
        return out;
    }

    /**
     * Final score used for generic recommendation:
     *  - preferenceMatchScore (0–5)
     *  - distanceBandScore    (0.2–1.0)
     *  - intrinsicScore       (0–1)
     * Weighted as: 0.45 * pref + 0.35 * distance + 0.20 * intrinsic.
     */
    private double finalScore(Trail t, UserPreference pref) {

        double prefScore = preferenceMatchScore(t, pref);
        double distScore = distanceBandScore(t, pref);
        double intrinsic = intrinsicScore(t);

        return prefScore * 0.45 + distScore * 0.35 + intrinsic * 0.20;
    }

    /* ---------- preference score (0–5) ---------- */

    private double preferenceMatchScore(Trail t, UserPreference pref) {
        double s = 0.0;

        // 1) length closeness
        if (pref.getTargetLength() > 0) {
            double d = Math.abs(t.getLength() - pref.getTargetLength());
            s += Math.max(0, 1 - d / 10.0) * 3.0;
        }

        // 2) difficulty closeness
        if (pref.getDifficulty() != null) {
            int d = Math.abs(t.getDifficulty().rank() - pref.getDifficulty().rank());
            s += (d == 0 ? 2.0 : d == 1 ? 1.0 : 0.0);
        }

        // 3) topic match
        if (pref.getTopic() != null && t.getTopic() == pref.getTopic())
            s += 1.2;

        // 4) boolean preferences
        if (pref.isPetFriendly() && t.isPetFriendly()) s += 0.6;
        if (pref.isCampingAllowed() && t.isCampingAllowed()) s += 0.6;
        if (pref.isPreferWildlife() && t.isWildAnimalPossible()) s += 0.8;

        return s;
    }

    /* ---------- distance band score (0.2–1.0) ---------- */

    private double distanceBandScore(Trail t, UserPreference pref) {

        double dist = distanceMiles(
                pref.getPreferredLat(),
                pref.getPreferredLon(),
                t.getLat(),
                t.getLon()
        );

        if (dist <= 30) return 1.0;
        if (dist <= 60) return 0.8;
        if (dist <= 120) return 0.6;
        if (dist <= 200) return 0.4;

        return 0.2;
    }

    /* ---------- intrinsic quality score (0–1) ---------- */

    private double intrinsicScore(Trail t) {
        double s = 0.0;

        // length tiers
        if (t.getLength() < 2) s += 0.2;
        else if (t.getLength() < 5) s += 0.4;
        else if (t.getLength() < 8) s += 0.6;
        else s += 0.8;

        // visit hours tiers
        if (t.getVisitHours() < 2) s += 0.2;
        else if (t.getVisitHours() < 4) s += 0.4;
        else if (t.getVisitHours() < 6) s += 0.6;
        else s += 0.8;

        // elevation gain tiers
        if (t.getElevationGain() < 500) s += 1.0;
        else if (t.getElevationGain() < 1500) s += 0.8;
        else if (t.getElevationGain() < 3000) s += 0.6;
        else s += 0.4;

        return s / 3.0;
    }

    /* ========================================================================
       7. Nearby Popular Recommendation — 70% distance + 30% preference
       ======================================================================== */

    /**
     * Nearby-popular recommendation:
     *  - Uses progressiveFilter to collect enough candidates.
     *  - Distance score (band-based) takes 70%.
     *  - Preference score (normalized within candidates) takes 30%.
     *  - Ensures at least up to topK items if possible.
     */
    public TrailList<Trail> nearbyPopularRecommend(UserPreference pref, int topK) {

        // If user has no location set, fallback to personalized recommendation
        if (pref.getPreferredLat() == 0.0 && pref.getPreferredLon() == 0.0) {
            return personalRecommendForCurrentUser(topK);
        }

        // Ensure we have enough candidates (at least 10)
        int candidateTarget = Math.max(topK * 2, 10);

        Double lengthPref =
                (pref.getTargetLength() > 0) ? pref.getTargetLength() : null;

        // Step 1 — collect candidates via progressiveFilter
        TrailList<Trail> candidates = progressiveFilter(
                lengthPref,
                pref.getDifficulty(),
                pref.getTopic(),
                pref.isPetFriendly(),
                pref.isCampingAllowed(),
                pref.isPreferWildlife(),
                candidateTarget
        );

        if (candidates.isEmpty()) return new TrailList<>();

     // Compute max preference score in candidate set for normalization
        double maxPrefScore = 0.0;
        for (Trail t : candidates.toList()) {
            double score = preferenceMatchScore(t, pref);
            if (score > maxPrefScore) {
                maxPrefScore = score;
            }
        }

        // IMPORTANT: make a final copy for lambda capture
        final double maxPrefFinal = maxPrefScore;

        // Comparator: 80% distance + 20% normalized preference
        Comparator<Trail> cmp =
                Comparator.<Trail>comparingDouble(
                        t -> nearbyScore(t, pref, maxPrefFinal)
                ).reversed();

        // Use TrailHeap (ADT) to get top-K
        TrailHeap<Trail> heap = new TrailHeap<>(cmp);
        for (Trail t : candidates.toList()) {
            heap.add(t);
        }

        TrailList<Trail> out = new TrailList<>();
        int limit = Math.min(topK, heap.getCurrentSize());
        for (int i = 0; i < limit; i++) {
            out.add(heap.removeTop());
        }

        return out;
    }

    /**
     * Score used only for nearbyPopularRecommend:
     *  - distanceBandScore: 0.2 ~ 1.0 (already normalized).
     *  - preferenceScore : normalized into 0 ~ 1 within candidate set.
     *  - Combined as 0.7 * distance + 0.3 * preference.
     */
    private double nearbyScore(Trail t, UserPreference pref, double maxPrefScore) {

        double distScore = distanceBandScore(t, pref); // already in [0.2, 1.0]

        double rawPref = preferenceMatchScore(t, pref);
        double prefNorm = (maxPrefScore > 0.0) ? (rawPref / maxPrefScore) : 0.0;

        return 0.8 * distScore + 0.2 * prefNorm;
    }

    /* ========================================================================
       8. Utilities: distance & TrailList helpers
       ======================================================================== */

    /** Haversine distance between two GPS points (in miles). */
    private double distanceMiles(double lat1, double lon1, double lat2, double lon2) {
        double R = 3958.8;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    /** Convert a java.util.List<Trail> to TrailList<Trail>. */
    private TrailList<Trail> fromJavaList(List<Trail> src) {
        TrailList<Trail> out = new TrailList<>();
        for (Trail t : src) out.add(t);
        return out;
    }

    /** Create a shallow copy of a TrailList. */
    private TrailList<Trail> copyOf(TrailList<Trail> src) {
        TrailList<Trail> copy = new TrailList<>();
        for (Trail t : src.toList()) copy.add(t);
        return copy;
    }
}
