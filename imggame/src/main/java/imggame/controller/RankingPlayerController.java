package imggame.controller;

import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.GameService;
import imggame.network.ServiceResult;
import imggame.utils.Async;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class RankingPlayerController {

    @FXML private TableView<User> rankingTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, Number> eloCol;
    @FXML private TableColumn<User, Number> rankCol;
    @FXML private Button refreshBtn;
    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        usernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        eloCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getElo()));
        rankCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(rankingTable.getItems().indexOf(data.getValue()) + 1));

        refreshBtn.setOnAction(e -> loadRanking());
        loadRanking();
    }

    private void loadRanking() {
        infoLabel.setText("Đang tải danh sách người chơi...");
        Async.run(() -> {
            GameService service = SessionManager.getService();
            ServiceResult<List<User>> result = service.getPlayerList(1,0, true);
            Platform.runLater(() -> {
                if (!result.success) {
                    infoLabel.setText("Không thể tải danh sách: " + result.message);
                    return;
                }
                List<User> players = result.data;
                players.sort((a, b) -> Integer.compare(b.getElo(), a.getElo())); // sắp xếp giảm dần theo ELO
                rankingTable.getItems().setAll(players);
                infoLabel.setText("Đã tải " + players.size() + " người chơi.");
            });
        });
    }
}
