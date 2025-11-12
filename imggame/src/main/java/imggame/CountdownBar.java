package imggame;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A simple horizontal countdown bar with "eating" effect (left-to-right) and flash near end.
 * It's a Region that draws into Canvas. Use start(totalSeconds) to run.
 */
public class CountdownBar extends Region {
    private final Canvas canvas = new Canvas(300, 18);
    private final DoubleProperty progress = new SimpleDoubleProperty(1.0); // 1.0 -> full, 0.0 -> empty
    private Timeline timeline;
    private Timeline flash;

    public CountdownBar() {
        getChildren().add(canvas);
        setPadding(new Insets(4));
        widthProperty().addListener((s, o, n) -> resize());
        heightProperty().addListener((s, o, n) -> resize());
        progress.addListener((p, o, n) -> draw());
        draw();
    }

    private void resize() {
        double w = Math.max(100, getWidth());
        double h = Math.max(18, getHeight());
        canvas.setWidth(w);
        canvas.setHeight(h);
        draw();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        // background
        g.clearRect(0,0,w,h);
        g.setFill(Color.web("#222"));
        g.fillRoundRect(0,0,w,h,6,6);
        // base track
        g.setFill(Color.web("#444"));
        g.fillRoundRect(2,2,w-4,h-4,6,6);
        // progress fill (left -> right erasing effect means we draw remaining)
        double fillW = Math.max(0, (w-6) * progress.get());
        // gradient like white -> color
        g.setFill(Color.web("#00c853"));
        g.fillRoundRect(3,3,fillW, h-6,6,6);
        // overlay glow
        g.setGlobalAlpha(0.12);
        g.setFill(Color.WHITE);
        g.fillRect(3,3,fillW, (h-6)/2);
        g.setGlobalAlpha(1.0);
        // border
        g.setStroke(Color.web("#111"));
        g.strokeRoundRect(0.5,0.5,w-1,h-1,6,6);
    }

    public void start(double totalSeconds, Runnable onFinish) {
        if (timeline != null) timeline.stop();
        if (flash != null) flash.stop();
        progress.set(1.0);
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress, 1.0)),
                new KeyFrame(Duration.seconds(totalSeconds), new KeyValue(progress, 0.0))
        );
        timeline.setOnFinished(e -> {
            stopFlash();
            onFinish.run();
        });
        timeline.play();
        // flash when time < 3s
        flash = new Timeline(new KeyFrame(Duration.seconds(Math.max(0, totalSeconds-3)), e-> startFlash()), new KeyFrame(Duration.seconds(totalSeconds), e->stopFlash()));
        flash.play();
    }

    private void startFlash() {
        // pulse the fill color by toggling a property (repaint faster)
        final Animation flashAnim = new Timeline(
                new KeyFrame(Duration.ZERO, evt -> {
                    // invert slightly via opacity change of canvas overlay
                    canvas.setOpacity(0.9);
                }),
                new KeyFrame(Duration.seconds(0.35), evt -> canvas.setOpacity(1.0))
        );
        flashAnim.setCycleCount(Animation.INDEFINITE);
        flashAnim.play();
        // stop previous if any
        canvas.getProperties().put("_flashAnim", flashAnim);
    }

    private void stopFlash() {
        Object obj = canvas.getProperties().get("_flashAnim");
        if (obj instanceof Animation) {
            ((Animation)obj).stop();
            canvas.getProperties().remove("_flashAnim");
            canvas.setOpacity(1.0);
        }
    }

    public void cancel() {
        if (timeline != null) timeline.stop();
        stopFlash();
        progress.set(0);
    }
}