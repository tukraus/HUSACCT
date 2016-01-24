package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_FreeRemainder extends MVCPattern {

	public MVCPattern_FreeRemainder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("View", "Model", "IsOnlyAllowedToUse", null);
		addSingleRule("Model", "View", "IsOnlyAllowedToUse", "Controller");
		addSingleRule("Controller", "View", "IsOnlyAllowedToUse", "Model");
		addSingleRule("View", "Controller", "IsOnlyAllowedToUse", "Model");
		addSingleRule("Model", "Controller", "IsOnlyAllowedToUse", "View");
	}

}
