package husacct.analyse.task.reconstruct.patterns;

import java.util.ArrayList;
import java.util.Map;

import husacct.ServiceProvider;
import husacct.common.dto.ModuleDTO;
import husacct.common.dto.RuleDTO;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.define.DomainToDtoParser;
import husacct.define.IDefineService;
import husacct.define.domain.module.ModuleStrategy;
import husacct.define.domain.services.ModuleDomainService;

// Abstract pattern class, parent of all architectural pattern classes.
public abstract class Pattern {

	protected DomainToDtoParser domainParser = new DomainToDtoParser();
	protected IDefineService defineService = ServiceProvider.getInstance().getDefineService();
	protected ModuleDomainService moduleService = new ModuleDomainService();
	protected ArrayList<SoftwareUnitDTO> unitsToMap;
	protected ArrayList<ModuleDTO> patternModules;
	protected int numberOfModules;
	protected int numberOfRules;
	protected String name;

	// Add modules to the intended architecture in line with an architectural pattern.
	protected abstract void defineModules();

	// Add rules to the intended architecture that apply to the pattern modules and form part of the pattern.
	protected abstract void defineRules();

	// Insert a pattern into the intended architecture.
	public void insertPattern() {
		numberOfRules = 0;
		defineModules();
		defineRules();
	}

	// Map specific SoftwareUnitDTOs from the analysed application to the defined pattern modules.
	public abstract void mapPattern(ArrayList<String> patternNames);

	protected void addRule(ModuleStrategy moduleTo, ModuleStrategy moduleFrom, String ruleType) {
		defineService.addRule(new RuleDTO(ruleType, true, domainParser.parseModule(moduleTo), domainParser.parseModule(moduleFrom), new String[0], "", null, false));
		numberOfRules++;
	}

	protected void addRule(ModuleStrategy moduleTo, ModuleStrategy moduleFrom, String ruleType, ModuleStrategy exception) {
		defineService.addRuleWithException(
				new RuleDTO(ruleType, true, domainParser.parseModule(moduleTo), domainParser.parseModule(moduleFrom), new String[0], "", null, false), exception);
		numberOfRules += 2; // An exception is essentially an additional rule (e.g. two "Is only allowed to use" rules coming from the same module is
							// simply one such rule plus an exception of that rule.)
	}

	public int getNumberOfModules() {
		return numberOfModules;
	}

	public int getNumberOfRules() {
		return numberOfRules;
	}

	public abstract void mapPatternAllowingAggregates(Map<Integer, ArrayList<String>> patternUnitNames);

	public String getName() {
		return name;
	}
}
