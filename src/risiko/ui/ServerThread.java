package risiko.ui;

import risiko.net.Server;

public class ServerThread extends Thread{
	
	private Server m_server;
	private String m_path;
	private String m_key;
	
	volatile boolean m_running = false;
	
	public ServerThread(String path, String key){
		m_path = path;
		m_key = key;
	}
	
	public void run(){
		System.out.println("Running the server ...");
		m_running = true;
		m_server = new Server(m_path, m_key);
		m_server.run();
		m_running = false;
		System.out.println("Server terminted");
	}
	
	synchronized public boolean isRunning(){
		return m_running;
	}
	
	public void terminate(){
		m_server.halt();
	}
	
}
