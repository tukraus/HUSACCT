package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_ControllerInterface extends MVCPattern {
	// Using IsTheOnlyModuleAllowedToUse rules, this version of the Model-View-Controller pattern has the Controller module act as an interface for
	// the other two.
	public MVCPattern_ControllerInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "IsTheOnlyModuleAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "IsTheOnlyModuleAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "IsOnlyAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "IsOnlyAllowedToUse");
	}

}
