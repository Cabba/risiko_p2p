package risiko.net.data.sendable;

import java.util.List;

import risiko.net.data.AttackPhase;
import risiko.net.gson.ISendable;

public class AttackData implements ISendable{

	private List<Integer> m_attacks;
	private List<Integer> m_defence;
	
	private int m_fromTerritory;
	private int m_toTerritory;
	
	private AttackPhase m_phase;
	
	public static final String ATTACK_DATA_MSG = "attack_data";
	
	public AttackData(int from, int to, List<Integer> attackValues){
		m_attacks = attackValues;
		m_toTerritory = to;
		m_fromTerritory = from;
		m_phase = AttackPhase.ATTACK;
	}
	
	public void setDefenceValues(List<Integer> defenceValues){
		m_defence = defenceValues;
		m_phase = AttackPhase.DEFENCE;
	}
	
	public List<Integer> getAttackValues(){
		return m_attacks;
	}
	
	public List<Integer> getDefenceValues(){
		return m_defence;
	}
	
	public int getAttackedID(){
		return m_toTerritory;
	}
	
	public int getAttackerID() {
		return m_fromTerritory;
	}
	
	public int getAttackingUnits(){
		return m_attacks.size();
	}
	
	public AttackPhase getPhase(){
		return m_phase;
	}
	
	@Override
	public String toString() {
		return m_attacks.toString() + " " + m_defence.toString();
	}

	public String getType(){
		return ATTACK_DATA_MSG;
	}
	
}
