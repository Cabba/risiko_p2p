package risiko.net.script;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import risiko.net.script.IRules;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.AttackData;

public class Rules implements IRules{

	Random random;

	public Rules(){
		random = new Random();
	}

	public boolean checkTerritoriesLayout(TerritoriesLayout oldlayout, TerritoriesLayout newLayout, PlayerInfo owner){
		println "CHECK TERRITORIES LAYOUT - DA IMPLEMENTARE";
		return true;
	}
	
	public boolean isValidAttack(AttackData attack, TerritoriesLayout layout){
		println "IS A VALID ATTACK - DA IMPLEMENTARE";
		return true;
	}

	public boolean isValidDefence(AttackData attack, TerritoriesLayout layout){
		println "IS A VALID DEFENCE - DA IMPLEMENTARE";
		return true;
	}

	public int attackerUnitsDestroyed(AttackData attack){
		println "Attack evaluation";
		List<Integer> atta = new ArrayList<Integer>(attack.getAttackValues());
		List<Integer> defe = new ArrayList<Integer>(attack.getDefenceValues());

		int counter = 0;
		int iterations = defe.size() <= atta.size() ? defe.size() : atta.size();
		for(int i = 0; i < iterations; ++i ){
			Integer maxAttack = findMaxAndPush(atta);
			Integer maxDefence = findMaxAndPush(defe);
			println "Comparing " + maxDefence + " with " + maxAttack;
			if( maxDefence >= maxAttack ) counter++;
		}
		return counter;
	}
	
	public int attackedUnitsDestoyed(AttackData attack){
		println "Defence evaluation";
		List<Integer> atta = new ArrayList<Integer>(attack.getAttackValues());
		List<Integer> defe = new ArrayList<Integer>(attack.getDefenceValues());

		println "attack size = " + atta.size() + " defence size = " + defe.size();

		int counter = 0;
		int iterations = defe.size() <= atta.size() ? defe.size() : atta.size();
		for(int i = 0; i < iterations; ++i ){
			Integer maxAttack = findMaxAndPush(atta);
			Integer maxDefence = findMaxAndPush(defe);
			println "Comparing " + maxDefence + " with " + maxAttack;
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
		return random.nextInt(5) + 1;
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
	
	public int getReinforcementUnits(TerritoriesLayout layout){
		return 0;
	}
}