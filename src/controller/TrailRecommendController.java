package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.animal.AnimalGroup;
import model.animal.TerrainType;
import model.group.Group;
import model.trail.Trail;
import model.trail.TrailList;
import service.AnimalSeasonService;
import service.GroupSearchService;
import service.GroupSearchServiceImpl;
import service.TerrainAnimalMapping;
import service.TerrainFilterService;

public class TrailRecommendController {

    @FXML private VBox trailContainer;
    @FXML private Label titleLabel;
    @FXML private Button sortButton;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private CheckBox petFilter;
    @FXML private CheckBox campFilter;
    @FXML private TextField maxLengthField;
    @FXML private Button applyFilterBtn;

    /** 改：用自定义 ADT 保存 trails */
    private TrailList<Trail> trailList = new TrailList<>();
    private AnimalGroup selectedGroup;
    private final GroupSearchService service = new GroupSearchServiceImpl();

    /** 代替 Collections.reverse 的简单“反向显示”开关 */
    private boolean reverseView = false;

    @FXML
    private void initialize() {
        // 初始化 Difficulty 下拉框
        difficultyFilter.getItems().addAll("All", "EASY", "MODERATE", "HARD");
        difficultyFilter.getSelectionModel().select("All");

        // 监听
        applyFilterBtn.setOnAction(e -> applyFilters());
        sortButton.setOnAction(e -> { reverseView = !reverseView; renderCards(trailList); });
    }

    /** 接收推荐结果（改：TrailList 版本） */
    public void setTrails(TrailList<Trail> trails, AnimalGroup group) {
        this.trailList.clear();
        if (trails != null) {
            for (int i = 0; i < trails.size(); i++) {
                this.trailList.add(trails.get(i));
            }
        }
        this.selectedGroup = group;

        GlobalData.bootstrap(service);
        renderCards(this.trailList);
    }

    /** 渲染所有卡片（不用 stream/Collections） */
    private void renderCards(TrailList<Trail> trails) {
        trailContainer.getChildren().clear();

        if (!reverseView) {
            for (int i = 0; i < trails.size(); i++) {
                trailContainer.getChildren().add(buildTrailCard(trails.get(i)));
            }
        } else {
            for (int i = trails.size() - 1; i >= 0; i--) {
                trailContainer.getChildren().add(buildTrailCard(trails.get(i)));
            }
        }
    }

    /** 构建单个卡片 */
    private VBox buildTrailCard(Trail t) {
        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 18;
            -fx-border-radius: 12;
            -fx-background-radius: 12;
            -fx-border-color: #d0d7d9;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);
        """);

        // 悬停动画
        card.setOnMouseEntered(e ->
            card.setStyle(card.getStyle() +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.20), 14, 0, 0, 4); -fx-translate-y: -2;")
        );
        card.setOnMouseExited(e ->
            card.setStyle(card.getStyle() +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-translate-y: 0;")
        );

        HBox header = new HBox(12);
        ImageView icon = new ImageView(loadTrailImage(t));
        icon.setFitWidth(40);
        icon.setFitHeight(40);

        Label name = new Label(t.getName());
        name.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        header.getChildren().addAll(icon, name);

        // 难度标签
        Label diffLabel = getDifficultyBadge(t);

        TerrainType terrain = TerrainFilterService.mapTopicToTerrain(t.getTopic());

        Label park = new Label("Park: " + t.getPark());
        Label topic = new Label("Terrain: " + t.getTopic());
        Label length = new Label(String.format("Length: %.1f mi", t.getLength()));
        Label elev = new Label("Elevation: " + t.getElevationGain() + " ft");
        Label pet = new Label("Pet Friendly: " + (t.isPetFriendly() ? "Yes" : "No"));
        Label camp = new Label("Camping Allowed: " + (t.isCampingAllowed() ? "Yes" : "No"));

        // —— animal 相关：保持原样（不修改 stream 等写法） ——
        Label eco = new Label("🌲 " + getAnimalSuggestion(terrain));
        eco.setStyle("-fx-text-fill: #2e7d32;");
        Label season = new Label("🌸 " + getSeasonTip(selectedGroup));
        Label warn = new Label("⚠ " + getPredatorWarning(terrain, selectedGroup));
        warn.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");

        card.getChildren().addAll(
            header, diffLabel, park, topic, length, elev, pet, camp, eco, season, warn
        );

        // 按钮栏
        HBox btnBar = new HBox(14);
        btnBar.setAlignment(Pos.CENTER_LEFT);
        btnBar.setStyle("-fx-padding: 12 0 0 0;");

        Button viewBtn = new Button("View Detail");
        viewBtn.setStyle("""
                -fx-background-color: #4CAF50;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 6;
                -fx-padding: 6 16;
        """);
        viewBtn.setOnAction(e -> onViewDetail(t));

        Button joinBtn = new Button("Join Group");
        joinBtn.setStyle("""
                -fx-background-color: #1E88E5;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 6;
                -fx-padding: 6 16;
        """);
        joinBtn.setOnAction(e -> onJoinGroup(t));

        btnBar.getChildren().addAll(viewBtn, joinBtn);
        card.getChildren().add(btnBar);

        return card;
    }

    // 彩色难度标签
    private Label getDifficultyBadge(Trail t) {
        Label badge = new Label(" Difficulty: " + t.getDifficulty() + " ");
        badge.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 6;");
        switch (t.getDifficulty()) {
            case EASY -> badge.setStyle(badge.getStyle() + "-fx-background-color: #c8e6c9; -fx-text-fill: #256029;");
            case MODERATE -> badge.setStyle(badge.getStyle() + "-fx-background-color: #fff59d; -fx-text-fill: #6d4c41;");
            case HARD -> badge.setStyle(badge.getStyle() + "-fx-background-color: #ef9a9a; -fx-text-fill: #b71c1c;");
        }
        return badge;
    }


    private String getAnimalSuggestion(TerrainType terrain) {
        var animals = TerrainAnimalMapping.getAnimalsByTerrain(terrain);
        return "Common animals here: " + String.join(", ",
                animals.stream().map(Enum::name).toList());
    }

    private String getSeasonTip(AnimalGroup group) {
        int m = java.time.LocalDate.now().getMonthValue();
        return switch (AnimalSeasonService.getActivityLevel(group, m)) {
            case 3 -> "Great time to observe " + group.name();
            case 2 -> "Moderate activity.";
            default -> "Low activity season.";
        };
    }

    private String getPredatorWarning(TerrainType terrain, AnimalGroup group) {
        if (group == AnimalGroup.PREDATOR) return "Predator habitat.";
        var animals = TerrainAnimalMapping.getAnimalsByTerrain(terrain);
        if (animals.contains(AnimalGroup.PREDATOR))
            return "Predators may be present.";
        return "Safe area — No major predator risk.";
    }


    private Image loadTrailImage(Trail t) {
        String fallback = "/ui/images/animals/default.jpg";
        try {
            return new Image(getClass().getResourceAsStream(fallback));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/ui/images/animals/default.jpg"));
        }
    }

    /** 过滤：TrailList + for 循环 */
    private void applyFilters() {
        TrailList<Trail> filtered = new TrailList<>();

        String diff = difficultyFilter.getValue();
        boolean needDiff = diff != null && !"All".equals(diff);

        Double maxLen = null;
        try {
            String txt = maxLengthField.getText();
            if (txt != null && !txt.isBlank()) {
                maxLen = Double.parseDouble(txt);
            }
        } catch (Exception ignore) {}

        for (int i = 0; i < trailList.size(); i++) {
            Trail t = trailList.get(i);
            if (t == null) continue;

            if (needDiff && !t.getDifficulty().name().equals(diff)) continue;
            if (petFilter.isSelected() && !t.isPetFriendly()) continue;
            if (campFilter.isSelected() && !t.isCampingAllowed()) continue;
            if (maxLen != null && t.getLength() > maxLen) continue;

            filtered.add(t);
        }

        renderCards(filtered);
    }

    private void onViewDetail(Trail t) {
        AppNavigator.showPageWithData(
            "/ui/trail_detail.fxml",
            (TrailDetailController c) -> c.setTrail(t)
        );
    }

    /** Join：group 也用 TrailList + for 循环（不使用 stream） */
    private void onJoinGroup(Trail t) {
        TrailList<Group> all = GlobalData.getAllGroups();
        TrailList<Group> groups = new TrailList<>();

        for (int i = 0; i < all.size(); i++) {
            Group g = all.get(i);
            if (g != null && g.getTrail() != null && g.getTrail().equals(t)) {
                groups.add(g);
            }
        }

        boolean hasGroup = groups.size() > 0;

        if (hasGroup) {
            Group g = groups.get(0);
            AppNavigator.showPageWithData(
                "/ui/group-detail.fxml",
                (GroupDetailController c) -> c.init(service, g, 1)
            );
        } else {
            // 把 List<Trail> 转成 TrailList<Trail>
            var src = GlobalData.getAllTrails();
            TrailList<Trail> tl = new TrailList<>();
            if (src != null) {
                for (int i = 0; i < src.size(); i++) tl.add(src.get(i));
            }

            AppNavigator.showPageWithData(
                "/ui/group-create.fxml",
                (GroupCreateController c) -> {
                    c.init(service, tl);
                    c.prefillTrail(t);
                }
            );
        }
    }
}

