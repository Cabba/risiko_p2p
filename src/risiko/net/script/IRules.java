package risiko.net.script;

import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.TerritoriesLayout;

public interface IRules {

	public boolean checkTerritoriesLayout(TerritoriesLayout layout, PlayerInfo owner);
	
}
