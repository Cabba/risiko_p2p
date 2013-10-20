package risiko.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zoolu.tools.Log;

import com.google.gson.Gson;

import risiko.net.configuration.ServerConfiguration;
import risiko.net.data.AttackPhase;
import risiko.net.data.PlayerColor;
import risiko.net.data.RisikoData;
import risiko.net.data.SignalType;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.AttackData;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.Signal;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.gson.JSONMessage;
import risiko.net.script.IRules;

import groovy.lang.GroovyClassLoader;
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

	// // UTILITIES
	private Log m_log;
	private ServerConfiguration m_serverConfig;
	private Gson m_jsonParser;
	private GroovyClassLoader m_scriptLoader;

	// // SCRIPTING
	private IRules m_rules;

	// // SYNCHRONIZATION
	private int m_barrierCount = 0;
	private List<Address> m_synchronizedClients;

	// // GAME LOGIC
	// Game state
	private boolean m_gameStarted = false;
	private TerritoriesLayout m_territories;
	private int m_playerNumber;
	private Map<NeighborPeerDescriptor, PlayerInfo> m_players;
	// Turn
	private PlayerInfo m_turnOwner;
	private int m_turnCounter;
	private boolean m_attackPhaseEnded;
	private boolean m_attackStarted;

	public Server(String pathConfig, String key) {
		super(pathConfig, key);

		// Configuration file
		m_serverConfig = new ServerConfiguration(pathConfig);

		// Parser
		m_jsonParser = new Gson();

		// Synchronization
		m_synchronizedClients = new ArrayList<Address>();

		// Load script
		m_scriptLoader = new GroovyClassLoader();
		Object script = null;
		try {
			Class clazz = m_scriptLoader.parseClass(new File(RisikoData.SCRIPT_NAME));
			script = clazz.newInstance();
		} catch (Exception e) {
			new RuntimeException();
			e.printStackTrace();
		}
		m_rules = (IRules) script;

		// Create the log file
		if (this.nodeConfig.log_path != null) {
			m_log = new Log(this.nodeConfig.log_path + "" + this.peerDescriptor.getName() + ".log", Log.LEVEL_MEDIUM);
		}
		m_log.print("creating log file.");

		// Initialize game logic
		m_players = new HashMap<NeighborPeerDescriptor, PlayerInfo>();

		m_territories = new TerritoriesLayout();
		for (int i = 0; i < RisikoData.mapColumns * RisikoData.mapRows; ++i) {
			// 1 unit per territory at least
			m_territories.put(new TerritoryInfo(i, 1, PlayerColor.NONE));
		}
	}

	@Override
	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
	}

	@Override
	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
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
			throw new RuntimeException(e);
		}

		if (type.equals(Signal.SIGNAL_MSG)) {

			Signal signal = m_jsonParser.fromJson(params, Signal.class);

			if (signal.getSignalType() == SignalType.CONNECTION) {
				// If there is another match refuse the connection
				if (m_gameStarted) {
					String response = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.CONNECTION_REFUSED,
							peerDescriptor)));
					sendJSON(sender, response);
				} else {
					PeerDescriptor peerDesc = signal.getPeerDescriptor();
					peerList.put(peerDesc.getKey(), new NeighborPeerDescriptor(peerDesc));
					m_log.println("Peer " + peerDesc.getName() + " added to available peer list.");

					String response = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.CONNECTION_ACCEPTED,
							peerDescriptor)));
					sendJSON(sender, response);
					// Increment the number of players
					m_playerNumber++;

					if (peerList.size() >= m_serverConfig.min_clients_number) {
						m_log.println("Minimum clients number reached.");
					}
				}
			} else if (signal.getSignalType() == SignalType.DISCONNECTION) {
				PeerDescriptor peerDesc = signal.getPeerDescriptor();
				peerList.remove(peerDesc.getKey());
				// Decrement the number of players
				m_playerNumber--;
				if (m_gameStarted) {
					String response = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.END_GAME,
							peerDescriptor)));
					broadcastMessage(response);
				}
			} else if (signal.getSignalType() == SignalType.ACK) {
				if (isClientSynchronized(new Address(sender))) {
					signal(new Address(sender));
				}
			} else if (signal.getSignalType() == SignalType.ATTACK_PHASE_ENDED) {
				m_attackPhaseEnded = true;
			}
		}

		if (type.equals(TerritoriesLayout.TERRITORIES_LAYOUT_MSG)) {
			if (isClientSynchronized(new Address(sender))) {
				TerritoriesLayout layout = m_jsonParser.fromJson(params, TerritoriesLayout.class);

				// Get player from address
				PlayerInfo player = new PlayerInfo();
				Iterator<NeighborPeerDescriptor> iter = m_players.keySet().iterator();
				while (iter.hasNext()) {
					NeighborPeerDescriptor client = iter.next();
					if (client.getAddress().equals(sender.getURL())) {
						player = m_players.get(client);
					}
				}

				System.out.println("Updating territories of player: " + player.getColor());
				if (m_rules.checkTerritoriesLayout(m_territories, layout, player)) {
					updateTerritories(layout, player);
					signal(new Address(sender));
				}
			} else {
				m_log.println("Client " + sender + " has already sent territory_layout message.");
			}
		}

		if (type.equals(AttackData.ATTACK_DATA_MSG)) {
			AttackData attack = m_jsonParser.fromJson(params, AttackData.class);
			// Attack
			if (m_rules.isValidAttack(attack, m_territories) && attack.getPhase() == AttackPhase.ATTACK) {
				m_log.println("Received an attack.");
				m_attackStarted = true;
				// Resend the message at all the other players
				broadcastMessage(msg.toString());
			}
			// Defence
			else if (m_rules.isValidDefence(attack, m_territories) && attack.getPhase() == AttackPhase.DEFENCE) {
				m_log.println("Received an defence.");
				m_log.println("Attacker unit destroied: " + m_rules.attackerUnitsDestroyed(attack));
				m_log.println("Defencer unit destroied: " + m_rules.attackedUnitsDestoyed(attack));
				m_attackStarted = false;
				// Resend the message at all the other players
				broadcastMessage(msg.toString());
				m_log.println("Wait for a syncronization.");
			}
			// TODO else not valid attack or defence!
		}

	}

	private boolean isClientSynchronized(Address clientAddress) {
		if (m_synchronizedClients.contains(clientAddress))
			return false;
		return true;
	}

	private void signal(Address clientAddress) {
		m_log.println("Client " + clientAddress.toString() + " is synchronized.");
		m_synchronizedClients.add(clientAddress);
		m_barrierCount++;
	}

	private void resetClientSynchronization() {
		m_synchronizedClients.clear();
	}

	/**
	 * This functions assumes that layout is a valid rappresentation of player
	 * territories.
	 */
	private void updateTerritories(TerritoriesLayout layout, PlayerInfo info) {
		TerritoriesLayout terrs = layout.getSubset(info.getColor());
		Iterator<Integer> iter = terrs.keySet().iterator();
		while (iter.hasNext()) {
			TerritoryInfo ter = terrs.get(iter.next());
			m_territories.put(ter);
		}

		m_log.println("New territories configuration: " + m_jsonParser.toJson(new JSONMessage(m_territories)));

	}

	public int getConnectedClientsNumber() {
		return peerList.size();
	}

	private void assignIDToClients() {
		Iterator<String> iter = peerList.keySet().iterator();
		for (PlayerColor color : PlayerColor.values()) {
			if (!iter.hasNext())
				return; // Iterated over all players
			if (color == PlayerColor.NONE)
				continue;

			PlayerInfo info = new PlayerInfo(color, m_rules.getInitUnits(m_playerNumber));
			NeighborPeerDescriptor peer = peerList.get((String) iter.next());
			m_players.put(peer, info);

			String msg = m_jsonParser.toJson(new JSONMessage(info));
			sendJSON(new Address(peer.getAddress()), msg);

			m_log.println("Sended color " + info.getColor() + " to client " + peer.getName());
		}
	}

	private void assignTerritoryToClients() {
		// Generate a list of number and assign that number to the players
		int size = RisikoData.mapColumns * RisikoData.mapRows;
		List<Integer> random = new ArrayList<Integer>();
		for (int i = 0; i < size; ++i) {
			random.add(new Integer(i));
		}
		// Randomize the list
		Collections.shuffle(random);

		int territoryForPlayer = size / m_playerNumber;
		int remainder = size % m_playerNumber;

		Iterator<NeighborPeerDescriptor> peer = m_players.keySet().iterator();
		while (peer.hasNext()) {
			PlayerColor owner = m_players.get(peer.next()).getColor();

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
		sendTerritoriesToClients();
	}

	private void sendTerritoriesToClients() {
		String msg = m_jsonParser.toJson(new JSONMessage(m_territories));
		broadcastMessage(msg);
	}

	/**
	 * A game can start if there are enough player and there are no other match
	 * occurring.
	 */
	private boolean gameCanStart() {
		if (m_gameStarted)
			return false;
		if (peerList.size() >= m_serverConfig.min_clients_number) {
			m_log.println("The game can start");
			System.out.println("The game can start");
			return true;
		} else
			return false;
	}

	private boolean isGameStarted() {
		return m_gameStarted;
	}

	private void startGame() {
		String msg = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.START_GAME, peerDescriptor)));
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

	/**
	 * Wait until all the peer send an ACK message.
	 */
	private void barrier() {
		while (peerList.size() != m_barrierCount) {
			// Do nothing
		}
		resetClientSynchronization();
		m_barrierCount = 0;
	}

	private void assignTurn(PlayerInfo player) {
		m_log.println("Turn assigned at player: " + player.getColor());
		m_turnOwner = player;

		m_turnOwner.incrementTotalUnit(m_rules.getReinforcementUnits(m_territories));
		broadcastMessage(m_jsonParser.toJson(new JSONMessage(m_turnOwner)));
	}

	public void attack() {
		if (m_attackStarted) {
			while (m_attackStarted) {
				System.out.println("Attack running");
			}
			barrier(); // Wait response
			m_log.println("Sending new territories configuration.");
			sendTerritoriesToClients();
			barrier();
			m_log.println("Attack ended!!");
		}
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
			Iterator<NeighborPeerDescriptor> players = m_players.keySet().iterator();
			while (players.hasNext()) {

				m_turnCounter++;
				PlayerInfo player = m_players.get(players.next());

				// Assign turn
				assignTurn(player);
				barrier();
				m_log.println("Turn assigned.");

				// Wait for unit disposal
				barrier();
				sendTerritoriesToClients();
				m_log.println("New territories configuration sended to clients.");

				m_attackPhaseEnded = false;
				while (!m_attackPhaseEnded) {
					System.out.println("ATTACK PHASE - DA IMPLEMENTARE");
					attack();
				}
				m_log.println("Attack phase finished.");

			}
		}

	}

}
