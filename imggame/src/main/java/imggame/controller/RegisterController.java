package imggame.controller;

import imggame.Main;
import imggame.config.ScenePath;
import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.ErrorResponse;
import imggame.network.packets.RegisterRequest;
import imggame.utils.Async;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.IOException;

public class RegisterController {
	@FXML
	private TextField usernameField;
	@FXML
	private TextField emailField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private PasswordField confirmField;
	@FXML
	private Button createBtn;
	@FXML
	private Button backBtn;
	@FXML
	private Label infoLabel;

	@FXML
	private void initialize() {
		infoLabel.setText("");
		Client client = SessionManager.getClient();
		client.setResponseHandler(new RegisterResponseHandler());

		createBtn.setOnAction(e -> {
			Async.run(() -> {
				try {
					String username = usernameField.getText();
					String email = emailField.getText();
					String password = passwordField.getText();
					String confirmPassword = confirmField.getText();

					if (username.isEmpty() || password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty()) {
						Platform.runLater(() -> infoLabel.setText("Please fill all the field!"));
					} else if (!password.equals(confirmPassword)) {
						Platform.runLater(() -> infoLabel.setText("Password must match when retype!"));
					} else {
						client.send(new RegisterRequest(username, email, password));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					Platform.runLater(() -> infoLabel.setText("Error: " + ex.getMessage()));
				}
			});
		});

		backBtn.setOnAction(e -> goBack());
	}

	private void goBack() {
		try {
			Main.getSceneManager().switchScene("/fxml/login.fxml");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private class RegisterResponseHandler implements ResponseHandler {
		@Override
		public void handleResponse(Object response) {
			if (response instanceof User) {
				// Registration successful - server sends success message
				Platform.runLater(() -> {
					infoLabel.setText("Register succeed, redirecting...");
					SessionManager.setCurrentUser((User) response);
					PauseTransition pause = new PauseTransition(Duration.seconds(1));
					pause.setOnFinished(event -> {
						try {
							Main.getSceneManager().switchScene(ScenePath.MAIN_MENU);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					});
					pause.play();
				});
			} else if (response instanceof ErrorResponse) {
				ErrorResponse error = (ErrorResponse) response;
				Platform.runLater(() -> infoLabel.setText("Registration failed: " + error.message));
			} else {
				Platform.runLater(() -> infoLabel.setText("Registration failed: " + response.toString()));
			}
		}
	}
}