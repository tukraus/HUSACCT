package husacct.analyse.task.reconstruct;

import java.util.ArrayList;
import java.util.Map;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.SoftwareUnitDTO;

public abstract class BrokerPattern extends Pattern {
	// In the Broker Pattern, the Requester calls on the Broker to use the correct services of the Provider. The Provider may want to use the Broker
	// and the Broker could depend on the Requester, but this is not required. If one of these three modules were to be an interface with respect to
	// the Remainder, i.e. the rest of the architecture, it would make most sense of the Requester were placed in this role.
	public BrokerPattern() {
		numberOfModules = 3;
		name = "Broker";
	}

	@Override
	protected void defineModules() {
		defineService.addModule("Broker", "**", "SubSystem", 1, null);
		defineService.addModule("Provider", "**", "SubSystem", 1, null);
		defineService.addModule("Requester", "**", "SubSystem", 1, null);
	}

	@Override
	public void mapPattern(String[] mapping) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[0]));
		defineService.editModule("Broker", "Broker", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[1]));
		defineService.editModule("Provider", "Provider", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[2]));
		defineService.editModule("Requester", "Requester", 1, temp);
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
				defineService.editModuleWithAggregation("Broker", "Broker", 1, temp);
			else if (i == 1)
				defineService.editModuleWithAggregation("Provider", "Provider", 1, temp);
			else if (i == 2)
				defineService.editModuleWithAggregation("Requester", "Requester", 1, temp);
			temp.clear();
		}
	}
}
