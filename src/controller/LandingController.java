package controller;

import java.util.List;

import application.AppNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import model.animal.Animal;
import model.auth.AuthContext;
import model.group.UserPreference;
import model.trail.Trail;
import model.trail.TrailList;
import service.TrailRecommendationService;

public class LandingController {

    @FXML private TextField searchInput;
    @FXML private Label welcomeLabel;
    @FXML private Label prefLabel;
    @FXML private HBox prefCard;
    @FXML private VBox recommendedList;

    // ADT-based recommendation engine
    private final TrailRecommendationService recService =
            new TrailRecommendationService(GlobalData.index);

    @FXML
    public void initialize() {
        refreshUserInfo();
    }

    /* ----------------------------------------------------------
     * Typing animation for welcome message
     * ---------------------------------------------------------- */
    private void playWelcomeAnimation(String text) {
        welcomeLabel.setText("");
        Timeline tl = new Timeline();

        for (int i = 0; i < text.length(); i++) {
            final int idx = i;
            tl.getKeyFrames().add(
                new KeyFrame(Duration.millis(40 * i),
                    e -> welcomeLabel.setText(text.substring(0, idx + 1)))
            );
        }
        tl.play();
    }

    /* ----------------------------------------------------------
     * Load user info and preference summary
     * ---------------------------------------------------------- */
    public void refreshUserInfo() {
        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) {
            welcomeLabel.setText("Welcome!");
            return;
        }

        var profile = sessionOpt.get().getProfile();
        UserPreference pref = profile.getPreferences();

        playWelcomeAnimation("Hi, " + profile.getNickname() + "!");

        prefLabel.setText(String.format(
            "%.1f mi • %s • %s • Pet:%s • Camp:%s • Wildlife:%s • Location:%s",
            pref.getTargetLength(),
            pref.getDifficulty(),
            pref.getTopic(),
            pref.isPetFriendly(),
            pref.isCampingAllowed(),
            pref.isPreferWildlife(),
            pref.getPreferredLocation()
        ));

        loadRecommendations();
    }

    @FXML
    private void onPreferenceClick() {
        AppNavigator.showPage("/ui/mine.fxml");
    }

    /* ----------------------------------------------------------
     * Load 3-card personalized recommended trails on landing page
     * ---------------------------------------------------------- */
    private void loadRecommendations() {

        TrailList<Trail> recs = recService.recommendForCurrentUser(3);

        recommendedList.getChildren().clear();

        for (Trail t : recs.toList()) {

            Label card = new Label(
                t.getName() + " • " + t.getLength() + " mi • " + t.getDifficulty()
            );

            card.setStyle("""
                -fx-background-color: #FFFFFF;
                -fx-padding: 8 12 8 12;
                -fx-border-color: #DDD;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-font-size: 14px;
            """);

            card.setOnMouseClicked(e ->
                AppNavigator.showPageWithData(
                    "/ui/trail_detail.fxml",
                    (TrailDetailController c) -> c.setTrail(t)
                )
            );

            recommendedList.getChildren().add(card);
        }
    }

    /* ----------------------------------------------------------
     * Search bar
     * ---------------------------------------------------------- */
    @FXML
    private void onSearchClicked() {
        String q = searchInput.getText();
        SearchController.setInitialQuery(q);
        AppNavigator.showPage("/ui/search.fxml");
    }

    @FXML
    private void onSearchIconClicked() {
        AppNavigator.showPage("/ui/search.fxml");
    }

    /* ----------------------------------------------------------
     * Nearby Popular Recommendation (Top 5)
     * ---------------------------------------------------------- */
    @FXML
    public void onNearbyPopularRecommend() {

        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) return;

        UserPreference pref = sessionOpt.get().getProfile().getPreferences();

        TrailList<Trail> results = recService.nearbyPopularRecommend(pref, 5);

        AppNavigator.showPageWithData(
                "/ui/results.fxml",
                (ResultsController rc) -> rc.setResults(results.toList())
        );
    }

    /* ----------------------------------------------------------
     * Personalized Recommendation (Top 8)
     * ---------------------------------------------------------- */
    @FXML
    public void onPersonalRecommend() {

        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) return;

        TrailList<Trail> results = recService.personalRecommendForCurrentUser(8);

        AppNavigator.showPageWithData(
            "/ui/results.fxml",
            (ResultsController rc) -> rc.setResults(results.toList())
        );
    }

    /* ----------------------------------------------------------
     * Group Recommendation Page
     * ---------------------------------------------------------- */
    @FXML
    public void onGroupRecommend() {
        AppNavigator.showPageWithData(
            "/ui/groupsearch.fxml",
            (GroupController c) -> c.setRecommendOnly(true)
        );
    }

    /* ----------------------------------------------------------
     * Monthly Animal Recommendation Page
     * (Animals kept as List because they are NOT Trail)
     * ---------------------------------------------------------- */
    @FXML
    public void onAnimalRecommend() {
        List<Animal> animals = GlobalData.getAllAnimals();
        int month = 4;
        TrailList<Animal> wrappedAnimals = new TrailList<>();
        for (Animal a : animals) {
            wrappedAnimals.add(a);
        }


        AppNavigator.showPageWithData(
                "/ui/animal_recommend.fxml",
                (AnimalRecommendController ctrl) -> {
                    ctrl.setAnimalData(wrappedAnimals, month);
                }
        );
    }

    /* ----------------------------------------------------------
     * Generic navigator used by old buttons (kept for compatibility)
     * ---------------------------------------------------------- */
    private void navigateRecommend(int k) {

        TrailList<Trail> results = recService.recommendForCurrentUser(k);

        AppNavigator.showPageWithData(
                "/ui/results.fxml",
                (ResultsController rc) -> rc.setResults(results.toList())
        );
    }
}