package risiko.net.messages;

import risiko.data.PlayerInfo;
import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class TurnOwnerMsg extends BasicMessage{

	private String m_owner;
	public static final String TURN_OWNER_MSG = "turn_owner";
	
	public TurnOwnerMsg(PeerDescriptor peer, PlayerInfo owner){
		super(TURN_OWNER_MSG, new Payload(peer));
		m_owner = owner.getColor();
	}
	
	public String getOwner(){
		return m_owner;
	}

}
