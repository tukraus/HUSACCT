package husacct.analyse.task.reconstruct;

public class MVCPattern_CompleteFreedom extends MVCPattern {
	// The version of the MVC pattern in which Model is not allowed to use View, nothing else is restricted.
	public MVCPattern_CompleteFreedom() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("View"), "IsNotAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Model"), "IsNotAllowedToUse");
	}

}
