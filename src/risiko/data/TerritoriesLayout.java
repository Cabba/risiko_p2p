package risiko.data;

import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TerritoriesLayout extends Hashtable<String, TerritoryInfo> {

	private static final long serialVersionUID = 1L;

	public TerritoriesLayout() {
		super();
	}

	public TerritoriesLayout(Map<String, ? extends TerritoryInfo> map) {
		super(map);
	}

	public TerritoriesLayout(List<TerritoryInfo> list) {
		super();
		for (int i = 0; i < list.size(); ++i) {
			put(Integer.toString(i), list.get(i));
		}
	}
	
	/**
	 * This method push a new value in the Hashtable using the filed id of TerritoryInfo
	 * as key.
	 */
	public void put(TerritoryInfo territory){
		put(territory.toString(), territory);
	}

	synchronized public boolean readList(InputStream istream) {
		// read the stream
		BufferedReader buffer = new BufferedReader(new InputStreamReader(istream));

		try {

			// create a json object
			JSONObject jsonObj = new JSONObject(buffer.readLine());
			buffer.close();

			JSONObject params;
			Iterator<String> territoriesKeys = jsonObj.keys();

			// parse json peer list
			while (territoriesKeys.hasNext()) {

				String key = territoriesKeys.next();

				params = jsonObj.getJSONObject(key);

				TerritoryInfo territory = new TerritoryInfo(0, 0, PlayerColor.NONE);

				territory.setId(params.getInt("id"));
				territory.setUnitNumber(params.getInt("unitNumber"));
				territory.setOwner(PlayerColor.valueOf(params.getString("owner")));

				this.put(Integer.toString(territory.getId()), territory);

			}

		} catch (IOException e) {
			return false;
		} catch (JSONException e) {
			return false;
		}

		return true;
	}

	
	synchronized public boolean writeList(OutputStream ostream) {
		
		try {
			
			JSONObject territories = new JSONObject(this);

			PrintStream printTerritories = new PrintStream(ostream);
			printTerritories.println(territories.toString());
			printTerritories.close();
			

		} catch (Exception e) {
			new RuntimeException(e);
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
