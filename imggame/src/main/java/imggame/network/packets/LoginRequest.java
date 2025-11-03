package imggame.network.packets;

public class LoginRequest extends BasePacket {
	public String username;
	public String password;

	public LoginRequest(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String getType() {
		return "LOGIN";
	}
}
