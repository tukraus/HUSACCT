package husacct.analyse.task.reconstruct.patterns;

public class LayeredPattern_FreeRemainder extends LayeredPattern {
	// The version of the N-Layered pattern where the Remainder can all on any layer, though all but the final layer cannot call on the Remainder.
	public LayeredPattern_FreeRemainder(int numberOfLayers) {
		super(numberOfLayers);
	}

	public LayeredPattern_FreeRemainder() {
		super();
	}


	@Override
	protected void defineRules() {
		for (int i = 1; i < numberOfModules; i++) {
			addSingleRule("Layer" + (i + 1), "Layer" + i, "IsOnlyAllowedToUse", null);
			addSingleRule("Layer" + i, "Layer" + numberOfModules, "IsNotAllowedToUse", null);
		}
	}

}
