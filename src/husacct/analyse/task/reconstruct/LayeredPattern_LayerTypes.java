package husacct.analyse.task.reconstruct;

public class LayeredPattern_LayerTypes extends LayeredPattern {
// This is the N-Layered pattern with actual layer-type modules and the associated skip-call and back-call bans.
	public LayeredPattern_LayerTypes(int numberOfLayers) {
		super(numberOfLayers);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		// Skip-call and back-call rules get added automatically, so there's no need for them here.
		for (int i = 2; i <= numberOfModules; i++) {
				addRule(moduleService.getModuleByLogicalPath("Layer" + i), moduleService.getModuleByLogicalPath("Layer" + (i - 1)), "MustUse");
		}
	}
	@Override
	protected void defineModules() {
		for (int i = 1; i <= numberOfModules; i++) {
			defineService.addModule("Layer" + i, "**", "Layer", i, null);
		}
	}
}
