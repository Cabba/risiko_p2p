package risiko.net.messages;

import java.util.List;

import risiko.data.TerritoriesLayout;
import risiko.data.TerritoryInfo;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

public class TerritoriesLayoutMsg extends BasicMessage {

	public static final String TERRITORIES_LAYOUT_MSG = "territories_layout";
	
	public TerritoriesLayoutMsg(TerritoriesLayout territories){
		super(TERRITORIES_LAYOUT_MSG, new Payload(territories) );
		
	}

}
