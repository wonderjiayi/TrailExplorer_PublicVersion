package controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import model.animal.Animal;
import model.animal.AnimalGroup;
import model.animal.TerrainType;
import model.trail.Trail;
import service.*;
import application.AppNavigator;
import java.net.URL;
import java.util.*;

public class AnimalController {

    // ========== UI ==========
    @FXML private ComboBox<String> monthSelector;
    @FXML private ComboBox<String> terrainSelector;
    @FXML private Button recommendButton;
    @FXML private Button backButton;
    @FXML private Label selectedAnimalLabel;

    @FXML private GridPane calendarGrid;
    @FXML private WebView mapView;
    @FXML private FlowPane animalGroupPane;

    private WebEngine mapEngine;


    // ========== Data ==========
    private List<Animal> allAnimals;   
    private List<Trail> allTrails;     

    private AnimalGroup selectedGroup;
    private TerrainType selectedTerrain = TerrainType.FOREST;
    private int selectedMonth = 4;



    private final AnimalRecommendationService recommendationService = new AnimalRecommendationService();
    private final AnimalHistoryStack<AnimalGroup> historyStack = new AnimalHistoryStack<>();

    // ===============================================================
    //                    INITIALIZE
    // ===============================================================
    @FXML
    public void initialize() {

        
        if (AppNavigator.rootController == null) {

            javafx.application.Platform.runLater(() -> {
                AppNavigator.goToRootLayout();
                AppNavigator.showPage("/ui/animal.fxml");
            });
            return;
        }

       
        GlobalData.loadOrSeedOnce();
        allTrails = GlobalData.getAllTrails();
        allAnimals = GlobalData.getAllAnimals();

        System.out.println("🐾 Animals loaded from GlobalData = " + allAnimals.size());
        System.out.println("🌲 Trails loaded from GlobalData  = " + allTrails.size());

        
        setupMonthSelector();
        setupTerrainSelector();

        setupCalendar();
        highlightSelectedMonth(selectedMonth);
        setupAnimalGroupCards();

        // map
        mapEngine = mapView.getEngine();
        terrainSelector.setOnAction(e -> updateMapByTerrain());


        URL mapUrl = getClass().getResource("/ui/map.html");
        if (mapUrl != null) {
            mapEngine.load(mapUrl.toExternalForm());
        }
    }


    // ===============================================================
    //                card eight
    // ===============================================================
    private void setupAnimalGroupCards() {
        animalGroupPane.getChildren().clear();

        for (AnimalGroup group : AnimalGroup.values()) {
            VBox card = new VBox(6);
            card.setAlignment(javafx.geometry.Pos.CENTER);
            
            card.setMinWidth(85);
            card.setMaxWidth(85);
            card.setPrefWidth(85);
            card.setPrefHeight(85);

            card.setStyle("""
                    -fx-background-color: #ffffff;
                    -fx-border-color: #d0d7d9;
                    -fx-border-radius: 12;
                    -fx-background-radius: 12;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 2);
                """);

            Label emoji = new Label(getEmojiForGroup(group));
            emoji.setStyle("-fx-font-size: 22px;");

            Label name = new Label(group.name());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

            card.getChildren().addAll(emoji, name);

            card.setOnMouseEntered(e ->
                    card.setStyle(card.getStyle() + "-fx-background-color:#e8f5e9;"));

            card.setOnMouseExited(e ->
                    card.setStyle(card.getStyle().replace("-fx-background-color:#e8f5e9;", "")));

            card.setOnMouseClicked(e -> openGroupResults(group));

            animalGroupPane.getChildren().add(card);
        }
    }

    private String getEmojiForGroup(AnimalGroup group) {
        return switch (group) {
            case MAMMAL -> "🦊";
            case BIRD -> "🦉";
            case REPTILE -> "🐍";
            case AMPHIBIAN -> "🐸";
            case INSECT -> "🦋";
            case FISH -> "🐠";
            case PREDATOR -> "🦁";
            default -> "🐾";
        };
    }

    private void openGroupResults(AnimalGroup group) {

        // map
        if (mapEngine != null) {
            switch (group) {
                case MAMMAL, BIRD, INSECT -> mapEngine.executeScript("setMapCenter(42.520, -71.350, 11)");
                case REPTILE -> mapEngine.executeScript("setMapCenter(42.450, -71.100, 12)");
                case AMPHIBIAN, FISH -> mapEngine.executeScript("setMapCenter(43.800, -71.200, 11)");
                case PREDATOR -> mapEngine.executeScript("setMapCenter(44.160, -71.500, 10)");
            }
        }

        AppNavigator.showPageWithData(
                "/ui/animal_results.fxml",
                (AnimalResultsController ctrl) -> {
                    ctrl.setGroup(group, allAnimals, selectedMonth);
                    ctrl.setAllTrails(allTrails);
                }
        );
    }

    // ===============================================================
    //                 only GlobalData
    // ===============================================================

    // ===============================================================
    //                    month selector
    // ===============================================================
    private void setupMonthSelector() {
        for (int i = 1; i <= 12; i++) {
            monthSelector.getItems().add("Month " + i);
        }
        monthSelector.getSelectionModel().select(3);
        monthSelector.setOnAction(e -> {
            selectedMonth = monthSelector.getSelectionModel().getSelectedIndex() + 1;
            setupCalendar();
            highlightSelectedMonth(selectedMonth);
        });
    }

    // ===============================================================
    //                     terrain selector
    // ===============================================================
    private void setupTerrainSelector() {
        for (TerrainType t : TerrainAnimalMapping.getSupportedTerrains()) {
            terrainSelector.getItems().add(t.name());
        }
        terrainSelector.getSelectionModel().select("FOREST");
        terrainSelector.setOnAction(e ->
                selectedTerrain = TerrainType.valueOf(terrainSelector.getValue()));


    }


    private void updateMapByTerrain() {
        if (mapEngine == null) return;

        String terrain = terrainSelector.getValue();
        if (terrain == null) return;


        double[] c = TerrainCoordinates.get(terrain);
        mapEngine.executeScript(
                "setMapCenter(%f, %f, %f)".formatted(c[0], c[1], c[2])
        );


    }

    // ===============================================================
    //                    Recommend
    // ===============================================================
    @FXML
    private void onRecommendClick() {

        if (selectedGroup == null) {
            showAlert("Please select an animal category first!");
            return;
        }


        List<Trail> recommended =
                recommendationService.recommendTrailsForAnimal(selectedGroup, selectedMonth, allTrails);

        AppNavigator.showPageWithData(
                "/ui/animal_results.fxml",
                (AnimalResultsController ctrl) -> {
                    ctrl.setResults(recommended, selectedGroup, selectedMonth);
                }
        );
    }

    // ===============================================================
    //                     back
    // ===============================================================
    @FXML
    private void onBackClick() {
        if (historyStack.size() > 1) {
            historyStack.pop();
            selectedGroup = historyStack.peek();
            selectedAnimalLabel.setText("Back to: " + selectedGroup.name());
        } else {
            showAlert("No previous animal in history!");
        }
    }

    // ===============================================================
    //                     calendar
    // ===============================================================
    private void setupCalendar() {
        calendarGrid.getChildren().clear();
        int daysInMonth = 30;

        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;

            List<Animal> active = allAnimals.stream()
                    .filter(a -> a.getActiveDays() != null &&
                            a.getActiveMonths() != null &&
                            a.getActiveDays().contains(currentDay) &&
                            a.getActiveMonths().contains(selectedMonth))
                    .toList();


            Label dayLabel = new Label(String.valueOf(currentDay));
            dayLabel.setPrefSize(40, 40);
            dayLabel.setAlignment(javafx.geometry.Pos.CENTER);
            dayLabel.setStyle("-fx-border-color:#cfd8dc; -fx-background-color:#ffffff;");

            StackPane cell = new StackPane(dayLabel);

            if (!active.isEmpty()) {

                Circle dot = new Circle(6);
                dot.setFill(javafx.scene.paint.Color.rgb(76, 175, 80, 0.35));
                StackPane.setAlignment(dot, javafx.geometry.Pos.CENTER);
                cell.getChildren().add(dot);


                cell.setOnMouseEntered(e -> {
                    dot.setRadius(8);
                    dot.setFill(javafx.scene.paint.Color.rgb(76, 175, 80, 0.8));
                    cell.setStyle("-fx-background-color: #e8f5e9; -fx-border-color:#4CAF50; -fx-border-width: 2;");
                });
                cell.setOnMouseExited(e -> {
                    dot.setRadius(6);
                    dot.setFill(javafx.scene.paint.Color.rgb(76, 175, 80, 0.35));
                    cell.setStyle("-fx-border-color:#cfd8dc;");
                });

                cell.setOnMouseClicked(e -> showActiveAnimalsPopup(currentDay, active));

            }

            calendarGrid.add(cell, (day - 1) % 10, (day - 1) / 10);
        }
    }



    private void showActiveAnimalsPopup(int day, List<Animal> animalsToday) {
        if (animalsToday.isEmpty()) return;

        StringBuilder msg =
                new StringBuilder("🗓️ " + selectedMonth + "month" + day + "day active animals:\n\n");

        for (Animal a : animalsToday) {
            msg.append("• ").append(a.getName())
                    .append(" (").append(a.getGroup().name()).append(")\n");
        }

        msg.append("\n📍 Recent active locations:\n");

        for (Animal a : animalsToday) {
            if (a.getRecentSpots() != null && !a.getRecentSpots().isEmpty()) {
                msg.append("   - ").append(a.getRecentSpots().get(0)).append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Animal Activity");
        alert.setHeaderText(null);
        alert.setContentText(msg.toString());
        alert.showAndWait();
    }

    private void highlightSelectedMonth(int selected) {
        for (var node : calendarGrid.getChildren()) {
            if (node instanceof StackPane pane) {
                pane.setStyle("-fx-border-color:#cfd8dc;");
            }
        }

        if (selected >= 1 && selected <= calendarGrid.getChildren().size()) {
            var selectedPane = (StackPane) calendarGrid.getChildren().get(selected - 1);
            selectedPane.setStyle("-fx-border-color:#2e7d32; -fx-border-width: 2px;");
        }
    }




    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}










