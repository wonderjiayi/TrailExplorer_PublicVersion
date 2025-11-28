package data;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import controller.TrailController;
import model.trail.*;

/**
 * Loads initial Trail dataset into the system.
 * Each trail is unique and ensures keyword hits for search & recommendation.
 */
public class TrailDataLoader {
	private static final Gson gson = new Gson();

    public static void loadFromJson(TrailIndex index, String jsonPath) {
        try (FileReader reader = new FileReader(jsonPath)) {

            Type listType = new TypeToken<List<Trail>>(){}.getType();
            List<Trail> trails = gson.fromJson(reader, listType);

            for (Trail t : trails) {
                index.addTrail(t);
            }

            System.out.println("Loaded " + trails.size() + " trails from JSON.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to load trails from: " + jsonPath);
        }
    }
//    public static void loadSampleTrails(TrailIndex index) {
//        // Lake trails
//        index.addTrail(new TrailBuilder()
//                .name("Echo Lake Loop")
//                .park("Franconia Notch State Park")
//                .state("NH")
//                .zipcode("03251")
//                .topic(Topic.LAKE)
//                .difficulty(Difficulty.EASY)
//                .length(2.4)
//                .visitHours(1.5)
//                .elevationGain(150)
//                .petFriendly(true)
//                .campingAllowed(false)
//                .build());
//
//        index.addTrail(new TrailBuilder()
//                .name("Crystal Lake Trail")
//                .park("White Mountain National Forest")
//                .state("NH")
//                .zipcode("03246")
//                .topic(Topic.LAKE)
//                .difficulty(Difficulty.MODERATE)
//                .length(3.2)
//                .visitHours(2.0)
//                .elevationGain(250)
//                .petFriendly(true)
//                .campingAllowed(true)
//                .build());
//
//        // Mountain trails
//        index.addTrail(new TrailBuilder()
//                .name("Mount Lafayette Summit")
//                .park("Franconia Notch State Park")
//                .state("NH")
//                .zipcode("03251")
//                .topic(Topic.MOUNTAIN)
//                .difficulty(Difficulty.HARD)
//                .length(8.9)
//                .visitHours(6.5)
//                .elevationGain(3900)
//                .petFriendly(true)
//                .campingAllowed(true)
//                .build());
//
//        index.addTrail(new TrailBuilder()
//                .name("Wildlife Ridge Trail")
//                .park("White Mountain National Forest")
//                .state("NH")
//                .zipcode("03246")
//                .topic(Topic.MOUNTAIN)
//                .difficulty(Difficulty.MODERATE)
//                .length(5.2)
//                .visitHours(3.0)
//                .elevationGain(1200)
//                .petFriendly(true)
//                .campingAllowed(true)
//                .build());
//
//        // River trails
//        index.addTrail(new TrailBuilder()
//                .name("River Bend Path")
//                .park("Middlesex Fells Reservation")
//                .state("MA")
//                .zipcode("02180")
//                .topic(Topic.RIVER)
//                .difficulty(Difficulty.MODERATE)
//                .length(3.6)
//                .visitHours(2.0)
//                .elevationGain(250)
//                .petFriendly(true)
//                .campingAllowed(false)
//                .build());
//
//        index.addTrail(new TrailBuilder()
//                .name("Bear Brook Trail")
//                .park("Bear Brook State Park")
//                .state("NH")
//                .zipcode("03032")
//                .topic(Topic.RIVER)
//                .difficulty(Difficulty.MODERATE)
//                .length(4.8)
//                .visitHours(2.5)
//                .elevationGain(600)
//                .petFriendly(true)
//                .campingAllowed(true)
//                .build());
//
//        // Beach trails
//        index.addTrail(new TrailBuilder()
//                .name("Beach Walk Trail")
//                .park("Cape Cod National Seashore")
//                .state("MA")
//                .zipcode("02642")
//                .topic(Topic.BEACH)
//                .difficulty(Difficulty.EASY)
//                .length(1.8)
//                .visitHours(1.0)
//                .elevationGain(20)
//                .petFriendly(false)
//                .campingAllowed(false)
//                .build());
//
//        index.addTrail(new TrailBuilder()
//                .name("Seaside Boardwalk")
//                .park("Revere Beach Reservation")
//                .state("MA")
//                .zipcode("02151")
//                .topic(Topic.BEACH)
//                .difficulty(Difficulty.EASY)
//                .length(2.0)
//                .visitHours(1.2)
//                .elevationGain(10)
//                .petFriendly(true)
//                .campingAllowed(false)
//                .build());
//
//        // Forest / Family trails
//        index.addTrail(new TrailBuilder()
//                .name("Pine Hill Nature Walk")
//                .park("Pine Hill Preserve")
//                .state("MA")
//                .zipcode("01923")
//                .topic(Topic.FOREST)
//                .difficulty(Difficulty.EASY)
//                .length(1.5)
//                .visitHours(1.0)
//                .elevationGain(50)
//                .petFriendly(true)
//                .campingAllowed(false)
//                .build());
//
//        index.addTrail(new TrailBuilder()
//                .name("Great Brook Farm Loop")
//                .park("Great Brook Farm State Park")
//                .state("MA")
//                .zipcode("01741")
//                .topic(Topic.FOREST)
//                .difficulty(Difficulty.MODERATE)
//                .length(3.2)
//                .visitHours(2.0)
//                .elevationGain(200)
//                .petFriendly(true)
//                .campingAllowed(false)
//                .build());
//    }
}