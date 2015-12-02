package husacct.analyse.task.reconstruct;

public class LayeredPattern_FreeRemainder extends LayeredPattern {
	// The version of the N-Layered pattern where the Remainder can all on any layer, though all but the final layer cannot call on the Remainder.
	public LayeredPattern_FreeRemainder(int numberOfLayers) {
		super(numberOfLayers);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineModules() {
		for (int i = 1; i <= numberOfModules; i++) {
			defineService.addModule("Layer" + i, "**", "Subsystem", i, null);
		}
	}

	@Override
	protected void defineRules() {
		for (int i = 1; i < numberOfModules; i++) {
			addRule(moduleService.getModuleByLogicalPath("Layer" + (i + 1)), moduleService.getModuleByLogicalPath("Layer" + i), "MustUse");
			addRule(moduleService.getModuleByLogicalPath("Layer" + (i + 1)), moduleService.getModuleByLogicalPath("Layer" + i), "IsOnlyAllowedToUse");
		}
	}

}
