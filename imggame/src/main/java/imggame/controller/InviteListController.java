package imggame.controller;

import imggame.core.SessionManager;
import imggame.models.User;
import imggame.network.Client;
import imggame.network.ResponseHandler;
import imggame.network.packets.ErrorResponse;
import imggame.network.packets.GetOnlineUsersRequest;
import imggame.network.packets.InviteRequest;
import imggame.utils.Async;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InviteListController {

    @FXML private TableView<User> onlineTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> statusCol;
    @FXML private TableColumn<User, Void> actionCol;

    @FXML private Button refreshBtn;
    @FXML private Button inviteSelectedBtn;
    @FXML private Button closeBtn;
    @FXML private Label infoLabel;

    private Client client;
    private final ObservableList<User> items = FXCollections.observableArrayList();

    // track invited users locally to avoid duplicate invites
    private final Set<Integer> invitedIds = new HashSet<>();

    @FXML
    private void initialize() {
        client = SessionManager.getClient();
        client.setResponseHandler(new OnlineResponseHandler());

        // table setup
        onlineTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        onlineTable.setItems(items);
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        statusCol.setCellValueFactory(data -> {
            boolean invited = invitedIds.contains(data.getValue().getId());
            return new SimpleStringProperty(invited ? "Đã mời" : "Sẵn sàng");
        });

        // action column (Invite button per row)
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button inviteBtn = new Button("Mời");

            {
                inviteBtn.getStyleClass().add("button-small");
                inviteBtn.setOnAction(ev -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        inviteUser(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user == null) {
                        setGraphic(null);
                    } else {
                        if (invitedIds.contains(user.getId())) {
                            inviteBtn.setText("Đã mời");
                            inviteBtn.setDisable(true);
                        } else {
                            inviteBtn.setText("Mời");
                            inviteBtn.setDisable(false);
                        }
                        setGraphic(inviteBtn);
                    }
                }
            }
        });

        // buttons
        refreshBtn.setOnAction(e -> loadOnlinePlayers());
        inviteSelectedBtn.setOnAction(e -> inviteSelected());
        closeBtn.setOnAction(e -> closeDialog());

        // double click to invite quickly
        onlineTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    inviteUser(row.getItem());
                }
            });
            return row;
        });

        // initial load
        loadOnlinePlayers();
    }

    private void loadOnlinePlayers() {
        infoLabel.setText("Đang tải danh sách online...");
        Async.run(() -> {
            try {
                int meId = SessionManager.getCurrentUser().getId();
                client.send(new GetOnlineUsersRequest(meId));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> infoLabel.setText("Lỗi: " + ex.getMessage()));
            }
        });
    }

    private void inviteUser(User user) {
        if (user == null) return;
        if (invitedIds.contains(user.getId())) return;

        // mark invited immediately in UI, prevents duplicate clicks
        invitedIds.add(user.getId());
        refreshStatusColumn();

        Async.run(() -> {
            try {
                // Replace with your server's invite packet
                client.send(new InviteRequest( SessionManager.getCurrentUser().getId(), user.getId()));
                // Optionally wait for server ack to confirm success; if fail, remove from invitedIds
            } catch (Exception ex) {
                ex.printStackTrace();
                // on error, revert invited state
                invitedIds.remove(user.getId());
                Platform.runLater(() -> {
                    refreshStatusColumn();
                    infoLabel.setText("Không thể mời: " + ex.getMessage());
                });
            }
        });
    }

    private void inviteSelected() {
        List<User> selected = onlineTable.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            infoLabel.setText("Chưa chọn người để mời.");
            return;
        }
        for (User u : selected) {
            inviteUser(u);
        }
        infoLabel.setText("Đã gửi lời mời cho " + selected.size() + " người.");
    }

    private void refreshStatusColumn() {
        // force table refresh so statusCol and actionCol re-render
        Platform.runLater(() -> {
            onlineTable.refresh();
        });
    }

    private void closeDialog() {
        // if this view is inside a Stage (modal), close it.
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        if (stage != null) {
            stage.close();
            return;
        }
        // otherwise hide parent node if embedded
        closeBtn.getParent().setVisible(false);
    }

    // Method to show this as modal dialog from anywhere:
    public static void showAsDialog() throws Exception {
        Parent root = FXMLLoader.load(InviteListController.class.getResource("/fxml/online_players.fxml"));
        Scene scene = new Scene(root);
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Mời người chơi");
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private class OnlineResponseHandler implements ResponseHandler {
        @Override
        @SuppressWarnings("unchecked")
        public void handleResponse(Object response) {
            if (response instanceof List) {
                List<User> players = (List<User>) response;
                Platform.runLater(() -> {
                    items.setAll(players);
                    infoLabel.setText("Đã tải " + players.size() + " người online.");
                });
            } else if (response instanceof ErrorResponse) {
                ErrorResponse err = (ErrorResponse) response;
                Platform.runLater(() -> infoLabel.setText("Lỗi: " + err.message));
            } else {
                Platform.runLater(() -> infoLabel.setText("Phản hồi không xác định"));
            }
        }
    }
}
