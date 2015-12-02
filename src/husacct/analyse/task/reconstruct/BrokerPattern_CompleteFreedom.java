package husacct.analyse.task.reconstruct;

public class BrokerPattern_CompleteFreedom extends BrokerPattern {
	// The version of the Broker pattern in which there are no restrictions with regard to the Remainder (so dependencies to and from are allowed).
	public BrokerPattern_CompleteFreedom() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("Provider"), moduleService.getModuleByLogicalPath("Broker"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Broker"), moduleService.getModuleByLogicalPath("Requester"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Requester"), moduleService.getModuleByLogicalPath("Provider"), "IsNotAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Provider"), moduleService.getModuleByLogicalPath("Requester"), "IsNotAllowedToUse");
	}

}
