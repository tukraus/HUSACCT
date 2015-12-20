package husacct.analyse.task.reconstruct.patterns;

import java.util.ArrayList;
import java.util.Map;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.SoftwareUnitDTO;

public abstract class MVCPattern extends Pattern {
	// The abstract Model-View-Controller pattern class.
	public MVCPattern() {
		numberOfModules = 3;
		name = "MVC";
	}

	@Override
	protected void defineModules() {
		defineService.addModule("Model", "**", "SubSystem", 1, null);
		defineService.addModule("View", "**", "SubSystem", 1, null);
		defineService.addModule("Controller", "**", "SubSystem", 1, null);
	}

	@Override
	public void mapPattern(ArrayList<String> mapping) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(0)));
		defineService.editModule("Model", "Model", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(1)));
		defineService.editModule("View", "View", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(2)));
		defineService.editModule("Controller", "Controller", 1, temp);
	}

	@Override
	public void mapPatternAllowingAggregates(Map<Integer, ArrayList<String>> patternUnitNames) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>();
		for (int i = 0; i < patternUnitNames.size(); i++) {
			for (int j = 0; j < patternUnitNames.get(i).size(); j++) {
				temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(i).get(j)));
			}
			if (i == 0)
				defineService.editModuleWithAggregation("Model", "Model", 1, temp);
			else if (i == 1)
				defineService.editModuleWithAggregation("View", "View", 1, temp);
			else if (i == 2)
				defineService.editModuleWithAggregation("Controller", "Controller", 1, temp);
			temp.clear();
		}
	}

}
