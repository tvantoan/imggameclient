package imggame.network;

import imggame.network.packets.ErrorResponse;
import imggame.network.packets.LoginRequest;
import imggame.network.packets.RegisterRequest;




public class GameService {
  private final GameClient client;

  public GameService(GameClient client) {
    this.client = client;
  }

    public ServiceResult login(String username, String password) {
        try {
            client.sendPacket(new LoginRequest(username, password));
            Object response = client.receivePacket();
            if (response instanceof ErrorResponse)
                return new ServiceResult(false, ((ErrorResponse) response).message);
            return new ServiceResult(true, response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }


    public ServiceResult register(String username, String email, String password) {
        try {
            client.sendPacket(new RegisterRequest(username, email, password));
            Object response = client.receivePacket();

            if (response instanceof ErrorResponse err)
                return new ServiceResult(false, ((ErrorResponse) response).message);

            return new ServiceResult(true, response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new ServiceResult(false, e.getMessage());
        }
    }
  }

