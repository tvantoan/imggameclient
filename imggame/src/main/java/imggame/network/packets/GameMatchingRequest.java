package imggame.network.packets;

public class GameMatchingRequest extends BasePacket {
	public int playerId;

	public GameMatchingRequest(int playerId) {
		this.playerId = playerId;
	}

	@Override
	public String getType() {
		return "GAME_MATCHING";
	}
}
