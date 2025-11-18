package imggame.core;

import imggame.config.Config;
import imggame.models.User;
import imggame.network.Client;

public class SessionManager {
	private static Client client;
	private static User currentUser;
	private static String currentRoomId;

	public static synchronized void connect() throws Exception {
		if (client == null) {
			client = new Client(Config.getHost(), Config.getPort());
		}
	}

	public static Client getClient() {
		return client;
	}

	public static void setCurrentUser(User u) {
		currentUser = u;
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentRoomId(String id) {
		currentRoomId = id;
	}

	public static String getCurrentRoomId() {
		return currentRoomId;
	}

	public static synchronized void disconnect() {
		try {
			if (client != null)
				client.close();
			System.out.println("Disconnected from server.");
		} catch (Exception ignored) {
			System.out.println("Error while disconnecting: " + ignored.getMessage());
		}
		client = null;
		currentUser = null;
		currentRoomId = null;
	}
}
