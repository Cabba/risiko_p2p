package risiko.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import risiko.net.gson.JSONMessage;

public class AttackData {

	List<Integer> m_attacks;
	List<Integer> m_defence;
	
	public static final String ATTACK_DATA_MSG = "attack_data";
	
	public AttackData(int prova){
		m_attacks = Arrays.asList(1, 2, 3, 4);
		m_defence = Arrays.asList(5, 6, 7, 8);
	}
	
	@Override
	public String toString() {
		return m_attacks.toString() + " " + m_defence.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JSONMessage example = new JSONMessage<AttackData>(new AttackData(5), "attack_data");
		Gson gson = new Gson();
		String msg = gson.toJson(example);
		System.out.println(msg);
		
		Type fooType = new TypeToken<JSONMessage<AttackData>>(){}.getType();
		JSONMessage<AttackData> example2 = gson.fromJson(msg, fooType);
		AttackData attack = example2.getPayload().getParams();
		
	}
	
}
