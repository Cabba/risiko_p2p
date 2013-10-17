package risiko.net.gson;

public class JSONTranslator<Param extends ISendable> {

	private String type;
	private long timestamp;
	private JSONPayload<Param> payload;

	public JSONTranslator(Param body) {
		timestamp = System.currentTimeMillis();
		type = body.getType();
		payload = new JSONPayload<Param>(body);
	}

	public JSONPayload<Param> getPayload() {
		return payload;
	}

}
