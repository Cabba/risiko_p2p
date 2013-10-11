package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class ConnectionAcceptedMsg extends BasicMessage {

	public static String CONNECTION_ACCEPTED_MSG = "connection_accepted";
	
	public ConnectionAcceptedMsg(PeerDescriptor peerDesc){
		super(CONNECTION_ACCEPTED_MSG, new Payload(peerDesc) );
	}

}