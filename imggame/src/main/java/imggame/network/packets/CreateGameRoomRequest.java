package imggame.network.packets;

import imggame.network.types.PacketType;

import java.io.Serial;

public class CreateGameRoomRequest extends BasePacket {
	@Serial
    private static final long serialVersionUID = 1L;

	public int userId;

	public CreateGameRoomRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public PacketType getType() {
		return PacketType.REQUEST;
	}
}
