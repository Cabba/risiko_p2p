package risiko.net;

import java.util.Iterator;

import org.zoolu.tools.Log;

import risiko.data.PlayerColor;
import risiko.data.TerritoriesLayout;
import risiko.data.TerritoryInfo;
import risiko.net.configuration.ClientConfiguration;
import risiko.net.messages.ConnectionAcceptedMsg;
import risiko.net.messages.ConnectionMsg;
import risiko.net.messages.ConnectionRefusedMsg;
import risiko.net.messages.DisconnectionMsg;
import risiko.net.messages.PlayerInfoMsg;
import risiko.net.messages.StartGameMsg;
import risiko.net.messages.TerritoriesLayoutMsg;

import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.sip.Address;

public class Client extends Peer {

	private Log m_log;
	private ClientConfiguration m_config;

	// Game logic
	private boolean m_gameStarted = false;
	private boolean m_connected = false;
	private boolean m_connectionRefused = false;

	private PlayerColor m_color;
	private int m_availableUnit;
	
	private TerritoriesLayout m_territories;

	public Client(String pathConfig, String key) {
		super(pathConfig, key);
		m_config = new ClientConfiguration(pathConfig);

		// Create the log file
		if (this.nodeConfig.log_path != null) {
			m_log = new Log(this.nodeConfig.log_path + "info_" + this.peerDescriptor.getName() + ".log", Log.LEVEL_MEDIUM);
		}
		m_log.println("creating log file.");
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

			if (type.equals(StartGameMsg.START_GAME_MSG)) {
				m_gameStarted = true;
			}

			if (type.equals(DisconnectionMsg.DISCONNECTION_MSG)) {
				m_log.println("Disconnecting from the server.");
				m_connected = false;
			}

			if (type.equals(ConnectionAcceptedMsg.CONNECTION_ACCEPTED_MSG)) {
				m_log.println("Connection accepted from the server");
				m_connected = true;
			}

			if (type.equals(ConnectionRefusedMsg.CONNECTION_REFUSED_MSG)) {
				m_log.println("Connection refused from the server");
				m_connected = false;
				m_connectionRefused = true;
			}

			if (type.equals(PlayerInfoMsg.PLAYER_COLOR_MSG)) {
				PlayerColor color = PlayerColor.valueOf(params.getString("color"));
				m_availableUnit = params.getInt("totalUnit");
				m_color = color;
				m_log.println("Assigned color to client is: " + m_color + ". Available unit are: " + m_availableUnit);
			}
			
			if(type.equals(TerritoriesLayoutMsg.TERRITORIES_LAYOUT_MSG)){
				// Clear the old data.
				m_territories.clear();
				// Refresh the data
				Iterator<String> iter = params.keys();
				while(iter.hasNext()){
					String key = iter.next();
					
					JSONObject territoryParam = params.getJSONObject(key);
					int id = (Integer)territoryParam.get("id");
					int unitNumber = (Integer)territoryParam.get("unitNumber");
					PlayerColor owner = PlayerColor.valueOf((territoryParam.get("owner").toString()));
					
					TerritoryInfo territory = new TerritoryInfo(id, unitNumber, owner);
					m_territories.put(territory);
				}
			}

		} catch (JSONException e) {
			// throw new RuntimeException(e);
			e.printStackTrace();
		}
	}

	@Override
	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	public void connect() {
		send(new Address(m_config.server_address), new ConnectionMsg(this.peerDescriptor));
		m_connectionRefused = false;
	}

	public void disconnect() {
		send(new Address(m_config.server_address), new DisconnectionMsg(this.peerDescriptor));
		m_connected = false;
	}

	public boolean isGameStarted() {
		return m_gameStarted;
	}

	public boolean isConnected() {
		return m_connected;
	}

	public boolean isConnectionRefused() {
		return m_connectionRefused;
	}
	
	public int getAvailableUnit(){
		return m_availableUnit;
	}

	public PlayerColor getColor() {
		if (m_color == null)
			return PlayerColor.NONE;
		return m_color;
	}
	
	public TerritoriesLayout getTerritoriesLayout(){
		return m_territories;
	}

	/**
	 * Run the client
	 */
	public static void main(String[] args) throws InterruptedException {
		Client client_1 = new Client("config/client_1.config", "2");
		Client client_2 = new Client("config/client_2.config", "3");

		client_1.connect();
		client_2.connect();

		Thread.sleep(2000);

		client_2.disconnect();
		client_1.disconnect();
	}
}