package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_ControllerInterface extends MVCPattern {

	public MVCPattern_ControllerInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("Controller", "View", "IsOnlyAllowedToUse", "Model");
		addSingleRule("View", "Model", "IsOnlyAllowedToUse", null);
		addSingleRule("Model", "Controller", "IsTheOnlyModuleAllowedToUse", "View");
		addSingleRule("View", "Controller", "IsTheOnlyModuleAllowedToUse", "Model");
	}

}
