package risiko.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zoolu.tools.Log;

import com.google.gson.Gson;

import risiko.net.configuration.ServerConfiguration;
import risiko.net.data.PlayerColor;
import risiko.net.data.RisikoData;
import risiko.net.data.SignalType;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.AttackData;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.Signal;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.gson.JSONTranslator;

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
	private Gson m_jsonParser;

	// Logic
	private boolean m_gameStarted = false;

	// TODO muovere le variabili che rappresentano la logica di gioco
	// in un altra classe ?
	private TerritoriesLayout m_territories;
	private int m_playerNumber;
	private Map<PlayerInfo, NeighborPeerDescriptor> m_players;

	private PlayerInfo m_turnOwner;
	private int m_turnCounter;

	public Server(String pathConfig, String key) {
		super(pathConfig, key);

		// Configuration file
		m_serverConfig = new ServerConfiguration(pathConfig);
		m_jsonParser = new Gson();

		// Create the log file
		if (this.nodeConfig.log_path != null) {
			m_log = new Log(this.nodeConfig.log_path + "" + this.peerDescriptor.getName() + ".log", Log.LEVEL_MEDIUM);
		}
		m_log.print("creating log file.");

		// Initialize game logic ----
		m_players = new HashMap<PlayerInfo, NeighborPeerDescriptor>();

		m_territories = new TerritoriesLayout();
		// Every territory have at least 1 unit
		for (int i = 0; i < RisikoData.mapColumns * RisikoData.mapRows; ++i) {
			m_territories.put(new TerritoryInfo(i, 1, PlayerColor.NONE));
		}
	}

	@Override
	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
	}

	@Override
	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
	}

	@Override
	protected void onReceivedJSONMsg(JSONObject msg, Address sender) {
		super.onReceivedJSONMsg(msg, sender);
		try {
			m_log.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '"
					+ msg.get("type").toString() + "' form: " + sender.toString());
			System.out.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '"
					+ msg.get("type").toString() + "' form: " + sender.toString());
			m_log.println(msg.toString());

			String params = msg.getJSONObject("payload").getJSONObject("params").toString();
			String type = msg.get("type").toString();

			// Connection ...
			if (type.equals(Signal.SIGNAL_MSG)) {

				Signal signal = m_jsonParser.fromJson(params, Signal.class);

				if (signal.getSignalType() == SignalType.CONNECTION) {
					// If there is another match refuse the connection
					if (m_gameStarted) {
						String response = m_jsonParser.toJson(new JSONTranslator<Signal>(new Signal(
								SignalType.CONNECTION_REFUSED, peerDescriptor)));
						sendJSON(sender, response);
					} else {
						PeerDescriptor peerDesc = signal.getPeerDescriptor();
						peerList.put(peerDesc.getKey(), new NeighborPeerDescriptor(peerDesc));
						m_log.println("Peer " + peerDesc.getName() + " added to available peer list.");

						String response = m_jsonParser.toJson(new JSONTranslator<Signal>(new Signal(
								SignalType.CONNECTION_ACCEPTED, peerDescriptor)));
						sendJSON(sender, response);

						if (peerList.size() >= m_serverConfig.min_clients_number) {
							m_log.println("Minimum clients number reached.");
						}
					}
				} else if (signal.getSignalType() == SignalType.DISCONNECTION) {
					PeerDescriptor peerDesc = signal.getPeerDescriptor();
					peerList.remove(peerDesc.getKey());
					if (m_gameStarted) {
						String response = m_jsonParser.toJson(new JSONTranslator<Signal>(new Signal(
								SignalType.END_GAME, peerDescriptor)));
						broadcastMessage(response);
					}
				} else if (signal.getSignalType() == SignalType.ACK) {
					// TODO gestire arrivo di ACK dallo stesso client
					m_barrierCount++;
				}
			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public int getConnectedClientsNumber() {
		return peerList.size();
	}

	public void assignIDToClients() {
		Iterator<String> iter = peerList.keySet().iterator();
		for (PlayerColor color : PlayerColor.values()) {
			if (!iter.hasNext())
				return; // Iterated over all players
			if (color == PlayerColor.NONE)
				continue;

			// TODO configurare le unità in base al numero di partecipanti nelle
			// regole
			PlayerInfo info = new PlayerInfo(color, 30);
			NeighborPeerDescriptor peer = peerList.get((String) iter.next());
			m_players.put(info, peer);

			String msg = m_jsonParser.toJson(new JSONTranslator<PlayerInfo>(info));
			sendJSON(new Address(peer.getAddress()), msg);

			m_playerNumber++; // Get the number of player

			m_log.println("Sended color " + info.getColor() + " to client " + peer.getName());
		}
	}

	public void assignTerritoryToClients() {
		// Generate a list of number and assign that number to the players
		int size = RisikoData.mapColumns * RisikoData.mapRows;
		List<Integer> random = new ArrayList<Integer>();
		for (int i = 0; i < size; ++i) {
			random.add(new Integer(i));
		}
		// Randomize the list of numbers
		Collections.shuffle(random);

		int territoryForPlayer = size / m_playerNumber;
		int remainder = size % m_playerNumber;

		Iterator<PlayerInfo> peer = m_players.keySet().iterator();
		while (peer.hasNext()) {
			PlayerColor owner = peer.next().getColor();

			for (int j = 0; j < territoryForPlayer; ++j) {
				int territoryId = random.get(0);
				m_territories.get(territoryId).setOwner(owner);
				random.remove(0);
				m_log.println("Assigned territory: " + territoryId + " at player " + owner);
			}

			if (remainder > 0) {
				int territoryId = random.get(0);
				m_territories.get(territoryId).setOwner(owner);
				random.remove(0);
				remainder--;
				m_log.println("Assigned territory: " + territoryId + " at player " + owner);
			}
		}

		// Send the new configuration at all the clients
		String msg = m_jsonParser.toJson(new JSONTranslator<TerritoriesLayout>(m_territories));
		broadcastMessage(msg);
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

	public boolean isGameStarted() {
		return m_gameStarted;
	}

	public void startGame() {
		String msg = m_jsonParser.toJson(new JSONTranslator<Signal>(new Signal(SignalType.START_GAME, peerDescriptor)));
		broadcastMessage(msg);
		m_gameStarted = true;
	}

	private void broadcastMessage(String msg) {
		Iterator<String> iter = this.peerList.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			NeighborPeerDescriptor neighborPD = this.peerList.get(key);
			sendJSON(new Address(neighborPD.getAddress()), msg);
		}
	}

	private void sendJSON(Address toAddress, String msg) {
		sendMessage(toAddress, getAddress(), msg, "application/json");
	}

	private int m_barrierCount = 0;

	/**
	 * Wait until all the peer send an ACK message.
	 */
	public void barrier() {
		// TODO implementare una versione che controlla il descrittore del
		// client
		while (peerList.size() != m_barrierCount) {
			// Do nothing
		}
		m_barrierCount = 0;
	}

	public void assignTurn(PlayerInfo player) {
		m_log.println("Turn assigned at player: " + player.getColor());
		m_turnOwner = player;
		// TODO settare on la logica di gioco il valore dell'incremento
		m_turnOwner.incrementTotalUnit(5);
		broadcastMessage(m_jsonParser.toJson(new JSONTranslator<PlayerInfo>(m_turnOwner)));
	}

	public void run() {
		// Initialization
		while (!gameCanStart()) {
		} // Loop until game can start

		m_log.println("Game is started");
		startGame();

		m_log.println("Assigning IDs to clients");
		assignIDToClients();
		barrier();

		m_log.println("Assign territories to clients");
		assignTerritoryToClients();
		barrier();

		boolean end = false;
		// Turn loop
		while (!end) {
			// Assign the turn at all the player in order.
			Iterator<PlayerInfo> players = m_players.keySet().iterator();
			while (players.hasNext()) {

				assignTurn(players.next());
				barrier();

			}
		}

	}

}
