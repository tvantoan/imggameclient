package imggame;

import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.packets.LeaveGameRoomRequest;
import imggame.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {
	private static SceneManager sceneManager;
    private static Stage primaryStage;

	@Override
	public void start(Stage stage) throws Exception {
        primaryStage = stage;
		SessionManager.connect();

		primaryStage.setTitle("Spot the Difference");

		sceneManager = new SceneManager(primaryStage);
		sceneManager.setScene("/fxml/login.fxml");

		primaryStage.hide();

		sceneManager.switchScene("/fxml/login.fxml");

        primaryStage.setOnCloseRequest(event -> {
			System.out.println("Application is closing...");
            Client client = SessionManager.getClient();
            User me = SessionManager.getCurrentUser();
            String roomId = SessionManager.getCurrentRoomId();

            if (me == null || roomId == null)
                return;

            client.send(new LeaveGameRoomRequest(me.getId(), roomId));
			SessionManager.disconnect();
			javafx.application.Platform.exit();
			System.exit(0);
		});
	}
    public static Stage getPrimaryStage() {
        return primaryStage;
    }


	public static SceneManager getSceneManager() {
		return sceneManager;
	}
}
