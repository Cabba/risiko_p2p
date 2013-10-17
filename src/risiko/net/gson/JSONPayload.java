package risiko.net.gson;

public class JSONPayload<Param extends ISendable> {

	private Param params;

	public JSONPayload(Param params) {
		this.params = params;
	}

	public Param getParams(){ return params; }
}
