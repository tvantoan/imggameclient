package imggame.network.packets;

import java.util.List;

class RoomResponse {
	public String roomId;
	public String roomName;
	public int playerCount;
	public int maxPlayers;

	public RoomResponse(String roomId, String roomName, int playerCount, int maxPlayers) {
		this.roomId = roomId;
		this.roomName = roomName;
		this.playerCount = playerCount;
		this.maxPlayers = maxPlayers;
	}
}

public class RoomListResponse {
	private List<RoomResponse> rooms;

	public List<RoomResponse> getRooms() {
		return rooms;
	}
}
