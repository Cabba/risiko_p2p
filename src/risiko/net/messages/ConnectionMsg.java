package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class ConnectionMsg extends BasicMessage {

	public static String CONNECTION_MSG = "connection";
	
	public ConnectionMsg(PeerDescriptor peerDesc){
		super(CONNECTION_MSG, new Payload(peerDesc) );
	}

}
