package husacct.analyse.task.reconstruct.patterns;

public class MVCPattern_CompleteFreedom extends MVCPattern {

	public MVCPattern_CompleteFreedom() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void defineRules() {
		addSingleRule("Controller", "Model", "IsNotAllowedToUse", null);
	}

}
