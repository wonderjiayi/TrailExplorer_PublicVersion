package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.group.Group;
import model.group.UserProfile;
import model.trail.Trail;
import model.trail.TrailList;
import service.GroupSearchService;
import util.Alerts;
import javafx.application.Platform;



import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import application.AppNavigator;

public class GroupCreateController {
    @FXML private ComboBox<Trail> trailCombo;
    @FXML private TextField titleField, startTimeField;
    @FXML private Spinner<Integer> capacitySpinner, partySpinner;
    @FXML private Label previewLabel, errorLabel;
    @FXML private Button createBtn;

    @FXML
    private void onBack() {
        application.AppNavigator.showPage("/ui/groupsearch.fxml");
    }
    
    private GroupSearchService service;
    private TrailList<Trail> allTrails;

    /** Called from previous page: c.init(service, allTrails); */
    public void init(GroupSearchService svc, TrailList<Trail> trails) {
        this.service = svc;
        this.allTrails = trails;

        // 1) Trail dropdown
        trailCombo.getItems().setAll(trails.toList());
        // Concise toString representation in the ComboBox
        trailCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Trail t) { return t==null? "" : t.getName() + " · " + t.getDifficulty(); }
            @Override public Trail fromString(String s) { return null; }
        });

        // 2) Spinner
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 8));
        partySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        capacitySpinner.setEditable(true);
        partySpinner.setEditable(true);

        // 3) Preview and validation: show remaining capacity in real time and disable Create when party > capacity
        Runnable refresh = this::refreshPreviewAndValidation;
        capacitySpinner.valueProperty().addListener((obs, o, n) -> refresh.run());
        partySpinner.valueProperty().addListener((obs, o, n) -> refresh.run());
        refresh.run();

        // Provide a user-friendly default title (optional)
        String promptExample;
        if (trails.size() == 0) {                      // 👈 TrailList.size()
            promptExample = "Morning Hike";
        } else {
            promptExample = trails.get(0).getName() + " meetup";   // 👈 TrailList.get()
        }
        titleField.setPromptText("e.g., " + promptExample);
    }

    private void refreshPreviewAndValidation() {
        int cap = safe(capacitySpinner.getValue(), 1);
        int party = safe(partySpinner.getValue(), 1);
        int remaining = Math.max(0, cap - party);

        previewLabel.setText("After creation: current " + party + " / capacity " + cap + " · remaining " + remaining);
        if (party > cap) {
            errorLabel.setText("Your party size exceeds capacity.");
            createBtn.setDisable(true);
        } else {
            errorLabel.setText("");
            createBtn.setDisable(false);
        }
    }

    @FXML
    private void onCreate() {
        try {
            Trail t = trailCombo.getValue();
            if (t == null) { warn("Please select a trail."); return; }

            String title = titleField.getText().isBlank() ? t.getName() : titleField.getText();
            LocalDateTime start = parseTime(startTimeField.getText());
            int cap = safe(capacitySpinner.getValue(), 1);
            int party = safe(partySpinner.getValue(), 1);

            if (party > cap) { warn("Party size cannot exceed capacity."); return; }

            Group g = new Group(UUID.randomUUID().toString(), t, title, start, cap);

            // Creator joins immediately (party size counts towards current size)
            UserProfile me = model.auth.AuthContext.currentUser()
                    .orElseThrow(() -> new IllegalStateException("Please login")).getProfile();

            if (!g.canJoin(party)) { // Secondary safety check
                warn("Not enough capacity for your party.");
                return;
            }
            g.join(me, party);

            // Register into index so others can discover this group
            TrailList<Group> one = new TrailList<>();
            one.add(g);
            service.indexGroups(one);             // Incremental indexing for current service
            controller.GlobalData.addGroup(g); // Save into global state and persist to CSV

            var username = model.auth.AuthContext.currentUser()
                    .map(s -> s.getAccount().getUsername()).orElse(null);
            if (username != null) {
                GlobalData.ACTIVITY.markCreated(username, g.getId());
            }
            info("Group created!");
            // Navigate to detail page or back to previous page
            AppNavigator.showPageWithData("/ui/group-detail.fxml",
                    (GroupDetailController c) -> c.init(service, g, Math.max(1, party)));
        } catch (Exception ex) {
            error("Failed to create group: " + ex.getMessage());
        }
    }

    private static int safe(Integer v, int def) { return v == null ? def : v; }

    private static LocalDateTime parseTime(String s) {
        if (s == null || s.isBlank()) {
            return LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        }
        try { return LocalDateTime.parse(s.replace(" ", "T")); }
        catch (Exception e) { return LocalDateTime.now().plusDays(1).withHour(9).withMinute(0); }
    }

    // Popup utility helpers used in this project
    private void warn(String msg){ Alerts.warn(msg); }
    private void info(String msg){ Alerts.info(msg); }
    private void error(String msg){ Alerts.error(msg); }
    
    /**
     * TrailRecommend → “Join Group”:
     * auto-fill trail dropdown and default title
     */
    public void setDefaultTrail(Trail t) {
        if (t == null) return;

        Platform.runLater(() -> {
            // Select trail in dropdown
            trailCombo.getSelectionModel().select(t);

            // Default group title = trail name
            titleField.setText(t.getName());

            // Refresh preview
            refreshPreviewAndValidation();
        });
    }
    
    public void prefillTrail(Trail t) {
        if (t == null) return;

        trailCombo.getSelectionModel().select(t);

        // Default title
        titleField.setText(t.getName() + " Meetup");

        // Default start time
        startTimeField.setText(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).toString()
        );
    }


}

