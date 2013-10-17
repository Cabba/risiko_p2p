package risiko.net.data.sendable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import risiko.net.data.PlayerColor;
import risiko.net.data.TerritoryInfo;
import risiko.net.gson.ISendable;

public class TerritoriesLayout implements ISendable{

	private HashMap<Integer, TerritoryInfo> m_territories;
	public static final String TERRITORIES_LAYOUT_MSG = "territories_layout";

	public TerritoriesLayout() {
		m_territories = new HashMap<Integer, TerritoryInfo>();
	}

	public TerritoriesLayout(List<TerritoryInfo> list) {
		for (int i = 0; i < list.size(); ++i) {
			m_territories.put(list.get(i).getId(), list.get(i));
		}
	}
	
	public void put(TerritoryInfo territory){
		m_territories.put(territory.getId(), territory);
	}
	
	public TerritoryInfo get(int id){
		return m_territories.get(id);
	}
	
	public void clear(){
		m_territories.clear();
	}
	
	public Set<Integer> keySet(){
		return m_territories.keySet();
	}
	
	public TerritoriesLayout getSubset(PlayerColor owner){
		TerritoriesLayout res = new TerritoriesLayout();
		Iterator<Integer> iter = keySet().iterator();
		while(iter.hasNext()){
			Integer key = iter.next();
			if(m_territories.get(key).getOwner() == owner){
				res.put(m_territories.get(key));
			}
		}
		return res;
	}
	
	public int getPlayerUnit(PlayerColor owner){
		int count = 0;
		Iterator<Integer> iter = keySet().iterator();
		while(iter.hasNext()){
			Integer key = iter.next();
			if(m_territories.get(key).getOwner() == owner){
				count += m_territories.get(key).getUnitNumber();
			}
		}
		return count;
	}
	
	public void updateTerritory(int id, int newUnitNumber, PlayerColor newOwner){
		TerritoryInfo terr = m_territories.get(id);
		terr.setOwner(newOwner);
		terr.setUnitNumber(newUnitNumber);
		m_territories.put(id, terr);
	}

	@Override
	public String getType() {
		return TERRITORIES_LAYOUT_MSG;
	}

}
