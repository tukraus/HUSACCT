package husacct.analyse.task.reconstruct.patterns;

import java.util.ArrayList;
import java.util.Map;

import husacct.common.dto.SoftwareUnitDTO;

public class Model_Viewcontroller extends Pattern {

	public Model_Viewcontroller() {
		numberOfModules = 2;
		name = "M-VC";
	}

	@Override
	protected void defineModules() {
		defineService.addModule("Model", "**", "Subsystem", 1, null);
		defineService.addModule("Viewcontroller", "**", "Subsystem", 1, null);
	}

	@Override
	protected void defineRules() {
		addSingleRule("Viewcontroller", "Model", "IsNotAllowedToUse", null);
	}

	@Override
	protected void defineMustUseRules() {
		addSingleRule("Model", "Viewcontroller", "MustUse", null);
	}

	@Override
	public void mapPattern(ArrayList<String> mapping) {
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(0)));
		defineService.editModule("Model", "Model", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(1)));
		defineService.editModule("Viewcontroller", "Viewcontroller", 1, temp);
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
		defineService.editModuleWithAggregation("Viewcontroller", "Viewcontroller", 1, temp);
		temp.clear();
	}

}
