package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.group.Group;
import model.trail.TrailList;
import service.GroupSearchService;
import java.util.List;

public class GroupResultsController {
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox listPane;

    @FXML
    private void onBack() {
        application.AppNavigator.showPage("/ui/groupsearch.fxml");
    }
    
    private GroupSearchService service;
    private int joinAs = 1;

    public void init(GroupSearchService svc,
                     String title,
                     String sub,
                     TrailList<Group> groups,
                     int joinAsParty) {
        this.service = svc;
        this.joinAs = Math.max(1, joinAsParty);
        titleLabel.setText(title);
        subtitleLabel.setText(sub);
        render(groups);
    }

    private void render(TrailList<Group> groups) {
        listPane.getChildren().clear();
        for (Group g : groups.toList()) {
            listPane.getChildren().add(
                    GroupCardFactory.card(
                            g,
                            joinAs,
                            this::onView,
                            this::onJoin
                    )
            );
        }
    }

    private void onView(Group g) {
        var username = model.auth.AuthContext.currentUser()
                .map(s -> s.getAccount().getUsername()).orElse(null);
        if (username != null) {
            GlobalData.ACTIVITY.markViewed(username, g.getId());
        }
        application.AppNavigator.showPageWithData("/ui/group-detail.fxml",
                (GroupDetailController c) -> c.init(service, g, joinAs));
    }

    private void onJoin(Group g) {
        var me = model.auth.AuthContext.currentUser()
                .orElseThrow(() -> new IllegalStateException("Please login"))
                .getProfile();
        if (!service.canJoin(g, joinAs)) {
            util.Alerts.warn("Not enough slots. Remaining: " + g.getRemainingSlots());
            return;
        }
        service.join(g, me, joinAs);
        controller.GlobalData.persistGroups();
        util.Alerts.info("Joined " + g.getTitle());
        
        TrailList<Group> single = new TrailList<>();
        single.add(g);
        render(single);
    }
}