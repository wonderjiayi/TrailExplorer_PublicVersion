package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import model.auth.AuthContext;
import model.group.Group;
import model.trail.TrailList;
import service.ActivityService;
import util.Alerts;

public class MyGroupsController {
    @FXML private Button btnJoined, btnCreated, btnViewed;

    @FXML
    public void initialize() {
        var opt = AuthContext.currentUser();
        if (opt.isEmpty()) {
            Alerts.warn("Please sign in first.");
            AppNavigator.goTo("/ui/login.fxml");
            return;
        }
        String username = opt.get().getAccount().getUsername();

        TrailList<Group> joined  = GlobalData.ACTIVITY.getJoined(username);
        TrailList<Group> created = GlobalData.ACTIVITY.getCreated(username);
        TrailList<Group> viewed  = GlobalData.ACTIVITY.getViewed(username);

        btnJoined.setText("Joined (" + joined.size() + ")");
        btnCreated.setText("Created (" + created.size() + ")");
        btnViewed.setText("Viewed (" + viewed.size() + ")");
    }

    @FXML
    private void goBack() {
        application.AppNavigator.showPage("/ui/mine.fxml");
    }

    @FXML
    private void openJoined() {
        openList("My Joined Groups",
                GlobalData.ACTIVITY.getJoined(AuthContext.currentUser().get().getAccount().getUsername()));
    }

    @FXML
    private void openCreated() {
        openList("Groups I Created",
                GlobalData.ACTIVITY.getCreated(AuthContext.currentUser().get().getAccount().getUsername()));
    }

    @FXML
    private void openViewed() {
        openList("Recently Viewed",
                GlobalData.ACTIVITY.getViewed(AuthContext.currentUser().get().getAccount().getUsername()));
    }

    private void openList(String title, TrailList<Group> groups) {
        AppNavigator.showPageWithData("/ui/my-groups-list.fxml",
                (MyGroupsListController c) -> c.init(title, groups));
    }
}

