package imggame.network.packets;

public class ErrorResponse extends BasePacket {
	public String message;

	public ErrorResponse(String message) {
		this.message = message;
	}

	@Override
	public String getType() {
		return "ERROR";
	}
}
