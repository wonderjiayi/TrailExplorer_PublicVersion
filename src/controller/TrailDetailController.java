package controller;

import java.util.List;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.trail.Trail;
import model.trail.TrailList;          // ★ 新增
import model.animal.Animal;
import model.group.Group;
import model.trail.Season;
import model.trail.Suitability;

public class TrailDetailController {

    @FXML private Label trailName;
    @FXML private Label parkLabel;
    @FXML private Label addressLabel;

    @FXML private Label difficultyLabel;
    @FXML private Label lengthLabel;
    @FXML private Label elevLabel;
    @FXML private Label visitLabel;

    @FXML private Label topicLabel;

    @FXML private Label petLabel;
    @FXML private Label campLabel;
    @FXML private Label birdLabel;
    @FXML private Label wildLabel;
    @FXML private Label icyLabel;

    @FXML private FlowPane seasonPane;
    @FXML private FlowPane suitPane;

    @FXML private Label alertLabel;
    @FXML private VBox groupsContainer;
    @FXML private Label groupsArrow;
    @FXML private Label groupsTitle;
    @FXML private Label animalsTitle;

    private boolean groupsExpanded = false;

    private Trail trail;

    /** Called by AppNavigator.showPageWithData(...) */
    public void setTrail(Trail t) {
        this.trail = t;
        render();
    }

    private void render() {
        trailName.setText(trail.getName());
        parkLabel.setText("Park: " + trail.getPark() + " (" + trail.getParkArea() + ")");
        addressLabel.setText("Address: " + trail.getAddress() + ", " + trail.getState() + " " + trail.getZipcode());

        difficultyLabel.setText(trail.getDifficulty().name());
        lengthLabel.setText(trail.getLength() + " mi");
        elevLabel.setText(trail.getElevationGain() + " ft");
        visitLabel.setText(trail.getVisitHours() + " hr");

        topicLabel.setText(trail.getTopic().name());

        petLabel.setText(trail.isPetFriendly() ? "Yes" : "No");
        campLabel.setText(trail.isCampingAllowed() ? "Yes" : "No");
        birdLabel.setText(trail.isBirdSpotted() ? "Yes" : "No");
        wildLabel.setText(trail.isWildAnimalPossible() ? "Yes" : "No");
        icyLabel.setText(trail.isIcyTrail() ? "Yes" : "No");

        // seasons
        seasonPane.getChildren().clear();
        for (Season s : trail.getSeasons()) {
            Label tag = new Label(s.name());
            tag.getStyleClass().add("tag");
            seasonPane.getChildren().add(tag);
        }

        // suitability
        suitPane.getChildren().clear();
        for (Suitability su : trail.getSuitability()) {
            Label tag = new Label(su.name());
            tag.getStyleClass().add("tag");
            suitPane.getChildren().add(tag);
        }

        alertLabel.setText(trail.getAlert() == null || trail.getAlert().isBlank()
                ? "None"
                : trail.getAlert());

        loadGroupsForTrail();   // ★ 用 TrailList 改写
        loadAnimalsForTrail();  // ★ 不改 animal 相关
    }

    // ========== 点击折叠功能 ==========
    @FXML
    private void toggleGroups() {
        groupsExpanded = !groupsExpanded;
        groupsArrow.setText(groupsExpanded ? "▾" : "▸");
        groupsContainer.setVisible(groupsExpanded);
        groupsContainer.setManaged(groupsExpanded);
    }

    // ========== 加载 Group 列表（TrailList 版，无 stream）==========
    private void loadGroupsForTrail() {
        groupsContainer.getChildren().clear();

        // 全部 group：TrailList<Group>
        TrailList<Group> allGroups = controller.GlobalData.getAllGroups();

        // 过滤匹配当前 trail 的 group
        TrailList<Group> matched = new TrailList<>();
        for (int i = 0; i < allGroups.size(); i++) {
            Group g = allGroups.get(i);
            if (g != null && g.getTrail() != null && g.getTrail().equals(trail)) {
                matched.add(g);
            }
        }

        groupsTitle.setText("Groups (" + matched.size() + ")");

        if (matched.size() == 0) {
            Button createBtn = new Button("Create a Group");
            createBtn.getStyleClass().add("badge");
            createBtn.setOnAction(e ->
                application.AppNavigator.showPageWithData(
                    "/ui/group-create.fxml",
                    (GroupCreateController c) -> {
                        // GlobalData.getAllTrails() 返回 List<Trail>，转成 TrailList<Trail>
                        java.util.List<Trail> src = controller.GlobalData.getAllTrails();
                        TrailList<Trail> tl = new TrailList<>();
                        if (src != null) {
                            for (int i2 = 0; i2 < src.size(); i2++) tl.add(src.get(i2));
                        }

                        c.init(new service.GroupSearchServiceImpl(), tl);
                        c.setDefaultTrail(trail);
                    }
                )
            );
            groupsContainer.getChildren().add(createBtn);
            return;
        }

        // 遍历 matched，使用 card 工厂
        for (int i = 0; i < matched.size(); i++) {
            Group g = matched.get(i);
            var card = controller.GroupCardFactory.card(
                    g,
                    1,
                    this::onViewGroup,
                    this::onJoinGroup
            );
            groupsContainer.getChildren().add(card);
        }
    }

    // ========== 查看 Group ==========
    private void onViewGroup(model.group.Group g) {
        application.AppNavigator.showPageWithData("/ui/group-detail.fxml",
                (GroupDetailController c) -> c.init(
                        new service.GroupSearchServiceImpl(),
                        g,
                        1
                ));
    }

    // ========== 直接加入 Group ==========
    private void onJoinGroup(model.group.Group g) {
        var me = model.auth.AuthContext.currentUser()
                .orElseThrow(() -> new IllegalStateException("Please login"))
                .getProfile();

        if (!new service.GroupSearchServiceImpl().canJoin(g, 1)) {
            util.Alerts.warn("Not enough slots.");
            return;
        }

        new service.GroupSearchServiceImpl().join(g, me, 1);
        controller.GlobalData.persistGroups();
        util.Alerts.info("Joined " + g.getTitle());

        loadGroupsForTrail(); // refresh
    }

    @FXML private Label animalsArrow;
    @FXML private FlowPane animalFlow;

    private boolean animalsExpanded = false;

    @FXML
    private void toggleAnimals() {
        animalsExpanded = !animalsExpanded;

        if (animalsExpanded) {
            animalsArrow.setText("▾");
            animalFlow.setVisible(true);
            animalFlow.setManaged(true);
        } else {
            animalsArrow.setText("▸");
            animalFlow.setVisible(false);
            animalFlow.setManaged(false);
        }
    }

    // ★★★ animal 相关完全不改 ★★★
    private void loadAnimalsForTrail() {
        animalFlow.getChildren().clear();

        List<Animal> allAnimals = controller.GlobalData.getAllAnimals();
        List<Animal> matched = service.TrailAnimalMatcher.matchAnimals(trail, allAnimals);

        animalsTitle.setText("Animals You May Encounter (" + matched.size() + ")");

        if (matched.isEmpty()) {
            Label empty = new Label("No animals likely in this trail.");
            empty.setStyle("-fx-text-fill: #666;");
            animalFlow.getChildren().add(empty);
            return;
        }

        for (Animal a : matched) {
            VBox card = new VBox(6);
            card.setStyle("""
                -fx-background-color: #ffffff;
                -fx-border-color: #d0d7d9;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                -fx-padding: 10;
                -fx-cursor: hand;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);
            """);
            card.setPrefSize(140, 160);
            card.setAlignment(Pos.CENTER);

            // 图片
            ImageView img = new ImageView();
            Image image;
            try {
                image = new Image(a.getImage());
                if (image.isError()) throw new Exception();
            } catch (Exception e) {
                image = new Image(getClass().getResourceAsStream("/ui/images/animals/default.jpg"));
            }
            img.setImage(image);
            img.setFitWidth(80);
            img.setFitHeight(80);
            img.setPreserveRatio(true);

            Label name = new Label(a.getName());
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label desc = new Label(
                    a.getDescription().length() > 40
                            ? a.getDescription().substring(0, 40) + "..."
                            : a.getDescription()
            );
            desc.setWrapText(true);
            desc.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

            card.getChildren().addAll(img, name, desc);

            // 点击跳转
            card.setOnMouseClicked(e ->
                application.AppNavigator.showPageWithData(
                        "/ui/animal_detail.fxml",
                        (AnimalDetailController ctrl) -> {
                        	TrailList<Animal> single = new TrailList<>();
                            single.add(a);
                            ctrl.setAnimal(a, 6, single, a.getGroup());
                        }
                )
            );

            animalFlow.getChildren().add(card);
        }
    }
}
