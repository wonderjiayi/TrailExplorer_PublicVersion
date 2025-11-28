package controller;

import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import model.animal.Animal;
import model.animal.AnimalGroup;
import model.trail.Trail;
import model.trail.TrailList;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.AppNavigator;

/**
 * Controller for displaying animals under a selected group.
 * Previously used for trail results; now expanded for animal browsing.
 */
public class AnimalResultsController {

    
    @FXML private Label titleLabel;
    @FXML private Label monthLabel;
    @FXML private FlowPane resultsPane;
    @FXML private Button backButton;

    
    private TrailList<Animal> displayedAnimals = new TrailList<>();

    private List<Trail> recommendedTrails = new ArrayList<>();
    private List<Trail> allTrails = new ArrayList<>();

    private AnimalGroup selectedGroup;

    private AnimalGroup currentGroup;
    private int currentMonth;

    // ---------------------------
    //
    // ---------------------------
    public void setGroup(AnimalGroup group, List<Animal> allAnimals, int month) {
        this.currentGroup = group;
        this.currentMonth = month;

        this.allTrails = GlobalData.getAllTrails();
        titleLabel.setText("Animal Group: " + group.name());
        monthLabel.setText("Month: " + month);

        
        displayedAnimals.clear();
        for (Animal a : allAnimals) {
            if (a.getGroup() == group) {
                displayedAnimals.add(a);
            }
        }
        populateAnimalCards();
    }
    
    /**
     * AnimalController
     */
    public void setResults(List<Trail> trails, AnimalGroup group, int month) {
        this.recommendedTrails = trails;
        this.selectedGroup = group;
        this.currentMonth = month;

       
        titleLabel.setText("Recommended Trails for " + group.name());
        monthLabel.setText("Month: " + month);

        populateResultsList();
    }
    
    /**
     * 
     */
    private void populateResultsList() {
        resultsPane.getChildren().clear();

        if (recommendedTrails == null || recommendedTrails.isEmpty()) {
            Label noData = new Label("No suitable trails found.");
            noData.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            resultsPane.getChildren().add(noData);
            return;
        }

        for (Trail t : recommendedTrails) {
            VBox card = new VBox(5);
            card.setPrefWidth(200);
            card.setStyle(
                "-fx-background-color: white;" +
                "-fx-padding: 10;" +
                "-fx-border-color: #cfd8dc;" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
            );

            Label nameLabel = new Label(t.getName());
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label infoLabel = new Label(
                String.format("Length: %.1f mi\nDifficulty: %s",
                    t.getLength(), t.getDifficulty())
            );
            infoLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

            Button detailBtn = new Button("View Trail");
            detailBtn.setStyle("-fx-background-color: #4a7c59; -fx-text-fill: white;");
            detailBtn.setOnAction(e -> openTrailDetail(t));

            card.getChildren().addAll(nameLabel, infoLabel, detailBtn);
            resultsPane.getChildren().add(card);
        }
    }
    
    /**  Trail Recommend */
    private void openTrailDetail(Trail t) {
        AppNavigator.showPageWithData(
            "/ui/trail_recommend.fxml",
            (TrailRecommendController ctrl) -> {
    
                TrailList<Trail> single = new TrailList<>();

                single.add(t);
                ctrl.setTrails(single, selectedGroup);
            }
        );
    }



 // ---------------------------
 // 🐾 
 // ---------------------------
 private void populateAnimalCards() {
     resultsPane.getChildren().clear();

     if (displayedAnimals.isEmpty()) {
         Label empty = new Label("No animals found for this group.");
         resultsPane.getChildren().add(empty);
         return;
     }

     for (int i = 0; i < displayedAnimals.size(); i++) {
    	    Animal a = displayedAnimals.get(i);
         VBox card = new VBox(8);
         card.setPrefSize(160, 180);
         card.setStyle("""
             -fx-background-color: #ffffff;
             -fx-border-color: #d0d7d9;
             -fx-border-radius: 10;
             -fx-background-radius: 10;
             -fx-cursor: hand;
             -fx-padding: 10;
             -fx-alignment: center;
         """);

         ImageView img = new ImageView();
         Image image;
         String rawPath = a.getImage();   // JSON 
         String defaultPath = "/ui/images/animals/default.jpg";  
         try {
            
             if (rawPath != null && !rawPath.isEmpty()) {
                 image = new Image(rawPath); 
                 if (image.isError()) throw new Exception();
             } else {
                 throw new Exception();
             }
         } catch (Exception e) {
              image = new Image(getClass().getResourceAsStream(defaultPath));
         }
         img.setImage(image);
         img.setFitWidth(100);
         img.setFitHeight(100);
         img.setPreserveRatio(true);

         Label name = new Label(a.getName());
         name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

         Label brief = new Label(a.getDescription().length() > 50
                 ? a.getDescription().substring(0, 50) + "..."
                 : a.getDescription());
         brief.setWrapText(true);
         brief.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

         card.getChildren().addAll(img, name, brief);

         card.setOnMouseClicked(e -> openAnimalDetail(a));

         resultsPane.getChildren().add(card);
     }
 }


    // ---------------------------
    // 🦊
    // ---------------------------

private void openAnimalDetail(Animal animal) {
	
	  TrailList<Animal> wrappedAnimals = new TrailList<>();
	  for (int i = 0; i < displayedAnimals.size(); i++) {
	        wrappedAnimals.add(displayedAnimals.get(i));
	    }
  AppNavigator.showPageWithData(
      "/ui/animal_detail.fxml",
      (AnimalDetailController ctrl) -> {
    	  ctrl.setAllTrails(allTrails);
          ctrl.setAnimal(animal, currentMonth, wrappedAnimals, currentGroup);
          System.out.println("👉 Sending trails to detail: " + allTrails.size());

      }
  );
}

public void setAllTrails(List<Trail> trails) {
    this.allTrails = trails;
}


    // ---------------------------
    // 
    // ---------------------------

    @FXML
    private void onBackClick() throws IOException {
       
        AppNavigator.showPage("/ui/animal.fxml");
    }
}
