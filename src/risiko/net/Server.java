package risiko.net;

import java.util.Iterator;

import org.zoolu.tools.Log;

import risiko.data.PlayerInfo;
import risiko.data.PlayerColor;
import risiko.net.configuration.ServerConfiguration;
import risiko.net.messages.ConnectionMsg;
import risiko.net.messages.ConnectionAcceptedMsg;
import risiko.net.messages.ConnectionRefusedMsg;
import risiko.net.messages.DisconnectionMsg;
import risiko.net.messages.PlayerInfoMsg;
import risiko.net.messages.StartGameMsg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;

/**
 * When the server reaches the minimum number of players the game starts.
 * 
 * @author Federico Cabassi
 * 
 */
public class Server extends Peer {

	private Log m_log;

	private ServerConfiguration m_serverConfig;

	// Logic
	private boolean m_gameStarted = false;

	public Server(String pathConfig, String key) {
		super(pathConfig, key);

		// Configuration file
		m_serverConfig = new ServerConfiguration(pathConfig);

		// Create the log file
		if (this.nodeConfig.log_path != null) {
			m_log = new Log(this.nodeConfig.log_path + "" + this.peerDescriptor.getName() + ".log", Log.LEVEL_MEDIUM);
		}
		m_log.print("creating log file.");
	}

	@Override
	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {

	}

	@Override
	protected void onReceivedJSONMsg(JSONObject msg, Address sender) {
		super.onReceivedJSONMsg(msg, sender);
		try {
			m_log.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '" + msg.get("type").toString() + "' form: "
					+ sender.toString());
			System.out.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '" + msg.get("type").toString() + "' form: "
					+ sender.toString());
			m_log.println(msg.toString());

			JSONObject params = msg.getJSONObject("payload").getJSONObject("params");
			String type = msg.get("type").toString();
		
			// Connection ...
			if (type.equals(ConnectionMsg.CONNECTION_MSG)) {
				// If there is another match refuse the connection
				if (m_gameStarted) {
					send(sender, new ConnectionRefusedMsg(peerDescriptor));
				} else {
					PeerDescriptor peerDesc = getPeerDescriptorFormJSON(msg);
					peerList.put(peerDesc.getKey(), new NeighborPeerDescriptor(peerDesc));
					send(sender, new ConnectionAcceptedMsg(peerDescriptor));
					m_log.println("Peer " + peerDesc.getName() + " added to available peer list.");
					if (peerList.size() >= m_serverConfig.min_clients_number) {
						m_log.println("Minimum clients number reached.");
					}
				}
			}
			// Disconnection ...
			if (type.equals(DisconnectionMsg.DISCONNECTION_MSG)) {
				PeerDescriptor peerDesc = getPeerDescriptorFormJSON(msg);
				peerList.remove(peerDesc.getKey());
			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Used for get a peer descriptor for a JSON message.
	 * 
	 * @param msg
	 *            The JSON message.
	 * @return The peer descriptor.
	 */
	private PeerDescriptor getPeerDescriptorFormJSON(JSONObject msg) {
		try {
			JSONObject params = msg.getJSONObject("payload").getJSONObject("params");
			PeerDescriptor peerDesc = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString());
			return peerDesc;

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public int getConnectedClientsNumber() {
		return peerList.size();
	}

	public void assignIDToClients(){
		Iterator<String> iter = peerList.keySet().iterator();
		for( PlayerColor color : PlayerColor.values() ){
			if(!iter.hasNext()) return; // Iterated over all players
			if( color == PlayerColor.NONE ) continue;
			
			PlayerInfo info = new PlayerInfo(color);
			NeighborPeerDescriptor peer = peerList.get((String)iter.next());
			PlayerInfoMsg msg = new PlayerInfoMsg(info);
			
			send(new Address(peer.getAddress()), msg );
			
			m_log.println("Sended color " + info.getColor() + " to client " + peer.getName() );
		}
	}
	/**
	 * A game can start if there are enough player and there are no other match
	 * occurring.
	 */
	public boolean gameCanStart() {
		if (m_gameStarted)
			return false;
		if (peerList.size() >= m_serverConfig.min_clients_number) {
			m_log.println("The game can start");
			System.out.println("The game can start");
			return true;
		} else
			return false;
	}
	
	public boolean isGameStarted(){
		return m_gameStarted;
	}

	public void startGame() {
		broadcastMessage(new StartGameMsg(peerDescriptor));
		m_gameStarted = true;

	}

	/**
	 * Send in broadcast a message to all know peer (peer in peerList)
	 * 
	 * @param msg
	 *            message to send.
	 */
	private void broadcastMessage(BasicMessage msg) {
		Iterator<String> iter = this.peerList.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			NeighborPeerDescriptor neighborPD = this.peerList.get(key);
			send(new Address(neighborPD.getAddress()), msg);
		}
	}

	/**
	 * Lunch the class for run the the server
	 * 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		Server server = new Server("config/server.config", "1");

		// Wait until the game can start ...
		while (!server.gameCanStart()) {
			System.out.println("Not enought players ...");
			Thread.sleep(1000);
		}
		server.startGame();
	}
}
