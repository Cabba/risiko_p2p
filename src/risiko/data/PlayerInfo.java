package risiko.data;

import risiko.data.PlayerColor;

public class PlayerInfo{
	private String m_color;
	private int m_totalUnit;
	
	public PlayerInfo(){}
	public PlayerInfo(PlayerColor color){ m_color = color.toString(); }
	
	public String getColor(){ return m_color; }
	public void setColor(PlayerColor color){ m_color = color.toString(); }
	
	public int getTotalUnit(){ return m_totalUnit; }
	public void setTotalUnit(int totalUnit){ m_totalUnit = totalUnit; }
}