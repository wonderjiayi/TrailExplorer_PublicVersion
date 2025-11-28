package controller;

import application.AppNavigator;
import javafx.fxml.FXML;

public class BottomBarController {

    @FXML
    public void goTrail() {
        AppNavigator.showPage("/ui/search.fxml");
    }

    @FXML
    public void goAnimal() {
        AppNavigator.showPage("/ui/animal.fxml");
    }

    @FXML
    public void goGroup() {
        AppNavigator.showPage("/ui/groupsearch.fxml");
    }

    @FXML
    public void goMine() {
        AppNavigator.showPage("/ui/mine.fxml");
    }
    
    @FXML
    public void goMain() {
        AppNavigator.showPage("/ui/landing.fxml");
    }
}