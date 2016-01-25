package husacct.analyse.task.reconstruct.patterns;

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
			for (int j = 1; j <= i; j++) {
				if (j < i)
					addSingleRule("Layer" + (i - j), "Layer" + i, "IsNotAllowedToUse", null);
				if ((i + j + 1) <= numberOfModules)
					addSingleRule("Layer" + (i + j + 1), "Layer" + i, "IsNotAllowedToUse", null);
			}
		}
	}


}
