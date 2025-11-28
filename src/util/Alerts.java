package util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Alerts {
 private Alerts() {}

 public static void info(String msg)  { show(Alert.AlertType.INFORMATION, "Info", msg); }
 public static void warn(String msg)  { show(Alert.AlertType.WARNING,     "Warning", msg); }
 public static void error(String msg) { show(Alert.AlertType.ERROR,       "Error", msg); }

 /** 返回 true 表示点击了 OK */
 public static boolean confirm(String msg) {
     final boolean[] res = {false};
     runOnFx(() -> {
         Alert a = new Alert(Alert.AlertType.CONFIRMATION);
         a.setTitle("Confirm");
         a.setHeaderText(null);
         a.setContentText(msg);
         a.showAndWait().ifPresent(bt -> res[0] = bt == ButtonType.OK);
     });
     return res[0];
 }

 private static void show(Alert.AlertType type, String title, String msg) {
     runOnFx(() -> {
         Alert a = new Alert(type);
         a.setTitle(title);
         a.setHeaderText(null);
         a.setContentText(msg);
         a.show();
     });
 }

 private static void runOnFx(Runnable r) {
     if (Platform.isFxApplicationThread()) r.run();
     else Platform.runLater(r);
 }
}
