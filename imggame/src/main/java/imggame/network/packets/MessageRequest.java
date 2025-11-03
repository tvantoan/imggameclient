package imggame.network.packets;

public class MessageRequest extends BasePacket {
	public String message;

	public MessageRequest(String message) {
		this.message = message;
	}

	@Override
	public String getType() {
		return "MESSAGE";
	}

}
