package husacct.analyse.task.reconstruct.patterns;

public class LayeredPattern_LayerTypes extends LayeredPattern {
	// This is the N-Layered pattern with actual layer-type modules and the associated skip-call and back-call bans.
	public LayeredPattern_LayerTypes(int numberOfLayers) {
		super(numberOfLayers);
	}

	public LayeredPattern_LayerTypes() {
		super();
	}

	@Override
	protected void defineRules() {
		// Skip-call and back-call rules get added automatically, so there's no need for them here.
	}

	@Override
	protected void defineModules() {
		for (int i = 1; i <= numberOfModules; i++) {
			defineService.addModule("Layer" + i, "**", "Layer", i, null);
		}
	}
}
