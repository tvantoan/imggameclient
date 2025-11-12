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

import java.util.List;

public class JoinRoomController {

    @FXML private ListView<GameRoomResponse> roomList;
    @FXML private Button joinBtn;
    @FXML private Button refreshBtn;
    @FXML private Button createBtn;
    @FXML private Label infoLabel;

    private GameRoomResponse selectedRoom;

    @FXML
    private void initialize() {
        roomList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(GameRoomResponse room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                } else {
                    String p1 = room.player1 != null ? room.player1.getUsername() : "Trống";
                    String p2 = room.player2 != null ? room.player2.getUsername() : "—";
                    setText("Phòng " + room.roomId + " | " + p1 + " vs " + p2 + " | Trạng thái: " + room.state);
                }
            }
        });

        roomList.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            selectedRoom = now;
            if (now != null) infoLabel.setText("Đã chọn phòng: " + now.roomId);
        });

        refreshBtn.setOnAction(e -> loadRooms());
        joinBtn.setOnAction(e -> joinSelectedRoom());
        createBtn.setOnAction(e -> createNewRoom());
        loadRooms();
    }

    private void loadRooms() {
        infoLabel.setText("Đang tải danh sách phòng...");
        Async.run(() -> {
            GameService service = SessionManager.getService();
            ServiceResult<List<GameRoomResponse>> result = service.getWaitingRooms();
            Platform.runLater(() -> {
                if (!result.success) {
                    infoLabel.setText("Không thể tải phòng: " + result.message);
                    return;
                }
                roomList.getItems().setAll(result.data);
                infoLabel.setText("Đã tải " + result.data.size() + " phòng chờ.");
            });
        });
    }

    private void createNewRoom() {
        User me = SessionManager.getCurrentUser();
        if (me == null) { infoLabel.setText("Bạn chưa đăng nhập."); return; }
        Async.run(() -> {
            GameService service = SessionManager.getService();
            ServiceResult<GameRoomResponse> result = service.createGameRoom(me.getId());
            System.out.println("ket qua:" + result.success);
            Platform.runLater(() -> {
                if (result.success && result.data != null) {
                    infoLabel.setText("Đã tạo phòng: " + result.data.roomId);
                    SessionManager.setCurrentRoomId(result.data.roomId);
                    openRoomView(result.data);
                } else {
                    infoLabel.setText("Tạo phòng thất bại: " + result.message);
                }
            });
        });
    }

    private void joinSelectedRoom() {
        if (selectedRoom == null) {
            infoLabel.setText("Chưa chọn phòng.");
            return;
        }

        User me = SessionManager.getCurrentUser();
        if (me == null) { infoLabel.setText("Bạn chưa đăng nhập."); return; }

        Async.run(() -> {
            GameService service = SessionManager.getService();
            ServiceResult<GameRoomResponse> result = service.joinGameRoom(me.getId(), selectedRoom.roomId);
            Platform.runLater(() -> {
                if (result.success && result.data != null) {
                    infoLabel.setText("Đã vào phòng: " + result.data.roomId);
                    SessionManager.setCurrentRoomId(result.data.roomId);
                    openRoomView(result.data);
                } else {
                    infoLabel.setText("Không thể tham gia: " + result.message);
                    // refresh list because race could have occurred
                    loadRooms();
                }
            });
        });
    }

    private void openRoomView(GameRoomResponse roomResp) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/room_view.fxml"));
            javafx.scene.Parent p = loader.load();
            RoomController rc = loader.getController();
            rc.setRoom(roomResp);
            javafx.stage.Stage st = (javafx.stage.Stage) roomList.getScene().getWindow();
            st.getScene().setRoot(p);
        } catch (Exception ex) {
            ex.printStackTrace();
            infoLabel.setText("Lỗi mở phòng: " + ex.getMessage());
        }
    }
}
