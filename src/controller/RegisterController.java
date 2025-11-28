package controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import util.Alerts;
import application.AppNavigator;

public class RegisterController {
    @FXML private TextField userField, nickField, emailField;
    @FXML private PasswordField passField;

    @FXML
    private void onRegister() {
        try {
            var auth = LoginController.getAuth();
            var acc = auth.register(userField.getText().trim(),
                                    emailField.getText().trim().isBlank()?null:emailField.getText().trim(),
                                    passField.getText(), nickField.getText().trim());
            Alerts.info("Account created: " + acc.getUsername());
            AppNavigator.goTo("/ui/login.fxml");
        } catch (Exception e) {
            Alerts.warn(e.getMessage());
        }
    }

    @FXML private void goLogin() { AppNavigator.goTo("/ui/login.fxml"); }
}
