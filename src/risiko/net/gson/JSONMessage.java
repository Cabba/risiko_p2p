package risiko.net.gson;

import risiko.data.AttackData;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class JSONMessage<Param> {

	private String type;
	private long timestamp;
	private Payload<Param> payload;

	public JSONMessage(Param body, String messageType) {
		timestamp = System.currentTimeMillis();
		type = messageType;
		payload = new Payload<Param>(body);
	}

	public Payload<Param> getPayload() {
		return payload;
	}

}
