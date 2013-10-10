package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class StartGameMsg extends BasicMessage {

	public static String START_GAME_MSG = "start_game";
	
	public StartGameMsg(PeerDescriptor peerDesc){
		super(START_GAME_MSG, new Payload(peerDesc) );
	}

}
