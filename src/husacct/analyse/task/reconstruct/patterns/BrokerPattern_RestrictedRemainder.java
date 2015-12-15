package husacct.analyse.task.reconstruct.patterns;

public class BrokerPattern_RestrictedRemainder extends BrokerPattern {
	// In this variety of the Broker Pattern, the pattern modules are free to call on the Remainder.
	public BrokerPattern_RestrictedRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("Provider"), moduleService.getModuleByLogicalPath("Broker"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Broker"), moduleService.getModuleByLogicalPath("Requester"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Broker"), moduleService.getModuleByLogicalPath("Requester"), "IsTheOnlyModuleAllowedToUse",
				moduleService.getModuleByLogicalPath("Provider"));// Exception:provider
		addRule(moduleService.getModuleByLogicalPath("Requester"), moduleService.getModuleByLogicalPath("Broker"), "IsTheOnlyModuleAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Provider"), moduleService.getModuleByLogicalPath("Broker"), "IsTheOnlyModuleAllowedToUse");
	}

}
