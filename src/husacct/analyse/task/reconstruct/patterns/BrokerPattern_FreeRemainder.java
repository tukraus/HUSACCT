package husacct.analyse.task.reconstruct.patterns;

public class BrokerPattern_FreeRemainder extends BrokerPattern {
	// This version of the Broker pattern allows the Remainder to access any of the three modules, but they can themselves not call upon the rest of
	// the architecture.
	public BrokerPattern_FreeRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("Broker", "Requester", "IsOnlyAllowedToUse", null);
		addSingleRule("Requester", "Broker", "IsOnlyAllowedToUse", "Provider");
		addSingleRule("Provider", "Broker", "IsOnlyAllowedToUse", "Requester");
		addSingleRule("Broker", "Provider", "IsOnlyAllowedToUse", null);
	}

}
