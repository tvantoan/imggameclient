package imggame.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
	private final Stage stage;
	private static final String STYLESHEET_PATH = "/styles/styles.css";

	public SceneManager(Stage stage) {
		this.stage = stage;
	}

	private void applyStylesheet(Scene scene) {
		String stylesheet = getClass().getResource(STYLESHEET_PATH).toExternalForm();
		scene.getStylesheets().add(stylesheet);
	}

	public <T> T switchScene(String fxmlPath) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
		Parent root = loader.load();
		Scene scene = new Scene(root, 1080, 720);
		applyStylesheet(scene);
		stage.setScene(scene);
		stage.show();
		stage.centerOnScreen();
		return loader.getController();
	}

	public void setScene(String fxmlPath) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
		Parent root = loader.load();
		Scene scene = new Scene(root, 1080, 720);
		applyStylesheet(scene);
		stage.setScene(scene);
		stage.show();
		stage.centerOnScreen();
	}

	public <T> T switchScene(String fxmlPath, int width, int height) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
		Parent root = loader.load();
		Scene scene = new Scene(root, width, height);
		applyStylesheet(scene);
		stage.setScene(scene);
		stage.show();
		stage.centerOnScreen();
		return loader.getController();
	}
}
