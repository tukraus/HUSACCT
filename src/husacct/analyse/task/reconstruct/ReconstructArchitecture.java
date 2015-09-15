package husacct.analyse.task.reconstruct;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;
import java.util.stream.IntStream;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.analyse.domain.IModelQueryService;
import husacct.common.dto.CategoryDTO;
import husacct.common.dto.RuleDTO;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.define.DomainToDtoParser;
import husacct.define.IDefineService;
import husacct.define.domain.module.ModuleStrategy;
import husacct.validate.IValidateService;

public class ReconstructArchitecture {
	private final Logger logger = Logger.getLogger(ReconstructArchitecture.class);
	private IModelQueryService queryService;
	private IDefineService defineService;
	private IValidateService validateService;
	private ArrayList<SoftwareUnitDTO> internalRootPackagesWithClasses; // The first packages (starting from the project root) that contain one or
																		// more classes.
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
		validateService = ServiceProvider.getInstance().getValidateService();
		identifyExternalSystems();
		determineInternalRootPackagesWithClasses();
		
		String pattern = "MVC";
		int numberOfLayers = 3; // Only matters for n-layered patterns.
		
		logger.info("Number of rules before applying patterns: " + defineService.getDefinedRules().length);
		Pattern currentPattern = null;
		switch (pattern) {
			case "layered":
				currentPattern = new LayeredPattern(numberOfLayers);
				currentPattern.insertPattern();
				break;
			case "MVC":
				currentPattern = new MVCPattern();
				currentPattern.insertPattern();
				break;
			case "Broker":
				currentPattern = new BrokerPattern();
				currentPattern.insertPattern();
				break;
		}
		logger.info("Number of rules after applying patterns: " + defineService.getDefinedRules().length);
		if (currentPattern != null) {
			//bruteForceApproach(pattern, currentPattern);
			geneticApproach();
		}
	}
	
	private void geneticApproach() {
		String[] args = {"50"}; 
		try {
			GeneticApproach.run(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void bruteForceApproach(String pattern, Pattern currentPattern) {
		int numberOfTopCandidates = 10;
		JDialog dialog = new JDialog();
		dialog.setSize(100, 50);
		dialog.setTitle("Working...");
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		// Temporary dialogue window to indicate reconstruction is busy. TO BE REPLACED!
		String[] patternUnitNames = new String[internalRootPackagesWithClasses.size()];
		for (int i = 0; i < patternUnitNames.length; i++)
			patternUnitNames[i] = internalRootPackagesWithClasses.get(i).uniqueName;
		MappingGenerator mapgen = new MappingGenerator(currentPattern.getNumberOfModules(), patternUnitNames);
		String[][] patternNames = mapgen.getPermutations();
		float[][] candidateScores = new float[numberOfTopCandidates][2];
		float fitness = 0;
		float lowestTopFitness = 0;
		ServiceProvider.getInstance().getControlService().setValidate(true);
		Instant start = Instant.now();
		for (int i = 0; i < patternNames.length; i++) { // This is where validation is requested and a top 10 is generated.
			logger.info(patternNames[i][0]);
			currentPattern.mapPattern(patternNames[i]);
			fitness = determineFitness(validatePatternCandidate(patternNames[i]));
			if (fitness == -1)
				continue;
			if (i > numberOfTopCandidates - 1) {
				if (fitness > lowestTopFitness) {
					candidateScores[0][0] = fitness;
					candidateScores[0][1] = i;
					sortCandidates(candidateScores);
				}
			} else if (i == numberOfTopCandidates - 1) {
				candidateScores[1][0] = fitness;
				candidateScores[1][1] = i;
				sortCandidates(candidateScores);
				lowestTopFitness = candidateScores[1][0];
			} else {
				candidateScores[numberOfTopCandidates - 1 - i][0] = fitness;
				candidateScores[numberOfTopCandidates - 1 - i][1] = i;
			}
		}
		Instant end = Instant.now();
		ServiceProvider.getInstance().getControlService().setValidate(false);
		dialog.setVisible(false);
		logger.info("Done \n");
		logger.info("ALL CANDIDATE PATTERNS HAVE BEEN VALIDATED FOR ONE ARCHITECTURAL PATTERN IN THE PROVIDED PACKAGE DEFINITIONS.");
		if (pattern == "layered")
			logger.info("Selected architectural pattern: " + currentPattern.getNumberOfModules() + " " + pattern);
		else
			logger.info("Selected architectural pattern: " + pattern);
		logger.info("Number of software units to map to the pattern: " + internalRootPackagesWithClasses.size());
		logger.info("This results in " + patternNames.length + " mappings to test.");
		logger.info("Time needed to map, validate and score all pattern candidates: " + Duration.between(start, end).getSeconds() + " seconds.");
		logger.info("Best " + numberOfTopCandidates + " candidates: ");
		for (int i = 0; i < numberOfTopCandidates; i++) {
			logger.info("Fitness score: " + candidateScores[i][0] + ". Mapped software units unique names: "
					+ Arrays.deepToString(patternNames[(int) candidateScores[i][1]]));
		}
		currentPattern.mapPattern(patternNames[(int) candidateScores[numberOfTopCandidates - 1][1]]);
		logger.info("This last mapping was selected for the intended architecture by default.");
	}
	
	private void sortCandidates(float[][] candidateScores) {
		Arrays.sort(candidateScores, new Comparator<float[]>() {
			
			@Override
			public int compare(float[] o1, float[] o2) {
				float fitness1 = o1[0];
				float fitness2 = o2[0];
				return Float.compare(fitness1, fitness2);
			}
		});
	}
	
	private float determineFitness(int[][] validationResults) {
		if (validationResults[0] == null)
			return -1; // In case of excluded candidate.
		float sumOfWeights = 1;
		int sum = 0;
		for (int t = 0; t < 6; t++) {
			// sum+= weight[t]*numberOfViolations[t];
			if (t == 0)
				logger.info("Number of MustUse violations (should be zero): " + validationResults[1][t]);
			sum += validationResults[1][t];
		}
		return (1 - (1 / (sumOfWeights * (float) validationResults[0][0])) * sum);
	}
	
	private int[][] validatePatternCandidate(String[] unitNames) {
		int[][] results = new int[2][6];
		validateService.checkConformance();
		int[] numberOfViolations = new int[6];
		int i = 6;
		for (RuleDTO currentAppliedRule : defineService.getDefinedRules()) {
			i = determineCategoryIndex(currentAppliedRule.ruleTypeKey);
			if (i == 0) {
				if (validateService.getViolationsByRule(currentAppliedRule).length != 0) {
					logger.info("Candidate was excluded due to violation of MustUse rule(s)");
					results[0] = null;
					results[1] = null;
					return results;
				}
			} else if (i != 6)
				numberOfViolations[i] += validateService.getViolationsByRule(currentAppliedRule).length;
		}
		int[] totalNumberOfDependencies = new int[1];
		for (String name : unitNames) {
			for (String otherName : unitNames) {
				if (name != otherName)
					totalNumberOfDependencies[0] += getNumberofDependenciesBetweenSoftwareUnits(name, otherName);
			}
		}
		logger.info("Number of dependencies within pattern: " + totalNumberOfDependencies[0] + ", resulting in a total of "
				+ IntStream.of(numberOfViolations).sum() + " violations.");
		results[0] = totalNumberOfDependencies;
		results[1] = numberOfViolations;
		return results;
	}
	
	private int determineCategoryIndex(String currentRuleType) {
		if (currentRuleType == "MustUse")
			return 0;
		else if (currentRuleType == "IsNotAllowedToMakeBackCall")
			return 1;
		else if (currentRuleType == "IsNotAllowedToMakeSkipCall")
			return 2;
		else if (currentRuleType == "IsNotAllowedToUse")
			return 3;
		else if (currentRuleType == "IsOnlyAllowedToUse")
			return 4;
		else if (currentRuleType == "IsTheOnlyModuleAllowedToUse")
			return 5;
		else
			return 6;
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
		// mergeLayersPartiallyBasedOnSkipCallAvoidance();
		// Extra step to minimise the number of skip-calls by moving problematic modules to lower layers.
		
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
