package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class RootLayoutController {

    @FXML
    private StackPane contentArea;

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }
}