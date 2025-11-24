package imggame.controller;

import imggame.Main;
import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.CreateGameRoomRequest;
import imggame.network.packets.GameRoomResponse;
import imggame.network.packets.JoinGameRoomRequest;
import imggame.network.packets.MessagePacket;
import imggame.network.packets.RoomListResponse;
import imggame.network.packets.RoomResponse;
import imggame.network.types.MessageContext;
import imggame.network.packets.ErrorResponse;
import imggame.utils.Async;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class JoinRoomController {

	@FXML
	private ListView<RoomResponse> roomList;
	@FXML
	private Button joinBtn;
	@FXML
	private Button refreshBtn;
	@FXML
	private Button createBtn;
	@FXML
	private Label infoLabel;

	private RoomResponse selectedRoom;

	@FXML
	private void initialize() {
		Client client = SessionManager.getClient();
		client.setResponseHandler(new JoinRoomResponseHandler());

		roomList.setCellFactory(param -> new ListCell<>() {
			@Override
			protected void updateItem(RoomResponse room, boolean empty) {
				super.updateItem(room, empty);
				if (empty || room == null) {
					setText(null);
				} else {
					String status = room.playerCount + "/" + room.maxPlayers + " người chơi";
					String roomName = room.roomName != null ? room.roomName : "Phòng ID: " + room.roomId;
					setText(roomName + " | " + status);
				}
			}
		});

		roomList.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
			selectedRoom = now;
			if (now != null)
				infoLabel.setText("Đã chọn phòng: " + now.roomId);
		});

		refreshBtn.setOnAction(e -> loadRooms());
		joinBtn.setOnAction(e -> joinSelectedRoom());
		createBtn.setOnAction(e -> createNewRoom());
		loadRooms();
	}

	private void loadRooms() {
		infoLabel.setText("Đang tải danh sách phòng...");
		Async.run(() -> {
			try {
				Client client = SessionManager.getClient();
				client.send(new MessagePacket("", MessageContext.GET_ROOM_LIST));
			} catch (Exception ex) {
				ex.printStackTrace();
				Platform.runLater(() -> infoLabel.setText("Lỗi: " + ex.getMessage()));
			}
		});
	}

	private void createNewRoom() {
		User me = SessionManager.getCurrentUser();
		if (me == null) {
			infoLabel.setText("Bạn chưa đăng nhập.");
			return;
		}
		Async.run(() -> {
			try {
				Client client = SessionManager.getClient();
				client.send(new CreateGameRoomRequest(me.getId()));
			} catch (Exception ex) {
				ex.printStackTrace();
				Platform.runLater(() -> infoLabel.setText("Lỗi: " + ex.getMessage()));
			}
		});
	}

	private void joinSelectedRoom() {
		if (selectedRoom == null) {
			infoLabel.setText("Chưa chọn phòng.");
			return;
		}

		User me = SessionManager.getCurrentUser();
		if (me == null) {
			infoLabel.setText("Bạn chưa đăng nhập.");
			return;
		}

		Async.run(() -> {
			try {
				Client client = SessionManager.getClient();
				client.send(new JoinGameRoomRequest(me.getId(), selectedRoom.roomId));
			} catch (Exception ex) {
				ex.printStackTrace();
				Platform.runLater(() -> infoLabel.setText("Lỗi: " + ex.getMessage()));
			}
		});
	}

	private void openRoomView(GameRoomResponse roomResp) {
		try {
			// Switch scene and get controller
			RoomController roomController = Main.getSceneManager().switchScene("/fxml/room_view.fxml", 1900, 890);

			// Pass room data to controller
			roomController.setRoom(roomResp);
		} catch (Exception ex) {
			ex.printStackTrace();
			infoLabel.setText("Lỗi mở phòng: " + ex.getMessage());
		}
	}

	private class JoinRoomResponseHandler implements ResponseHandler {
		@Override
		public void handleResponse(Object response) {
			if (response instanceof RoomListResponse) {
				// Room list response
				RoomListResponse roomListResponse = (RoomListResponse) response;
				List<RoomResponse> rooms = roomListResponse.getRooms();
				Platform.runLater(() -> {
					if (rooms != null) {
						roomList.getItems().setAll(rooms);
						infoLabel.setText("Đã tải " + rooms.size() + " phòng chờ.");
					} else {
						roomList.getItems().clear();
						infoLabel.setText("Không có phòng nào.");
					}
				});
			} else if (response instanceof GameRoomResponse) {
				// Successfully created or joined a room
				GameRoomResponse room = (GameRoomResponse) response;
				Platform.runLater(() -> {
					infoLabel.setText("Đã vào phòng: " + room.roomId);
					SessionManager.setCurrentRoomId(room.roomId);
					openRoomView(room);
				});
			} else if (response instanceof ErrorResponse) {
				ErrorResponse error = (ErrorResponse) response;
				Platform.runLater(() -> {
					infoLabel.setText("Lỗi: " + error.message);
					loadRooms(); // Refresh list on error
				});
			} else {
				Platform.runLater(() -> infoLabel.setText("Phản hồi không xác định: " + response));
			}
		}
	}
}
