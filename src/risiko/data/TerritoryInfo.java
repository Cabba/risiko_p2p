package risiko.data;

import java.util.List;

public class TerritoryInfo{
	private int m_id;
	private int m_unitNumber;
	private String m_owner;
	
	public TerritoryInfo(int id, int unitNumber, PlayerColor owner){
		m_id = id;
		m_unitNumber = unitNumber;
		m_owner = owner.toString();
	}
	
	public int getId(){ return m_id; }
	public void setId(int id){ m_id = id; }
	
	public int getUnitNumber(){ return m_unitNumber; }
	public void setUnitNumber(int unitNumber){ m_unitNumber = unitNumber; }
	
	public String getOwner(){ return m_owner; }
	public void setOwner(PlayerColor owner){ m_owner = owner.toString(); }
}
