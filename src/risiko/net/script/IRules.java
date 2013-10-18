package risiko.net.script;

import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.TerritoriesLayout;

public interface IRules {

	public boolean checkTerritoriesLayout(TerritoriesLayout layout, PlayerInfo owner);
	
	public boolean isValidAttack(TerritoryInfo ter1, TerritoryInfo ter2, TerritoriesLayout layout);
	
	public int getDiceValue();
	
}
