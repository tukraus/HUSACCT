package husacct.analyse.task.reconstruct;

import java.util.ArrayList;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.SoftwareUnitDTO;

public class LayeredPattern extends Pattern {
	
	public LayeredPattern(int numberOfLayers) {
		numberOfModules = numberOfLayers;
	}
	
	@Override
	protected void defineModules() {
		for (int i = 1; i <= numberOfModules; i++) {
			defineService.addModule("Layer" + i, "**", "Layer", i, null);
		}
	}
	
	@Override
	protected void defineRules() {
		// Skip-call and back-call rules get added automatically, so there's no need for them here.
		for (int i = 0; i <= numberOfModules; i++) {
			if (i > 1) {
				addRule(moduleService.getModuleByLogicalPath("Layer" + i), moduleService.getModuleByLogicalPath("Layer" + (i - 1)), "MustUse");
			}
		}
	}
	
	@Override
	public void mapPattern(String[] mapping) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		for (int i = 1; i <= mapping.length; i++) {
			temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[i - 1]));
			defineService.editModule("Layer" + i, "Layer" + i, i, temp);
			temp.clear();
		}
	}
}
