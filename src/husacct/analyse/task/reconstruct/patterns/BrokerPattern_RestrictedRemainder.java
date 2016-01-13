package husacct.analyse.task.reconstruct.patterns;

public class BrokerPattern_RestrictedRemainder extends BrokerPattern {
	// In this variety of the Broker Pattern, the pattern modules are free to call on the Remainder.
	public BrokerPattern_RestrictedRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("Broker", "Requester", "IsTheOnlyModuleAllowedToUse", "Provider");// Exception:provider
		addSingleRule("Requester", "Broker", "IsTheOnlyModuleAllowedToUse", null);
		addSingleRule("Provider", "Broker", "IsTheOnlyModuleAllowedToUse", null);
	}

}
