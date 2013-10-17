package risiko.net;

import org.zoolu.tools.Log;

import com.google.gson.Gson;

import risiko.net.configuration.ClientConfiguration;
import risiko.net.data.PlayerColor;
import risiko.net.data.SignalType;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.AttackData;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.Signal;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.gson.JSONTranslator;

import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.sip.Address;

public class Client extends Peer {

	// UTILITIES
	private Log m_log;
	private ClientConfiguration m_config;
	private Gson m_jsonParser;

	// GAME LOGIC
	private boolean m_gameStarted = false;
	private boolean m_connected = false;
	private PlayerInfo m_clientInfo;
	private TerritoriesLayout m_territories;
	private boolean m_initialized = false;
	private PlayerColor m_turnOwner;
	
	// State management
	private ClientState m_state;
	private boolean m_stateChanged = false;

	public Client(String pathConfig, String key) {
		super(pathConfig, key);
		// Configuration file
		m_config = new ClientConfiguration(pathConfig);

		// Create the log file
		if (this.nodeConfig.log_path != null) {
			m_log = new Log(this.nodeConfig.log_path + "info_" + this.peerDescriptor.getName() + ".log",
					Log.LEVEL_MEDIUM);
		}
		m_log.println("creating log file.");
		m_territories = new TerritoriesLayout();
		m_jsonParser = new Gson();

		// Set startup state
		setState(ClientState.WAIT_FOR_CONFIGURATION);
	}

	private void printLogInformation(JSONObject msg, Address sender) {
		try {
			m_log.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '"
					+ msg.get("type").toString() + "' form: " + sender.toString());
			System.out.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '"
					+ msg.get("type").toString() + "' form: " + sender.toString());
			m_log.println(msg.toString());
		} catch (JSONException e) {
			new RuntimeException();
			e.printStackTrace();
		}
	}

	@Override
	protected void onReceivedJSONMsg(JSONObject msg, Address sender) {
		super.onReceivedJSONMsg(msg, sender);
		
		printLogInformation(msg, sender);
		String params, type;
		
		try {
			params = msg.getJSONObject("payload").getJSONObject("params").toString();
			type = msg.get("type").toString();
		} catch (JSONException e) {
			throw new RuntimeException();
		}
		
		// Parsing messages without content
		if (type.equals(Signal.SIGNAL_MSG)) {
			Signal signal = m_jsonParser.fromJson(params, Signal.class);

			if (signal.getSignalType() == SignalType.START_GAME) {
				m_gameStarted = true;
			} else if (signal.getSignalType() == SignalType.DISCONNECTION) {
				m_log.println("Disconnecting from the server.");
				m_connected = false;
			} else if (signal.getSignalType() == SignalType.CONNECTION_ACCEPTED) {
				m_log.println("Connection accepted from the server");
				m_connected = true;
			} else if (signal.getSignalType() == SignalType.CONNECTION_REFUSED) {
				m_log.println("Connection refused from the server");
				m_connected = false;
				setState(ClientState.CONNECTION_REFUSED);
			} else if (signal.getSignalType() == SignalType.END_GAME) {
				m_log.println("Server terminated the game.");
				disconnect();
			}
		}

		// If message is used during the initialization phase specifies
		// the available number of unit at the beginning and the player
		// color.
		if (type.equals(PlayerInfo.PLAYER_COLOR_MSG)) {
			PlayerInfo player = m_jsonParser.fromJson(params, PlayerInfo.class);

			if (m_initialized == false) {
				m_clientInfo = player;
				m_log.println("Assigned color to client is: " + m_clientInfo.getColor() + ". Available unit are: "
						+ m_clientInfo.getTotalUnit());
				setState(ClientState.CONFIGURED);
				m_initialized = true;
			} else {
				m_turnOwner = player.getColor();
				if (isClientTurn()) {
					m_log.println("It's my turn ...");
					m_clientInfo.setTotalUnit(player.getTotalUnit());
				} else {
					m_log.println("It's player " + m_turnOwner + " turn.");
				}
				setState(ClientState.NEW_TURN);
			}
		}

		if (type.equals(TerritoriesLayout.TERRITORIES_LAYOUT_MSG)) {
			m_territories = m_jsonParser.fromJson(params, TerritoriesLayout.class);
			setState(ClientState.TERRITORIES_UPDATED);
		}

		if (type.equals(AttackData.ATTACK_DATA_MSG)) {
			AttackData data = m_jsonParser.fromJson(params, AttackData.class);
			m_log.println(data.toString());
		}

	}

	private void sendJSON(Address toAddress, String msg) {
		sendMessage(toAddress, getAddress(), msg, "application/json");
	}

	@Override
	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
	}

	@Override
	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
	}

	public void connect() {
		String msg = m_jsonParser.toJson(new JSONTranslator<Signal>(new Signal(SignalType.CONNECTION, peerDescriptor)));
		sendJSON(new Address(m_config.server_address), msg);
	}

	public void disconnect() {
		String msg = m_jsonParser.toJson(new JSONTranslator<Signal>(
				new Signal(SignalType.DISCONNECTION, peerDescriptor)));
		sendJSON(new Address(m_config.server_address), msg);
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
		return m_clientInfo.getTotalUnit();
	}

	public PlayerColor getColor() {
		return m_clientInfo.getColor();
	}

	public PlayerColor getTurnOwner() {
		return m_turnOwner;
	}

	public boolean isClientTurn() {
		return (m_turnOwner == m_clientInfo.getColor());
	}

	public TerritoriesLayout getTerritoriesLayout() {
		return m_territories;
	}

	public void setState(ClientState state) {
		m_log.println("State changed from " + m_state + " to " + state);
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
		String msg = m_jsonParser.toJson(new JSONTranslator<Signal>(new Signal(SignalType.ACK, peerDescriptor)));
		sendJSON(new Address(m_config.server_address), msg);
	}

	public void sendNewTerritoriesConfig(TerritoriesLayout layout) {
		String msg = m_jsonParser.toJson(new JSONTranslator<TerritoriesLayout>(layout));
		sendJSON(new Address(m_config.server_address), msg);
	}
}