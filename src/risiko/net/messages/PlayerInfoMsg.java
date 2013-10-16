package risiko.net.messages;

import risiko.data.PlayerInfo;
import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;

public class PlayerInfoMsg extends BasicMessage {

	public static final String PLAYER_COLOR_MSG = "player_color";
	
	public PlayerInfoMsg(PlayerInfo player){
		super(PLAYER_COLOR_MSG, new Payload(player));
	}

}
