package model.trail;

import java.util.*;

/**
 * Multi-dimensional index using ONLY your custom ADTs:
 * - TrailHashMap
 * - TrailTreeMap
 * - TrailList
 *
 * Index fields:
 *  topic, difficulty, length, visitHours, petFriendly, campingAllowed, wildlife
 */
public class TrailIndex {
    // ─────── Primary storage ───────
    private final TrailList<Trail> allTrails = new TrailList<>();
    private final TrailHashMap<String, Trail> nameIndex = new TrailHashMap<>();
    // ─────── Secondary index (custom ADTs) ───────
    private final TrailHashMap<Difficulty, TrailList<Trail>> difficultyIndex =
            new TrailHashMap<>();
    // length → list of trails（≤ maxLength range query）
    private final TrailTreeMap<Double, TrailList<Trail>> lengthIndex =
            new TrailTreeMap<>();
    // visitHours → list of trails（≤ maxVisitHours range query）
    private final TrailTreeMap<Double, TrailList<Trail>> visitHoursIndex =
            new TrailTreeMap<>();
    // feature → list of trails (pet, camping, bird, icy, wildlife)
    private final TrailHashMap<String, TrailList<Trail>> featureIndex =
            new TrailHashMap<>();
    // topic → list of trails
    private final TrailHashMap<String, TrailList<Trail>> topicIndex =
            new TrailHashMap<>();

    // ─────── Constructor ───────
    public TrailIndex() {
        // init difficulty
        for (Difficulty d : Difficulty.values()) {
            difficultyIndex.put(d, new TrailList<>());
        }
        // init boolean feature categories
        featureIndex.put("pet_friendly", new TrailList<>());
        featureIndex.put("camping", new TrailList<>());
        featureIndex.put("bird", new TrailList<>());
        featureIndex.put("animal", new TrailList<>());   // 保留占位（目前未单独使用）
        featureIndex.put("icy", new TrailList<>());
        featureIndex.put("wildlife", new TrailList<>());
    }
    // ─────── Add Trail ───────
    public void addTrail(Trail t) {
        // 1. master list
        allTrails.add(t);
        // 2. name index
        nameIndex.put(t.getName().toLowerCase(), t);
        // 3. difficulty index
        TrailList<Trail> diffList = difficultyIndex.get(t.getDifficulty());
        if (diffList == null) {
            diffList = new TrailList<>();
            difficultyIndex.put(t.getDifficulty(), diffList);
        }
        diffList.add(t);

        // 4. length index
        TrailList<Trail> lenList = lengthIndex.get(t.getLength());
        if (lenList == null) {
            lenList = new TrailList<>();
            lengthIndex.put(t.getLength(), lenList);
        }
        lenList.add(t);

        // 5. visitHours index
        TrailList<Trail> vhList = visitHoursIndex.get(t.getVisitHours());
        if (vhList == null) {
            vhList = new TrailList<>();
            visitHoursIndex.put(t.getVisitHours(), vhList);
        }
        vhList.add(t);

        // 6. features
        if (t.isPetFriendly()) {
            TrailList<Trail> list = featureIndex.get("pet_friendly");
            if (list == null) {
                list = new TrailList<>();
                featureIndex.put("pet_friendly", list);
            }
            list.add(t);
        }

        if (t.isCampingAllowed()) {
            TrailList<Trail> list = featureIndex.get("camping");
            if (list == null) {
                list = new TrailList<>();
                featureIndex.put("camping", list);
            }
            list.add(t);
        }

        if (t.isBirdSpotted()) {
            TrailList<Trail> list = featureIndex.get("bird");
            if (list == null) {
                list = new TrailList<>();
                featureIndex.put("bird", list);
            }
            list.add(t);
        }

        if (t.isWildAnimalPossible()) {
            TrailList<Trail> list = featureIndex.get("wildlife");
            if (list == null) {
                list = new TrailList<>();
                featureIndex.put("wildlife", list);
            }
            list.add(t);
        }

        if (t.isIcyTrail()) {
            TrailList<Trail> list = featureIndex.get("icy");
            if (list == null) {
                list = new TrailList<>();
                featureIndex.put("icy", list);
            }
            list.add(t);
        }

        // 7. topic index
        String topicKey = t.getTopic().name().toLowerCase();
        TrailList<Trail> tp = topicIndex.get(topicKey);
        if (tp == null) {
            tp = new TrailList<>();
            topicIndex.put(topicKey, tp);
        }
        tp.add(t);
    }


    // ─────── Accessors ───────
    public List<Trail> getFeature(String feature) {
        TrailList<Trail> list = featureIndex.get(feature);
        return list == null ? List.of() : list.toList();
    }

    public List<Trail> getByTopic(String topic) {
        TrailList<Trail> list = topicIndex.get(topic.toLowerCase());
        return list == null ? List.of() : list.toList();
    }

    public List<Trail> getAll() {
        return allTrails.toList();
    }

    public List<Trail> getByDifficulty(Difficulty d) {
        TrailList<Trail> list = difficultyIndex.get(d);
        return list != null ? list.toList() : List.of();
    }

    /** length ≤ maxLength */
    public List<Trail> getWithinLength(double maxLength) {
        List<Trail> result = new ArrayList<>();
        for (Double key : lengthIndex.keySet()) {
            if (key <= maxLength) {
                TrailList<Trail> list = lengthIndex.get(key);
                if (list != null) result.addAll(list.toList());
            }
        }
        return result;
    }

    /** visitHours ≤ maxVisitHours */
    public List<Trail> getWithinVisitHours(double maxVisitHours) {
        List<Trail> result = new ArrayList<>();
        for (Trail t : allTrails.toList()) {
            if (t.getVisitHours() <= maxVisitHours) {
                result.add(t);
            }
        }
        return result;
    }


    // ─────── Combined Filtering (expanded to include visitHours) ───────
    public Set<Trail> filter(
            Difficulty diff,
            Double maxLength,
            Boolean petFriendly,
            Boolean camping,
            String topic,
            Boolean wildlife,
            Double maxVisitHours
    ) {
        Set<Trail> candidates = new HashSet<>(getAll()); // start with all trails
        if (diff != null)
            candidates.retainAll(getByDifficulty(diff)); // match difficulty
        if (maxLength != null)
            candidates.retainAll(getWithinLength(maxLength)); // length <= maxLength
        if (maxVisitHours != null)
            candidates.retainAll(getWithinVisitHours(maxVisitHours)); // visitHours <= maxVisitHours
        if (petFriendly != null && petFriendly)
            candidates.retainAll(getFeature("pet_friendly")); // must be pet-friendly
        if (camping != null && camping)
            candidates.retainAll(getFeature("camping")); // must allow camping
        if (topic != null)
            candidates.retainAll(getByTopic(topic)); // match topic type
        if (wildlife != null && wildlife)
            candidates.retainAll(getFeature("wildlife")); // wildlife desired
        return candidates; // final filtered result
    }


    // ─────── Remove Trail ───────
    public boolean removeTrail(Trail t) {

        boolean removed = allTrails.remove(t);
        if (!removed) return false;

        // name index
        nameIndex.remove(t.getName().toLowerCase());

        // difficulty index
        TrailList<Trail> diffList = difficultyIndex.get(t.getDifficulty());
        if (diffList != null) diffList.remove(t);

        // length index
        TrailList<Trail> list1 = lengthIndex.get(t.getLength());
        if (list1 != null) list1.remove(t);

        // visitHours index
        TrailList<Trail> list2 = visitHoursIndex.get(t.getVisitHours());
        if (list2 != null) list2.remove(t);

        // feature index
        for (String key : featureIndex.keySet()) {
            TrailList<Trail> fl = featureIndex.get(key);
            if (fl != null) fl.remove(t);
        }

        // topic index
        for (String key : topicIndex.keySet()) {
            TrailList<Trail> tl = topicIndex.get(key);
            if (tl != null) tl.remove(t);
        }

        return true;
    }


    public int size() {
        return allTrails.size();
    }

    public void printStats() {
        System.out.printf(
                "Trails=%d | Features=%d | Topics=%d | VisitHoursIndexedKeys=%d%n",
                size(),
                featureIndex.size(),
                topicIndex.size(),
                // 这里的 size 表示有多少不同的 visitHours key
                visitHoursIndex.size()
        );
    }

    public Trail getByName(String trailName) {
        if (trailName == null) return null;
        return nameIndex.get(trailName.toLowerCase());
    }
}