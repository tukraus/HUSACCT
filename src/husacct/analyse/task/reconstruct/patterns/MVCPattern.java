package husacct.analyse.task.reconstruct.patterns;

import java.util.ArrayList;
import java.util.Map;

import husacct.common.dto.SoftwareUnitDTO;

public abstract class MVCPattern extends Pattern {
	// The abstract class for all Model-View-Controller patterns. Constructor and mapping methods can be defined here, as well as "MustUse" rules and
	// the modules themselves.
	public MVCPattern() {
		numberOfModules = 3;
		name = "MVC";
	}

	@Override
	protected void defineModules() {
		defineService.addModule("Model", "**", "Subsystem", 1, null);
		defineService.addModule("View", "**", "Subsystem", 1, null);
		defineService.addModule("Controller", "**", "Subsystem", 1, null);
	}

	@Override
	protected void defineMustUseRules() {
		addSingleRule("Model", "Controller", "MustUse", null);
		addSingleRule("Model", "View", "MustUse", null);
		addSingleRule("View", "Controller", "MustUse", null);
	}

	@Override
	public void mapPattern(ArrayList<String> mapping) {
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(0)));
		defineService.editModule("Model", "Model", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(1)));
		defineService.editModule("View", "View", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(2)));
		defineService.editModule("Controller", "Controller", 1, temp);
		temp.clear();
	}

	@Override
	public void mapPatternAllowingAggregates(Map<Integer, ArrayList<String>> patternUnitNames) {
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>();
		for (int j = 0; j < patternUnitNames.get(0).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(0).get(j)));
		}
		defineService.editModuleWithAggregation("Model", "Model", 1, temp);
		temp.clear();
		for (int j = 0; j < patternUnitNames.get(1).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(1).get(j)));
		}
		defineService.editModuleWithAggregation("View", "View", 1, temp);
		temp.clear();
		for (int j = 0; j < patternUnitNames.get(2).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(2).get(j)));
		}
		defineService.editModuleWithAggregation("Controller", "Controller", 1, temp);
	}

}
