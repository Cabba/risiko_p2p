package risiko.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import risiko.data.RisikoData;
import risiko.net.Client;
import risiko.net.Server;

public class TestUI {

	private Display m_display;
	private Shell m_shell;
	
	private class ClientUI{
		public TabItem clientTab;
		
		public FillLayout mainLayout;
		
		public Composite mainComposite;
		public Composite mapComposite;
		
		public Group mapGroup;
		public Group actionGroup;
		public Group infoGroup;
		
		// Networking
		public Client net;
	}
	List<ClientUI> m_clients;
	
	private class ServerUI{
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
		m_clients.add( createClientUI(folder, "Client 1"));
		m_clients.add( createClientUI(folder, "Client 2"));
		
		m_server = createServerUI(folder);
		
		
		folder.pack();
		
		m_shell.open();
	}
	
	private ClientUI createClientUI(TabFolder folder, String name){
		ClientUI ui = new ClientUI();
		
		ui.clientTab = new TabItem(folder, SWT.NONE);
		ui.clientTab.setText(name);
	
		
		ui.mainLayout = new FillLayout();
		ui.mainLayout.type = SWT.VERTICAL;
		
		ui.mainComposite = new Composite(folder, SWT.NONE);
		ui.mainComposite.setLayout(ui.mainLayout);
		
		ui.mapGroup = new Group(ui.mainComposite,SWT.NONE );
		ui.mapGroup.setText("Map");
		ui.mapGroup.setLayout(ui.mainLayout);
		
		ui.mapComposite = new Composite(ui.mapGroup, SWT.NONE);
		ui.mapComposite.setLayout(new GridLayout(RisikoData.mapColumns, true));
		GridData buttonGrid = new GridData(SWT.FILL, SWT.FILL, true, true);
		for(int i = 0; i < RisikoData.mapColumns*RisikoData.mapRows; ++i){
			Button button = new Button(ui.mapComposite, SWT.PUSH);
			button.setLayoutData(buttonGrid);
		}
		
		
		ui.actionGroup  = new Group(ui.mainComposite, SWT.NONE);
		ui.actionGroup.setText("Actions");
		ui.actionGroup.setLayout(ui.mainLayout);
		
		Group infoGroup = new Group(ui.mainComposite, SWT.NONE);
		infoGroup.setText("Info");
		infoGroup.setLayout(ui.mainLayout);
		
		ui.clientTab.setControl(ui.mainComposite);
		
		return ui;
	}
	
	private ServerUI createServerUI(TabFolder folder){
		ServerUI ui = new ServerUI();
		ui = new ServerUI();
		
		ui.serverTab = new TabItem(folder, SWT.BORDER);
		ui.serverTab.setText("Server");
		
		ui.startServer = new Button(folder, SWT.PUSH);
		ui.startServer.setText("Run server");
		ui.serverTab.setControl(ui.startServer);
		
		return ui;
		
	}

	public void update() {
		while (!m_shell.isDisposed()) {
			if (!m_display.readAndDispatch()) {				
				m_display.sleep();
			}
		}
		m_display.dispose();
	}

	/**
	 * Lunch user interface
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		TestUI ui = new TestUI();

		/*
		 * ui.setClientConfigurationData("Client 1", "config/client_1.config", "2");
		 * ui.setClientConfigurationData("Client 2", "config/client_2.config", "3");
		 * ui.setClientConfigurationData("Client 3", "config/client_3.config", "4");
		 */

		ui.build();

		ui.update();

	}

}
