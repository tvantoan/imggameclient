package imggame.controller;

import imggame.Main;
import imggame.core.SessionManager;
import imggame.game.GameRoom;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.*;
import imggame.utils.Async;
import imggame.utils.MediaPlayerManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class RoomController {
    // Player panes for highlighting
    @FXML
    private AnchorPane leftPlayerPane;
    @FXML
    private AnchorPane rightPlayerPane;

    // Left player (Player 1)
    @FXML
    private Label leftUserNameLbl;
    @FXML
    private Label leftEloLbl;
    @FXML
    private Label leftScoreLbl;
    @FXML
    private Label leftTimeLbl;
    @FXML
    private Button leftReadyBtn;
    @FXML
    private Button leftInviteBtn;

    // Right player (Player 2)
    @FXML
    private Label rightUserNameLbl;
    @FXML
    private Label rightEloLbl;
    @FXML
    private Label rightScoreLbl;
    @FXML
    private Label rightTimeLbl;
    @FXML
    private Button rightReadyBtn;
    @FXML
    private Button rightInviteBtn;

    // Images
    @FXML
    private ImageView orgImage;
    @FXML
    private ImageView diffImage;

    // Overlay panes for markers
    @FXML
    private javafx.scene.layout.Pane orgImageOverlay;
    @FXML
    private javafx.scene.layout.Pane diffImageOverlay;

    // Control buttons
    @FXML
    private Button quitBtn;

    // Remaining differences label
    @FXML
    private Label remainingDifferencesLbl;

    private GameRoomResponse currentRoom;
    private boolean gameStarted = false;
    private boolean myTurn = false;
    private boolean iAmPlayer1 = false; // Track if current user is player 1
    private Client client;

    public void setRoom(GameRoomResponse room) {
        this.currentRoom = room;
        Platform.runLater(() -> {
            // Player 1 (Left side - always present)
            if (room.player1 != null) {
                leftUserNameLbl.setText(room.player1.getUsername());
                leftEloLbl.setText("ELO: " + room.player1.getElo());
                leftScoreLbl.setText("Điểm: 0");
                leftTimeLbl.setText("--:--");
                leftInviteBtn.setVisible(false);
            } else {
                leftUserNameLbl.setText("Chờ người chơi...");
                leftEloLbl.setText("");
                leftScoreLbl.setText("");
                leftTimeLbl.setText("");
                leftInviteBtn.setVisible(true);
            }

            // Player 2 (Right side - may be empty)
            if (room.player2 != null) {
                rightUserNameLbl.setText(room.player2.getUsername());
                rightEloLbl.setText("ELO: " + room.player2.getElo());
                rightScoreLbl.setText("Điểm: 0");
                rightTimeLbl.setText("--:--");
                rightReadyBtn.setDisable(false);
                rightInviteBtn.setVisible(false);
            } else {
                rightUserNameLbl.setText("Chờ người chơi...");
                rightEloLbl.setText("");
                rightScoreLbl.setText("");
                rightTimeLbl.setText("");
                rightReadyBtn.setDisable(true);
                rightInviteBtn.setVisible(true);
            }

            // Determine which player is the current user
            User me = SessionManager.getCurrentUser();
            if (me != null) {
                boolean isPlayer1 = room.player1 != null && room.player1.getId() == me.getId();
                boolean isPlayer2 = room.player2 != null && room.player2.getId() == me.getId();

                // Save which player the current user is
                iAmPlayer1 = isPlayer1;

                // Enable ready button for the current user
                if (isPlayer1) {
                    leftReadyBtn.setDisable(false);
                    rightReadyBtn.setDisable(true);
                    // Highlight left panel as current user
                    highlightMyPanel(true);
                } else if (isPlayer2) {
                    leftReadyBtn.setDisable(true);
                    rightReadyBtn.setDisable(false);
                    // Highlight right panel as current user
                    highlightMyPanel(false);
                }
            }
        });
    }

    @FXML
    private void initialize() {
        client = SessionManager.getClient();
        client.setResponseHandler(new RoomResponseHandler());

        // Setup button actions
        quitBtn.setOnAction(e -> leaveRoom());
        leftReadyBtn.setOnAction(e -> readyUp(true));
        rightReadyBtn.setOnAction(e -> readyUp(false));
        leftInviteBtn.setOnAction(e -> {
            try {
                invitePlayer();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        rightInviteBtn.setOnAction(e -> {
            try {
                invitePlayer();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Initialize UI
        leftScoreLbl.setText("Điểm: 0");
        rightScoreLbl.setText("Điểm: 0");
        leftTimeLbl.setText("--:--");
        rightTimeLbl.setText("--:--");

        // Initially hide invite buttons
        leftInviteBtn.setVisible(false);
        rightInviteBtn.setVisible(false);

        // Initialize remaining differences label
        remainingDifferencesLbl.setText("Chờ bắt đầu...");
        remainingDifferencesLbl.setStyle("-fx-text-fill: #9E9E9E;");

        // Setup click handler ONLY for diffImage (right side)
        diffImage.setOnMouseClicked(this::onImageClick);

        // Remove click handler from orgImage to prevent confusion
        orgImage.setOnMouseClicked(this::onImageClick);

        // Initially no highlight
        clearHighlight();
    }

    private void leaveRoom() {
        Async.run(() -> {
            try {
                Client client = SessionManager.getClient();
                User me = SessionManager.getCurrentUser();
                String roomId = SessionManager.getCurrentRoomId();

                if (me == null || roomId == null)
                    return;

                client.send(new LeaveGameRoomRequest(me.getId(), roomId));
                SessionManager.setCurrentRoomId(null);

                Platform.runLater(() -> {
                    try {
                        Main.getSceneManager().switchScene("/fxml/main_menu.fxml");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(
                        () -> new Alert(Alert.AlertType.ERROR, "Không thể rời phòng: " + ex.getMessage()).show());
            }
        });
    }

    private void readyUp(boolean isLeftPlayer) {
        Platform.runLater(() -> {
            if (isLeftPlayer) {
                leftReadyBtn.setText("Đã sẵn sàng");
                leftReadyBtn.setDisable(true);
            } else {
                rightReadyBtn.setText("Đã sẵn sàng");
                rightReadyBtn.setDisable(true);
            }
        });
        client.send(new StartGameRequest(currentRoom.roomId, SessionManager.getCurrentUser().getId()));
    }
    private void invitePlayer() throws Exception {
        // TODO: Implement invite functionality
        // This could open a player list dialog or send an invite request

        InviteListController.showAsDialog();
    }

    private void onImageClick(MouseEvent event) {

        System.out.println("Image clicked at: " + event.getX() + ", " + event.getY());
        if (!gameStarted || !myTurn) {
            System.out.println("Click ignored: gameStarted=" + gameStarted + ", myTurn=" + myTurn);
            return; // Ignore clicks when game not started or not player's turn
        }

        // Get relative coordinates from diffImage (right side)
        double x = event.getX();
        double y = event.getY();

        // Make sure click is within image bounds
        if (x < 0 || y < 0 || x > diffImage.getFitWidth() || y > diffImage.getFitHeight()) {
            return;
        }

        // Send coordinates to server for validation
        client.send(
                new GuessPointRequest(currentRoom.roomId, SessionManager.getCurrentUser().getId(), (int) x, (int) y));
        MediaPlayerManager.getInstance().playSoundEffect("click.mp3");
        System.out.println("Sent guess point: (" + (int) x + ", " + (int) y + ")");
    }

    private void loadImageFromServer(String imagePath, boolean isOriginal) {
        Async.run(() -> {
            try {
                Client client = SessionManager.getClient();
                // Send request to get image data
                client.send(new GetImageRequest(imagePath, isOriginal));

                // Note: Response will be handled in ResponseHandler
                // which should update the ImageView with received image data
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(
                        () -> new Alert(Alert.AlertType.ERROR, "Không thể tải ảnh: " + ex.getMessage()).show());
            }
        });
    }

    private void updatePlayerScores(int p1Score, int p2Score, int p1Timer, int p2Timer) {
        Platform.runLater(() -> {
            leftScoreLbl.setText("Điểm: " + p1Score);
            leftTimeLbl.setText(formatTime(p1Timer));
            rightScoreLbl.setText("Điểm: " + p2Score);
            rightTimeLbl.setText(formatTime(p2Timer));
        });
    }

    private void highlightPlayerTurn(boolean isPlayer1Turn) {
        Platform.runLater(() -> {
            if (isPlayer1Turn) {
                // Highlight left player with green for turn
                String leftStyle = "-fx-background-color: rgba(76, 175, 80, 0.2); -fx-border-color: #4CAF50; -fx-border-width: 3;";
                // If this is current user, keep blue accent
                if (iAmPlayer1) {

//                    MediaPlayerManager.getInstance().playSoundEffect("myturn.mp3");
                    leftStyle = "-fx-background-color: rgba(76, 175, 80, 0.2); -fx-border-color: #2196F3; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-radius: 5;";
                }
                leftPlayerPane.setStyle(leftStyle);

                // Right player no turn highlight
                String rightStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 3;";
                // If this is current user, keep blue accent
                if (!iAmPlayer1) {
                    rightStyle = "-fx-background-color: rgba(33, 150, 243, 0.1); -fx-border-color: #2196F3; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-radius: 5;";
                }
                rightPlayerPane.setStyle(rightStyle);
            } else {
                // Highlight right player with green for turn
                String rightStyle = "-fx-background-color: rgba(76, 175, 80, 0.2); -fx-border-color: #4CAF50; -fx-border-width: 3;";
                // If this is current user, keep blue accent
                if (!iAmPlayer1) {

//                    MediaPlayerManager.getInstance().playSoundEffect("click.mp3");
                    rightStyle = "-fx-background-color: rgba(76, 175, 80, 0.2); -fx-border-color: #2196F3; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-radius: 5;";
                }
                rightPlayerPane.setStyle(rightStyle);

                // Left player no turn highlight
                String leftStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 3;";
                // If this is current user, keep blue accent
                if (iAmPlayer1) {
                    leftStyle = "-fx-background-color: rgba(33, 150, 243, 0.1); -fx-border-color: #2196F3; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-radius: 5;";
                }
                leftPlayerPane.setStyle(leftStyle);
            }
        });
    }

    private void clearHighlight() {
        Platform.runLater(() -> {
            leftPlayerPane
                    .setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 3;");
            rightPlayerPane
                    .setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 3;");
        });
    }

    private void highlightMyPanel(boolean isLeftPanel) {
        Platform.runLater(() -> {
            if (isLeftPanel) {
                // Highlight left panel with blue border and "BẠN" indicator
                leftPlayerPane.setStyle(
                        "-fx-background-color: rgba(33, 150, 243, 0.1); " +
                                "-fx-border-color: #2196F3; " +
                                "-fx-border-width: 3; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-radius: 5;");
                leftUserNameLbl.setText(leftUserNameLbl.getText().replace(" (BẠN)", "") + " (BẠN)");
                leftUserNameLbl.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            } else {
                // Highlight right panel with blue border and "BẠN" indicator
                rightPlayerPane.setStyle(
                        "-fx-background-color: rgba(33, 150, 243, 0.1); " +
                                "-fx-border-color: #2196F3; " +
                                "-fx-border-width: 3; " +
                                "-fx-border-radius: 5; " +
                                "-fx-background-radius: 5;");
                rightUserNameLbl.setText(rightUserNameLbl.getText().replace(" (BẠN)", "") + " (BẠN)");
                rightUserNameLbl.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            }
        });
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void drawCorrectMarker(imggame.game.DiffBox box) {
        // Vẽ hình chữ nhật xanh trên cả 2 ảnh
        Rectangle rectOrg = new Rectangle(box.x, box.y, box.width, box.height);
        rectOrg.setFill(Color.TRANSPARENT);
        rectOrg.setStroke(Color.web("#00FF00")); // Màu xanh lá
        rectOrg.setStrokeWidth(3);

        Rectangle rectDiff = new Rectangle(box.x, box.y, box.width, box.height);
        rectDiff.setFill(Color.TRANSPARENT);
        rectDiff.setStroke(Color.web("#00FF00")); // Màu xanh lá
        rectDiff.setStrokeWidth(3);

        orgImageOverlay.getChildren().add(rectOrg);
        diffImageOverlay.getChildren().add(rectDiff);
    }

    private void drawWrongMarker(int x, int y) {
        // Vẽ hình tròn đỏ tại vị trí click trên ảnh diff
        Circle wrongCircle = new Circle(x, y, 3);
        wrongCircle.setFill(Color.web("#FF0000")); // Màu đỏ
        wrongCircle.setStroke(Color.web("#FFFFFF")); // Viền trắng
        wrongCircle.setStrokeWidth(1);

        diffImageOverlay.getChildren().add(wrongCircle);
    }

    private void updateRemainingDifferences(int remaining) {
        Platform.runLater(() -> {
            if (remaining > 0) {
                remainingDifferencesLbl.setText("Còn lại: " + remaining + " điểm");
            } else {
                remainingDifferencesLbl.setText("Hoàn thành!");
                remainingDifferencesLbl.setStyle(
                        "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-alignment: center;");
            }
        });
    }

    private void showGameEndPopup(GameEndNotification gameEnd) {
        // Xác định người thắng và thua
        User me = SessionManager.getCurrentUser();
        boolean iAmWinner = (me != null && me.getId() == gameEnd.winnerId);

        // Tạo dialog với style đẹp
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết thúc trận đấu");

        // Lấy thông tin người chơi
        String winnerName = "";
        String loserName = "";

        if (currentRoom != null) {
            if (currentRoom.player1 != null && currentRoom.player1.getId() == gameEnd.winnerId) {
                winnerName = currentRoom.player1.getUsername();
            } else if (currentRoom.player2 != null && currentRoom.player2.getId() == gameEnd.winnerId) {
                winnerName = currentRoom.player2.getUsername();
            }

            if (currentRoom.player1 != null && currentRoom.player1.getId() == gameEnd.loserId) {
                loserName = currentRoom.player1.getUsername();
            } else if (currentRoom.player2 != null && currentRoom.player2.getId() == gameEnd.loserId) {
                loserName = currentRoom.player2.getUsername();
            }
        }

        // Tạo nội dung hiển thị
        if (iAmWinner) {
            MediaPlayerManager.getInstance().playSoundEffect("win.mp3");
            alert.setHeaderText("🎉 CHIẾN THẮNG! 🎉");
            alert.setContentText(
                    "Chúc mừng! Bạn đã chiến thắng!\n\n" +
                            "Thay đổi ELO: +" + gameEnd.winnerEloChange + "\n" +
                            "Đối thủ: " + loserName + " (" + gameEnd.loserEloChange + ")");

        } else {

            MediaPlayerManager.getInstance().playSoundEffect("lose.mp3");
            alert.setHeaderText("😢 THUA CUỘC");
            alert.setContentText(
                    "Rất tiếc! Bạn đã thua.\n\n" +
                            "Thay đổi ELO: " + gameEnd.loserEloChange + "\n" +
                            "Người chiến thắng: " + winnerName + " (+" + gameEnd.winnerEloChange + ")");
        }

        // Thêm CSS styling cho dialog
        alert.getDialogPane().setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
                        "-fx-padding: 20px;");

        // Style cho header
        alert.getDialogPane().lookup(".header-panel").setStyle(
                "-fx-background-color: " + (iAmWinner ? "#4CAF50" : "#F44336") + ";" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 15px;");

        // Style cho content
        alert.getDialogPane().lookup(".content").setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-padding: 20px;");

        alert.showAndWait();

        client.send(new GetRoomRequest(currentRoom.roomId, SessionManager.getCurrentUser().getId()));
    }

    private void resetRoomState() {
        // Clear markers
        if (orgImageOverlay != null) {
            orgImageOverlay.getChildren().clear();
        }
        if (diffImageOverlay != null) {
            diffImageOverlay.getChildren().clear();
        }

        // Clear images
        orgImage.setImage(null);
        diffImage.setImage(null);

        // Reset labels
        remainingDifferencesLbl.setText("Chờ bắt đầu...");
        remainingDifferencesLbl.setStyle("-fx-text-fill: #9E9E9E;");

        // Reset ready buttons
        leftReadyBtn.setText("Sẵn sàng");
        leftReadyBtn.setDisable(false);
        rightReadyBtn.setText("Sẵn sàng");
        rightReadyBtn.setDisable(false);

        // Enable ready button only for current user
        User me = SessionManager.getCurrentUser();
        if (me != null && currentRoom != null) {
            boolean isPlayer1 = currentRoom.player1 != null && currentRoom.player1.getId() == me.getId();
            boolean isPlayer2 = currentRoom.player2 != null && currentRoom.player2.getId() == me.getId();

            if (isPlayer1) {
                leftReadyBtn.setDisable(false);
                rightReadyBtn.setDisable(true);
            } else if (isPlayer2) {
                leftReadyBtn.setDisable(true);
                rightReadyBtn.setDisable(false);
            }
        }

        // Reset game state
        gameStarted = false;
        myTurn = false;
        clearHighlight();
    }

    private class RoomResponseHandler implements ResponseHandler {
        @Override
        public void handleResponse(Object response) {
            if (response instanceof GameRoomResponse) {
                GameRoomResponse room = (GameRoomResponse) response;
                currentRoom = room;
                setRoom(room);
            } else if (response instanceof ErrorResponse) {
                ErrorResponse error = (ErrorResponse) response;
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, error.message).show());
            } else if (response instanceof String) {
                String message = (String) response;
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, message).show());
            } else if (response instanceof GameStateUpdateNotification) {
                GameStateUpdateNotification gameState = (GameStateUpdateNotification) response;
                Platform.runLater(() -> {
                    // Cập nhật điểm số và thời gian
                    updatePlayerScores(gameState.player1Score, gameState.player2Score,
                            gameState.player1Timer, gameState.player2Timer);

                    // Xác định lượt chơi
                    User me = SessionManager.getCurrentUser();
                    if (me != null && currentRoom != null) {
                        // Kiểm tra xem user hiện tại là player1 hay player2
                        boolean isCurrentUserPlayer1 = currentRoom.player1 != null &&
                                currentRoom.player1.getId() == me.getId();

                        // Cập nhật myTurn dựa trên isPlayer1Turn
                        if (isCurrentUserPlayer1) {
                            myTurn = gameState.isPlayer1Turn;
                        } else {
                            myTurn = !gameState.isPlayer1Turn;
                        }
                    }

                    // Highlight player whose turn it is
                    if (gameState.isPlayer1Turn) {
                        highlightPlayerTurn(true);
                    } else {
                        highlightPlayerTurn(false);
                    }

                    if (gameState.gameState == GameRoom.GameState.PLAYING) {
                        gameStarted = true;
                    }
                    if (gameState.gameState == GameRoom.GameState.FINISHED) {
                        gameStarted = false;
                        clearHighlight();
                    }

                });
            } else if (response instanceof GameStartNotification) {
                GameStartNotification gameStart = (GameStartNotification) response;
                Platform.runLater(() -> {
                    gameStarted = true;

                    // Load images từ server
                    if (gameStart.originImagePath != null && !gameStart.originImagePath.isEmpty()) {
                        loadImageFromServer(gameStart.originImagePath, true);
                    }
                    if (gameStart.diffImagePath != null && !gameStart.diffImagePath.isEmpty()) {
                        loadImageFromServer(gameStart.diffImagePath, false);
                    }
                    MediaPlayerManager.getInstance().playMusic("ingame.mp3", true);

                });
            } else if (response instanceof ImageBufferResponse) {
                ImageBufferResponse imgResp = (ImageBufferResponse) response;
                Platform.runLater(() -> {
                    try {
                        javafx.scene.image.Image img = new javafx.scene.image.Image(
                                new java.io.ByteArrayInputStream(imgResp.imageBuffer));
                        if (imgResp.isOriginal) {
                            orgImage.setImage(img);
                        } else {
                            diffImage.setImage(img);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Không thể hiển thị ảnh: " + ex.getMessage()).show();
                    }
                });
            } else if (response instanceof GuessPointResponse) {
                GuessPointResponse guessResp = (GuessPointResponse) response;
                System.out.println("Received GuessPointResponse: " + guessResp.correct);
                System.out.println("Coordinates: (" + guessResp.guessX + ", " + guessResp.guessY + ")");
                Platform.runLater(() -> {
                    if (guessResp.correct && guessResp.foundBox != null) {
                        // Đoán đúng: Vẽ hình chữ nhật xanh theo bounding box
                        drawCorrectMarker(guessResp.foundBox);
                    } else {
                        // Đoán sai: Vẽ hình tròn đỏ tại vị trí click
                        drawWrongMarker(guessResp.guessX, guessResp.guessY);
                    }

                    // Cập nhật số điểm còn lại
                    updateRemainingDifferences(guessResp.remainingDifferences);
                });
            } else if (response instanceof GameEndNotification) {
                GameEndNotification gameEnd = (GameEndNotification) response;
                Platform.runLater(() -> {
                    gameStarted = false;
                    clearHighlight();
                    showGameEndPopup(gameEnd);
                    resetRoomState();
                    MediaPlayerManager.getInstance().playMusic("hall.mp3", true);
                });
            }
        }
    }
}
