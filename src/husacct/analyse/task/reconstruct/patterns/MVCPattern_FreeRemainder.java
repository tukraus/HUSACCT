package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_FreeRemainder extends MVCPattern {
	// The rest of the architecture can access the Model and View modules, but they are themselves restricted to using the Controller.
	public MVCPattern_FreeRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "MustUse");

		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "IsOnlyAllowedToUse",
				moduleService.getModuleByLogicalPath("Model"));
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "IsOnlyAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "IsOnlyAllowedToUse");
	}

}
