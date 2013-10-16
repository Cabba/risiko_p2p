package risiko.data;

import risiko.data.PlayerColor;

public class PlayerInfo {
	private String m_color;
	private int m_totalUnit;

	public PlayerInfo() {
	}

	public PlayerInfo(PlayerColor color, int totalUnit) {
		m_color = color.toString();
		m_totalUnit = totalUnit;
	}

	public String getColor() {
		return m_color;
	}

	public void setColor(PlayerColor color) {
		m_color = color.toString();
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
}