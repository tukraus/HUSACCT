package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_RestrictedRemainder extends MVCPattern {

	public MVCPattern_RestrictedRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("View", "Model", "IsTheOnlyModuleAllowedToUse", "Controller");
		addSingleRule("Model", "View", "IsTheOnlyModuleAllowedToUse", "Controller");
		addSingleRule("Controller", "View", "IsTheOnlyModuleAllowedToUse", null);
		addSingleRule("View", "Controller", "IsTheOnlyModuleAllowedToUse", "Model");
		addSingleRule("Model", "Controller", "IsTheOnlyModuleAllowedToUse", "View");
	}

}
