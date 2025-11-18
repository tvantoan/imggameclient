package imggame.controller;

import imggame.Main;
import imggame.core.SessionManager;
import imggame.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainMenuController {
	@FXML
	private Label usernameLabel;
	@FXML
	private Label eloLabel;
	@FXML
	private Button logoutBtn;
	@FXML
	private VBox contentBox;

	public void setPlayer(User user) {
		usernameLabel.setText("username: " + user.getUsername());
		eloLabel.setText("elo: " + String.valueOf(user.getElo()));
	}

	@FXML
	private void initialize() {
		logoutBtn.setOnAction(e -> doLogout());
		setPlayer(SessionManager.getCurrentUser());
	}

	private void doLogout() {
		try {
			SessionManager.setCurrentUser(null);
			Main.getSceneManager().switchScene("/fxml/login.fxml");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	private void onRoomList() {
		loadContentView("/fxml/room_list_view.fxml");
	}

	@FXML
	private void onJoinRoom() {
		// Alias for onRoomList
		onRoomList();
	}

	@FXML
	private void onCreateRoom() {
		// Load room list view which has Create button
		onRoomList();
	}

	@FXML
	private void onLeaderboard() {
		loadContentView("/fxml/ranking_view.fxml");
	}

	private void loadContentView(String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
			Parent content = loader.load();
			contentBox.getChildren().clear();
			contentBox.getChildren().add(content);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Failed to load view: " + fxmlPath);
		}
	}
}
