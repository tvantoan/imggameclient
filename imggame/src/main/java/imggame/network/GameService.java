package imggame.network;

import imggame.models.User;
import imggame.network.packets.*;

import java.io.IOException;
import java.util.List;


public class GameService {
    private final GameClient client;

    public GameService(GameClient client) {
        this.client = client;
    }

    public ServiceResult<User
            > login(String username, String password) {
        try {
            client.sendPacket(new LoginRequest(username, password));
            Object response = client.receivePacket();
            if (response instanceof ErrorResponse)
                return new ServiceResult<>(false, ((ErrorResponse) response).message);
            else if (response instanceof User) {
                return new ServiceResult<User>(true, (User) response);
            }
            return new ServiceResult<>(false, "Unknown response type: " + response.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }


    public ServiceResult<Void> register(String username, String email, String password) {
        try {
            client.sendPacket(new RegisterRequest(username, email, password));
            Object response = client.receivePacket();

            if (response instanceof ErrorResponse err)
                return new ServiceResult<>(false, err.message);

            return new ServiceResult<>(true, response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<List<User>> getPlayerList(int pageSize, int offset, boolean isDESC) {
        try {
            GetPlayerListRequest request = new GetPlayerListRequest(pageSize, offset, isDESC);
            client.sendPacket(request);

            Object response = client.receivePacket();
            if (response instanceof ErrorResponse)
                return new ServiceResult<>(false, ((ErrorResponse) response).message);
            if (response instanceof User) return new ServiceResult<>(true, (List<User>) response);
            return new ServiceResult<>(true, (List<User>) response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<List<GameRoomResponse>> getWaitingRooms() {
        try {
            GetWaitingRoomsRequest req = new GetWaitingRoomsRequest();
            client.sendPacket(req);
            Object resp = client.receivePacket();
            if (resp instanceof ErrorResponse err) return new ServiceResult<>(false, err.message);
            if (resp instanceof WaitingRoomsResponse wr) return new ServiceResult<>(true, wr.rooms);
            return new ServiceResult<>(false, "Unexpected response: " + resp.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<GameRoomResponse> createGameRoom(int userId) {
        try {
            System.out.println("clicked");
            CreateGameRoomRequest req = new CreateGameRoomRequest(userId);
            System.out.println("req = " + req);
            System.out.println("client = " + client);
            System.out.println("before send");
            try {
                client.sendPacket(req);
                System.out.println("after send");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Object resp = client.receivePacket();
            System.out.println("receive");
            System.out.println("resp" + resp);
            if (resp instanceof ErrorResponse err) {
                return new ServiceResult<>(false, err.message);
            }
            if (resp instanceof GameRoomResponse gr) {
                return new ServiceResult<>(true, gr);
            }
            return new ServiceResult<>(false, "Unexpected response: " + resp.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<GameRoomResponse> joinGameRoom(int userId, String roomId) {
        try {
            JoinGameRoomRequest req = new JoinGameRoomRequest(userId, roomId);
            client.sendPacket(req);
            Object resp = client.receivePacket();
            if (resp instanceof ErrorResponse err) return new ServiceResult<>(false, err.message);
            if (resp instanceof GameRoomResponse gr) return new ServiceResult<>(true, gr);
            return new ServiceResult<>(false, "Unexpected response: " + resp.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<Void> leaveRoom(int userId, String roomId) {
        try {
            LeaveGameRoomRequest req = new LeaveGameRoomRequest(userId, roomId);
            client.sendPacket(req);
            Object resp = client.receivePacket();
            if (resp instanceof ErrorResponse err) return new ServiceResult<>(false, err.message);
            return new ServiceResult<>(true, (Void) null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<Void> startGame(String roomId, int userId) {
        try {
            StartGameRequest req = new StartGameRequest(roomId, userId);
            client.sendPacket(req);
            Object resp = client.receivePacket();
            if (resp instanceof ErrorResponse err) return new ServiceResult<>(false, err.message);
            return new ServiceResult<>(true, (Void) null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    public ServiceResult<InviteResponse> invitePlayer(int senderId, int receiverId) {
        try {
            InviteRequest req = new InviteRequest(senderId, receiverId);
            client.sendPacket(req);
            Object resp = client.receivePacket();
            if (resp instanceof ErrorResponse err) return new ServiceResult<>(false, err.message);
            if (resp instanceof InviteResponse ir) return new ServiceResult<>(true, ir);
            return new ServiceResult<>(false, "Unexpected response: " + resp.getClass().getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }
}

