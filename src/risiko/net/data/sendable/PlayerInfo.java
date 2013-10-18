package risiko.net.data.sendable;

import risiko.net.data.PlayerColor;
import risiko.net.gson.ISendable;

public class PlayerInfo implements ISendable{
	
	private PlayerColor m_color;
	private int m_totalUnit;
	
	public static final String PLAYER_COLOR_MSG = "player_info";

	public PlayerInfo() {
		m_color = PlayerColor.NONE;
	}

	public PlayerInfo(PlayerColor color, int totalUnit) {
		m_color = color;
		m_totalUnit = totalUnit;
	}

	public PlayerColor getColor() {
		return m_color;
	}

	public void setColor(PlayerColor color) {
		m_color = color;
	}

	public int getTotalUnit() {
		return m_totalUnit;
	}

	public void setTotalUnit(int totalUnit) {
		m_totalUnit = totalUnit;
	}
	
	public void incrementTotalUnit(int increment){
		m_totalUnit += increment;
	}

	@Override
	public String getType() {
		return PLAYER_COLOR_MSG;
	}
}