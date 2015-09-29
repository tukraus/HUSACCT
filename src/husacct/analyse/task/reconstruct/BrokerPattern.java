package husacct.analyse.task.reconstruct;

import java.util.ArrayList;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.SoftwareUnitDTO;

public class BrokerPattern extends Pattern {
	
	public BrokerPattern() {
		numberOfModules = 3;
	}
	
	@Override
	protected void defineModules() {
		defineService.addModule("Broker", "**", "SubSystem", 1, null);
		defineService.addModule("Services", "**", "SubSystem", 1, null);
		defineService.addModule("Client", "**", "SubSystem", 1, null);
	}
	
	@Override
	protected void defineRules() {
		addRule(moduleService.getModuleByLogicalPath("Broker"), moduleService.getModuleByLogicalPath("Services"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Client"), moduleService.getModuleByLogicalPath("Broker"), "MustUse");
		addRule(moduleService.getModuleByLogicalPath("Client"), moduleService.getModuleByLogicalPath("Services"), "IsNotAllowedToUse");
		addRule(moduleService.getModuleByLogicalPath("Services"), moduleService.getModuleByLogicalPath("Client"), "IsNotAllowedToUse");
	}
	
	@Override
	public void mapPattern(String[] mapping) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		ArrayList<SoftwareUnitDTO> temp = new ArrayList<>(1);
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[0]));
		defineService.editModule("Broker", "Broker", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[1]));
		defineService.editModule("Services", "Services", 1, temp);
		temp.clear();
		temp.add(analyseService.getSoftwareUnitByUniqueName(mapping[2]));
//		defineService.editModule("Client", "Client", 1, temp);
	}
	
}
