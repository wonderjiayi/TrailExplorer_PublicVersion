package controller;

import application.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.trail.Difficulty;
import model.trail.Topic;
import model.trail.Trail;

import java.util.ArrayList;
import java.util.List;

public class SearchController {
    private static String initialQuery = "";

    public static void setInitialQuery(String q) {
        initialQuery = q == null ? "" : q;
    }

    @FXML private TextField searchField;

    @FXML private TitledPane filtersPane;
    @FXML private ComboBox<Topic> topicBox;
    @FXML private ComboBox<Difficulty> difficultyBox;

    @FXML private Slider lengthSlider;
    @FXML private Label lengthValueLabel;

    @FXML private Slider hoursSlider;
    @FXML private Label hoursValueLabel;

    @FXML private Slider elevationSlider;
    @FXML private Label elevationValueLabel;

    @FXML private CheckBox petCheck;
    @FXML private CheckBox campCheck;
    @FXML private CheckBox wildlifeCheck;

    @FXML private VBox resultsPreview;

    private List<Trail> allTrails;

    @FXML
    public void initialize() {
        // 1) 准备数据
        allTrails = GlobalData.index.getAll();

        // 2) 初始化下拉框
        topicBox.getItems().setAll(Topic.values());
        difficultyBox.getItems().setAll(Difficulty.values());

        // 3) Slider 显示当前值
        lengthSlider.valueProperty().addListener((obs, o, n) ->
                lengthValueLabel.setText(String.format("%.1f", n.doubleValue())));
        hoursSlider.valueProperty().addListener((obs, o, n) ->
                hoursValueLabel.setText(String.format("%.1f", n.doubleValue())));
        elevationSlider.valueProperty().addListener((obs, o, n) ->
                elevationValueLabel.setText(String.format("%.0f", n.doubleValue())));

        // 设置初始值
        lengthSlider.setValue(8);
        hoursSlider.setValue(4);
        elevationSlider.setValue(2000);

        // 先触发一次 label 更新
        lengthValueLabel.setText(String.format("%.1f", lengthSlider.getValue()));
        hoursValueLabel.setText(String.format("%.1f", hoursSlider.getValue()));
        elevationValueLabel.setText(String.format("%.0f", elevationSlider.getValue()));

        // 4) Filters 默认展开（第一次进入）
        filtersPane.setExpanded(true);

        // 5) 监听 Filters 变化 → 实时过滤（不折叠）
        difficultyBox.valueProperty().addListener((obs, o, n) -> applySearch(false));
        topicBox.valueProperty().addListener((obs, o, n) -> applySearch(false));
        petCheck.selectedProperty().addListener((obs, o, n) -> applySearch(false));
        campCheck.selectedProperty().addListener((obs, o, n) -> applySearch(false));
        wildlifeCheck.selectedProperty().addListener((obs, o, n) -> applySearch(false));

        // 滑块在鼠标释放时再触发（避免每个像素都刷新）
        lengthSlider.setOnMouseReleased(e -> applySearch(false));
        hoursSlider.setOnMouseReleased(e -> applySearch(false));
        elevationSlider.setOnMouseReleased(e -> applySearch(false));
        
        // 7) 用户开始输入 → 自动折叠 filters
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                filtersPane.setExpanded(false);   // 输入 → 折叠
            } else {
                filtersPane.setExpanded(true);    // 清空 → 展开
            }
        });
        
        // 6) 如果从 Landing 传入了搜索词，自动搜索一次
        if (!initialQuery.isBlank()) {
            searchField.setText(initialQuery);
            applySearch(true);   // 搜索后自动折叠 filters
            // 用完一次就清空，避免下次又自动搜索
            initialQuery = "";
        } else {
            // 初始默认显示所有路线（不过 Filters 仍可筛）
            display(allTrails);
        }
    }

    @FXML
    public void onSearch() {
        applySearch(true);   // 用户显式点击搜索 → 有结果则折叠 Filters
    }

    /** Reset 按钮：清空 Filters & 搜索词，展开 Filters 显示全部 */
    @FXML
    public void onResetFilters() {
        searchField.clear();
        topicBox.setValue(null);
        difficultyBox.setValue(null);
        petCheck.setSelected(false);
        campCheck.setSelected(false);
        wildlifeCheck.setSelected(false);
        lengthSlider.setValue(8);
        hoursSlider.setValue(4);
        elevationSlider.setValue(2000);
        filtersPane.setExpanded(true);
        display(allTrails);
    }
    
    /** 手动折叠按钮 */
    @FXML
    private void toggleFilterCollapse() {
        filtersPane.setExpanded(!filtersPane.isExpanded());
    }
    
    /**
     * 核心：应用当前 keyword + Filters，更新结果列表。
     * @param collapseAfter 有搜索条件且有结果时是否折叠 Filters
     */
    private void applySearch(boolean collapseAfter) {

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        // 1) 先读当前 Filter 控件的值
        Topic topic = topicBox.getValue();
        Difficulty diff = difficultyBox.getValue();

        boolean pet = petCheck.isSelected();
        boolean camp = campCheck.isSelected();
        boolean wildlife = wildlifeCheck.isSelected();

        double maxLen = lengthSlider.getValue();
        double maxHours = hoursSlider.getValue();
        double maxElev = elevationSlider.getValue();

        // 2) 如果 keyword 中包含一些结构化条件，则解析并回填到 Filters
        ParsedFromKeyword parsed = parseKeyword(keyword);

        // 只在用户没手动选的情况下，用 keyword 解析出来的值去覆盖
        if (diff == null && parsed.diff != null) {
            diff = parsed.diff;
            difficultyBox.setValue(diff);
        }
        if (topic == null && parsed.topic != null) {
            topic = parsed.topic;
            topicBox.setValue(topic);
        }
        // 长度
        if (parsed.maxLen != null) {
            maxLen = parsed.maxLen;
            lengthSlider.setValue(maxLen);
        }
        if (parsed.pet != null && parsed.pet) {
            pet = true;
            petCheck.setSelected(true);
        }
        if (parsed.camp != null && parsed.camp) {
            camp = true;
            campCheck.setSelected(true);
        }
        if (parsed.wildlife != null && parsed.wildlife) {
            wildlife = true;
            wildlifeCheck.setSelected(true);
        }

        // 3) 开始过滤
        List<Trail> base = new ArrayList<>(allTrails);
        List<Trail> filtered = new ArrayList<>();

        for (Trail t : base) {
            // 3.1 文本匹配：name + park + topic + state 中包含所有 textTokens
            if (!parsed.textTokens.isEmpty()) {
                String text = (t.getName() + " " + t.getPark() + " " +
                        t.getTopic() + " " + t.getState()).toLowerCase();
                boolean match = true;
                for (String tk : parsed.textTokens) {
                    if (!text.contains(tk)) {
                        match = false;
                        break;
                    }
                }
                if (!match) continue;
            }

            // 3.2 结构化过滤
            if (diff != null && t.getDifficulty() != diff) continue;
            if (topic != null && t.getTopic() != topic) continue;
            if (t.getLength() > maxLen) continue;
            if (t.getVisitHours() > maxHours) continue;
            if (t.getElevationGain() > maxElev) continue;
            if (pet && !t.isPetFriendly()) continue;
            if (camp && !t.isCampingAllowed()) continue;
            if (wildlife && !t.isWildAnimalPossible()) continue;
            filtered.add(t);
        }

        // 4) 根据结果控制 Filters 展开/折叠
        display(filtered);

        boolean hasAnyCondition =
                !keyword.isBlank() ||
                diff != null || topic != null ||
                pet || camp || wildlife ||
                maxLen != lengthSlider.getMin() ||
                maxHours != hoursSlider.getMin() ||
                maxElev != elevationSlider.getMin();

        if (filtered.isEmpty()) {
            // 没有结果 → 展开 Filters，鼓励用户调整
            filtersPane.setExpanded(true);
        } else if (collapseAfter && hasAnyCondition) {
            // 有搜索条件且显式点击搜索 → 折叠 Filters
            filtersPane.setExpanded(false);
        }
    }

    /**
     * 将结果列表渲染到 UI
     */
    private void display(List<Trail> trails) {
        resultsPreview.getChildren().clear();

        if (trails.isEmpty()) {
            Label empty = new Label("No trails found. Try adjusting filters.");
            empty.setStyle("-fx-text-fill: #888;");
            resultsPreview.getChildren().add(empty);
            return;
        }

        for (Trail t : trails) {
            VBox card = new VBox(4);
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-color: #E0E0E0;
                -fx-border-radius: 12;
                -fx-padding: 10;
            """);

            Label name = new Label(t.getName());
            name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

            Label line1 = new Label(String.format(
                    "%s • %.1f mi • %s • Elev %.0fft",
                    t.getDifficulty(),
                    t.getLength(),
                    t.getTopic(),
                    t.getElevationGain()
            ));
            line1.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

            Label line2 = new Label(String.format(
                    "Time %.1f h • Pet:%s • Camp:%s • Wildlife:%s",
                    t.getVisitHours(),
                    t.isPetFriendly() ? "Yes" : "No",
                    t.isCampingAllowed() ? "Yes" : "No",
                    t.isWildAnimalPossible() ? "Yes" : "No"
            ));
            line2.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");

            card.getChildren().addAll(name, line1, line2);

            // 点击某条结果 → 跳转到详情页
            card.setOnMouseClicked(e -> {
                AppNavigator.showPageWithData("/ui/trail_detail.fxml", (TrailDetailController dc) -> {
                    dc.setTrail(t);
                });
            });

            resultsPreview.getChildren().add(card);
        }
    }

    // ────────────────── keyword 解析 ─────────────────────

    /**
     * 解析 keyword 中的一些结构化条件：
     * easy / moderate / hard / lake / mountain / ...
     * pet / camping / wildlife / 数字 (max length)
     */
    private ParsedFromKeyword parseKeyword(String keyword) {
        ParsedFromKeyword pf = new ParsedFromKeyword();
        if (keyword == null || keyword.isBlank()) return pf;

        String[] tokens = keyword.trim().toLowerCase().split("\\s+");

        for (String token : tokens) {
            if (token.isBlank()) continue;

            // 难度
            switch (token) {
                case "easy":
                    pf.diff = Difficulty.EASY;
                    continue;
                case "moderate":
                case "mod":
                    pf.diff = Difficulty.MODERATE;
                    continue;
                case "hard":
                    pf.diff = Difficulty.HARD;
                    continue;
            }

            // topic
            switch (token) {
                case "lake":
                    pf.topic = Topic.LAKE;
                    continue;
                case "mountain":
                    pf.topic = Topic.MOUNTAIN;
                    continue;
                case "river":
                    pf.topic = Topic.RIVER;
                    continue;
                case "beach":
                    pf.topic = Topic.BEACH;
                    continue;
                case "forest":
                    pf.topic = Topic.FOREST;
                    continue;
            }

            // boolean
            if (token.equals("pet") || token.equals("petfriendly")) {
                pf.pet = true; continue;
            }
            if (token.equals("camp") || token.equals("camping")) {
                pf.camp = true; continue;
            }
            if (token.equals("wild") || token.equals("wildlife") || token.equals("animal")) {
                pf.wildlife = true; continue;
            }

            // 长度（流水式简单处理：数字或 "<3"）
            if (token.matches("<\\d+(\\.\\d+)?")) {
                pf.maxLen = Double.parseDouble(token.substring(1));
                continue;
            }
            if (token.matches("\\d+(\\.\\d+)?")) {
                pf.maxLen = Double.parseDouble(token);
                continue;
            }

            // 其他 token 当作纯文本
            pf.textTokens.add(token);
        }

        return pf;
    }

    /** keyword 解析结果结构 */
    private static class ParsedFromKeyword {
        Difficulty diff;
        Topic topic;
        Double maxLen;
        Boolean pet;
        Boolean camp;
        Boolean wildlife;

        List<String> textTokens = new ArrayList<>();
    }
}