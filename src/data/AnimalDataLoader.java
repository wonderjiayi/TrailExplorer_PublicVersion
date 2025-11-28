package data;

import com.google.gson.*;
import model.trail.TrailList;

import com.google.gson.reflect.TypeToken;
import model.animal.Animal;
import model.animal.AnimalGroup;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Loader for animal data with extended fields:
 * - icon, image, activeDays, activeMonths, recentSpots
 */
public class AnimalDataLoader {

    public static List<Animal> loadAnimals(String filePath) {
        List<Animal> animals = new ArrayList<>();
        Gson gson = new Gson();

        try {
            InputStream inputStream;

           
            if (filePath.startsWith("/")) {
                inputStream = AnimalDataLoader.class.getResourceAsStream(filePath);
            } else {
                inputStream = AnimalDataLoader.class.getResourceAsStream("/application/" + filePath);
            }

     
            if (inputStream == null) {
                File file = new File(filePath);
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                } else {
                    throw new FileNotFoundException("Cannot find " + filePath);
                }
            }

 
            InputStreamReader reader = new InputStreamReader(inputStream);
            Type listType = new TypeToken<List<JsonObject>>() {}.getType();
            List<JsonObject> jsonList = gson.fromJson(reader, listType);

            for (JsonObject obj : jsonList) {
                Animal animal = new Animal();
                animal.setId(obj.get("id").getAsInt());
                animal.setName(obj.get("name").getAsString());
                animal.setGroup(AnimalGroup.valueOf(obj.get("group").getAsString()));
                animal.setHabitat(obj.get("habitat").getAsString());

                // icon + image
                animal.setIcon(obj.has("icon") ? obj.get("icon").getAsString() : "/icons/default.png");
                animal.setImage(obj.has("image") ? obj.get("image").getAsString() : "/images/animals/default.jpg");

                // description
                animal.setDescription(obj.has("description")
                        ? obj.get("description").getAsString()
                        : "No description available.");

                // activeMonths
                TrailList<Integer> months = new TrailList<>();

                if (obj.has("activeMonths")) {
                    for (JsonElement e : obj.getAsJsonArray("activeMonths")) {
                        months.add(e.getAsInt());
                    }
                } else {
                    months.add(3);
                    months.add(4);
                    months.add(5);
                }

                animal.setActiveMonths(months);


                // activeDays
                TrailList<Integer> days = new TrailList<>();

                if (obj.has("activeDays")) {
                    for (JsonElement e : obj.getAsJsonArray("activeDays")) {
                        days.add(e.getAsInt());
                    }
                } else {
                    Random rand = new Random();
                    for (int i = 0; i < 5; i++) {
                        days.add(rand.nextInt(28) + 1);
                    }
                }

                animal.setActiveDays(days);

                // recentSpots
                TrailList<String> spots = new TrailList<>();

                if (obj.has("recentSpots")) {
                    for (JsonElement e : obj.getAsJsonArray("recentSpots")) {
                        spots.add(e.getAsString());
                    }
                } else {
                    spots.add("Unknown location");
                }

                animal.setRecentSpots(spots);


                animals.add(animal);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Failed to load animals: " + e.getMessage());
        }

        return animals;
    }
}

