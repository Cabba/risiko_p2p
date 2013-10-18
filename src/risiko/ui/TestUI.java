package risiko.ui;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import risiko.net.Client;
import risiko.net.ClientState;
import risiko.net.Server;
import risiko.net.data.PlayerColor;
import risiko.net.data.RisikoData;
import risiko.net.data.sendable.TerritoriesLayout;

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

		public Label labelPlayerColor;
		public Label labelAvailableUnits;
		public Label labelMsg;

		// Logic
		public TerritoriesLayout tempTerritories;

		// Networking
		public Client net;
	}

	List<ClientUI> m_clients;

	private class ServerUI {
		public TabItem serverTab;
		public Button startServer;

		public Server net;
		public Thread serverThread;
	}

	private ServerUI m_server;

	public TestUI() {
		m_display = new Display();
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
			// TODO assolutamente da mettere a posto
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ClientUI ui = (ClientUI) e.widget.getData("client");
					int id = (Integer) e.widget.getData("id");

					if (ui.net.getState() == ClientState.REINFORCEMENT) {
						PlayerColor owner = ui.net.getColor();
						int occupiedUnits = ui.net.getTerritoriesLayout().getSubset(owner).getPlayerUnit(owner);
						int totalUnits = ui.net.getAvailableUnit();
						PlayerColor terrOwner = ui.net.getTerritoriesLayout().get(id).getOwner();
						System.out.println("ocupiedUnits = " + occupiedUnits + " totalUnits = " + totalUnits
								+ " terrOwner = " + terrOwner);
						if (totalUnits - occupiedUnits > 0 && owner == terrOwner) {
							int units = ui.net.getTerritoriesLayout().get(id).getUnitNumber();
							System.out.println("unit in the territory are: " + units);
							ui.net.getTerritoriesLayout().updateTerritory(id, units + 1, owner);
							updateGrid(ui);
						}
					}
				}
			});
			ui.buttonsMap.add(button);
		}

		// ACTION BAR
		ui.groupAction = new Group(ui.mainComposite, SWT.NONE);
		ui.groupAction.setText("Actions");
		ui.groupAction.setLayout(ui.layoutMain);

		Composite actionComposite = new Composite(ui.groupAction, SWT.NONE);
		FillLayout actionCompositeLayout = new FillLayout();
		actionCompositeLayout.type = SWT.HORIZONTAL;
		actionComposite.setLayout(actionCompositeLayout);

		ui.buttonSync = new Button(actionComposite, SWT.PUSH);
		ui.buttonSync.setText("OK");
		ui.buttonSync.setData(ui);
		ui.buttonSync.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ClientUI ui = (ClientUI) e.widget.getData();
				if (ui.net != null) {
					// Reinforcement phase
					if (ui.net.getState() == ClientState.REINFORCEMENT) {
						ui.net.updateTerritoriesLayout();
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
		ui.labelPlayerColor.setText("NONE");

		ui.labelAvailableUnits = new Label(infoComposite, SWT.BORDER | SWT.CENTER);
		ui.labelAvailableUnits.setText("Unit: 0");

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
				// runing network part ...
				System.out.println("Starting the server ...");
				// Running the server thread ...
				ui.serverThread = new Thread() {
					public void run() {
						Server server = new Server("config/server.config", "1");
						server.run();
					}
				};
				ui.serverThread.start();
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
				client.labelAvailableUnits.setText(RisikoData.AVALABLE_UNIT_TEXT + client.net.getAvailableUnit());
				client.net.synchronize();
			}

			if (state == ClientState.TURN_ASSIGNED) {
				System.out.println("Upating territories");
				updateGrid(client);
				client.net.synchronize();
			}

			if (state == ClientState.REINFORCEMENT) {
				if (client.net.isClientTurn()) {
					client.labelMsg.setText("Its your turn. " + RisikoData.DISPOSE_UNITS_TEXT);
					client.labelAvailableUnits.setText(RisikoData.AVALABLE_UNIT_TEXT + client.net.getAvailableUnit());
				} else {
					client.labelMsg.setText("Its " + client.net.getTurnOwner() + " turn. "
							+ RisikoData.DISPOSE_UNITS_TEXT);
				}
				client.net.synchronize();
			}

			if (state == ClientState.END_REINFORCEMENT) {
				updateGrid(client);
				if(client.net.isClientTurn()){
					client.labelMsg.setText("Its your turn. Attack one player.");
				}else{
					client.labelMsg.setText("Its " + client.net.getTurnOwner() + " turn owner (ATTACK PHASE).");
				}
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
		}
	}

	// TODO se viene usato solo in una funzione spostarla all'interno
	private String mapButtonMessage(int id, PlayerColor color, int unitNumber) {
		return "ID: " + id + " COLOR: " + color + " UNIT: " + unitNumber;
	}

	private void reset() {
		System.out.println("RESET - DA IMPLEMENTARE");
	}

	/**
	 * Lunch user interface
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		TestUI ui = new TestUI();

		ui.setClientConfigurationData("Client 1", "config/client_1.config", "2");
		ui.setClientConfigurationData("Client 2", "config/client_2.config", "3");
		ui.setClientConfigurationData("Client 3", "config/client_3.config", "4");

		ui.build();

		ui.update();

	}

}
