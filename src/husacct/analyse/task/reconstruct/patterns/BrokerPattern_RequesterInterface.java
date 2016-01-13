package husacct.analyse.task.reconstruct.patterns;

public class BrokerPattern_RequesterInterface extends BrokerPattern {
	// In this interpretation, the Requester acts as an interface for the other two modules.
	public BrokerPattern_RequesterInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("Broker"), moduleService.getModuleByLogicalPath("Requester"), "IsTheOnlyModuleAllowedToUse",
				moduleService.getModuleByLogicalPath("Provider")); // Overloaded addRule() method takes exception argument as well.
		addRule(moduleService.getModuleByLogicalPath("Requester"), moduleService.getModuleByLogicalPath("Broker"), "IsOnlyAllowedToUse",
				moduleService.getModuleByLogicalPath("Provider"));
	}
}
