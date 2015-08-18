package husacct.analyse.task.reconstruct;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.analyse.domain.IModelQueryService;
import husacct.common.dto.ModuleDTO;
import husacct.common.dto.RuleDTO;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.define.DomainToDtoParser;
import husacct.define.IDefineService;
import husacct.define.domain.module.ModuleStrategy;
import husacct.define.domain.services.ModuleDomainService;
import husacct.validate.IValidateService;

public class ReconstructArchitecture {
	
	private final Logger logger = Logger.getLogger(ReconstructArchitecture.class);
	private IModelQueryService queryService;
	private IDefineService defineService;
	
	private ArrayList<SoftwareUnitDTO> internalRootPackagesWithClasses; // The first packages (starting from the project root) that contain one or
																		// more classes.
	private ArrayList<SoftwareUnitDTO> unitsMappedToPatternElements; // Software Unit DTOs mapped to modules within the architectural pattern.
	// External system variables
	private String xLibrariesRootPackage = "xLibraries";
	private ArrayList<SoftwareUnitDTO> xLibrariesMainPackages = new ArrayList<SoftwareUnitDTO>();
	// Layer variables
	private TreeMap<Integer, ArrayList<SoftwareUnitDTO>> layers = new TreeMap<Integer, ArrayList<SoftwareUnitDTO>>();
	private int layerThreshold = 10; // Percentage of allowed violating dependencies (back-calls) for adding layers.
	private int skipCallThreshold = 10; // Percentage of allowed violating dependencies (skip-calls) for partially merging layers.
	
	public ReconstructArchitecture(IModelQueryService queryService) {
		this.queryService = queryService;
		defineService = ServiceProvider.getInstance().getDefineService();
		IValidateService validateService = ServiceProvider.getInstance().getValidateService();
		identifyExternalSystems();
		// identifyLayers();
		logger.info("Number of rules before applying patterns: " + defineService.getDefinedRules().length);
		createPattern("MVC"); 
		logger.info("Number of rules after applying patterns: " + defineService.getDefinedRules().length);
		
		// TODO: Replace temporary mapping with automated mapping loop that applies to all patterns.
		try {
			determineInternalRootPackagesWithClasses();
			unitsMappedToPatternElements = new ArrayList<SoftwareUnitDTO>();
			ArrayList<SoftwareUnitDTO> temp = new ArrayList<SoftwareUnitDTO>();
			for (int i = 1; i < 4; i++) {
				temp.add(internalRootPackagesWithClasses.get(i - 1));
				defineService.editModule("Layer" + i, "Layer" + i, i, temp);
				unitsMappedToPatternElements.addAll(temp);
				temp.clear();
			}
			logger.info("Software units successfully mapped to pattern elements");
		} catch (Exception e) {
			logger.info("Failed to map a software unit to a pattern element. ");
		}
		
		ServiceProvider.getInstance().getControlService().setValidate(true);
		logger.info(new Date().toString() + " CheckConformanceTask is Starting: IValidateService.checkConformance()");
		validateService.getCategories();
		validateService.checkConformance();
		int numberOfViolations = 0;
		for (RuleDTO appliedRule : defineService.getDefinedRules()) {
			numberOfViolations += validateService.getViolationsByRule(appliedRule).length;
		}
		ServiceProvider.getInstance().getControlService().setValidate(false);
		logger.info(new Date().toString() + " CheckConformanceTask sets state Validating to false");
		int totalNumberOfDependencies = 0;
		for (SoftwareUnitDTO softwareUnit : unitsMappedToPatternElements) {
			for (SoftwareUnitDTO otherSoftwareUnit : unitsMappedToPatternElements) {
				if (softwareUnit != otherSoftwareUnit) {
					totalNumberOfDependencies += getNumberofDependenciesBetweenSoftwareUnits(softwareUnit.uniqueName, otherSoftwareUnit.uniqueName);
				}
			}
		}
		logger.info("Number of dependencies within pattern: " + totalNumberOfDependencies + ", resulting in a total of " + numberOfViolations
				+ " violations.");
				
		// identifyComponents();
		// identifySubSystems();
		// IdentifyAdapters();
	}
	
	// TODO: Refactor/extract pattern creation.
	private void createPattern(String pattern) { // Create a particular pattern in the intended architecture.
		ModuleDomainService moduleService = new ModuleDomainService();
		ArrayList<ModuleStrategy> patternModule = new ArrayList<ModuleStrategy>();
		switch (pattern) {
			case "3 layered":
				for (int i = 1; i <= 3; i++) {
					defineService.addModule("Layer" + i, "**", "Layer", i, null);
					patternModule.add(moduleService.getModuleByLogicalPath("Layer" + i));
					if (i > 1) {
						createRule(patternModule.get(i-1), patternModule.get(i - 2), "MustUse");
					}
				}
				logger.info("3-layered pattern added. ");
				break;
			case "MVC":
				defineService.addModule("Model", "**", "Subsystem", 1, null);
				patternModule.add(moduleService.getModuleByLogicalPath("Model"));
				defineService.addModule("View", "**", "Subsystem", 1, null);
				patternModule.add(moduleService.getModuleByLogicalPath("View"));
				defineService.addModule("Controller", "**", "Subsystem", 1, null);
				patternModule.add(moduleService.getModuleByLogicalPath("Controller"));
				logger.info("MVC pattern added. ");
				break;
		}
	}
	
	private String[][] mapSoftwareUnitsToPatternElements(ArrayList<String> patternElements, ArrayList<SoftwareUnitDTO> toMapSoftwareUnits) {
		PatternMapper bruteForceMapper = new PatternMapper(patternElements.size(), toMapSoftwareUnits.toArray(new String[toMapSoftwareUnits.size()]));
		logger.info("Determining all possible mappings for an architectural pattern. ");
		return bruteForceMapper.getPermutations();
	}
	
	private int getNumberofDependenciesBetweenSoftwareUnits(String fromUnit, String toUnit) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		return analyseService.getDependenciesFromSoftwareUnitToSoftwareUnit(fromUnit, toUnit).length;
	}
	
	private void identifyExternalSystems() {
		// Create module "ExternalSystems"
		ArrayList<SoftwareUnitDTO> emptySoftwareUnitsArgument = new ArrayList<SoftwareUnitDTO>();
		defineService.addModule("ExternalSystems", "**", "ExternalLibrary", 0, emptySoftwareUnitsArgument);
		// Create a module for each childUnit of xLibrariesRootPackage
		int nrOfExternalLibraries = 0;
		for (SoftwareUnitDTO mainUnit : queryService.getChildUnitsOfSoftwareUnit(xLibrariesRootPackage)) {
			xLibrariesMainPackages.add(mainUnit);
			ArrayList<SoftwareUnitDTO> softwareUnitsArgument = new ArrayList<SoftwareUnitDTO>();
			softwareUnitsArgument.add(mainUnit);
			defineService.addModule(mainUnit.name, "ExternalSystems", "ExternalLibrary", 0, softwareUnitsArgument);
			nrOfExternalLibraries++;
		}
		logger.info(" Number of added ExternalLibraries: " + nrOfExternalLibraries);
	}
	
	private void determineInternalRootPackagesWithClasses() {
		internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
		SoftwareUnitDTO[] allRootUnits = queryService.getSoftwareUnitsInRoot();
		for (SoftwareUnitDTO rootModule : allRootUnits) {
			if (!rootModule.uniqueName.equals(xLibrariesRootPackage)) {
				for (String internalPackage : queryService.getRootPackagesWithClass(rootModule.uniqueName)) {
					internalRootPackagesWithClasses.add(queryService.getSoftwareUnitByUniqueName(internalPackage));
				}
			}
		}
		if (internalRootPackagesWithClasses.size() == 1) {
			// Temporal solution useful for HUSACCT20 test. To be improved! E.g., classes in root are excluded from the process.
			String newRoot = internalRootPackagesWithClasses.get(0).uniqueName;
			internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
			for (SoftwareUnitDTO child : queryService.getChildUnitsOfSoftwareUnit(newRoot)) {
				if (child.type.equalsIgnoreCase("package")) {
					internalRootPackagesWithClasses.add(child);
				}
			}
		}
	}
	
	private void identifyLayers() {
		// 1) Assign all internalRootPackages to bottom layer
		int layerId = 1;
		ArrayList<SoftwareUnitDTO> assignedUnits = new ArrayList<SoftwareUnitDTO>();
		assignedUnits.addAll(internalRootPackagesWithClasses);
		layers.put(layerId, assignedUnits);
		
		// 2) Identify the bottom layer. Look for packages with dependencies to external systems only.
		identifyTopLayerBasedOnUnitsInBottomLayer(layerId);
		
		// 3) Look iteratively for packages on top of the bottom layer, et cetera.
		while (layers.lastKey() > layerId) {
			layerId++;
			identifyTopLayerBasedOnUnitsInBottomLayer(layerId);
		}
		
		// mergeLayersPartiallyBasedOnSkipCallAvoidance(); // Extra step to minimise the number of skip-calls by moving problematic modules to lower
		// layers.
		
		// 4) Add the layers to the intended architecture
		int highestLevelLayer = layers.size();
		if (highestLevelLayer > 1) {
			// Reverse the layer levels. The numbering of the layers within the intended architecture is different: the highest level layer has
			// hierarchcalLevel = 1
			int lowestLevelLayer = 1;
			int raise = highestLevelLayer - lowestLevelLayer;
			TreeMap<Integer, ArrayList<SoftwareUnitDTO>> tempLayers = new TreeMap<Integer, ArrayList<SoftwareUnitDTO>>();
			for (int i = lowestLevelLayer; i <= highestLevelLayer; i++) {
				ArrayList<SoftwareUnitDTO> unitsOfLayer = layers.get(i);
				int level = lowestLevelLayer + raise;
				tempLayers.put(level, unitsOfLayer);
				raise--;
			}
			layers = tempLayers;
			for (Integer hierarchicalLevel : layers.keySet()) {
				defineService.addModule("Layer" + hierarchicalLevel, "**", "Layer", hierarchicalLevel, layers.get(hierarchicalLevel));
			}
		}
		
		logger.info(" Number of added Layers: " + layers.size());
	}
	
	private void identifyTopLayerBasedOnUnitsInBottomLayer(int bottomLayerId) {
		ArrayList<SoftwareUnitDTO> assignedUnitsOriginalBottomLayer = layers.get(bottomLayerId);
		@SuppressWarnings("unchecked")
		ArrayList<SoftwareUnitDTO> assignedUnitsBottomLayerClone = (ArrayList<SoftwareUnitDTO>) assignedUnitsOriginalBottomLayer.clone();
		ArrayList<SoftwareUnitDTO> assignedUnitsNewBottomLayer = new ArrayList<SoftwareUnitDTO>();
		ArrayList<SoftwareUnitDTO> assignedUnitsTopLayer = new ArrayList<SoftwareUnitDTO>();
		for (SoftwareUnitDTO softwareUnit : assignedUnitsOriginalBottomLayer) {
			boolean rootPackageDoesNotUseOtherPackage = true;
			for (SoftwareUnitDTO otherSoftwareUnit : assignedUnitsBottomLayerClone) {
				if (!otherSoftwareUnit.uniqueName.equals(softwareUnit.uniqueName)) {
					int nrOfDependenciesFromsoftwareUnitToOther = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName,
							otherSoftwareUnit.uniqueName).length;
					int nrOfDependenciesFromOtherTosoftwareUnit = queryService
							.getDependenciesFromSoftwareUnitToSoftwareUnit(otherSoftwareUnit.uniqueName, softwareUnit.uniqueName).length;
					if (nrOfDependenciesFromsoftwareUnitToOther > ((nrOfDependenciesFromOtherTosoftwareUnit / 100) * layerThreshold)) {
						rootPackageDoesNotUseOtherPackage = false;
					}
				}
			}
			if (rootPackageDoesNotUseOtherPackage) { // Leave unit in the lower layer
				assignedUnitsNewBottomLayer.add(softwareUnit);
			} else { // Assign unit to the higher layer
				assignedUnitsTopLayer.add(softwareUnit);
			}
			
		}
		if ((assignedUnitsTopLayer.size() > 0) && (assignedUnitsNewBottomLayer.size() > 0)) {
			layers.remove(bottomLayerId);
			layers.put(bottomLayerId, assignedUnitsNewBottomLayer);
			bottomLayerId++;
			layers.put(bottomLayerId, assignedUnitsTopLayer);
		}
	}
	
	private void mergeLayersPartiallyBasedOnSkipCallAvoidance() {
		for (int currentLayerID = layers.size(); currentLayerID > 2; currentLayerID--) {
			ArrayList<SoftwareUnitDTO> unitsInCurrentLayer = layers.get(currentLayerID);
			ArrayList<SoftwareUnitDTO> unitsInCurrentLowerLayer;
			ArrayList<SoftwareUnitDTO> assignedUnitsNewLowerLayer = new ArrayList<SoftwareUnitDTO>();
			ArrayList<SoftwareUnitDTO> assignedUnitsNewUpperLayer = new ArrayList<SoftwareUnitDTO>();
			for (SoftwareUnitDTO softwareUnit : unitsInCurrentLayer) {
				int nrOfDependenciesFromSoftwareUnitToOtherWithinLayer = 0;
				int nrOfSkipCallsFromSoftwareUnitToOtherLayers = 0;
				for (SoftwareUnitDTO otherSoftwareUnit : unitsInCurrentLayer) {
					nrOfDependenciesFromSoftwareUnitToOtherWithinLayer += queryService
							.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName, otherSoftwareUnit.uniqueName).length;
				}
				for (int currentLowerLayerID = currentLayerID - 2; currentLowerLayerID < 0; currentLowerLayerID--) {
					unitsInCurrentLowerLayer = layers.get(currentLowerLayerID);
					for (SoftwareUnitDTO lowerSoftwareUnit : unitsInCurrentLowerLayer) {
						if (!lowerSoftwareUnit.uniqueName.equals(softwareUnit.uniqueName)) {
							nrOfSkipCallsFromSoftwareUnitToOtherLayers += queryService
									.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName, lowerSoftwareUnit.uniqueName).length;
						}
					}
				}
				if (nrOfSkipCallsFromSoftwareUnitToOtherLayers < ((nrOfDependenciesFromSoftwareUnitToOtherWithinLayer / 100) * skipCallThreshold)) {
					assignedUnitsNewUpperLayer.add(softwareUnit); // Keep in current layer.
				} else {
					assignedUnitsNewLowerLayer.add(softwareUnit); // Move to one layer below.
				}
			}
			layers.remove(currentLayerID);
			if (assignedUnitsNewLowerLayer.size() > 0) {
				logger.info(" Number of modules moved downwards: " + assignedUnitsNewLowerLayer);
				layers.put(currentLayerID, assignedUnitsNewUpperLayer);
				layers.get(currentLayerID - 1).addAll(assignedUnitsNewLowerLayer);
			}
		}
	}
	
	private void identifyComponents() {
	
	}
	
	private void identifySubSystems() {
	
	}
	
	private void IdentifyAdapters() { // Here, and adapter is a module with a IsTheOnlyModuleAllowedToUse rule.
	
	}
	
	private void createModule() {
	
	}
	
	private void createRule(ModuleStrategy moduleTo, ModuleStrategy moduleFrom, String ruleType) {
		DomainToDtoParser domainParser = new DomainToDtoParser();
		defineService.addRule(new RuleDTO(ruleType, true, domainParser.parseModule(moduleTo), domainParser.parseModule(moduleFrom), new String[0], "",
				null, false));
	}
}
