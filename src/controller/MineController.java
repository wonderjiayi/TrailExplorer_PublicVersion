package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.auth.AuthContext;
import model.group.UserPreference;
import model.group.UserProfile;
import model.trail.Difficulty;
import model.trail.Topic;
import model.trail.Trail;
import service.TrailRecommendationService;
import util.Alerts;

import java.util.List;

public class MineController {

    @FXML private Label usernameLabel;

    @FXML private TextField lengthField;
    @FXML private ComboBox<Difficulty> difficultyBox;
    @FXML private ComboBox<Topic> topicBox;
    @FXML private CheckBox petCheck;
    @FXML private CheckBox campCheck;
    @FXML private CheckBox wildlifeCheck;   

    @FXML
    public void initialize() {
        var opt = AuthContext.currentUser();
        if (opt.isEmpty()) {
            Alerts.warn("Please sign in first.");
            AppNavigator.showPage("/ui/login.fxml");
            return;
        }

        var session = opt.get();
        usernameLabel.setText(session.getAccount().getUsername());

        UserProfile profile = session.getProfile();
        UserPreference pref = profile.getPreferences();

        difficultyBox.getItems().setAll(Difficulty.values());
        topicBox.getItems().setAll(Topic.values());

        if (pref.getTargetLength() > 0)
            lengthField.setText(String.valueOf(pref.getTargetLength()));

        difficultyBox.setValue(pref.getDifficulty());
        topicBox.setValue(pref.getTopic());

        petCheck.setSelected(pref.isPetFriendly());
        campCheck.setSelected(pref.isCampingAllowed());
        wildlifeCheck.setSelected(pref.isPreferWildlife()); 
    }

    @FXML
    private void onLogout() {
        AuthContext.logout();
        Alerts.info("Signed out.");
        AppNavigator.showPage("/ui/login.fxml");
    }

    @FXML
    private void savePreferences() {
        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) return;

        var session = sessionOpt.get();
        UserPreference pref = session.getProfile().getPreferences();

        try {
            String txt = lengthField.getText();
            if (txt != null && !txt.isBlank()) {
                pref.setTargetLength(Double.parseDouble(txt.trim()));
            }
        } catch (Exception e) {
            Alerts.warn("Target length must be a number.");
        }

        pref.setDifficulty(difficultyBox.getValue());
        pref.setTopic(topicBox.getValue());
        pref.setPetFriendly(petCheck.isSelected());
        pref.setCampingAllowed(campCheck.isSelected());
        pref.setPreferWildlife(wildlifeCheck.isSelected());  
        Alerts.info("Preferences saved.");
        AppNavigator.showPage("/ui/landing.fxml");
    }

    @FXML
    private void recommendTrails() {
        var sessionOpt = AuthContext.currentUser();
        if (sessionOpt.isEmpty()) {
            Alerts.warn("Please sign in first.");
            AppNavigator.showPage("/ui/login.fxml");
            return;
        }

        var session = sessionOpt.get();
        UserPreference p = session.getProfile().getPreferences();
 
        TrailRecommendationService service = new TrailRecommendationService(GlobalData.index);

        List<Trail> rec = (List<Trail>) service.recommend(
                p.getTargetLength(),
                p.getDifficulty(),
                p.getTopic(),
                p.isPetFriendly(),
                p.isCampingAllowed(),
                p.isPreferWildlife(),  
                20
        );

        AppNavigator.showPageWithData("/ui/results.fxml", (ResultsController rc) -> {
            rc.setResults(rec);
            rc.setTitle("Personalized Trails for " + session.getAccount().getUsername());
        });
    }   
    @FXML
    private void goMyGroups() {
        AppNavigator.showPage("/ui/my-groups.fxml");
    }
}
