package risiko.net.data.sendable;

import java.util.List;

import risiko.net.gson.ISendable;

public class AttackData implements ISendable{

	private List<Integer> m_attacks;
	private List<Integer> m_defence;
	
	private int m_attackerTerritory;
	private int m_attackedTerritory;
	
	public static final String ATTACK_DATA_MSG = "attack_data";
	
	public AttackData(List<Integer> attackValues, int attacker, int attacked){
		m_attacks = attackValues;
		m_attackedTerritory = attacked;
		m_attackerTerritory = attacker;
	}
	
	public void setDefenceValues(List<Integer> defenceValues){
		m_defence = defenceValues;
	}
	
	public List<Integer> getAttackValues(){
		return m_attacks;
	}
	
	public List<Integer> getDefenceValues(){
		return m_defence;
	}
	
	public int getAttackedID(){
		return m_attackedTerritory;
	}
	
	public int getAttackerID() {
		return m_attackerTerritory;
	}
	
	@Override
	public String toString() {
		return m_attacks.toString() + " " + m_defence.toString();
	}

	public String getType(){
		return ATTACK_DATA_MSG;
	}
	
}
