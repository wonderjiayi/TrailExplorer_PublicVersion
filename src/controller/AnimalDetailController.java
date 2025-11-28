package controller;
import controller.GlobalData;

import javafx.fxml.FXML;

import javafx.geometry.Pos;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import model.animal.Animal;
import model.animal.AnimalGroup;
import model.trail.Trail;
import model.trail.TrailList;
import service.AnimalHistoryStack;
import service.AnimalRecommendationService;
import service.SpotCoordinates;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import application.AppNavigator;

public class AnimalDetailController {

    @FXML private ImageView animalImage;
    @FXML private Label animalName;
    @FXML private Label animalGroup;
    @FXML private TextArea animalDescription;
    @FXML private ListView<String> recentSightingsList;
    @FXML private GridPane activityCalendar;
    @FXML private WebView detailMapView;
    @FXML private ComboBox<String> monthSelector;
    @FXML private FlowPane recentlyViewedPane;


    private WebEngine detailMapEngine;


    private Animal currentAnimal;
    private int currentMonth;
    private TrailList<Animal> allAnimals;

    private AnimalGroup currentGroup;
   
    private final AnimalRecommendationService recommendationService = new AnimalRecommendationService();
    private List<Trail> allTrails;
    
    public void setAllTrails(List<Trail> trails) {
        this.allTrails = trails;
        System.out.println("💡 Detail received trails: " + this.allTrails.size());
    }


    // ---------------------------
    // 🐾 
    // ---------------------------
    public void setAnimal(Animal animal, int month, TrailList<Animal> animals, AnimalGroup group) {

       
        if (detailMapEngine != null) {
            detailMapEngine.getLoadWorker().stateProperty().addListener((obs, o, n) -> {});
        }

       
        this.currentAnimal = animal;
        this.currentMonth = month;
        this.allAnimals = animals;
        this.currentGroup = group;
       


       
        animalName.setText(animal.getName());
        animalGroup.setText(animal.getGroup().name());
        animalDescription.setText(animal.getDescription());

        
        String rawPath = animal.getImage();
        String defaultPath = "/ui/images/animals/default.jpg";
        Image img;

        try {
            if (rawPath != null && !rawPath.isEmpty()) {
                img = new Image(rawPath);
                if (img.isError()) throw new Exception();
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            img = new Image(getClass().getResourceAsStream(defaultPath));
        }
        animalImage.setImage(img);


       
        recentSightingsList.getItems().clear();
        for (int i = 0; i < animal.getRecentSpots().size(); i++) {
            String spot = animal.getRecentSpots().get(i);
            recentSightingsList.getItems().add(spot);
        }


        
        recentSightingsList.setOnMouseClicked(event -> {
            String selectedSpot = recentSightingsList.getSelectionModel().getSelectedItem();
            if (selectedSpot != null) {
                openTrailDetailFromSpot(selectedSpot);
            }
        });


        
        setupActivityCalendar();
        setupMonthSelector();
        updateRecentlyViewed();


        
        initDetailMap();
        showAnimalOnMap(currentAnimal);


     
        GlobalHistory.viewedAnimals.push(currentAnimal);
    }
    
    

    
    
    private void initDetailMap() {
        detailMapEngine = detailMapView.getEngine();
        URL mapUrl = getClass().getResource("/ui/map.html");
        if (mapUrl != null) {
            detailMapEngine.load(mapUrl.toExternalForm());
        }
    }
    
    private void showAnimalOnMap(Animal animal) {

        if (detailMapEngine == null) return;

        
        detailMapEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {

            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {

                
                if (!animal.getRecentSpots().isEmpty()) {
                    String firstSpot = animal.getRecentSpots().get(0);
                    double[] center = SpotCoordinates.get(firstSpot);

                    detailMapEngine.executeScript(
                            "setMapCenter(" + center[0] + ", " + center[1] + ", 12);"
                    );
                }

                //  marker
                for (int i = 0; i < animal.getRecentSpots().size(); i++) {
                    String spot = animal.getRecentSpots().get(i);
                    double[] pos = SpotCoordinates.get(spot);
                    detailMapEngine.executeScript("addMarker(" + pos[0] + ", " + pos[1] + ", '" + spot + "');");
                }

            }
        });
    }




 // 
    private void setupActivityCalendar() {
        activityCalendar.getChildren().clear();
        for (int day = 1; day <= 30; day++) {
            final int currentDay = day;
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setPrefSize(40, 40);
            dayLabel.setAlignment(javafx.geometry.Pos.CENTER);
            dayLabel.setStyle("-fx-border-color:#cfd8dc; -fx-background-color:#ffffff;");
            
            boolean active =
                    currentAnimal.getActiveMonths().contains(currentMonth) &&
                    currentAnimal.getActiveDays().contains(currentDay);
            
            if (active) {
                
                Circle dot = new Circle(6);  
                dot.setFill(javafx.scene.paint.Color.rgb(76, 175, 80, 0.35));
                
                StackPane cell = new StackPane(dayLabel, dot);
                StackPane.setAlignment(dot, javafx.geometry.Pos.CENTER);
                
               
                cell.setOnMouseEntered(e -> {
                    dot.setRadius(8);  
                    dot.setFill(javafx.scene.paint.Color.rgb(76, 175, 80, 0.8));  
                    cell.setStyle("-fx-background-color: #e8f5e9; -fx-cursor: hand; -fx-border-color:#4CAF50; -fx-border-width: 2;");
                });
                cell.setOnMouseExited(e -> {
                    dot.setRadius(6); 
                    dot.setFill(javafx.scene.paint.Color.rgb(76, 175, 80, 0.35)); 
                    cell.setStyle("-fx-background-color: transparent; -fx-border-color:#cfd8dc; -fx-border-width: 1;");
                });
                
                
                cell.setOnMouseClicked(e -> showDayActivityPopup(currentDay));
                
                activityCalendar.add(cell, (day - 1) % 10, (day - 1) / 10);
            } else {
                StackPane cell = new StackPane(dayLabel);
                activityCalendar.add(cell, (day - 1) % 10, (day - 1) / 10);
            }
        }
    }

   
    private void showDayActivityPopup(int day) {
        Alert popup = new Alert(Alert.AlertType.INFORMATION);
        popup.setTitle("Activity Details");
        popup.setHeaderText("🗓️ Day " + day + " - " + currentAnimal.getName());
        
        StringBuilder content = new StringBuilder();
        content.append("📅 Month: ").append(currentMonth).append("\n");
        content.append("🐾 Animal: ").append(currentAnimal.getName()).append("\n");
        content.append("📍 Group: ").append(currentAnimal.getGroup().name()).append("\n\n");
        content.append("✅ This animal is ACTIVE on this day!\n\n");
        
        
        if (currentAnimal.getRecentSpots() != null && !currentAnimal.getRecentSpots().isEmpty()) {
            content.append("📍 Recent Sightings:\n");
            for (int i = 0; i < currentAnimal.getRecentSpots().size(); i++) {
                String spot = currentAnimal.getRecentSpots().get(i);
                content.append("   • ").append(spot).append("\n");
            }
        }
        
        popup.setContentText(content.toString());
        popup.showAndWait();
    }

    // ---------------------------
    // 🏞 Trail 
    // ---------------------------
    @FXML
    private void onRecommendTrailClick() throws IOException {

       
        List<Trail> recs = recommendationService
                .recommendTrailsForAnimal(currentAnimal.getGroup(), currentMonth, allTrails);

        System.out.println("🔍 NumberOfResult = " + recs.size());

        if (recs == null || recs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Trail Found");
            alert.setHeaderText("No Suitable Trails");
            alert.setContentText("Sorry, we cannot find a suitable trail based on this animal.");
            alert.show();
            return;
        }


        AppNavigator.showPageWithData(
            "/ui/trail_recommend.fxml",
            (TrailRecommendController ctrl) -> {
                TrailList<Trail> tl = new TrailList<>();
                if (recs != null) {
                    for (Trail x : recs) tl.add(x);
                }
                ctrl.setTrails(tl, currentAnimal.getGroup());
            }
        );

    }


    // ---------------------------
    // 🔙BACK
    // ---------------------------
 
    @FXML
    private void onBackClick() throws IOException {
        // AppNavigator 
        AppNavigator.showPageWithData(
            "/ui/animal_results.fxml",
            (AnimalResultsController ctrl) -> {
                ctrl.setGroup(currentGroup, allAnimals.toList(), currentMonth);
            }
        );
    }
    
    private void openTrailDetailFromSpot(String spotName) {
        
        if (allTrails == null || allTrails.isEmpty()) {
            System.out.println("⚠ No trails loaded.");
            return;
        }
        
       
        Trail matched = null;
        for (Trail t : allTrails) {
            if (t.getName().equalsIgnoreCase(spotName)) {
                matched = t;
                break;
            }
        }
        
        if (matched == null) {
            System.out.println("⚠ Trail not found in DB: " + spotName);
            return;
        }

       
        Trail finalMatched = matched;  
        AppNavigator.showPageWithData(
            "/ui/trail_detail.fxml",
            (TrailDetailController ctrl) -> { ctrl.setTrail(finalMatched); }
        );
    }
    
    private void setupMonthSelector() {
        monthSelector.getItems().clear();

        for (int i = 1; i <= 12; i++) {
            monthSelector.getItems().add("Month " + i);
        }

        monthSelector.getSelectionModel().select(currentMonth - 1);

        monthSelector.setOnAction(e -> {
            currentMonth = monthSelector.getSelectionModel().getSelectedIndex() + 1;
            setupActivityCalendar(); 
        });
    }
    
    @FXML
    private void onPreviousAnimalClick() {
        if (allAnimals == null || allAnimals.isEmpty()) return;

        int index = -1;
        for (int i = 0; i < allAnimals.size(); i++) {
            if (allAnimals.get(i).equals(currentAnimal)) {
                index = i;
                break;
            }
        }

        if (index <= 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Previous Animal");
            alert.setHeaderText(null);
            alert.setContentText("You are already at the first animal in this group.");
            alert.show();
            return;
        }

        Animal previous = allAnimals.get(index - 1);
        setAnimal(previous, currentMonth, allAnimals, currentGroup);
    }

    
    @FXML
    private void onNextAnimalClick() {
        if (allAnimals == null || allAnimals.isEmpty()) return;

        int index = -1;
        for (int i = 0; i < allAnimals.size(); i++) {
            if (allAnimals.get(i).equals(currentAnimal)) {
                index = i;
                break;
            }
        }

        if (index == -1 || index >= allAnimals.size() - 1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No More Animals");
            alert.setHeaderText(null);
            alert.setContentText("You have reached the last animal in this group.");
            alert.show();
            return;
        }

        Animal next = allAnimals.get(index + 1);
        setAnimal(next, currentMonth, allAnimals, currentGroup);
    }
    
 
    private void updateRecentlyViewed() {
        
        recentlyViewedPane.getChildren().clear();

       
        List<Animal> temp = new ArrayList<>();
        AnimalHistoryStack<Animal> stack = GlobalHistory.viewedAnimals;

        AnimalIterator: 
        for (int i = 0; i < stack.size(); i++) {
            Animal a = stack.get(i);

           
            if (a == currentAnimal) continue;

           
            for (Animal t : temp) {
                if (t.getId() == a.getId()) continue AnimalIterator;
            }

            temp.add(a);

           
            if (temp.size() >= 5) break;
        }

       
        for (Animal a : temp) {

            VBox card = new VBox(5);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; "
                         + "-fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6;");
            card.setPrefWidth(110);

           
            ImageView img = new ImageView(safeImage(a.getImage()));

            img.setFitHeight(60);
            img.setFitWidth(60);
            img.setPreserveRatio(true);

         
            Label name = new Label(a.getName());
            name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

            card.getChildren().addAll(img, name);

            
            card.setOnMouseClicked(e -> {
                setAnimal(a, currentMonth, allAnimals, currentGroup);
            });

            recentlyViewedPane.getChildren().add(card);
        }
    }
    
    private Image safeImage(String path) {
        String defaultPath = "/ui/images/animals/default.jpg";

        try {
            if (path != null && !path.isEmpty()) {
                Image img = new Image(path, false);
                if (!img.isError()) return img;
            }
        } catch (Exception ignored) {}

        return new Image(getClass().getResourceAsStream(defaultPath));
    }




    @FXML private Button backButton;
    @FXML private Button recommendTrailButton;
}
