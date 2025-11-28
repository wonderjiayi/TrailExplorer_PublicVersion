package application;

import controller.RootLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class AppNavigator {

    private static Stage stage;
    public static RootLayoutController rootController;
    public static void setStage(Stage s) {
        stage = s;
    }

    public static void goTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, 430, 780);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goToRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource("/ui/root_layout.fxml"));
            Parent root = loader.load();

            rootController = loader.getController();   // remember RootLayoutController

            Scene scene = new Scene(root, 430, 780);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showPage(String fxml) {
        if (rootController == null) {

            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(fxml));
            Node content = loader.load();
            rootController.setContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void showPageWithData(String fxml, Consumer<T> controllerConsumer) {
        if (rootController == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(fxml));
            Node content = loader.load();

            T controller = loader.getController();
            controllerConsumer.accept(controller);

            rootController.setContent(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}