package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.auth.AuthContext;
import model.group.Group;
import model.group.GroupSearchCriteria;
import model.trail.Difficulty;
import model.trail.Topic;
import model.trail.Trail;
import service.GroupSearchService;
import service.GroupSearchServiceImpl;
import util.Alerts;

public class GroupController {

    @FXML private TextField keywordField;
    @FXML private ComboBox<Topic> topicCombo;
    @FXML private ComboBox<Difficulty> difficultyCombo;
    @FXML private CheckBox petBox;
    @FXML private Spinner<Double> maxHoursSpinner;
    @FXML private Spinner<Integer> joinPartySizeSpinner;
    @FXML private VBox recommendedPane;
    
    @FXML private HBox topSearchBar;
    @FXML private TitledPane advancedPane;
    @FXML private Button createBtn;

    private final GroupSearchService service = new GroupSearchServiceImpl();
    private int joinAs = 1;

    private boolean recommendOnly = false;
    public void setRecommendOnly(boolean v) {
        this.recommendOnly = v;
        applyRecommendOnly(); // <-- If FXML has been injected, apply immediately
    }
    @FXML
    private void initialize() {
        topicCombo.getItems().setAll(Topic.values());
        difficultyCombo.getItems().setAll(Difficulty.values());

        maxHoursSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 72.0, 1.0, 0.5));
        maxHoursSpinner.setEditable(true);

        joinPartySizeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1, 1));
        joinPartySizeSpinner.setEditable(true);

        controller.GlobalData.bootstrap(service); // Index Trails/Groups
        
        if (recommendOnly) {
            // Recommendation-only mode: hide search area and create button (invisible and unmanaged)
            hide(topSearchBar);
            hide(advancedPane);
            hide(createBtn);
        }
        applyRecommendOnly();     // <-- If the flag was set earlier, ensure it also takes effect here
        // Refresh recommendations (Top 5) when entering the page
        renderRecommendationsTop5();
    }

    private void applyRecommendOnly() {
        if (!recommendOnly) return;
        hide(topSearchBar);
        hide(advancedPane);
        hide(createBtn);
        // Optional: adjust title style for "Recommended Groups" (bold, centered, etc.) – omitted here
    }
    
    private static void hide(javafx.scene.Node n){
        if (n != null) { n.setVisible(false); n.setManaged(false); }
    }

    // ====== Recommendations: call advanced search to get Top-5 and render them ======
    private void renderRecommendationsTop5() {
        int joinAs = valueOr(joinPartySizeSpinner.getValue(), 1);

        // Build a “user-friendly” default criteria (can be tuned as needed)
        GroupSearchCriteria c = new GroupSearchCriteria(
                topicCombo.getValue(),           // Nullable: no topic filter
                difficultyCombo.getValue(),      // Nullable: no difficulty filter
                petBox.isSelected(),             // Allow pets or not
                maxHoursSpinner.getValue(),      // Maximum duration
                joinAs                           // Party size to join with
        );

        var top = service.advancedSearch(c, 5); // Top-5
        recommendedPane.getChildren().clear();

        for (Group g : top.toList()) {
            recommendedPane.getChildren().add(
                GroupCardFactory.card(
                    g,
                    joinAs,
                    this::onView,   // View details
                    this::onJoin    // Join group
                )
            );
        }
    }

    // ====== Callback handlers (must match GroupCardFactory.card signature) ======
    private void onView(Group g) {
        // 1) Record “viewed” activity
        var username = model.auth.AuthContext.currentUser()
                .map(s -> s.getAccount().getUsername())
                .orElse(null);
        if (username != null && g != null && g.getId() != null) {
            GlobalData.ACTIVITY.markViewed(username, g.getId());
        }

        application.AppNavigator.showPageWithData("/ui/group-detail.fxml",
                (GroupDetailController c) -> c.init(service, g, joinAs));
    }

    

    private void onJoin(Group g) {
        var me = AuthContext.currentUser()
                .orElseThrow(() -> new IllegalStateException("Please login"))
                .getProfile();

        int joinAs = valueOr(joinPartySizeSpinner.getValue(), 1);
        if (!service.canJoin(g, joinAs)) {
            Alerts.warn("Not enough slots. Remaining: " + g.getRemainingSlots());
            return;
        }
        service.join(g, me, joinAs);
        controller.GlobalData.persistGroups();
        Alerts.info("Joined " + g.getTitle());

        // After joining successfully, refresh recommendations (group sizes and ranking may change)
        renderRecommendationsTop5();
    }

    /** Keyword search */
    @FXML
    private void onKeywordSearch() {
        controller.GlobalData.bootstrap(service);

        String kw = keywordField.getText();
        var groups = service.searchGroupsByTrailKeyword(kw, 50);

        AppNavigator.showPageWithData("/ui/group-results.fxml",
                (GroupResultsController c) -> c.init(
                        service,
                        "Search: " + (kw == null ? "" : kw),
                        "All groups for matching trails",
                        groups,
                        joinPartySizeSpinner.getValue()
                ));
    }

    /** Advanced search */
    @FXML
    private void onAdvancedSearch() {
        var c = new model.group.GroupSearchCriteria(
                topicCombo.getValue(),
                difficultyCombo.getValue(),
                petBox.isSelected(),
                maxHoursSpinner.getValue(),
                joinPartySizeSpinner.getValue()
        );

        var groups = service.advancedSearch(c, 50);

        AppNavigator.showPageWithData("/ui/group-results.fxml",
                (GroupResultsController grc) -> grc.init(
                        service,
                        "Advanced Search",
                        "Filtered groups",
                        groups,
                        joinPartySizeSpinner.getValue()
                ));
    }

    /** Create a new group */
    @FXML
    private void onCreateGroupClicked() {
        var trailsJava = GlobalData.getAllTrails();

        model.trail.TrailList<Trail> trails = new model.trail.TrailList<>();
        for (Trail t : trailsJava) {
            trails.add(t);
        }

        AppNavigator.showPageWithData("/ui/group-create.fxml",
                (GroupCreateController c) -> c.init(service, trails));
    }

    private static <T> T valueOr(T v, T d) { return v == null ? d : v; }
}
