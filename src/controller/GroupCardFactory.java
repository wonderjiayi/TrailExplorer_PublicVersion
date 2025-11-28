package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import model.group.Group;

import java.util.function.Consumer;

public final class GroupCardFactory {
    private GroupCardFactory(){}

    public static VBox card(Group g, int joinAs,
                            Consumer<Group> onView, Consumer<Group> onJoin) {
        // Safety fallback for missing trail or title data
        String trailName = (g.getTrail() == null || g.getTrail().getName() == null) ? "Unknown Trail" : g.getTrail().getName();
        String diff      = (g.getTrail() == null || g.getTrail().getDifficulty() == null) ? "-" : g.getTrail().getDifficulty().toString();
        String titleText = (g.getTitle() == null || g.getTitle().isBlank()) ? trailName : g.getTitle();

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");

        Label meta = new Label(String.format("Trail: %s  ·  %s  ·  %d/%d ppl (remain %d)",
                trailName, diff, g.getCurrentSize(), g.getCapacity(), g.getRemainingSlots()));
        meta.setWrapText(true);

        Button joinBtn = new Button("Join");
        Button viewBtn = new Button("View");

        // Join availability: disable button or show tooltip if joining is not possible
        boolean canJoin = g.canJoin(Math.max(1, joinAs));
        joinBtn.setDisable(!canJoin);
        if (!canJoin) {
            joinBtn.setTooltip(new javafx.scene.control.Tooltip("Not enough slots"));
        }

        joinBtn.setOnAction(e -> onJoin.accept(g));
        viewBtn.setOnAction(e -> onView.accept(g));

        HBox actions = new HBox(8, joinBtn, viewBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(6, title, meta, actions);
        box.setPadding(new Insets(12));
        box.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 2);
                """);

        // Auto-adjust width to parent (looks better inside a ScrollPane's VBox)
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);

        // Make the whole card clickable to view details
        box.setOnMouseClicked(e -> onView.accept(g));
        box.setCursor(Cursor.HAND);

        return box;
    }
}


