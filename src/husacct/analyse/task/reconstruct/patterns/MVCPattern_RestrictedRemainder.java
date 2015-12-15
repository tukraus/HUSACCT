package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_RestrictedRemainder extends MVCPattern {
	// A variety of the MVC pattern in which Model and View can call upon the Remainder, but the Remainder cannot call on them.
	public MVCPattern_RestrictedRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "IsTheOnlyModuleAllowedToUse",
				moduleService.getModuleByLogicalPath("Model"));
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "IsTheOnlyModuleAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "IsTheOnlyModuleAllowedToUse");
	}

}
