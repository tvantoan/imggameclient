package imggame.core;

import imggame.config.Config;
import imggame.network.GameClient;

public class SessionManager {
    private static GameClient client;
    private static String username;

    public static void connect() throws Exception {
        if (client == null) {
            client = new GameClient(Config.getHost(), Config.getPort());
        }
    }

    public static GameClient getClient() {
        return client;
    }

    public static void setUsername(String name) {
        username = name;
    }

    public static String getUsername() {
        return username;
    }

    public static void disconnect() {
        try {
            if (client != null) client.close();
        } catch (Exception ignored) {}
        client = null;
        username = null;
    }
}
