package risiko.net.script;

import risiko.net.script.IRules;
import risiko.net.data.sendable.TerritoriesLayout;
import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.PlayerInfo;

public class Rules implements IRules{

	public boolean checkTerritoriesLayout(TerritoriesLayout layout, PlayerInfo owner){
		println "CHECK TERRITORIES LAYOUT - DA IMPLEMENTARE";
		return true;
	}
	
	public boolean isValidAttack(TerritoryInfo ter1, TerritoryInfo ter2, TerritoriesLayout layout){
		println "IS A VALID ATTACK - DA IMPLEMENTARE";
	}
	
	public int getDiceValue(){
		return 1;
	}
}