package imggame.controller;

import imggame.Main;
import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.GameRoomResponse;
import imggame.network.packets.InviteResponse;
import imggame.network.packets.JoinGameRoomRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainMenuController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label eloLabel;
    @FXML
    private Button logoutBtn;
    @FXML
    private VBox contentBox;

    public void setPlayer(User user) {
        usernameLabel.setText("username: " + user.getUsername());
        eloLabel.setText("elo: " + String.valueOf(user.getElo()));
    }

    @FXML
    private void initialize() {
        Client client = SessionManager.getClient();
        client.setResponseHandler(new MainMenuController.MainMenuResponseHandler());
        logoutBtn.setOnAction(e -> doLogout());
        setPlayer(SessionManager.getCurrentUser());
    }

    private void doLogout() {
        try {
            SessionManager.setCurrentUser(null);
            Main.getSceneManager().switchScene("/fxml/login.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onRoomList() {
        loadContentView("/fxml/room_list_view.fxml");
    }

    @FXML
    private void onJoinRoom() {
        // Alias for onRoomList
        onRoomList();
    }

    @FXML
    private void onCreateRoom() {
        // Load room list view which has Create button
        onRoomList();
    }

    @FXML
    private void onLeaderboard() {
        loadContentView("/fxml/ranking_view.fxml");
    }

    private void showInviteDialog(User user, Runnable onAccept, Runnable onDecline) {
        javafx.application.Platform.runLater(() -> {
            try {
                // Tải FXML dialog
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invite_dialog.fxml"));
                Parent dialogRoot = loader.load();

                InviteDialogController controller = loader.getController();
                controller.setInviteInfo(user, onAccept, onDecline);

                // Tạo Stage dạng dialog giữa màn hình
                javafx.stage.Stage dialog = new javafx.stage.Stage();
                dialog.setTitle("Lời mời vào phòng");
                dialog.initOwner(Main.getPrimaryStage());
                dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
                dialog.setResizable(false);

                dialog.setScene(new javafx.scene.Scene(dialogRoot));
                dialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void loadContentView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentBox.getChildren().clear();
            contentBox.getChildren().add(content);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Failed to load view: " + fxmlPath);
        }
    }

    private class MainMenuResponseHandler implements ResponseHandler {
        @Override
        public void handleResponse(Object response) {
            if (response instanceof InviteResponse) {
                InviteResponse invite = (InviteResponse) response;
                Platform.runLater(() -> {

                    showInviteDialog(
                            invite.inviteUser,
                            () -> {
                                System.out.println("Called");

                                Client client = SessionManager.getClient();
                                System.out.println(SessionManager.getCurrentUser().getId() + "   " + invite.roomId);

                                client.send(new JoinGameRoomRequest(SessionManager.getCurrentUser().getId(), invite.roomId));
                            },
                            () -> {


                            }
                    );
                });
            } else if (response instanceof GameRoomResponse) {
                // Successfully created or joined a room
                GameRoomResponse room = (GameRoomResponse) response;
                Platform.runLater(() -> {
//                    infoLabel.setText("Đã vào phòng: " + room.roomId);
                    SessionManager.setCurrentRoomId(room.roomId);
                    try {
                        // Switch scene and get controller
                        RoomController roomController = Main.getSceneManager().switchScene("/fxml/room_view.fxml", 1900, 890);

                        // Pass room data to controller
                        roomController.setRoom(room);
                    } catch (Exception ex) {
                        ex.printStackTrace();
//                            infoLabel.setText("Lỗi mở phòng: " + ex.getMessage());
                    }

                });
            }
        }

    }
}