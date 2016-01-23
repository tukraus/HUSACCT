package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_ModelInterface extends MVCPattern {

	public MVCPattern_ModelInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("View", "Model", "IsTheOnlyModuleAllowedToUse", "Controller");
		addSingleRule("View", "Controller", "IsTheOnlyModuleAllowedToUse", "Model");
		addSingleRule("Controller", "View", "IsTheOnlyModuleAllowedToUse", null);
		addSingleRule("Model", "View", "IsOnlyAllowedToUse", "Controller");
		addSingleRule("Model", "Controller", "IsOnlyAllowedToUse", "View");
	}

}
