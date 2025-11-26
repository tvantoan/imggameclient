package imggame.controller;

import imggame.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class InviteDialogController {

    @FXML
    private Label userLabel;
    @FXML
    private Button acceptBtn;
    @FXML
    private Button declineBtn;

    private Runnable onAccept;
    private Runnable onDecline;

    public void setInviteInfo(User user, Runnable onAccept, Runnable onDecline) {
        this.onAccept = onAccept;
        this.onDecline = onDecline;

        userLabel.setText("Người chơi " + user.getUsername() + "có elo: " + user.getElo() + " mời bạn");
    }

    @FXML
    private void initialize() {
        acceptBtn.setOnAction(e -> {
            if (onAccept != null) onAccept.run();
            closeDialog();
        });

        declineBtn.setOnAction(e -> {
            if (onDecline != null) onDecline.run();
            closeDialog();
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) userLabel.getScene().getWindow();
        stage.close();
    }
}
