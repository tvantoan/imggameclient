package imggame.network.packets;

public class RegisterRequest extends BasePacket {
	public String username;
	public String email;
	public String password;

	public RegisterRequest(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Override
	public String getType() {
		return "REGISTER";
	}
}
