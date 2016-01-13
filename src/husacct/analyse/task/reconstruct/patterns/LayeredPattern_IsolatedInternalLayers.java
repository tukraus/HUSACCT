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
			addSingleRule("Layer" + i, "Layer" + numberOfModules, "IsNotAllowedToUse", null);
			if (i == 1) {
				addSingleRule("Layer" + 2, "Layer" + 1, "IsTheOnlyModuleAllowedToUse", null);
			} else {
				addSingleRule("Layer" + (i + 1), "Layer" + i, "IsOnlyAllowedToUse", null);
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
