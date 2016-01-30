package husacct.analyse.task.reconstruct.patterns;

import java.util.ArrayList;
import java.util.Map;

import husacct.common.dto.SoftwareUnitDTO;

public class HUSACCT_Analyse extends Pattern {

	public HUSACCT_Analyse() {
		// TODO Auto-generated constructor stub
		numberOfModules = 5;
	}

	@Override
	protected void defineModules() {
		// TODO Auto-generated method stub
		defineService.addModule("PresentationLayer", "**", "Subsystem", 1, null);
		defineService.addModule("TaskLayer", "**", "Subsystem", 1, null);
		defineService.addModule("DomainLayer", "**", "Subsystem", 1, null);
		defineService.addModule("AbstractionLayer", "**", "Subsystem", 1, null);
		defineService.addModule("InfrastructureLayer", "**", "Subsystem", 1, null);
	}

	@Override
	protected void defineRules() {
		// TODO Auto-generated method stub
		addSingleRule("TaskLayer", "PresentationLayer", "IsOnlyAllowedToUse", null);
		addSingleRule("PresentationLayer","TaskLayer","IsNotAllowedToUse", null);
		addSingleRule("PresentationLayer", "DomainLayer", "IsNotAllowedToUse", null);
		addSingleRule("PresentationLayer", "InfrastructureLayer", "IsNotAllowedToUse", null);
		addSingleRule("TaskLayer", "InfrastructureLayer", "IsNotAllowedToUse", null);
		addSingleRule("DomainLayer", "InfrastructureLayer", "IsNotAllowedToUse", null);
		addSingleRule("TaskLayer", "DomainLayer", "IsNotAllowedToUse", null);
		addSingleRule("AbstractionLayer","TaskLayer","IsTheOnlyModuleAllowedToUse", null);
		addSingleRule("InfrastructureLayer","TaskLayer","IsTheOnlyModuleAllowedToUse", "AbstractionLayer");
		addSingleRule("InfrastructureLayer","AbstractionLayer","IsOnlyAllowedToUse", null);
	}

	@Override
	protected void defineMustUseRules() {
		// TODO Auto-generated method stub
		addSingleRule("TaskLayer", "PresentationLayer", "MustUse", null);
		addSingleRule("DomainLayer", "TaskLayer", "MustUse", null);
		addSingleRule("AbstractionLayer", "TaskLayer", "MustUse", null);
		addSingleRule("InfrastructureLayer", "TaskLayer", "MustUse", null);
//		addSingleRule("InfrastructureLayer", "AbstractionLayer", "MustUse", null);
	}

	@Override
	public void mapPattern(ArrayList<String> patternNames) {
		// TODO Auto-generated method stub
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(patternNames.get(0)));
		defineService.editModule("PresentationLayer", "PresentationLayer", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(patternNames.get(1)));
		defineService.editModule("TaskLayer", "TaskLayer", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(patternNames.get(2)));
		defineService.editModule("DomainLayer", "DomainLayer", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(patternNames.get(3)));
		defineService.editModule("AbstractionLayer", "AbstractionLayer", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(patternNames.get(4)));
		defineService.editModule("InfrastructureLayer", "InfrastructureLayer", 1, temp);
	}

	@Override
	public void mapPatternAllowingAggregates(Map<Integer, ArrayList<String>> patternUnitNames) {
		// TODO Auto-generated method stub
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>();
		for (int j = 0; j < patternUnitNames.get(0).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(0).get(j)));
		}
		defineService.editModuleWithAggregation("PresentationLayer", "PresentationLayer", 1, temp);
		temp.clear();
		for (int j = 0; j < patternUnitNames.get(1).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(1).get(j)));
		}
		defineService.editModuleWithAggregation("TaskLayer", "TaskLayer", 1, temp);
		temp.clear();
		for (int j = 0; j < patternUnitNames.get(2).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(2).get(j)));
		}
		defineService.editModuleWithAggregation("DomainLayer", "DomainLayer", 1, temp);
		temp.clear();
		for (int j = 0; j < patternUnitNames.get(3).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(3).get(j)));
		}
		defineService.editModuleWithAggregation("AbstractionLayer", "AbstractionLayer", 1, temp);
		temp.clear();
		for (int j = 0; j < patternUnitNames.get(4).size(); j++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(patternUnitNames.get(4).get(j)));
		}
		defineService.editModuleWithAggregation("InfrastructureLayer", "InfrastructureLayer", 1, temp);
	}

}
