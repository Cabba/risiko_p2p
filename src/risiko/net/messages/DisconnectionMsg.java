package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class DisconnectionMsg extends BasicMessage {

	public static String DISCONNECTION_MSG = "disconnection";
	
	public DisconnectionMsg(PeerDescriptor peerDesc){
		super(DISCONNECTION_MSG, new Payload(peerDesc) );
	}

}
