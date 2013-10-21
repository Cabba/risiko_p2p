package risiko.ui;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import risiko.net.Client;
import risiko.net.ClientState;
import risiko.net.Server;
import risiko.net.data.PlayerColor;
import risiko.net.data.RisikoData;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.AttackData;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.script.IRules;

public class TestUI {

	private Display m_display;
	private Shell m_shell;

	private class ClientInfo {
		public String name;
		public String configPath;
		public String key;

		public ClientInfo(String name, String configPath, String key) {
			this.name = name;
			this.configPath = configPath;
			this.key = key;
		}
	}

	List<ClientInfo> m_clientInfo;

	private class ClientUI {
		public TabItem tabClient;

		public FillLayout layoutMain;

		public Composite mainComposite;
		public Composite mapComposite;

		public Group groupMap;
		public Group groupAction;
		public Group groupInfo;

		public Button buttonConnection;
		public List<Button> buttonsMap;
		public Button buttonSync;
		public Button buttonAttack;
		public Button buttonRemoveUnits;

		public Label labelPlayerColor;
		public Label labelAvailableUnits;
		public Label labelMsg;
		public Label labelAttacker;
		public Label labelAttacked;
		public Label labelAttackingUnits;

		public Spinner spinnerAttacker;
		public Spinner spinnerAttacked;
		public Spinner spinnerAttackingUnits;

		public boolean removeUnits = false;

		// Networking
		public Client net;

	}

	List<ClientUI> m_clients;

	private class ServerUI {
		public TabItem serverTab;
		public Button startServer;

		public Thread serverThread;
		public boolean isStarted;
		public ServerThread m_thread;
	}

	private ServerUI m_server;

	private GroovyClassLoader m_scriptLoader;
	private IRules m_rules;

	public TestUI() {
		m_display = new Display();

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
	}

	public void build() {
		m_shell = new Shell(m_display);
		m_shell.setText("Risiko Test Interface");

		m_shell.setLayout(new FillLayout());

		TabFolder folder = new TabFolder(m_shell, SWT.BORDER);

		m_server = createServerUI(folder);

		m_clients = new ArrayList<ClientUI>();
		for (int i = 0; i < m_clientInfo.size(); ++i) {
			m_clients.add(createClientUI(folder, m_clientInfo.get(i)));
		}

		folder.pack();

		m_shell.open();
	}

	// TODO vedere se si riesce a spostare tutti i gruppi fuori dalla classe
	// ClientUI dato che servono solo in fase di inizializzazione
	// dell'interfaccia
	private ClientUI createClientUI(TabFolder folder, ClientInfo info) {
		ClientUI ui = new ClientUI();

		System.out.println("Create client: " + info.name + "...");
		ui.net = new Client(info.configPath, info.key);

		ui.tabClient = new TabItem(folder, SWT.NONE);
		ui.tabClient.setText(info.name);

		ui.layoutMain = new FillLayout();
		ui.layoutMain.type = SWT.VERTICAL;

		ui.mainComposite = new Composite(folder, SWT.NONE);
		ui.mainComposite.setLayout(ui.layoutMain);

		// MAP BAR
		ui.groupMap = new Group(ui.mainComposite, SWT.NONE);
		ui.groupMap.setText("Map");
		ui.groupMap.setLayout(ui.layoutMain);

		ui.mapComposite = new Composite(ui.groupMap, SWT.NONE);
		ui.mapComposite.setLayout(new GridLayout(RisikoData.mapColumns, true));
		GridData buttonGrid = new GridData(SWT.FILL, SWT.FILL, true, true);
		ui.buttonsMap = new ArrayList<Button>();
		for (int i = 0; i < RisikoData.mapColumns * RisikoData.mapRows; ++i) {
			Button button = new Button(ui.mapComposite, SWT.PUSH);
			button.setLayoutData(buttonGrid);
			button.setData("client", ui);
			button.setData("id", i);
			// Setting buttons function
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ClientUI ui = (ClientUI) e.widget.getData("client");
					int id = (Integer) e.widget.getData("id");

					// Update the units in the territory
					if (ui.net.getState() == ClientState.REINFORCEMENT) {
						PlayerColor player = ui.net.getColor();
						int occupiedUnits = ui.net.getUsedUnits();
						int totalUnits = ui.net.getAvailableUnits();
						PlayerColor terrOwner = ui.net.getTerritoriesLayout().get(id).getOwner();

						if (totalUnits - occupiedUnits > 0 && player == terrOwner) {
							int units = ui.net.getTerritoriesLayout().get(id).getUnitNumber();
							int increment = ui.removeUnits ? -1 : 1;
							if (units == 1 && increment == -1)
								increment = 0;
							ui.net.getTerritoriesLayout().updateTerritory(id, units + increment, player);
							updateGrid(ui);
							// Update available units message
							ui.labelAvailableUnits.setText(unitLabelMessage(totalUnits, totalUnits - occupiedUnits
									- increment));

						}
					}
				}
			});
			ui.buttonsMap.add(button);
		}

		ui.buttonRemoveUnits = new Button(ui.mapComposite, SWT.CHECK);
		ui.buttonRemoveUnits.setText("Remove units");
		ui.buttonRemoveUnits.setData(ui);
		ui.buttonRemoveUnits.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ClientUI ui = (ClientUI) e.widget.getData();
				ui.removeUnits = !ui.removeUnits;
			}
		});

		// ACTION BAR
		ui.groupAction = new Group(ui.mainComposite, SWT.NONE);
		ui.groupAction.setText("Actions");
		ui.groupAction.setLayout(ui.layoutMain);

		Composite actionComposite = new Composite(ui.groupAction, SWT.NONE);
		FillLayout actionCompositeLayout = new FillLayout();
		actionCompositeLayout.type = SWT.HORIZONTAL;
		actionComposite.setLayout(actionCompositeLayout);

		ui.labelAttacker = new Label(actionComposite, SWT.NONE);
		ui.labelAttacker.setText("-");
		ui.spinnerAttacker = new Spinner(actionComposite, SWT.BORDER);
		ui.spinnerAttacker.setMinimum(0);
		ui.spinnerAttacker.setMaximum(RisikoData.mapColumns * RisikoData.mapRows);

		ui.labelAttacked = new Label(actionComposite, SWT.NONE);
		ui.labelAttacked.setText("-");
		ui.spinnerAttacked = new Spinner(actionComposite, SWT.BORDER);
		ui.spinnerAttacked.setMinimum(0);
		ui.spinnerAttacked.setMaximum(RisikoData.mapColumns * RisikoData.mapRows);

		ui.labelAttackingUnits = new Label(actionComposite, SWT.NONE);
		ui.labelAttackingUnits.setText("-");
		ui.spinnerAttackingUnits = new Spinner(actionComposite, SWT.BORDER);

		ui.buttonAttack = new Button(actionComposite, SWT.PUSH);
		ui.buttonAttack.setText("-");
		ui.buttonAttack.setEnabled(false);
		ui.buttonAttack.setData(ui);
		ui.buttonAttack.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ClientUI ui = (ClientUI) e.widget.getData();
				int attacker = Integer.parseInt(ui.spinnerAttacker.getText());
				int attacked = Integer.parseInt(ui.spinnerAttacked.getText());
				int units = Integer.parseInt(ui.spinnerAttackingUnits.getText());
				if (ui.net.attack(attacker, attacked, units)) {
					ui.buttonAttack.setText("Attack sended.");
				} else {
					ui.buttonAttack.setText("Invalid attack. Retry");
				}
			}
		});

		ui.buttonSync = new Button(actionComposite, SWT.PUSH);
		ui.buttonSync.setText("Next phase.");
		ui.buttonSync.setEnabled(false);
		ui.buttonSync.setData(ui);
		ui.buttonSync.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ClientUI ui = (ClientUI) e.widget.getData();
				if (ui.net != null) {
					// Reinforcement phase
					if (ui.net.getState() == ClientState.REINFORCEMENT) {
						ui.net.sendNewTerritoriesConfiguration();
					} else if (ui.net.getState() == ClientState.END_REINFORCEMENT) {
						ui.net.finishAttackPhase();
					} else if (ui.net.getState() == ClientState.NEW_DISPOSITION) {
						ui.net.finishAttackPhase();
					}
				}
			}
		});

		// INFO BAR
		ui.groupInfo = new Group(ui.mainComposite, SWT.NONE);
		ui.groupInfo.setText("Info");
		ui.groupInfo.setLayout(ui.layoutMain);

		Composite infoComposite = new Composite(ui.groupInfo, SWT.NONE);
		FillLayout infoCompositeLayout = new FillLayout();
		infoCompositeLayout.type = SWT.VERTICAL;
		infoComposite.setLayout(infoCompositeLayout);

		ui.labelPlayerColor = new Label(infoComposite, SWT.BORDER | SWT.CENTER);
		ui.labelPlayerColor.setText("Player color");

		ui.labelAvailableUnits = new Label(infoComposite, SWT.BORDER | SWT.CENTER);
		ui.labelAvailableUnits.setText("Units counter");

		ui.labelMsg = new Label(infoComposite, SWT.BORDER | SWT.CENTER);
		ui.labelMsg.setText("Message box");

		ui.buttonConnection = new Button(infoComposite, SWT.PUSH);
		ui.buttonConnection.setText(RisikoData.CONNECT_TEXT);
		ui.buttonConnection.setData(ui);
		ui.buttonConnection.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ClientUI ui = (ClientUI) e.widget.getData();
				// running network side
				if (!ui.net.isConnected()) {
					ui.net.connect();
					ui.buttonConnection.setText(RisikoData.DISCONNECT_TEXT);
				} else {
					ui.net.disconnect();
					ui.buttonConnection.setText(RisikoData.CONNECT_TEXT);
				}
			}
		});

		ui.tabClient.setControl(ui.mainComposite);

		return ui;
	}

	private ServerUI createServerUI(TabFolder folder) {
		ServerUI ui = new ServerUI();
		ui = new ServerUI();

		ui.serverTab = new TabItem(folder, SWT.BORDER);
		ui.serverTab.setText("Server");

		ui.startServer = new Button(folder, SWT.PUSH);
		ui.startServer.setData(ui);
		ui.startServer.setText("Run server");
		ui.startServer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ServerUI ui = (ServerUI) e.widget.getData();
				System.out.println("Starting the server ...");
				// Running the server thread ...
				ui.serverThread = new Thread() {
					public Server server;

					public void run() {
						server = new Server("config/server.config", "1");
						server.run();
					}

				};
				//ui.serverThread.start();
				ui.m_thread = new ServerThread("config/server.config", "1");
				ui.m_thread.start();
				ui.isStarted = true;
				ui.startServer.setText("Running..");
				ui.startServer.setEnabled(false);
			}
		});
		ui.serverTab.setControl(ui.startServer);

		return ui;

	}

	public void setClientConfigurationData(String name, String configPath, String key) {
		if (m_clientInfo == null)
			m_clientInfo = new ArrayList<ClientInfo>();
		m_clientInfo.add(new ClientInfo(name, configPath, key));
	}

	public void update() {
		while (!m_shell.isDisposed()) {
			if (!m_display.readAndDispatch()) {
				m_display.sleep();
			}

			for (int i = 0; i < m_clients.size(); ++i) {
				if (!m_shell.isDisposed())
					clientLogic(m_clients.get(i));
			}
		}
		m_display.dispose();

	}

	private void clientLogic(ClientUI client) {
		// Parsing the states
		if (client.net.isStateChanged()) {
			ClientState state = client.net.getState();

			if (state == ClientState.CONNECTION_REFUSED) {
				System.out.println("Connection refused ...");
				client.buttonConnection.setEnabled(true);
			}

			if (state == ClientState.CONFIGURED) {
				System.out.println("Client connected - Setting parameters");
				client.labelPlayerColor.setText(client.net.getColor().toString());
				client.net.synchronize();
			}

			if (state == ClientState.TURN_ASSIGNED) {
				System.out.println("Upating territories");
				int totalUnits = client.net.getAvailableUnits();
				int usedUnits = client.net.getUsedUnits();
				client.labelAvailableUnits.setText(unitLabelMessage(totalUnits, totalUnits - usedUnits));

				updateGrid(client);

				client.net.synchronize();
			}

			if (state == ClientState.REINFORCEMENT) {
				System.out.println("Begin of reinforcement.");
				if (client.net.isClientTurn()) {
					client.labelMsg.setText("It's your turn. " + RisikoData.DISPOSE_UNITS_TEXT);
				} else {
					client.labelMsg.setText("It's " + client.net.getTurnOwner() + " turn. "
							+ RisikoData.DISPOSE_UNITS_TEXT);
				}
				// Set new text in actions buttons
				client.buttonSync.setText("Units disposed.");
				client.buttonSync.setEnabled(true);

				client.buttonAttack.setText("-");
				client.buttonAttack.setEnabled(false);
				client.labelAttacked.setText("-");
				client.labelAttacker.setText("-");
				client.labelAttackingUnits.setText("-");

				client.net.synchronize();
			}

			if (state == ClientState.END_REINFORCEMENT) {
				System.out.println("End of reinforcement.");
				updateGrid(client);

				// Set new text in actions buttons
				if (client.net.isClientTurn()) {
					client.buttonAttack.setEnabled(true);
					client.buttonAttack.setText("Attack");
					client.buttonSync.setText("Finish turn.");
					client.labelMsg.setText("It's your turn. Attack one player.");
					client.labelAttacker.setText("Attacker id:");
					client.labelAttacked.setText("Attacked id:");
					client.labelAttackingUnits.setText("Attacking units:");
				} else {
					client.buttonSync.setText("Wait turn end.");
					client.buttonSync.setEnabled(false);
					client.labelMsg.setText(client.net.getTurnOwner() + " is turn owner (ATTACK PHASE).");
				}
			}

			if (state == ClientState.ATTACK_PHASE) {
				AttackData attack = client.net.getAttackData();
				client.labelMsg.setText("Territory " + attack.getAttackedID() + " is under attack from "
						+ attack.getAttackerID());
			}

			if (state == ClientState.AFTER_ATTACK) {
				AttackData attack = client.net.getAttackData();

				client.labelMsg.setText("Attack resolved. Death from " + attack.getAttackerID() + " are "
						+ m_rules.attackerUnitsDestroyed(attack) + ". Death from " + attack.getAttackedID() + " are "
						+ m_rules.attackedUnitsDestroyed(attack));
				client.net.synchronize();
			}

			if (state == ClientState.NEW_DISPOSITION) {
				client.buttonAttack.setText("Attack completed.");

				updateGrid(client);
				client.net.synchronize();
			}

			if (state == ClientState.LOSE) {
				client.labelMsg.setText("You lose!!");
			}

			if (state == ClientState.WIN) {
				client.labelMsg.setText("You win!");
			}

			if (state == ClientState.GAME_DISCONNECTION) {
				reset();
			}
		}

	}

	private void updateGrid(ClientUI client) {
		Iterator<Integer> iter = client.net.getTerritoriesLayout().keySet().iterator();
		TerritoriesLayout territories = client.net.getTerritoriesLayout();
		while (iter.hasNext()) {
			Integer key = iter.next();

			Button button = client.buttonsMap.get(territories.get(key).getId());

			PlayerColor owner = territories.get(key).getOwner();
			int unitNumber = territories.get(key).getUnitNumber();
			int id = territories.get(key).getId();

			button.setText(mapButtonMessage(id, owner, unitNumber));
			if (client.net.getColor() != owner)
				button.setEnabled(false);
			else
				button.setEnabled(true);
		}
	}

	private String mapButtonMessage(int id, PlayerColor color, int unitNumber) {
		return "ID: " + id + " COLOR: " + color + " UNIT: " + unitNumber;
	}

	private String unitLabelMessage(int totalUnits, int remainingUnits) {
		return "You have " + totalUnits + " total units, and " + remainingUnits + " can be placed.";
	}

	private void reset() {
		System.out.println("RESET - DA IMPLEMENTARE");
	}

	private void clear() {
		System.out.println("Freeing the ports...");
		for (int i = 0; i < m_clients.size(); ++i) {
			m_clients.get(i).net.halt();
		}
		m_server.m_thread.terminate();
		// TODO Change stop function
		m_server.m_thread.stop();
		
	}

	public static void main(String[] args) {
		TestUI ui = new TestUI();

		ui.setClientConfigurationData("Client 1", "config/client_1.config", "2");
		ui.setClientConfigurationData("Client 2", "config/client_2.config", "3");
		ui.setClientConfigurationData("Client 3", "config/client_3.config", "4");

		ui.build();

		ui.update();

		ui.clear();
	}

}
