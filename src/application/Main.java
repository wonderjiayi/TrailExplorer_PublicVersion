package application; 


import controller.GlobalData;
import data.TrailDataLoader;
import javafx.application.Application;
import javafx.stage.Stage;
import model.auth.AuthContext;
import repo.RememberMe;
import repo.UserRepository;

public class Main extends Application {

    public static final UserRepository USERS = new UserRepository("data/users.csv");
    public static final RememberMe REMEMBER = new RememberMe("data/remember.txt");

    @Override
    public void start(Stage primaryStage) {

        AppNavigator.setStage(primaryStage);

        GlobalData.loadOrSeedOnce();

        String remembered = REMEMBER.load();

        if (remembered != null) {

            USERS.findByUsername(remembered).ifPresentOrElse(user -> {

                // Remembered user exists → auto login
                AuthContext.login(user);

                // Launch root layout
                AppNavigator.goToRootLayout();  
                AppNavigator.showPage("/ui/landing.fxml");

            }, () -> {
                // User not found → clear and go to login page
                REMEMBER.clear();
                AppNavigator.goTo("/ui/login.fxml");
            });

        } else {
            // No remembered user
        	AppNavigator.goTo("/ui/login.fxml");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
