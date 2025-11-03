package imggame.network.packets;

public class GetPlayerListRequest extends BasePacket {
	public int pageSize;
	public int offset;
	public Boolean isDESC;

	public GetPlayerListRequest(int pageSize, int offset, Boolean isDESC) {
		this.pageSize = pageSize;
		this.offset = offset;
		this.isDESC = isDESC;
	}

	@Override
	public String getType() {
		return "GET_PLAYER_LIST";
	}
}
