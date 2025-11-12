package imggame.controller;

import imggame.Main;
import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.GameService;
import imggame.network.ServiceResult;
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
        loginBtn.setOnAction(e -> {
            Async.run(() -> {

                String username = usernameField.getText();
                String password = passwordField.getText();
                if (username.isEmpty() || password.isEmpty()) {
                    errorLabel.setText("Please type username and password!");
                } else {

                    GameService service = SessionManager.getService();
                    if (service == null) {
                        Platform.runLater(() -> errorLabel.setText("Service not connected!"));
                        return;
                    }
                    ServiceResult<User> result = service.login(username, password);
                    SessionManager.setCurrentUser(result.data);
                    Platform.runLater(() -> {
                        if (result.success) {
                            try {
                                Main.getSceneManager().switchScene("/fxml/main_menu.fxml");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            errorLabel.setText(result.message);
                        }
                    });
                }
            });
        });

        registerBtn.setOnAction(e -> openRegister());
    }

    private void openRegister() {
        try {
            Main.getSceneManager().switchScene("/fxml/register.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}