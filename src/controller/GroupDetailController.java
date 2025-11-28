package controller;


import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import model.group.Group;
import model.group.UserProfile;
import service.GroupSearchService;
import util.Alerts;
import model.auth.AuthContext;

public class GroupDetailController {

    @FXML private Label titleLabel, metaLabel; 
    @FXML private VBox membersPane;
    @FXML private Spinner<Integer> joinSpinner;
    @FXML private Label trailName, trailTopic, trailDiff, trailHours, trailPet;
    @FXML private Label groupStart, groupCap, groupCur, groupRemain;
    
    @FXML
    private void onBack() {
        application.AppNavigator.showPage("/ui/groupsearch.fxml");
    }

    private GroupSearchService service;
    private Group group;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void init(GroupSearchService svc, Group g, int defaultJoinAs) {
        this.service = svc;
        this.group = g;

        titleLabel.setText(g.getTitle());
        fillTrailAndGroupInfo();
        refreshMeta(); // Extracted into a method so it can be reused after joining

        int maxJoin = Math.max(1, g.getRemainingSlots());
        int defJoin = Math.max(1, Math.min(defaultJoinAs, maxJoin));
        joinSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxJoin, defJoin, 1)
        );
        joinSpinner.setEditable(true);

        renderMembers();
    }

    private void fillTrailAndGroupInfo() {
        var t = group.getTrail();

        // Trail information
        trailName.setText(t.getName());
        trailTopic.setText(String.valueOf(t.getTopic()));
        trailDiff.setText(String.valueOf(t.getDifficulty()));
        trailHours.setText(String.format("%.1f hr", t.getVisitHours()));
        trailPet.setText(t.isPetFriendly() ? "Yes" : "No");

        // Group information
        groupStart.setText(group.getStartTime() == null ? "—" : DT.format(group.getStartTime()));
        groupCap.setText(String.valueOf(group.getCapacity()));
        groupCur.setText(String.valueOf(group.getCurrentSize()));
        groupRemain.setText(String.valueOf(group.getRemainingSlots()));
    }
    
    private void refreshMeta() {
        metaLabel.setText(String.format(
                "Trail: %s • %s • capacity %d • current %d • remaining %d",
                group.getTrail().getName(),
                group.getTrail().getDifficulty(),
                group.getCapacity(),
                group.getCurrentSize(),
                group.getRemainingSlots()
        ));
        
        // Keep the “Group information” panel in sync (values may change)
        if (groupCap != null) {
            groupCap.setText(String.valueOf(group.getCapacity()));
            groupCur.setText(String.valueOf(group.getCurrentSize()));
            groupRemain.setText(String.valueOf(group.getRemainingSlots()));
        }
    }

    private void renderMembers() {
        membersPane.getChildren().clear();
        for (var p : group.getParticipants()) {
            membersPane.getChildren().add(
                    new Label(p.getUser().getNickname() + " · party " + p.getPartySize())
            );
        }
    }

    @FXML
    private void onJoin() {
        // Get current logged-in user
        UserProfile me = AuthContext.currentUser()
                .orElseThrow(() -> new IllegalStateException("Please login"))
                .getProfile();
        
        var username = model.auth.AuthContext.currentUser()
                .map(s -> s.getAccount().getUsername()).orElse(null);
        if (username != null) {
            GlobalData.ACTIVITY.markJoined(username, group.getId());
        }

        int n = joinSpinner.getValue();
        if (!service.canJoin(group, n)) {
            Alerts.warn("Not enough slots. Remaining: " + group.getRemainingSlots());
            return;
        }

        service.join(group, me, n);
        controller.GlobalData.persistGroups();
        Alerts.info("Joined " + group.getTitle());

        // Refresh UI: members list, meta info, and spinner upper bound
        renderMembers();
        refreshMeta();

        int maxJoin = Math.max(1, group.getRemainingSlots());
        int current = Math.min(joinSpinner.getValue(), maxJoin);
        joinSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxJoin, Math.max(1, current), 1)
        );
        joinSpinner.setEditable(true);
    }
}
