package risiko.net.data.sendable;

import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import risiko.net.data.SignalType;
import risiko.net.gson.ISendable;

public class Signal implements ISendable{

	public static String SIGNAL_MSG = "signal_message";
	private SignalType m_flag;
	private PeerDescriptor m_descriptor;
	
	public Signal(SignalType flag, PeerDescriptor descriptor){
		m_flag = flag;
		m_descriptor = descriptor;
	}
	
	public SignalType getSignalType(){
		return m_flag;
	}
	
	public PeerDescriptor getPeerDescriptor(){
		return m_descriptor;
	}
	
	public void setSignalType(SignalType type){
		m_flag = type;
	}
	
	@Override
	public String getType() {
		return SIGNAL_MSG;
	}

}
