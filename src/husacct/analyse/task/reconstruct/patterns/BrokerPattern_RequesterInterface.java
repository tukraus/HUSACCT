package husacct.analyse.task.reconstruct.patterns;

public class BrokerPattern_RequesterInterface extends BrokerPattern {
	// In this interpretation, the Requester acts as an interface for the other two modules.
	public BrokerPattern_RequesterInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("Requester", "Broker", "IsOnlyAllowedToUse", "Provider");
		addSingleRule("Provider", "Broker", "IsTheOnlyModuleAllowedToUse", null);
		addSingleRule("Broker", "Provider", "IsOnlyAllowedToUse", null);
		addSingleRule("Broker", "Requester", "IsTheOnlyModuleAllowedToUse", "Provider");
	}
}
