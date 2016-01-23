package husacct.analyse.task.reconstruct.patterns;

public class LayeredPattern_RestrictedRemainder extends LayeredPattern {
	// An interpretation of the N-Layered pattern in which the layers can call upon the Remainder, but not vice versa.
	public LayeredPattern_RestrictedRemainder(int numberOfLayers) {
		super(numberOfLayers);
	}

	public LayeredPattern_RestrictedRemainder() {
		super();
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
			addSingleRule("Layer" + (i + 1), "Layer" + i, "IsTheOnlyModuleAllowedToUse", null);
		}
		addSingleRule("Layer" + 1, "Layer" + numberOfModules, "IsNotAllowedToUse", null);
	}

}
