package risiko.net.script;

import java.util.Random;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import risiko.net.script.IRules;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.PlayerColor;
import risiko.net.data.sendable.AttackData;

public class Rules implements IRules{

	private Random m_random;

	public Rules(){
		m_random = new Random();
	}

	// Check if all the territories are owned by only one player 
	public PlayerColor getWinner(TerritoriesLayout layout){
		Iterator<Integer> iter = layout.keySet().iterator();
		PlayerColor owner = layout.get(iter.next()).getOwner();
		while(iter.hasNext()){
			Integer key = iter.next();
			if(layout.get(key).getOwner() != owner){
				return PlayerColor.NONE;
			}
		}
		return owner;
	}

	public boolean checkTerritoriesLayout(TerritoriesLayout oldLayout, TerritoriesLayout newLayout, PlayerInfo owner){
		println "Cheking territories ..." + owner.getColor();
		if (oldLayout.getPlayerUnits(owner.getColor()) > newLayout.getPlayerUnits(owner.getColor()) ){
			println "Bad territories configuration";
			return false;
		}
		println "Configuration OK!";

		return true;
	}

	public boolean isNear(int id1, int id2){
		if( id1 == id2 + 1 || id1 == id2 - 1 || id1 == id2 - 3 || id1 == id2 + 3) return true;
		else return false;
	}
	
	public boolean isValidAttack(AttackData attack, TerritoriesLayout layout, PlayerColor player){
		int attID = attack.getAttackerID();
		int defID = attack.getAttackedID();
		int attUnits = attack.getAttackValues().size();

		if( layout.get(attID).getOwner() != player ){
			println "The attacker isn't the owner of this territory.";
			return false;
		}

		if(layout.get(attID).getOwner() == layout.get(defID).getOwner()){
			println "Territories have the same owner.";
			return false;
		}
		// Sufficient units
		if( layout.get(attID).getUnitNumber() - attUnits < 1 ){
			println "Insufficient units";
			return false;
		}

		// Rules of proximity
		if( !isNear(attID, defID) ){
			println "Territories are not near.";
			return false; 
		}
		return true;
	}

	public boolean isValidDefence(AttackData attack, TerritoriesLayout layout){
		println "IS A VALID DEFENCE - DA IMPLEMENTARE";
		return true;
	}

	public int attackerUnitsDestroyed(AttackData attack){
		List<Integer> atta = new ArrayList<Integer>(attack.getAttackValues());
		List<Integer> defe = new ArrayList<Integer>(attack.getDefenceValues());

		int counter = 0;
		int iterations = defe.size() <= atta.size() ? defe.size() : atta.size();
		for(int i = 0; i < iterations; ++i ){
			Integer maxAttack = findMaxAndPush(atta);
			Integer maxDefence = findMaxAndPush(defe);
			if( maxDefence >= maxAttack ) counter++;
		}
		return counter;
	}

	public int attackedUnitsDestroyed(AttackData attack){
		List<Integer> atta = new ArrayList<Integer>(attack.getAttackValues());
		List<Integer> defe = new ArrayList<Integer>(attack.getDefenceValues());

		int counter = 0;
		int iterations = defe.size() <= atta.size() ? defe.size() : atta.size();
		for(int i = 0; i < iterations; ++i ){
			Integer maxAttack = findMaxAndPush(atta);
			Integer maxDefence = findMaxAndPush(defe);
			if( maxAttack > maxDefence ) counter++;
		}
		return counter;
	}

	private Integer findMaxAndPush(List<Integer> list){
		Integer max = 0;
		int index = 0;
		for(int i = 0; i < list.size(); ++i){
			if( max < list.get(i) ){
				max = list.get(i);
				index = i;
			}
		}
		list.remove(index);
		return max;
	}
	
	public int getDiceValue(){
		return m_random.nextInt(5) + 1;
	}

	public int getInitUnits(int playerNumber){
		if(playerNumber == 2)
			return 40;
		else if(playerNumber == 3)
			return 35;
		else if (playerNumber == 4)
			return 30;
		else if (playerNumber == 5)
			return 25;
		else if (playerNumber == 6)
			return 20;
		return 0;
	}
	
	public int getReinforcementUnits(int turnCounter, TerritoriesLayout layout){
		if( turnCounter == 1 ) return 0;
		else return 1;
	}

	public int getRequiredPlayerNumber(){
		return 3;
	}
}