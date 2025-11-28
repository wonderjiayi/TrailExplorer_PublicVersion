package controller;

import application.AppNavigator;
import application.Main;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.auth.AuthContext;
import service.AuthService;
import util.Alerts;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private CheckBox rememberBox; 

    // Use the file-based AuthService you provided (only needs Main.USERS)
    private static final AuthService AUTH = new AuthService(Main.USERS);

    @FXML
    private void initialize() {
        // On startup, read username from remember.txt and pre-fill the field
        String remembered = Main.REMEMBER.load();
        if (remembered != null && !remembered.isBlank()) {
            userField.setText(remembered);
            rememberBox.setSelected(true);
        }
    }
    
    @FXML
    private void onLogin() {
        String username = userField.getText() == null ? "" : userField.getText().trim();
        String password = passField.getText() == null ? "" : passField.getText();

        if (username.isEmpty()) {
            Alerts.warn("Please enter username.");
            return;
        }

        // AuthService.login returns Optional<UserAccount>
        var opt = AUTH.login(username, password);
        if (opt.isEmpty()) {
            Alerts.warn("Invalid username or password.");
            return;
        }

        // Establish login session
        var account = opt.get();
        AuthContext.login(account);
        
        // Remember me: save username if checked; clear it if not checked
        if (rememberBox != null && rememberBox.isSelected()) {
            Main.REMEMBER.save(username);
        } else {
            Main.REMEMBER.clear();
        }

        Alerts.info("Welcome, " + account.getProfile().getNickname());
//        AppNavigator.showPage("/ui/landing.fxml");
        // On successful login: load root_layout and then show landing page
        AppNavigator.goToRootLayout();
        AppNavigator.showPage("/ui/landing.fxml");
    }

    @FXML
    private void goRegister() {
        AppNavigator.showPage("/ui/register.fxml");
    }

    // Allow the registration page to use the same AuthService
    public static AuthService getAuth() {
        return AUTH;
    }
}



