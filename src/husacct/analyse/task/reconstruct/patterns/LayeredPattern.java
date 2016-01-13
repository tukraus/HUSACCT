package husacct.analyse.task.reconstruct.patterns;

import java.util.ArrayList;
import java.util.Map;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.SoftwareUnitDTO;

public abstract class LayeredPattern extends Pattern {
	// The abstract class for all N-Layered patterns. Constructor and mapping methods can be defined here.
	public LayeredPattern(int numberOfLayers) {
		numberOfModules = numberOfLayers;
		name = "Layered";
	}

	public LayeredPattern() {
		numberOfModules = 3;
		name = "Layered";
	}

	@Override
	protected void defineMustUseRules() {
		for (int i = 1; i < numberOfModules; i++)
			addSingleRule("Layer" + (i + 1), "Layer" + i, "MustUse", null);
	}

	@Override
	public void mapPattern(ArrayList<String> mapping) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		for (int i = 1; i <= mapping.size(); i++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(mapping.get(i - 1)));
			defineService.editModule("Layer" + i, "Layer" + i, i, temp);
			temp.clear();
		}
	}

	@Override
	public void mapPatternAllowingAggregates(Map<Integer, ArrayList<String>> patternUnitNames) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>();
		for (int i = 0; i < patternUnitNames.size(); i++) {
			for (int j = 0; j < patternUnitNames.get(i).size(); j++) {
				temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(i).get(j)));
			}
			defineService.editModuleWithAggregation("Layer" + (i + 1), "Layer" + (i + 1), i + 1, temp);
			temp.clear();
		}
	}

}
