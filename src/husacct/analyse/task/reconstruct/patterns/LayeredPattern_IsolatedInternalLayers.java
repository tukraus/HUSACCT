package husacct.analyse.task.reconstruct.patterns;

public class LayeredPattern_IsolatedInternalLayers extends LayeredPattern {
	// The version of the N-Layered pattern in which the internal layers are isolated from the Remainder.
	public LayeredPattern_IsolatedInternalLayers(int numberOfLayers) {
		super(numberOfLayers);
	}

	public LayeredPattern_IsolatedInternalLayers() {
		super();
	}

	@Override
	protected void defineRules() {
		for (int i = 1; i < numberOfModules; i++) {
			addRule(moduleService.getModuleByLogicalPath("Layer" + i), moduleService.getModuleByLogicalPath("Layer" + numberOfModules), "IsNotAllowedToUse");
			if (i == 1) {
				addRule(moduleService.getModuleByLogicalPath("Layer" + 2), moduleService.getModuleByLogicalPath("Layer" + 1), "IsTheOnlyModuleAllowedToUse");
				addRule(moduleService.getModuleByLogicalPath("Layer" + 2), moduleService.getModuleByLogicalPath("Layer" + 1), "MustUse");
			} else {
				addRule(moduleService.getModuleByLogicalPath("Layer" + (i + 1)), moduleService.getModuleByLogicalPath("Layer" + i), "IsOnlyAllowedToUse");
				addRule(moduleService.getModuleByLogicalPath("Layer" + (i + 1)), moduleService.getModuleByLogicalPath("Layer" + i), "MustUse");
			}
		}

	}

	@Override
	protected void defineModules() {
		for (int i = 1; i <= numberOfModules; i++) {
			defineService.addModule("Layer" + i, "**", "Subsystem", i, null);
		}
	}
}
