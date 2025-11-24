package imggame.controller;

import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.ErrorResponse;
import imggame.network.packets.GetPlayerListRequest;
import imggame.utils.Async;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class RankingPlayerController {

	@FXML
	private TableView<User> rankingTable;
	@FXML
	private TableColumn<User, String> usernameCol;
	@FXML
	private TableColumn<User, Number> eloCol;
	@FXML
	private TableColumn<User, Number> rankCol;
	@FXML
	private Button refreshBtn;
	@FXML
	private Label infoLabel;

	@FXML
	private void initialize() {
		Client client = SessionManager.getClient();
		client.setResponseHandler(new RankingResponseHandler());

		usernameCol.setCellValueFactory(
				data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
		eloCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getElo()));
		rankCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
				rankingTable.getItems().indexOf(data.getValue()) + 1));

		refreshBtn.setOnAction(e -> loadRanking());
		loadRanking();
	}

	private void loadRanking() {
		infoLabel.setText("Đang tải danh sách người chơi...");
		Async.run(() -> {
			try {
				Client client = SessionManager.getClient();
				// Request all players sorted by ELO descending
				client.send(new GetPlayerListRequest(100, 0, true));
			} catch (Exception ex) {
				ex.printStackTrace();
				Platform.runLater(() -> infoLabel.setText("Lỗi: " + ex.getMessage()));
			}
		});
	}

	private class RankingResponseHandler implements ResponseHandler {
		@Override
		@SuppressWarnings("unchecked")
		public void handleResponse(Object response) {
			if (response instanceof List) {
				List<User> players = (List<User>) response;
				Platform.runLater(() -> {
					players.sort((a, b) -> Integer.compare(b.getElo(), a.getElo())); // sắp xếp giảm dần theo ELO
					rankingTable.getItems().setAll(players);
					infoLabel.setText("Đã tải " + players.size() + " người chơi.");
				});
			} else if (response instanceof ErrorResponse) {
				ErrorResponse error = (ErrorResponse) response;
				Platform.runLater(() -> infoLabel.setText("Không thể tải danh sách: " + error.message));
			} else {
				Platform.runLater(() -> infoLabel.setText("Phản hồi không xác định"));
			}
		}
	}
}
