package risiko.net.data.sendable;

import risiko.net.data.PlayerColor;
import risiko.net.gson.ISendable;

public class Winner implements ISendable {

	private PlayerColor m_winner;
	public static final String WINNER_MSG = "winner";
	
	public Winner(PlayerColor winner){
		m_winner = winner;
	}
	
	public PlayerColor getWinner(){
		return m_winner;
	}
	
	public String getType(){
		return WINNER_MSG;
	}
	
}
