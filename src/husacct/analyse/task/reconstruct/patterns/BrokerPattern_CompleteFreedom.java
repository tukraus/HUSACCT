package husacct.analyse.task.reconstruct.patterns;

public class BrokerPattern_CompleteFreedom extends BrokerPattern {
	// The version of the Broker pattern in which there are no restrictions with regard to the Remainder (so dependencies to and from are allowed).
	public BrokerPattern_CompleteFreedom() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("Requester", "Provider", "IsNotAllowedToUse", null);
		addSingleRule("Provider", "Requester", "IsNotAllowedToUse", null);
	}

}
