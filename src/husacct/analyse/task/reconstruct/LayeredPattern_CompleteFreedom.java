package husacct.analyse.task.reconstruct;

public class LayeredPattern_CompleteFreedom extends LayeredPattern {
	// The interpretation of the N-Layered pattern for which there exist no restriction for the Remainder to use the layers.
	public LayeredPattern_CompleteFreedom(int numberOfLayers) {
		super(numberOfLayers);
	}

	public LayeredPattern_CompleteFreedom() {
		super();
	}

	@Override
	protected void defineRules() {
		for (int i = 1; i <= numberOfModules; i++) {
			if (i < numberOfModules)
				addRule(moduleService.getModuleByLogicalPath("Layer" + (i + 1)), moduleService.getModuleByLogicalPath("Layer" + i), "MustUse");
			for (int j = 1; j <= i; j++) {
				if (j < i)
					addRule(moduleService.getModuleByLogicalPath("Layer" + (i - j)), moduleService.getModuleByLogicalPath("Layer" + i), "IsNotAllowedToUse");
				if ((i + j + 1) <= numberOfModules)
					addRule(moduleService.getModuleByLogicalPath("Layer" + (i + j + 1)), moduleService.getModuleByLogicalPath("Layer" + i), "IsNotAllowedToUse");
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
