package imggame.controller;

import imggame.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuController {
    @FXML private ImageView avatarImg;
    @FXML private Label usernameLabel;
    @FXML private Label eloLabel;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox contentBox;

    public void setPlayer(User user) {
        usernameLabel.setText(user.getUsername());
        eloLabel.setText(String.valueOf(user.getElo()));
    }

    @FXML
    private void initialize() {
        settingsBtn.setOnAction(e -> openSettings());
        logoutBtn.setOnAction(e -> doLogout());
    }

    private void openSettings() {
        try {
            Parent p = FXMLLoader.load(getClass().getResource("/fxml/settings_popup.fxml"));
            contentBox.getChildren().setAll(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void doLogout() {
        try {
            Parent p = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage st = (Stage) logoutBtn.getScene().getWindow();
            st.getScene().setRoot(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void onJoinRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/join_room_view.fxml"));
            Parent p = loader.load();
            contentBox.getChildren().setAll(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void onCreateRoom() {
        // simply open the join/create view which has Create button
        onJoinRoom();
    }

    @FXML
    private void onLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ranking_player.fxml"));
            Parent p = loader.load();
            contentBox.getChildren().setAll(p);
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
