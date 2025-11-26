package imggame.utils;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class MediaPlayerManager {

    private static MediaPlayerManager instance;
    private MediaPlayer currentPlayer;

    private MediaPlayerManager() {}

    public static MediaPlayerManager getInstance() {
        if (instance == null) {
            instance = new MediaPlayerManager();
        }
        return instance;
    }

    // Phát nhạc mới
    public void playMusic(String resourcePath, boolean loop) {
        stopMusic(); // Dừng nhạc cũ nếu đang phát

        URL url = getClass().getResource("/sounds/" + resourcePath);
        if (url == null) {
            System.out.println("Không tìm thấy nhạc: " + resourcePath);
            return;
        }

        Media media = new Media(url.toExternalForm());
        currentPlayer = new MediaPlayer(media);

        if (loop) {
            currentPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }

        currentPlayer.play();
    }

    // Dừng nhạc hiện tại
    public void stopMusic() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }

    public void playSoundEffect(String resourcePath) {
        URL url = getClass().getResource("/sounds/" + resourcePath);
        if (url == null) {
            System.out.println("Không tìm thấy nhạc: " + resourcePath);
            return;
        }

        Media media = new Media(url.toExternalForm());
        MediaPlayer player = new MediaPlayer(media);
        player.play();

    }
}
