/* ---------- File: src/main/java/imggame/GameController.java ---------- */
package imggame.controller;

import imggame.CountdownBar;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Random;

public class GameController {
    @FXML private StackPane leftPane;
    @FXML private StackPane rightPane;
    @FXML private ImageView leftImage;
    @FXML private ImageView rightImage;
    @FXML private Pane leftOverlay;
    @FXML private Pane rightOverlay;
    @FXML private Label statusLabel;
    @FXML private Button leaveBtn;
    @FXML private Button readyBtn;
    @FXML private Button hintBtn;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private Button sendBtn;
    @FXML private StackPane countdownWrap;

    private CountdownBar countdownBar;
    private boolean myTurn = false;
    private final Random rnd = new Random();

    @FXML
    private void initialize() {
        // load placeholder images
        leftImage.setImage(loadResourceImage("/images/sample_left.jpg"));
        rightImage.setImage(loadResourceImage("/images/sample_right.jpg"));

        // set up countdown bar
        countdownBar = new CountdownBar();
        countdownBar.setPrefWidth(240);
        countdownWrap.getChildren().add(countdownBar);

        // events
        leftPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onClickImage(e, leftOverlay));
        rightPane.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> onClickImage(e, rightOverlay));

        leaveBtn.setOnAction(e -> onLeave());
        readyBtn.setOnAction(e -> onReady());
        sendBtn.setOnAction(e -> sendChat());

        // initial state: waiting for players
        setStatus("Waiting for opponent...");
        setMyTurn(false);
    }

    private Image loadResourceImage(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            // fallback to a simple colored placeholder
            return new Image("https://via.placeholder.com/480x480.png?text=No+Image");
        }
        return new Image(is);
    }

    private void setStatus(String s) {
        statusLabel.setText(s);
    }

    private void setMyTurn(boolean v) {
        this.myTurn = v;
        if (v) {
            setStatus("Your turn");
            countdownBar.start(20, () -> onTimeUp());
        } else {
            setStatus("Opponent's turn");
            countdownBar.cancel();
        }
    }

    private void onReady() {
        // simulate start: give this player the turn randomly
        boolean start = rnd.nextBoolean();
        setMyTurn(start);
        appendChat("System: Match started. " + (start?"You go first":"Opponent goes first"));
    }

    private void onTimeUp() {
        Platform.runLater(() -> {
            appendChat("System: Time's up!");
            // show red flash because time's up and no selection
            showResultOverlay(false, true);
            setMyTurn(false);
        });
    }

    private void onClickImage(MouseEvent e, Pane overlay) {
        if (!myTurn) {
            // ignore clicks when not our turn
            return;
        }
        double x = e.getX();
        double y = e.getY();
        // for demo randomly decide right/wrong
        boolean correct = rnd.nextBoolean();
        // stop timer
        countdownBar.cancel();
        // show marker
        showMarker(overlay, x, y, correct);
        // end turn
        setMyTurn(false);
        // notify opponent (in real app send packet)
        appendChat("You selected at (" + (int)x + "," + (int)y + ") - " + (correct?"CORRECT":"WRONG"));
    }

    private void showMarker(Pane overlay, double x, double y, boolean correct) {
        Circle marker = new Circle(x, y, 8);
        marker.setStroke(Color.WHITE);
        marker.setStrokeWidth(2);
        marker.setFill(correct ? Color.web("#00e676") : Color.web("#ff1744"));
        marker.setOpacity(0);
        overlay.getChildren().add(marker);

        ScaleTransition st = new ScaleTransition(Duration.millis(200), marker);
        st.setFromX(0.2); st.setFromY(0.2); st.setToX(1.6); st.setToY(1.6);
        FadeTransition ft = new FadeTransition(Duration.millis(250), marker);
        ft.setFromValue(0); ft.setToValue(1.0);
        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(ev -> {
            // pulse then fade out
            PauseTransition p = new PauseTransition(Duration.seconds(0.8));
            p.setOnFinished(ev2 -> {
                FadeTransition out = new FadeTransition(Duration.millis(400), marker);
                out.setToValue(0);
                out.setOnFinished(ev3 -> overlay.getChildren().remove(marker));
                out.play();
            });
            p.play();
        });
        pt.play();

        // screen flash & sound stub
        flashScreen(correct);
        playSound(correct);
    }

    private void flashScreen(boolean correct) {
        // flash whole window with color overlay
        Color color = correct ? Color.web("#00e676", 0.25) : Color.web("#ff1744", 0.25);
        Pane flash = new Pane();
        flash.setStyle("-fx-background-color: rgba(" + (int)(color.getRed()*255) + "," + (int)(color.getGreen()*255) + "," + (int)(color.getBlue()*255) + ",0.5);");
        leftPane.getChildren().add(flash);
        rightPane.getChildren().add(flash);
        FadeTransition ft = new FadeTransition(Duration.millis(450), flash);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            leftPane.getChildren().remove(flash);
            rightPane.getChildren().remove(flash);
        });
        ft.play();
    }

    private void playSound(boolean correct) {
        // placeholder: user can replace with MediaPlayer to play sound files
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    private void appendChat(String text) {
        chatArea.appendText(text + "\n");
    }

    private void sendChat() {
        String t = chatInput.getText();
        if (t == null || t.isBlank()) return;
        appendChat("You: " + t);
        chatInput.clear();
        // in real app send to server
    }

    private void onLeave() {
        // if match ongoing ask confirm
        boolean matchOngoing = true; // replace with real state
        if (matchOngoing) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Match still ongoing. Leave?", ButtonType.YES, ButtonType.NO);
            a.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) closeRoom();
            });
        } else closeRoom();
    }

    private void closeRoom() {
        // cleanup and go back to menu
        appendChat("Left the room");
        // for demo we just clear overlays
        leftOverlay.getChildren().clear();
        rightOverlay.getChildren().clear();
    }

    private void showResultOverlay(boolean win, boolean neutral) {
        // simple modal-like effect
        Label lbl = new Label(win?"YOU WIN":"YOU LOSE");
        lbl.setStyle("-fx-font-size:48px; -fx-text-fill: white; -fx-font-weight:bold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10,0,0,4);");
        StackPane cover = new StackPane(lbl);
        cover.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        cover.setPrefSize(leftPane.getWidth()+rightPane.getWidth()+20, Math.max(leftPane.getHeight(), rightPane.getHeight()));
        leftPane.getChildren().add(cover);
        rightPane.getChildren().add(cover);

        FadeTransition ft = new FadeTransition(Duration.millis(600), cover);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }
}