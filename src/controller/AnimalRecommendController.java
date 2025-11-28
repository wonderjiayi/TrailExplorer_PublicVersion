package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.animal.Animal;
import model.trail.TrailList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnimalRecommendController {

    @FXML private Label titleLabel;
    @FXML private FlowPane animalPane;

    private TrailList<Animal> allAnimals;

    private int month;

    // -------------------------------------------------------
    //  ⭐
    // -------------------------------------------------------
    private Image loadSafeImage(String path) {
        try {
            if (path != null && !path.isEmpty()) {
                Image img = new Image(path);
                if (!img.isError()) return img;
            }
        } catch (Exception ignored) {}

        // fallback
        return new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/ui/images/animals/default.jpg")
                )
        );
    }

    // -------------------------------------------------------
    //  
    // -------------------------------------------------------
    public void setAnimalData(TrailList<Animal> animals, int month) {
        this.allAnimals = animals;
        this.month = month;

        titleLabel.setText("Animals Active in Month " + month);
        loadRecommendations();
    }

    // -------------------------------------------------------
    //  
    // -------------------------------------------------------
    private void loadRecommendations() {
        animalPane.getChildren().clear();

        TrailList<Animal> recList = new TrailList<>();

        for (int i = 0; i < allAnimals.size(); i++) {
            Animal a = allAnimals.get(i);
            if (a.getActiveMonths().contains(month)) {
                recList.add(a);
            }
        }

        for (Animal a : recList.toList()) {
            VBox card = createAnimalCard(a);
            animalPane.getChildren().add(card);
        }
    }

    // -------------------------------------------------------
    // 
    // -------------------------------------------------------
    private VBox createAnimalCard(Animal a) {
        VBox box = new VBox(10);
        
   
        box.setMinWidth(165);
        box.setMaxWidth(165);
        box.setPrefWidth(165);
       
        box.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #ddd;
            -fx-border-radius: 12;
            -fx-background-radius: 12;
            -fx-padding: 14;
            -fx-cursor: hand;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);
        """);

   
        ImageView img = new ImageView(loadSafeImage(a.getImage()));
        img.setFitWidth(120);
        img.setFitHeight(120);
        img.setPreserveRatio(true);

    
        Label name = new Label(a.getName());
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        name.setMaxWidth(150);
        name.setWrapText(true);
        
        // Badge
        Label badge = new Label(a.getGroup().name());
        badge.setStyle("""
            -fx-background-color: #e0f2f1;
            -fx-text-fill: #00695c;
            -fx-padding: 3 10;
            -fx-font-size: 11px;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-font-weight: bold;
        """);

        // activeDays
        int activeDayCount = a.getActiveDays().size();
        double score = Math.min(activeDayCount / 10.0, 1.0); // cap at 1.0

        ProgressBar activeBar = new ProgressBar(score);
        activeBar.setPrefWidth(140);
        activeBar.setStyle("-fx-accent: #4caf50;");

        Label activeLabel = new Label("Activity Level: " + activeDayCount + " days");
        activeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        // 
        box.getChildren().addAll(
                img,
                name,
                badge,
                activeBar,
                activeLabel
        );

        // 
        box.setOnMouseClicked(e -> {
            AppNavigator.showPageWithData(
                    "/ui/animal_detail.fxml",
                    (AnimalDetailController ctrl) -> {
                    	TrailList<Animal> sameGroup = new TrailList<>();
                    	for (int i = 0; i < allAnimals.size(); i++) {
                    	    Animal an = allAnimals.get(i);
                    	    if (an.getGroup() == a.getGroup()) {
                    	        sameGroup.add(an);
                    	    }
                    	}

                        ctrl.setAnimal(a, month, sameGroup, a.getGroup());
                    }
            );
        });

        return box;
    }

}

