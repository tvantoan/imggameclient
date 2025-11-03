package imggame.network.packets;

public class InviteRequest extends BasePacket {
	public int senderId;
	public int receiverId;

	public InviteRequest(int senderId, int receiverId) {
		this.senderId = senderId;
		this.receiverId = receiverId;
	}

	@Override
	public String getType() {
		return "INVITE";
	}
}
