package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class ConnectionRefusedMsg extends BasicMessage {

	public static String CONNECTION_REFUSED_MSG = "connection_refused";
	
	public ConnectionRefusedMsg(PeerDescriptor peerDesc){
		super(CONNECTION_REFUSED_MSG, new Payload(peerDesc) );
	}

}
