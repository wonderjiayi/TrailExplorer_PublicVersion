package controller;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.AnimalDataLoader;
import data.GroupSeeder;
import data.TrailDataLoader;
import model.animal.Animal;
import model.group.Group;
import model.trail.Trail;
import model.trail.TrailIndex;
import model.trail.TrailList;
import repo.ActivityRepository;
import repo.GroupRepository;
import service.ActivityService;
import service.GroupSearchService;

public class GlobalData {

    public static TrailIndex index = new TrailIndex();

    private static final TrailList<Group> liveGroups = new TrailList<>();
    private static final GroupRepository GROUPS = new GroupRepository("data/groups.csv");
    private static boolean loaded = false;

    private static List<Animal> allAnimals = new ArrayList<>();

    /** Get all animals (read-only list) */
    public static List<Animal> getAllAnimals() {
        return Collections.unmodifiableList(allAnimals);
    }

    /** Load animal data from JSON */
    public static void loadAnimals() {
        allAnimals = AnimalDataLoader.loadAnimals("animalData.json");
        if (allAnimals == null) allAnimals = new ArrayList<>();

        System.out.println("🐾 Loaded animals count = " + allAnimals.size());
    }


    /** TrailIndex.getAll() still returns java.util.List<Trail>, which is fine */
    public static List<Trail> getAllTrails() {
        return index.getAll();
    }


    public static TrailList<Group> getAllGroups() {
        return liveGroups;
    }

    public static void addGroup(Group g) {
        if (g != null) {
            liveGroups.add(g);
            persistGroups();
        }
    }

    public static void persistGroups() {
        GROUPS.saveAll(liveGroups);
    }

    // ───────────────────────────────────────────────
    // Global loading entry (called by Main.start or AppNavigator.goToRootLayout)
    // ───────────────────────────────────────────────
    public static void loadOrSeedOnce() {
        if (loaded) return;

        // 1) Load Trails
        // data.TrailDataLoader.loadSampleTrails(index);
        TrailDataLoader.loadFromJson(index, "src/application/trailsData.json");
        System.out.println("Loaded trails: " + GlobalData.index.size());

        // 2) Load Groups (CSV -> TrailList<Group>)
        liveGroups.clear();
        var loadedGroups = GROUPS.loadAll(index);  // TrailList<Group>
        for (int i = 0; i < loadedGroups.size(); i++) {
            Group g = loadedGroups.get(i);
            if (g != null) liveGroups.add(g);
        }

        // If there are no groups in file, generate seed groups per Trail
        if (liveGroups.size() == 0) {

            // 1) Convert getAllTrails() List<Trail> into TrailList<Trail>
            TrailList<Trail> trailList = new TrailList<>();
            for (Trail t : getAllTrails()) {
                trailList.add(t);
            }

            // 2) Call updated GroupSeeder: TrailList<Trail> -> TrailList<Group>
            TrailList<Group> seeds = GroupSeeder.makeSampleGroupsPerTrail(trailList);

            // 3) Auto-join demo user to several seed groups
            model.auth.AuthContext.ensureDemoUser();
            var cur = model.auth.AuthContext.currentUser();
            cur.ifPresent(sess -> {
                var me = sess.getProfile();
                for (int i = 0; i < Math.min(3, seeds.size()); i++) {
                    int party = (i % 2 == 0) ? 2 : 1;
                    Group g = seeds.get(i);
                    if (g != null && g.canJoin(party)) {
                        g.join(me, party);
                    }
                }
            });

            // 4) Push seeds (TrailList<Group>) into global liveGroups (TrailList<Group>)
            for (int i = 0; i < seeds.size(); i++) {
                Group g = seeds.get(i);
                if (g != null) {
                    liveGroups.add(g);
                }
            }

            persistGroups();
        }

        loadAnimals();

        loaded = true;
    }

    public static void bootstrap(GroupSearchService svc) {
        TrailList<Trail> trailList = new TrailList<>();
        for (Trail t : getAllTrails()) {
            trailList.add(t);
        }

        TrailList<Group> groupList = getAllGroups();

        svc.indexTrails(trailList);
        svc.indexGroups(groupList);
    }

    public static final ActivityService ACTIVITY =
            new ActivityService(new ActivityRepository(Paths.get("data/activity.csv")));
}
