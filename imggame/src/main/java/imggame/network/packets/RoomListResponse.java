package imggame.network.packets;

import java.util.List;

import imggame.network.types.PacketType;

public class RoomListResponse extends BasePacket {
	private static final long serialVersionUID = 1L;
	public List<RoomResponse> rooms;

	public RoomListResponse() {
	}

	public List<RoomResponse> getRooms() {
		return rooms;
	}

	public void setRooms(List<RoomResponse> rooms) {
		this.rooms = rooms;
	}

	@Override
	public PacketType getType() {
		return PacketType.DIRECT_RESPONSE;
	}
}
