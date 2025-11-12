package imggame.controller;


import imggame.Main;
import imggame.core.SessionManager;
import imggame.network.GameService;
import imggame.network.ServiceResult;
import imggame.utils.Async;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
        createBtn.setOnAction(e -> {
            Async.run(() -> {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = passwordField.getText();
                String confirmPassword = confirmField.getText();
                System.out.println(username);
                if(username.isEmpty() || password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty()){
                    infoLabel.setText("Please fill all the field!");
                }
                else if (!password.equals(confirmPassword)) {
                    infoLabel.setText("Password must match when retype!");
                } else {
                    GameService service = new GameService(SessionManager.getClient());
                    ServiceResult result = service.register(username, email, password);
                    Platform.runLater(() -> {
                        if (result.success) {
                            infoLabel.setText("Register succeed, redirecting...");
                            PauseTransition pause = new PauseTransition(Duration.seconds(2));
                            pause.setOnFinished(event -> {
                                try {
                                    Main.getSceneManager().switchScene("/fxml/login.fxml");
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
                            pause.play();
                        }
                    });
                }
            });
        });
        backBtn.setOnAction(e -> goBack());
    }


    private void doCreate() {
        String u = usernameField.getText();
        if (u == null || u.isBlank()) {
            infoLabel.setText("username trống");
            return;
        }
        infoLabel.setText("Đăng ký thành công (demo)");
    }


    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage st = (Stage) backBtn.getScene().getWindow();
            st.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}