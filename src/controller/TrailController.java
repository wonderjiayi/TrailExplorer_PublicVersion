package controller;

import service.*;
import java.util.List;

import application.AppNavigator;
import model.trail.*;

/**
 * Acts as a bridge between UI and service layer.
 */
public class TrailController {

    private final TrailIndex index = GlobalData.index;

    private final TrailSearchService searchService = new TrailSearchService(index);
    private final TrailRecommendationService recService = new TrailRecommendationService(index);

    /** Add a trail to index */
    public void addTrail(Trail t) {
        index.addTrail(t);
    }

    /** Print all trails */
    public void printAllTrails() {
        index.getAll().forEach(System.out::println);
    }
    
    /** Search by multi-filters：difficulty + length + pet + camp + topic */ 
    public List<Trail> searchTrails(Difficulty diff, Double maxLen,
                                    Boolean pet, Boolean camp, String topic,Boolean wildlife) {
        return searchService.search(diff, maxLen, pet, camp, topic,wildlife);
    }

    /** Fuzzy search by name/park/topic/state */
    public List<Trail> fuzzy(String keyword) {
        return searchService.fuzzy(keyword);
    }

    /** 
     * Preference-based recommendation 
     */
    public List<Trail> recommend(double targetLen,
                                 Difficulty prefDiff,
                                 Topic prefTopic,
                                 boolean preferPet,
                                 boolean preferCamp,
                                 boolean preferWildlife,    // ⭐ 新增字段
                                 int topK) {

        return (List<Trail>) recService.recommend(
                targetLen,
                prefDiff,
                prefTopic,
                preferPet,
                preferCamp,
                preferWildlife,
                topK
        );
    }
}