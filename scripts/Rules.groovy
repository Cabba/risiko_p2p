package risiko.net.script;

import java.util.Random;
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
	
	public boolean isValidAttack(int ter1, int ter2, AttackData attack, TerritoriesLayout layout){
		println "IS A VALID ATTACK - DA IMPLEMENTARE";
		return true;
	}

	public boolean isValidDefence(int ter1, int ter2, AttackData attack, TerritoriesLayout layout){
		println "IS A VALID DEFENCE - DA IMPLEMENTARE";
		return true;
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
		return 1;
	}
}