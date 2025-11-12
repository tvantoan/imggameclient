package imggame.network.packets;

import imggame.network.types.PacketType;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class WaitingRoomsResponse extends BasePacket implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public List<GameRoomResponse> rooms;

    public WaitingRoomsResponse(List<GameRoomResponse> rooms) {
        this.rooms = rooms;
    }

    @Override
    public PacketType getType() {
        return PacketType.DIRECT_RESPONSE;
    }
}
