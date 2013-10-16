package risiko.net;

import java.lang.reflect.Type;
import java.util.Iterator;

import org.zoolu.tools.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import risiko.data.AttackData;
import risiko.data.PlayerColor;
import risiko.data.TerritoriesLayout;
import risiko.data.TerritoryInfo;
import risiko.net.configuration.ClientConfiguration;
import risiko.net.gson.JSONMessage;
import risiko.net.messages.AckMsg;
import risiko.net.messages.ConnectionAcceptedMsg;
import risiko.net.messages.ConnectionMsg;
import risiko.net.messages.ConnectionRefusedMsg;
import risiko.net.messages.DisconnectionMsg;
import risiko.net.messages.EndGameMsg;
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
	private Gson m_jsonParser;
	
	// Game logic
	private boolean m_gameStarted = false;
	private boolean m_connected = false;

	private PlayerColor m_color;
	private int m_availableUnit;

	private TerritoriesLayout m_territories;
	private boolean m_initialized = false;
	private PlayerColor m_turnOwner;
	
	// State management
	private ClientState m_state;
	private boolean m_stateChanged = false;

	public Client(String pathConfig, String key) {
		super(pathConfig, key);
		m_config = new ClientConfiguration(pathConfig);

		// Create the log file
		if (this.nodeConfig.log_path != null) {
			m_log = new Log(this.nodeConfig.log_path + "info_" + this.peerDescriptor.getName() + ".log", Log.LEVEL_MEDIUM);
		}
		m_log.println("creating log file.");
		m_territories = new TerritoriesLayout();
		m_jsonParser = new Gson();
		
		setState(ClientState.WAIT_FOR_CONFIGURATION);
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
				setState(ClientState.CONNECTION_REFUSED);
			}

			if (type.equals(PlayerInfoMsg.PLAYER_COLOR_MSG)) {
				PlayerColor color = PlayerColor.valueOf(params.getString("color"));
				// If message is used during the initialization phase specifies the 
				// available number of unit at the beginning and the player color.
				if (m_initialized == false) {
					m_availableUnit = params.getInt("totalUnit");
					m_color = color;
					m_log.println("Assigned color to client is: " + m_color + ". Available unit are: " + m_availableUnit);
					setState(ClientState.CONFIGURED);
					m_initialized = true;
				} else {
					m_turnOwner = color;
					if(color == m_color){
						m_log.println("It's my turn ...");
						m_availableUnit = params.getInt("totalUnit");
					}
					else{
						m_log.println("It's player " + m_turnOwner + " turn.");					
					}
					setState(ClientState.NEW_TURN);
				}
			}

			if (type.equals(TerritoriesLayoutMsg.TERRITORIES_LAYOUT_MSG)) {
				// Clear the old data.
				m_territories.clear();
				// Refresh the data
				Iterator<String> iter = params.keys();
				while (iter.hasNext()) {
					String key = iter.next();

					JSONObject territoryParam = params.getJSONObject(key);
					int id = (Integer) territoryParam.get("id");
					int unitNumber = (Integer) territoryParam.get("unitNumber");
					PlayerColor owner = PlayerColor.valueOf((territoryParam.get("owner").toString()));

					TerritoryInfo territory = new TerritoryInfo(id, unitNumber, owner);
					m_territories.put(territory);
				}
				setState(ClientState.TERRITORIES_UPDATED);
			}

			if (type.equals(EndGameMsg.END_GAME_MSG)) {
				m_log.println("Server terminated the game.");
				disconnect();
			}
			
			if(type.equals(AttackData.ATTACK_DATA_MSG)){
				Type attackType = new TypeToken<JSONMessage<AttackData>>(){}.getType();
				JSONMessage<AttackData> data = m_jsonParser.fromJson(msg.toString(), attackType);
				AttackData attack = data.getPayload().getParams();
				
				m_log.println( attack.toString() );
			}

		} catch (JSONException e) {
			new RuntimeException(e);
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
	}

	public void disconnect() {
		send(new Address(m_config.server_address), new DisconnectionMsg(this.peerDescriptor));
		m_connected = false;
		setState(ClientState.GAME_DISCONNECTION);
	}

	public boolean isGameStarted() {
		return m_gameStarted;
	}

	public boolean isConnected() {
		return m_connected;
	}

	public int getAvailableUnit() {
		return m_availableUnit;
	}

	public PlayerColor getColor() {
		if (m_color == null)
			return PlayerColor.NONE;
		return m_color;
	}
	
	public PlayerColor getTurnOwner(){
		return m_turnOwner;
	}

	public boolean isClientTurn(){
		return (m_turnOwner == m_color);
	}
	
	public TerritoriesLayout getTerritoriesLayout() {
		return m_territories;
	}

	public void setState(ClientState state) {
		m_log.println("State changed from " + m_state + " to " + state);
		System.out.println("State changed from " + m_state + " to " + state);
		m_state = state;
		m_stateChanged = true;
	}

	public ClientState getState() {
		m_stateChanged = false;
		return m_state;
	}

	public boolean isStateChanged() {
		return m_stateChanged;
	}

	public void synchronize() {
		send(new Address(m_config.server_address), new AckMsg(peerDescriptor));
	}
}