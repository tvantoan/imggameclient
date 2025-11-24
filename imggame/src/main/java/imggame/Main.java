package imggame;

import imggame.core.SessionManager;
import imggame.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	private static SceneManager sceneManager;

	@Override
	public void start(Stage primaryStage) throws Exception {
		SessionManager.connect();

		primaryStage.setTitle("Spot the Difference");

		sceneManager = new SceneManager(primaryStage);
		sceneManager.setScene("/fxml/login.fxml");

		primaryStage.hide();

		sceneManager.switchScene("/fxml/login.fxml");

		primaryStage.setOnCloseRequest(event -> {
			System.out.println("Application is closing...");
			SessionManager.disconnect();
			javafx.application.Platform.exit();
			System.exit(0);
		});
	}

	public static SceneManager getSceneManager() {
		return sceneManager;
	}
}
