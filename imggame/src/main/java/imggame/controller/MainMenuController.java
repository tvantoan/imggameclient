package imggame.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainMenuController {
    @FXML
    private ImageView avatarImg;
    @FXML private Label usernameLabel;
    @FXML private Label eloLabel;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox contentBox; // area to load subviews (matchmaking, challenge, leaderboard)



//    public void setPlayer(String username, int elo, MockServerService server) {
//        this.server = server;
//        usernameLabel.setText(username);
//        eloLabel.setText("ELO: " + elo);
//    }


    @FXML
    private void initialize() {
//        settingsBtn.setOnAction(e -> openSettings());
//        logoutBtn.setOnAction(e -> doLogout());
    }

//
//    private void openSettings() {
//// Simple toggle small menu: for demo we will swap contentBox with settings pane
//        try {
//            Parent p = FXMLLoader.load(getClass().getResource("/fxml/settings_popup.fxml"));
//            contentBox.getChildren().setAll(p);
//        } catch (Exception ex) { ex.printStackTrace(); }
//    }
//
//
//    private void doLogout() {
//        try {
//            Parent p = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
//            Stage st = (Stage) logoutBtn.getScene().getWindow();
//            st.getScene().setRoot(p);
//        } catch (Exception ex) { ex.printStackTrace(); }
//    }
//
//
//    @FXML
//    private void onRandomMatch() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/matchmaking.fxml"));
//            Parent p = loader.load();
//            MatchmakingController mc = loader.getController();
//            mc.init(server);
//            contentBox.getChildren().setAll(p);
//        } catch (Exception ex) { ex.printStackTrace(); }
//    }
//
//
//    @FXML
//    private void onChallenge() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/challenge_list.fxml"));
//            Parent p = loader.load();
//            ChallengeListController cc = loader.getController();
//            cc.init(server);
//            contentBox.getChildren().setAll(p);
//        } catch (Exception ex) { ex.printStackTrace(); }
//    }


    @FXML
    private void onLeaderboard() {
        contentBox.getChildren().clear();
        Label lb = new Label("Leaderboard (demo)");
        contentBox.getChildren().add(lb);
    }
}