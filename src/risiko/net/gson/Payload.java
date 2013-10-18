package risiko.net.gson;

public class Payload<Param extends ISendable> {

	private Param params;

	public Payload(Param params) {
		this.params = params;
	}

	public Param getParams(){ return params; }
}
