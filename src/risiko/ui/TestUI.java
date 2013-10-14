package risiko.ui;

import java.util.ArrayList;
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

import risiko.data.RisikoData;
import risiko.data.PlayerColor;
import risiko.net.Client;
import risiko.net.Server;

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
		public TabItem clientTab;

		public FillLayout mainLayout;

		public Composite mainComposite;
		public Composite mapComposite;

		public Group mapGroup;
		public Group actionGroup;
		public Group infoGroup;

		public Button lunchClientButton;

		public Label playerColorLabel;
		public Label availableUnitLabel;

		// Networking
		public Client net;
	}

	List<ClientUI> m_clients;

	private class ServerUI {
		public TabItem serverTab;
		public Button startServer;

		public Server net;
	}

	private ServerUI m_server;

	public TestUI() {
		m_display = new Display();
	}

	public void build() {
		m_shell = new Shell(m_display);
		m_shell.setText("Risiko Test Interface");

		m_shell.setLayout(new FillLayout());

		// One client
		TabFolder folder = new TabFolder(m_shell, SWT.BORDER);

		m_clients = new ArrayList<ClientUI>();
		for (int i = 0; i < m_clientInfo.size(); ++i) {
			m_clients.add(createClientUI(folder, m_clientInfo.get(i)));
		}
		m_server = createServerUI(folder);

		folder.pack();

		m_shell.open();
	}

	// TODO vedere se si riesce a spostare tutti i gruppi fuori dalla classe
	// ClientUI dato che servono
	// solo in fase di inizializzazione dell'interfaccia
	private ClientUI createClientUI(TabFolder folder, ClientInfo info) {
		ClientUI ui = new ClientUI();

		System.out.println("Create client: " + info.name + "...");
		ui.net = new Client(info.configPath, info.key);

		ui.clientTab = new TabItem(folder, SWT.NONE);
		ui.clientTab.setText(info.name);

		ui.mainLayout = new FillLayout();
		ui.mainLayout.type = SWT.VERTICAL;

		ui.mainComposite = new Composite(folder, SWT.NONE);
		ui.mainComposite.setLayout(ui.mainLayout);

		ui.mapGroup = new Group(ui.mainComposite, SWT.NONE);
		ui.mapGroup.setText("Map");
		ui.mapGroup.setLayout(ui.mainLayout);

		ui.mapComposite = new Composite(ui.mapGroup, SWT.NONE);
		ui.mapComposite.setLayout(new GridLayout(RisikoData.mapColumns, true));
		GridData buttonGrid = new GridData(SWT.FILL, SWT.FILL, true, true);
		for (int i = 0; i < RisikoData.mapColumns * RisikoData.mapRows; ++i) {
			Button button = new Button(ui.mapComposite, SWT.PUSH);
			button.setLayoutData(buttonGrid);
		}

		ui.actionGroup = new Group(ui.mainComposite, SWT.NONE);
		ui.actionGroup.setText("Actions");
		ui.actionGroup.setLayout(ui.mainLayout);

		ui.infoGroup = new Group(ui.mainComposite, SWT.NONE);
		ui.infoGroup.setText("Info");
		ui.infoGroup.setLayout(ui.mainLayout);

		Composite infoComposite = new Composite(ui.infoGroup, SWT.NONE);
		FillLayout infoCompositeLayout = new FillLayout();
		infoCompositeLayout.type = SWT.HORIZONTAL;
		infoComposite.setLayout(infoCompositeLayout);

		ui.playerColorLabel = new Label(infoComposite, SWT.BORDER | SWT.CENTER);
		ui.playerColorLabel.setText("NONE");
		
		ui.availableUnitLabel = new Label(infoComposite, SWT.BORDER | SWT.CENTER );
		ui.availableUnitLabel.setText("Unit: 0");

		ui.lunchClientButton = new Button(infoComposite, SWT.PUSH);
		ui.lunchClientButton.setText(RisikoData.CONNECT_TEXT);
		ui.lunchClientButton.setData(ui);
		ui.lunchClientButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ClientUI ui = (ClientUI) e.widget.getData();
				// running network side
				ui.net.connect();
				ui.lunchClientButton.setEnabled(false);
			}
		});

		ui.clientTab.setControl(ui.mainComposite);

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
				ui.net = new Server("config/server.config", "1");
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

			// Server updates
			if (!m_shell.isDisposed())
				serverLogic(m_server);

			for (int i = 0; i < m_clients.size(); ++i) {
				if (!m_shell.isDisposed())
					clientLogic(m_clients.get(i));
			}
		}
		m_display.dispose();
	}

	private boolean m_serverInit = false;

	private void serverLogic(ServerUI server) {
		if (server.net != null) {
			if (server.net.gameCanStart()) {
				server.net.startGame();
				server.startServer.setText(RisikoData.GAME_STARTED_TEXT);
			}
			if (server.net.isGameStarted()) {
				if (!m_serverInit) {
					server.net.assignIDToClients();
					server.net.assignTerritoryToClients();
					m_serverInit = true;
				}
			}
		}
	}

	private void clientLogic(ClientUI client) {
		// Lunch button states
		if (client.lunchClientButton.getText() == RisikoData.CONNECT_TEXT) {
			if (client.net.isConnected())
				client.lunchClientButton.setText(RisikoData.CONNECTED_TEXT);
			if (client.net.isConnectionRefused()) {
				client.lunchClientButton.setEnabled(true);
			}
		}
		if (client.lunchClientButton.getText() == RisikoData.CONNECTED_TEXT) {
			if (client.net.isGameStarted())
				client.lunchClientButton.setText(RisikoData.GAME_STARTED_TEXT);
		}
		if (client.lunchClientButton.getText() == RisikoData.GAME_STARTED_TEXT) {

		}

		if (client.net.isGameStarted()) {

			if (client.net.getColor() != PlayerColor.NONE) {
				client.playerColorLabel.setText(client.net.getColor().toString());
				client.availableUnitLabel.setText("Unit: " + client.net.getAvailableUnit());
			}
		}

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
