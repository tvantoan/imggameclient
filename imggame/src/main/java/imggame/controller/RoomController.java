package imggame.controller;

import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.GameService;
import imggame.network.ServiceResult;
import imggame.network.packets.GameRoomResponse;
import imggame.utils.Async;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class RoomController {
    @FXML private ImageView leftAvatar;
    @FXML private Label leftName;
    @FXML private Label leftElo;
    @FXML private Label leftRole;

    @FXML private ImageView rightAvatar;
    @FXML private Label rightName;
    @FXML private Label rightElo;
    @FXML private Label rightRole;
    @FXML private Button readyBtn;
    @FXML private Button startBtn;
    @FXML private Button leaveBtn;
    @FXML private Label roomInfo;
    @FXML private Button inviteBtn;

    private GameRoomResponse currentRoom;

    public void setRoom(GameRoomResponse room) {
        this.currentRoom = room;
        Platform.runLater(() -> {
            roomInfo.setText("ID phòng: " + room.roomId);
            if (room.player1 != null) {
                leftName.setText(room.player1.getUsername());
                leftElo.setText("ELO: " + room.player1.getElo());
                leftRole.setText("Chủ phòng");
            } else {
                leftName.setText("Trống");
                leftElo.setText("");
                leftRole.setText("");
            }

            if (room.player2 != null) {
                rightName.setText(room.player2.getUsername());
                rightElo.setText("ELO: " + room.player2.getElo());
                rightRole.setText("");
                readyBtn.setDisable(false);
            } else {
                rightName.setText("Chưa có đối thủ");
                rightElo.setText("");
                rightRole.setText("");
                readyBtn.setDisable(true);
            }

            User me = SessionManager.getCurrentUser();
            boolean amOwner = me != null && room.player1 != null && room.player1.getId() == me.getId();
            startBtn.setDisable(!amOwner);
            inviteBtn.setDisable(!amOwner || (room.player2 != null));
        });
    }

    @FXML
    private void initialize() {
        leaveBtn.setOnAction(e -> leaveRoom());
        startBtn.setOnAction(e -> startGame());
        readyBtn.setOnAction(e -> readyUp());
        inviteBtn.setOnAction(e -> openInvitePicker());
    }

    private void leaveRoom() {
        Async.run(() -> {
            GameService service = SessionManager.getService();
            User me = SessionManager.getCurrentUser();
            if (service == null || me == null || SessionManager.getCurrentRoomId() == null) return;
            ServiceResult<Void> r = service.leaveRoom(me.getId(), SessionManager.getCurrentRoomId());
            if (r.success) {
                SessionManager.setCurrentRoomId(null);
                Platform.runLater(() -> {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
                        javafx.scene.Parent p = loader.load();
                        javafx.stage.Stage st = (javafx.stage.Stage) leaveBtn.getScene().getWindow();
                        st.getScene().setRoot(p);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
            } else {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Không thể rời phòng: " + r.message).show());
            }
        });
    }

    private void startGame() {
        Async.run(() -> {
            GameService service = SessionManager.getService();
            User me = SessionManager.getCurrentUser();
            if (service == null || me == null || SessionManager.getCurrentRoomId() == null) return;
            ServiceResult<Void> r = service.startGame(SessionManager.getCurrentRoomId(), me.getId());
            Platform.runLater(() -> {
                if (!r.success) {
                    new Alert(Alert.AlertType.ERROR, "Không thể bắt đầu: " + r.message).show();
                    return;
                }
                new Alert(Alert.AlertType.INFORMATION, "Yêu cầu bắt đầu đã gửi. Chờ server xác nhận.").show();
            });
        });
    }

    private void readyUp() {
        Platform.runLater(() -> {
            readyBtn.setText("Đã sẵn sàng");
            readyBtn.setDisable(true);
        });
    }

    private void openInvitePicker() {
        // open available rooms/users to pick and send InviteRequest via GameService.invitePlayer(...)
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/join_room_view.fxml"));
            javafx.scene.Parent pane = loader.load();
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initOwner(inviteBtn.getScene().getWindow());
            dialog.setScene(new javafx.scene.Scene(pane));
            dialog.setTitle("Chọn người để mời");
            dialog.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
