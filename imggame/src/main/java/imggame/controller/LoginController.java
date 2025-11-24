package imggame.controller;

import imggame.Main;
import imggame.config.ScenePath;
import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.LoginRequest;
import imggame.utils.Async;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Button loginBtn;
	@FXML
	private Button registerBtn;
	@FXML
	private Label errorLabel;

	@FXML
	private void initialize() {
		errorLabel.setText("");
		Client client = SessionManager.getClient();
		client.setResponseHandler(new LoginResponseHandler());
		loginBtn.setOnAction(ev -> {
			Async.run(() -> {
				try {

					String username = usernameField.getText();
					String password = passwordField.getText();
					if (username.isEmpty() || password.isEmpty()) {
						errorLabel.setText("Please type username and password!");
					} else {
						client.send(new LoginRequest(username, password));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		});

		registerBtn.setOnAction(ev -> openRegister());
	}

	private void openRegister() {
		try {
			Main.getSceneManager().switchScene("/fxml/register.fxml");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class LoginResponseHandler implements ResponseHandler {
		@Override
		public void handleResponse(Object response) {
			if (response instanceof User) {
				User user = (User) response;
				SessionManager.setCurrentUser(user);
				Platform.runLater(() -> {
					try {
						Main.getSceneManager().switchScene(ScenePath.MAIN_MENU);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} else {
				Platform.runLater(() -> {
					errorLabel.setText("Login failed: " + response.toString());
				});
			}
		}
	}
}