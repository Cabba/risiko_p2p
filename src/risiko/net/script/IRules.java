package risiko.net.script;

import risiko.net.data.TerritoryInfo;
import risiko.net.data.sendable.AttackData;
import risiko.net.data.sendable.PlayerInfo;
import risiko.net.data.sendable.TerritoriesLayout;

// TODO vedere se spostare il numero di partecipanti nello script e la posizione dello script nel file di configurazione del server
public interface IRules {

	public boolean checkTerritoriesLayout(TerritoriesLayout oldlayout, TerritoriesLayout newLayout, PlayerInfo owner);

	public boolean isValidAttack(int ter1, int ter2, AttackData attack, TerritoriesLayout layout);

	public boolean isValidDefence(int ter1, int ter2, AttackData attack, TerritoriesLayout layout);

	public int getDiceValue();

	public int getInitUnits(int playerNumber);

	public int getReinforcementUnits(TerritoriesLayout layout);

}
