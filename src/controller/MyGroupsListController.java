package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.group.Group;
import model.trail.TrailList;


public class MyGroupsListController {
    @FXML private Label titleLabel;
    @FXML private VBox listPane;

    private TrailList<Group> data;

    public void init(String title, TrailList<Group> groups) {
        this.data = groups;
        titleLabel.setText(title);
        render();
    }

    private void render() {
        listPane.getChildren().clear();
        int joinAs = 1; // Default to joining as 1 person; can be replaced with a user-specific setting
        for (Group g : data.toList()) {
            listPane.getChildren().add(
                GroupCardFactory.card(g, joinAs, this::onView, this::onJoin)
            );
        }
    }

    private void onView(Group g) {
        var username = model.auth.AuthContext.currentUser()
                .map(s -> s.getAccount().getUsername()).orElse(null);
        if (username != null) {
            GlobalData.ACTIVITY.markViewed(username, g.getId());
        }
        AppNavigator.showPageWithData("/ui/group-detail.fxml",
                (GroupDetailController c) -> c.init(new service.GroupSearchServiceImpl(), g, 1));
        // Note: A new ServiceImpl is created here; if you use a singleton/DI, replace this with the shared instance
    }

    private void onJoin(Group g) {
        // Navigate to the detail page and let the user decide party size (avoid changing data silently)
        onView(g);
    }

    @FXML
    private void goBack() {
        application.AppNavigator.showPage("/ui/my-groups.fxml");
    }
}


