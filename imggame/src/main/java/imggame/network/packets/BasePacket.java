package imggame.network.packets;

import java.io.Serializable;

public class BasePacket implements Serializable {
	private static final long serialVersionUID = 1L;

	public String getType() {
		return "BasePacket";
	}

}
