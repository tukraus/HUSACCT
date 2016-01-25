package husacct.analyse.task.reconstruct.patterns;

public class CentralisedLayering extends LayeredPattern_CompleteFreedom {

	public CentralisedLayering() {
		numberOfModules = 3;
		name = "Centralised Layering";
	}
	
	
	@Override
	protected void defineRules() {
		addSingleRule("Layer" + 1, "Layer" + 2, "IsNotAllowedToUse", null);
		addSingleRule("Layer" + 3, "Layer" + 2, "IsNotAllowedToUse", null);
		addSingleRule("Layer" + 3, "Layer" + 1, "IsNotAllowedToUse", null);
		addSingleRule("Layer" + 1, "Layer" + 3, "IsNotAllowedToUse", null);
	}

	@Override
	protected void defineMustUseRules() {
			addSingleRule("Layer" + 2, "Layer" + 1, "MustUse", null);
			addSingleRule("Layer" + 2, "Layer" + 3, "MustUse", null);
	}
}
