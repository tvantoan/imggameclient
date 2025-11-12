package imggame.core;

import imggame.config.Config;
import imggame.models.User;
import imggame.network.GameClient;
import imggame.network.GameService;

public class SessionManager {
    private static GameClient client;
    private static GameService service;
    private static User currentUser;
    private static String currentRoomId;

    public static synchronized void connect() throws Exception {
        if (client == null) {
            client = new GameClient(Config.getHost(), Config.getPort());
            service = new GameService(client);
        }
    }

    public static GameClient getClient() { return client; }
    public static GameService getService() { return service; }

    public static void setCurrentUser(User u) { currentUser = u; }
    public static User getCurrentUser() { return currentUser; }

    public static void setCurrentRoomId(String id) { currentRoomId = id; }
    public static String getCurrentRoomId() { return currentRoomId; }

    public static synchronized void disconnect() {
        try { if (client != null) client.close(); } catch (Exception ignored) {}
        client = null;
        service = null;
        currentUser = null;
        currentRoomId = null;
    }
}
