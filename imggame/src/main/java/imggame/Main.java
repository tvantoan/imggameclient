package imggame;

import imggame.core.SessionManager;
import imggame.utils.SceneManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static SceneManager sceneManager;
    @Override
    public void start(Stage primaryStage) throws Exception {
        sceneManager = new SceneManager(primaryStage);
        sceneManager.setScene("/fxml/login.fxml");

        Scene scene = primaryStage.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        }

        primaryStage.setTitle("Spot the Difference");
        primaryStage.show();
        SessionManager.connect();
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }
}
