package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.trail.Trail;
import model.trail.TrailList;
import model.trail.Difficulty;
import model.trail.Topic;

import java.util.List;

public class ResultsController {

    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private VBox resultsList;

    private List<Trail> results;

    /** Called by AppNavigator.showPageWithData() */
    public void setResults(List<Trail> list) {
        this.results = (List<Trail>) list;
        render();
    }

    /** -----------------------------------------
     * Render results into beautiful color cards
     * ----------------------------------------- */
    private void render() {
        resultsList.getChildren().clear();

        if (results == null || results.isEmpty()) {
            Label empty = new Label("No trails found.");
            empty.setStyle("-fx-text-fill:#777; -fx-font-size:14px;");
            resultsList.getChildren().add(empty);
            return;
        }

        for (Trail t : results) {
            resultsList.getChildren().add(createTrailCard(t));
        }
    }


    /** -------------------------------------------------
     * Create a beautiful trail result card
     * ------------------------------------------------- */
    private Node createTrailCard(Trail t) {

        HBox card = new HBox(12);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);

        card.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #DDD;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);
                """);

        /* ----------- Topic Icon (left) ----------- */
        ImageView icon = new ImageView();
        try {
            icon.setImage(new Image(getClass().getResourceAsStream(
                    t.getTopic().iconPath()
            )));
        } catch (Exception e) {
            icon.setImage(new Image(getClass().getResourceAsStream(
                    "/ui/images/animals/default.jpg"
            )));
        }
        icon.setFitWidth(36);
        icon.setFitHeight(36);
        icon.setPreserveRatio(true);


        /* ----------- Center text area ----------- */
        VBox content = new VBox(4);

        Label name = new Label(t.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label park = new Label(t.getPark());
        park.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        Label info = new Label(
                "%.1f mi  •  %s  •  Elev %d ft".formatted(
                        t.getLength(),
                        t.getDifficulty().toString(),
                        (int)t.getElevationGain()
                )
        );
        info.setStyle("-fx-text-fill:#555; -fx-font-size: 12px;");

        content.getChildren().addAll(name, park, info);


        /* ----------- Difficulty Badge (right) ----------- */
        Label diff = new Label(t.getDifficulty().toString());
        diff.setStyle("""
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 4 10;
                -fx-background-radius: 12;
        """);

        switch (t.getDifficulty()) {
            case EASY -> diff.setStyle(diff.getStyle() + "-fx-background-color: #4CAF50;");
            case MODERATE -> diff.setStyle(diff.getStyle() + "-fx-background-color: #3F51B5;");
            case HARD -> diff.setStyle(diff.getStyle() + "-fx-background-color: #E53935;");
        }


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(icon, content, spacer, diff);


        /* ----------- Click to go to detail page ----------- */
        card.setOnMouseClicked(e ->
                AppNavigator.showPageWithData("/ui/trail_detail.fxml",
                        (TrailDetailController c) -> c.setTrail(t)
                )
        );

        return card;
    }


    /** Search again */
    @FXML
    private void onSearchEnter() {
        SearchController.setInitialQuery(searchField.getText());
        AppNavigator.showPage("/ui/search.fxml");
    }
    

    public void setTitle(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }
}