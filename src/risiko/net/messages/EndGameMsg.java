package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class EndGameMsg extends BasicMessage {

	public static String END_GAME_MSG = "end_game";
	
	public EndGameMsg(PeerDescriptor peerDesc){
		super(END_GAME_MSG, new Payload(peerDesc) );
	}

}