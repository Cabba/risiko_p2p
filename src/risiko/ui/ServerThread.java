package risiko.ui;

import risiko.net.Server;

public class ServerThread extends Thread{
	
	private Server m_server;
	private String m_path;
	private String m_key;
	
	public ServerThread(String path, String key){
		m_path = path;
		m_key = key;
	}
	
	public void run(){
		System.out.println("Running the server ...");
		m_server = new Server(m_path, m_key);
		m_server.run();
		System.out.println("Server terminted");
	}
	
	public void terminate(){
		m_server.halt();
		
	}
	
}
