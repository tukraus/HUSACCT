package husacct.analyse.task.reconstruct;

import java.util.ArrayList;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.SoftwareUnitDTO;

public class MVCPattern extends Pattern {
	
	public MVCPattern() {
		numberOfModules = 3;
	}
	
	@Override
	protected void defineModules() {
		defineService.addModule("Model", "**", "SubSystem", 1, null);
		defineService.addModule("View", "**", "SubSystem", 1, null);
		defineService.addModule("Controller", "**", "SubSystem", 1, null);
	}
	
	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("View"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("Controller"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Controller"), moduleService.getModuleByLogicalPath("Model"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Model"), moduleService.getModuleByLogicalPath("View"), "IsNotAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("View"), moduleService.getModuleByLogicalPath("Model"), "IsNotAllowedToUse");
	}
	
	@Override
	public void mapPattern(String[] mapping) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[0]));
		defineService.editModule("Model", "Model", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[1]));
		defineService.editModule("View", "View", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[2]));
		defineService.editModule("Controller", "Controller", 1, temp);
	}
	
}
