package risiko.net.messages;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class AckMsg extends BasicMessage {

	public static String ACK_MSG = "ack";
	
	public AckMsg(PeerDescriptor peerDesc){
		super(ACK_MSG, new Payload(peerDesc) );
	}

}
