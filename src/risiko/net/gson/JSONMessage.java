package risiko.net.gson;

public class JSONMessage<Param extends ISendable> {

	private String type;
	private long timestamp;
	private Payload<Param> payload;

	public JSONMessage(Param body) {
		timestamp = System.currentTimeMillis();
		type = body.getType();
		payload = new Payload<Param>(body);
	}

	public Payload<Param> getPayload() {
		return payload;
	}

}
