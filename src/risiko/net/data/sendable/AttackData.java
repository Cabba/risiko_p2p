package risiko.net.data.sendable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import risiko.net.gson.ISendable;
import risiko.net.gson.JSONTranslator;

public class AttackData implements ISendable{

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

	public String getType(){
		return ATTACK_DATA_MSG;
	}
	
}
