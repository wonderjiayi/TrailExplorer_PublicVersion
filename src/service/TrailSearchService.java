package service;

import java.util.ArrayList;
import java.util.List;

import model.trail.*;

/**
 * Handles trail searching logic (fuzzy + multi-criteria)
 */
public class TrailSearchService {
    private final TrailIndex index;

    public TrailSearchService(TrailIndex index) {
        this.index = index;
    }


    /** 🔍 Fuzzy search + multi-criteria using TrailIndex acceleration */
    public List<Trail> fuzzy(String keyword) {
        if (keyword == null || keyword.isEmpty())
            return new ArrayList<>();

        String[] tokens = keyword.toLowerCase().split("\\s+");

        // ----------- parsed structured conditions -----------
        Difficulty diff = null;
        Boolean pet = null;
        Boolean camp = null;
        Double maxLen = null;
        Topic topic = null;

        List<String> textTokens = new ArrayList<>();

        for (String tk : tokens) {

            // difficulty
            if (tk.equals("easy")) { diff = Difficulty.EASY; continue; }
            if (tk.equals("moderate") || tk.equals("mod")) { diff = Difficulty.MODERATE; continue; }
            if (tk.equals("hard")) { diff = Difficulty.HARD; continue; }

            // topic
            switch (tk) {
                case "lake": topic = Topic.LAKE; continue;
                case "mountain": topic = Topic.MOUNTAIN; continue;
                case "river": topic = Topic.RIVER; continue;
                case "beach": topic = Topic.BEACH; continue;
                case "forest": topic = Topic.FOREST; continue;
            }

            // boolean features
            if (tk.equals("pet") || tk.equals("petfriendly")) { pet = true; continue; }
            if (tk.equals("camp") || tk.equals("camping")) { camp = true; continue; }

            // length pattern: <3, <=3, 3mi
            if (tk.matches("<\\d+")) {
                maxLen = Double.parseDouble(tk.substring(1));
                continue;
            }

            if (tk.matches("\\d+(\\.\\d+)?")) {
                maxLen = Double.parseDouble(tk);
                continue;
            }

            textTokens.add(tk);
        }

        // ---------- Step1: fuzzy text match ----------
        List<Trail> textMatched;

        if (!textTokens.isEmpty()) {
            textMatched = new ArrayList<>();
            for (Trail t : index.getAll()) {
                String text = (t.getName() + " " + t.getPark() + " " +
                               t.getTopic() + " " + t.getState()).toLowerCase();

                boolean ok = true;
                for (String tk : textTokens) {
                    if (!text.contains(tk)) { ok = false; break; }
                }
                if (ok) textMatched.add(t);
            }
        } else {
            textMatched = index.getAll();
        }

        // ---------- Step2: use structured conditions ----------
        List<Trail> filtered = new ArrayList<>();

        for (Trail t : textMatched) {

            if (diff != null && t.getDifficulty() != diff) continue;
            if (topic != null && t.getTopic() != topic) continue;
            if (pet != null && t.isPetFriendly() != pet) continue;
            if (camp != null && t.isCampingAllowed() != camp) continue;
            if (maxLen != null && t.getLength() > maxLen) continue;

            filtered.add(t);
        }

        return filtered;
    }


    /** Structured search — uses TrailIndex.filter() */
    public List<Trail> search(Difficulty diff, Double maxLen,
                              Boolean pet, Boolean camp, String topic, Boolean wildlife) {

        // ⭐ Key optimization: use TrailIndex.filter()
        return new ArrayList<>(
                index.filter(
                        diff,
                        maxLen,
                        pet,
                        camp,
                        topic,
                        wildlife,
                        null   // maxVisitHours
                )
        );
    }


    // Predefined recommendation templates
    public List<Trail> recommendGroupFriendly() {
        return index.getWithinLength(5.0)
                .stream()
                .sorted((a, b) -> {
                    int c1 = Double.compare(a.getLength(), b.getLength());
                    if (c1 != 0) return c1;
                    return a.getDifficulty().compareTo(b.getDifficulty());
                })
                .limit(5)
                .toList();
    }

    public List<Trail> recommendWildlife() {
        // leverage index for topic filtering
        List<Trail> forest = index.getByTopic("forest");
        List<Trail> river = index.getByTopic("river");

        List<Trail> out = new ArrayList<>();
        out.addAll(forest);
        out.addAll(river);

        return out.stream()
                .filter(t -> t.getLength() <= 4.0)
                .limit(5)
                .toList();
    }

    public List<Trail> recommendPersonal() {
        return index.getByDifficulty(Difficulty.MODERATE)
                .stream()
                .filter(t -> t.getLength() >= 3 && t.getLength() <= 6)
                .filter(Trail::isPetFriendly)
                .limit(5)
                .toList();
    }

    public List<Trail> recommendNearbyPopular() {
        return index.getAll().stream()
                .sorted((a, b) -> {
                    int c = Double.compare(a.getLength(), b.getLength());
                    if (c != 0) return c;
                    return a.getDifficulty().compareTo(b.getDifficulty());
                })
                .limit(5)
                .toList();
    }
}