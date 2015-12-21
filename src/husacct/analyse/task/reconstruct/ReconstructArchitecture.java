package husacct.analyse.task.reconstruct;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jgap.Chromosome;
import org.jgap.Gene;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.analyse.domain.IModelQueryService;
import husacct.analyse.task.reconstruct.bruteForce.AggregateMappingGenerator;
import husacct.analyse.task.reconstruct.bruteForce.MappingGenerator;
import husacct.analyse.task.reconstruct.genetic.GeneticAlgorithm;
import husacct.analyse.task.reconstruct.patterns.BrokerPattern_CompleteFreedom;
import husacct.analyse.task.reconstruct.patterns.LayeredPattern_FreeRemainder;
import husacct.analyse.task.reconstruct.patterns.MVCPattern_FreeRemainder;
import husacct.analyse.task.reconstruct.patterns.Pattern;
import husacct.common.dto.RuleDTO;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.define.DomainToDtoParser;
import husacct.define.IDefineService;
import husacct.define.domain.module.ModuleStrategy;
import husacct.validate.IValidateService;

/** Software Architecture Reconstruction based on architectural patterns (i.e. NOT design patterns).
 * 
 * @author Joeri Peters */
public class ReconstructArchitecture {

	private final Logger logger = Logger.getLogger(ReconstructArchitecture.class);
	private IModelQueryService queryService;
	private IDefineService defineService;
	private IValidateService validateService;
	private ArrayList<SoftwareUnitDTO> internalRootPackagesWithClasses;
	// The first packages starting from the project root) that contain one or more classes.
	// External system variables
	private String xLibrariesRootPackage = "xLibraries";
	private ArrayList<SoftwareUnitDTO> xLibrariesMainPackages = new ArrayList<SoftwareUnitDTO>();
	// Layer variables
	private TreeMap<Integer, ArrayList<SoftwareUnitDTO>> layers = new TreeMap<Integer, ArrayList<SoftwareUnitDTO>>();
	private int layerThreshold = 10; // Percentage of allowed violating dependencies (back-calls) for adding layers.
	private int skipCallThreshold = 10; // Percentage of allowed violating dependencies (skip-calls) for partially merging layers.

	/** The main method for the ReconstructArchitecture class. Currently requires hard-coded modification instead of arguments.
	 * 
	 * @param queryService */
	public ReconstructArchitecture(IModelQueryService queryService) {
		this.queryService = queryService;
		defineService = ServiceProvider.getInstance().getDefineService();
		validateService = ServiceProvider.getInstance().getValidateService();
		identifyExternalSystems();
		determineInternalRootPackagesWithClasses();
		// determineInternalRootPackagesWithClassesIncludingClasses();
		// If source code is not well structured in a package hierarchy, including individual classes in the root might help. This can make n way too
		// big, though, so be careful if you're taking the brute force approach. This may cause memory issues.

		boolean aggregation = false;
		boolean remainder = false; // Only relevant if aggregation = true.

		String pattern = "Layered";
		int numberOfLayers = 3; // Only matters for N-Layered patterns.

		logger.info("Number of rules before applying patterns: " + defineService.getDefinedRules().length);
		Pattern currentPattern = null;
		switch (pattern) {
			case "Layered":
				// currentPattern = new LayeredPattern_CompleteFreedom(numberOfLayers);
				currentPattern = new LayeredPattern_FreeRemainder(numberOfLayers);
				// currentPattern = new LayeredPattern_IsolatedInternalLayers(numberOfLayers);
				// currentPattern = new LayeredPattern_LayerTypes(numberOfLayers);
				// currentPattern = new LayeredPattern_RestrictedRemainder(numberOfLayers);
				break;
			case "MVC":
				// currentPattern = new MVCPattern_CompleteFreedom();
				// currentPattern = new MVCPattern_ControllerInterface();
				currentPattern = new MVCPattern_FreeRemainder();
				// currentPattern = new MVCPattern_RestrictedRemainder();
				break;
			case "Broker":
				currentPattern = new BrokerPattern_CompleteFreedom();
				// currentPattern = new BrokerPattern_FreeRemainder();
				// currentPattern = new BrokerPattern_RequesterInterface();
				// currentPattern = new BrokerPattern_RestrictedRemainder();
				break;
		}
		currentPattern.insertPattern(); // This adds modules and rules (including exceptions if need be) to the intended architecture.
		logger.info("Number of rules after applying patterns: " + defineService.getDefinedRules().length);
		if (currentPattern != null) {
			bruteForceApproach(currentPattern, remainder, aggregation);
			// geneticApproach(currentPattern, remainder);
		}
	}

	/** SAR approach using a genetic algorithm to find architectural pattern candidates.
	 * 
	 * @param currentPattern
	 * @param aggregates */
	private void geneticApproach(Pattern currentPattern, boolean remainder) {
		try {
			String[] patternUnitNames = new String[internalRootPackagesWithClasses.size()];
			for (int i = 0; i < patternUnitNames.length; i++)
				patternUnitNames[i] = internalRootPackagesWithClasses.get(i).uniqueName;
			ArrayList<Chromosome> bestSolutions = new ArrayList<Chromosome>(10);
			ServiceProvider.getInstance().getControlService().setValidate(true);
			bestSolutions.addAll(GeneticAlgorithm.run(currentPattern, patternUnitNames, true, this, remainder)); // TODO: print best candidates
																													// elsewhere.
			ServiceProvider.getInstance().getControlService().setValidate(false);

			for (int i = 0; i < bestSolutions.size(); i++) {
				Gene[] genes = bestSolutions.get(i).getGenes();
				System.out.println("Chromosome " + (i + 1) + ": ");
				for (int j = 0; j < genes.length; j++) {
					System.out.print(genes[j].getAllele().toString());
				}
				System.out.println("\nFitness value: " + (bestSolutions.get(i).getFitnessValue() - 1));
				if (i == 0) {
					System.out.println("Placing best candidate in defined architecture...");
					int[] bestAlleles = new int[genes.length];
					for (int j = 0; j < genes.length; j++) {
						bestAlleles[j] = (int) genes[j].getAllele();
					}
					placeBestSolutionInIntendedArchitecture(currentPattern, bestAlleles);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** The exhaustive search for architectural pattern candidates as an SAR approach.
	 * 
	 * @param currentPattern
	 * @param aggregates */
	private void bruteForceApproach(Pattern currentPattern, boolean remainder, boolean aggregation) {
		int numberOfTopCandidates = 10;
		String[] patternUnitNames = new String[internalRootPackagesWithClasses.size()];
		for (int i = 0; i < patternUnitNames.length; i++)
			patternUnitNames[i] = internalRootPackagesWithClasses.get(i).uniqueName;
		if (aggregation)
			aggregationBruteForce(currentPattern, numberOfTopCandidates, patternUnitNames, remainder);
		else
			simpleBruteForce(currentPattern, numberOfTopCandidates, patternUnitNames);
	}

	// This is the brute force (try everything!) approach without aggregation.
	private void simpleBruteForce(Pattern currentPattern, int numberOfTopCandidates, String[] patternUnitNames) {
		MappingGenerator mapgen = new MappingGenerator(patternUnitNames, currentPattern.getNumberOfModules());
		ArrayList<String> patternNames = new ArrayList<String>(currentPattern.getNumberOfModules());
		float[][] candidateScores = new float[numberOfTopCandidates][2];
		float fitness = 0;
		float lowestTopFitness = 0;
		ServiceProvider.getInstance().getControlService().setValidate(true);
		patternNames = mapgen.next();
		int candidateNumber = 0;
		while (patternNames != null) {
			// This is where validation is requested and a top 10 is generated.
			currentPattern.mapPattern(patternNames);
			fitness = determineFitness(validatePatternCandidate(patternNames));
			lowestTopFitness = keepScore(numberOfTopCandidates, candidateScores, fitness, lowestTopFitness, candidateNumber);
			patternNames = mapgen.next();
			candidateNumber++;
		}
		ServiceProvider.getInstance().getControlService().setValidate(false);
		sortCandidates(candidateScores);
		logger.info("Done \n");
		logger.info("Best " + numberOfTopCandidates + " candidates: ");
		ArrayList<String> bestMapping;
		for (int i = 0; i < numberOfTopCandidates; i++) {
			// A fitness score between 0 and 1 is more intuitive for the output, but the genetic algorithm requires 1.0 to be the lowest score.
			bestMapping = mapgen.getMapping((int) candidateScores[i][1]);
			logger.info("Fitness score: " + (candidateScores[i][0] - 1) + ". Mapped software units unique names: " + bestMapping);
		}
		bestMapping = mapgen.getMapping((int) candidateScores[numberOfTopCandidates - 1][1]);
		currentPattern.mapPattern(bestMapping);
		logger.info("This last mapping was selected for the intended architecture by default.");
	}

	private float keepScore(int numberOfTopCandidates, float[][] candidateScores, float fitness, float lowestTopFitness, int i) {
		if (fitness == -1)
			return lowestTopFitness;
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
		return lowestTopFitness;
	}

	// This is the aggregation case of the brute force approach.
	private void aggregationBruteForce(Pattern currentPattern, int numberOfTopCandidates, String[] patternUnitNames, boolean remainder) {
		AggregateMappingGenerator mapCal = new AggregateMappingGenerator(patternUnitNames, currentPattern.getNumberOfModules(), false);
		Map<Integer, ArrayList<String>> patternMapping = new HashMap<Integer, ArrayList<String>>();
		// Although this is the brute force approach and not the genetic, the same genetic encoding is used for the mapping here.

		float[][] candidateScores = new float[numberOfTopCandidates][2];
		float fitness = 0;
		float lowestTopFitness = 0;
		ServiceProvider.getInstance().getControlService().setValidate(true);
		int candidateNumber = 0;
		while (true) {
			List<List<String>> map = mapCal.next();
			if (map == null)
				break;
			for (int i = 0; i < map.size(); i++) {
				patternMapping.put(i, (ArrayList<String>) map.get(i));
			}
			currentPattern.mapPatternAllowingAggregates(patternMapping);
			fitness = determineFitness(validatePatternCandidateAllowingAggregates(patternMapping));
			lowestTopFitness = keepScore(numberOfTopCandidates, candidateScores, fitness, lowestTopFitness, candidateNumber);
			candidateNumber++;
			System.out.println("Candidate number: " + candidateNumber);
		}
		sortCandidates(candidateScores);
		int remainderNumber = candidateNumber;
		AggregateMappingGenerator mapCalRemainder = null;
		if (remainder) {
			System.out.println("HERE STARTS THE REMAINDER LOOP.");
			mapCalRemainder = new AggregateMappingGenerator(patternUnitNames, currentPattern.getNumberOfModules(), remainder);
			while (true) {
				List<List<String>> map = mapCalRemainder.next();
				if (map == null)
					break;
				for (int i = 0; i < map.size(); i++) {
					patternMapping.put(i, (ArrayList<String>) map.get(i));
				}
				currentPattern.mapPatternAllowingAggregates(patternMapping);
				fitness = determineFitness(validatePatternCandidateAllowingAggregates(patternMapping));
				lowestTopFitness = keepScore(numberOfTopCandidates, candidateScores, fitness, lowestTopFitness, candidateNumber);
				candidateNumber++;
				System.out.println("Candidate number: " + candidateNumber);
			}
			sortCandidates(candidateScores);
		}
		ServiceProvider.getInstance().getControlService().setValidate(false);
		logger.info("Done \n");
		logger.info("Best " + numberOfTopCandidates + " candidates: ");
		Map<Integer, ArrayList<String>> bestmapping = new HashMap<>();
		for (int i = 0; i < numberOfTopCandidates; i++) {
			// A fitness score between 0 and 1 is more intuitive for the output, but the genetic algorithm requires 1.0 to be the lowest score.
			if ((int) candidateScores[i][1] < remainderNumber)
				bestmapping = mapCal.getMappingMap((int) candidateScores[i][1]);
			else
				bestmapping = mapCalRemainder.getMappingMap((int) candidateScores[i][1] - remainderNumber);
			logger.info("Fitness score: " + (candidateScores[i][0] - 1) + ". Mapped software units unique names: " + bestmapping);
		}
		if ((int) candidateScores[numberOfTopCandidates - 1][1] < remainderNumber) {
			bestmapping = mapCal.getMappingMap((int) candidateScores[numberOfTopCandidates - 1][1]);
			currentPattern.mapPatternAllowingAggregates(bestmapping);
		} else {
			bestmapping = mapCalRemainder.getMappingMap((int) candidateScores[numberOfTopCandidates - 1][1] - remainderNumber);
			currentPattern.mapPatternAllowingAggregates(bestmapping);
		}
		logger.info("This last mapping was selected for the intended architecture by default.");
	}

	/** Fitness scores are calculated for a chromosome. This allows for aggregation (multiple SUs in one pattern module).
	 * 
	 * @param pattern
	 * @param alleles
	 * @return */
	public float getFitnessScore(Pattern pattern, int[] alleles) {
		Map<Integer, ArrayList<String>> patternUnitNames = new HashMap<Integer, ArrayList<String>>();
		for (int i = 0; i < alleles.length; i++) {
			if (alleles[i] != 0) {
				ArrayList<String> temp = new ArrayList<String>(1);
				if (patternUnitNames.get(alleles[i] - 1) != null) {
					temp = patternUnitNames.get(alleles[i] - 1);
					temp.add(internalRootPackagesWithClasses.get(i).uniqueName);
					patternUnitNames.put(alleles[i] - 1, temp);
				} else {
					temp.add(internalRootPackagesWithClasses.get(i).uniqueName);
					patternUnitNames.put(alleles[i] - 1, temp);
				}
			} else
				System.out.println("Remainder: " + internalRootPackagesWithClasses.get(i).uniqueName);
		}
		if (patternUnitNames.keySet().size() == pattern.getNumberOfModules()) {
			pattern.mapPatternAllowingAggregates(patternUnitNames);
			return determineFitness(validatePatternCandidateAllowingAggregates(patternUnitNames));
		} else
			return -1;
	}

	/** Calculate the fitness score for a single candidate, as displayed at the end of a search. This works for all cases (aggregate/remainder,
	 * genetic/brute force).
	 * 
	 * @param validationResults
	 * @return */
	private float determineFitness(int[][] validationResults) {
		if (validationResults[0] == null)
			return -1; // In case of excluded candidate.
		int sum = 0;
		for (int t = 0; t < 6; t++) {
			sum += validationResults[1][t];
		}
		System.out.println("Number of dependencies: " + validationResults[0][0]);
		System.out.println("Number of violations: " + sum);
		return (2 - ((sum) / ((float) validationResults[0][0])));
	}

	private int[][] validatePatternCandidate(ArrayList<String> patternNames) {
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
		for (String name : patternNames) {
			for (String otherName : patternNames) {
				if (name != otherName)
					totalNumberOfDependencies[0] += getNumberofDependenciesBetweenSoftwareUnits(name, otherName);
			}
		}
		// logger.info("Number of dependencies within pattern: " + totalNumberOfDependencies[0] + ", resulting in a total of "
		// + IntStream.of(numberOfViolations).sum() + " violations.");
		results[0] = totalNumberOfDependencies;
		results[1] = numberOfViolations;
		return results;
	}

	/** A particular pattern candidate is validated while allowing for multiple software units assigned to the same pattern module.
	 * 
	 * @param patternUnitNames
	 * @return */
	private int[][] validatePatternCandidateAllowingAggregates(Map<Integer, ArrayList<String>> patternUnitNames) {
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
		for (int j = 0; j < patternUnitNames.size(); j++) {
			for (String name : patternUnitNames.get(j)) {
				for (int k = 0; k < patternUnitNames.size(); k++) {
					for (String otherName : patternUnitNames.get(k)) {
						if (j != k) {
							if (name != otherName)
								totalNumberOfDependencies[0] += getNumberofDependenciesBetweenSoftwareUnits(name, otherName);
						}
					}
				}
			}
		}
		// logger.info("Number of dependencies within pattern: " + totalNumberOfDependencies[0] + ", resulting in a total of "
		// + IntStream.of(numberOfViolations).sum() + " violations.");
		results[0] = totalNumberOfDependencies;
		results[1] = numberOfViolations;
		return results;
	}

	/** Determine the rule type of a rule.
	 * 
	 * @param currentRuleType
	 * @return */
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

	/** Sorting two candidates based on their fitness scores. This uses a custom Comparator.
	 * 
	 * @param candidateScores */
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

	private int getNumberofDependenciesBetweenSoftwareUnits(String fromUnit, String toUnit) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		return analyseService.getDependenciesFromSoftwareUnitToSoftwareUnit(fromUnit, toUnit).length;
	}

	/** Identify the external libraries within the source code. */
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

	/** Determine which packages form the root of the source code hierarchy, excluding single classes. If there is just a single package in that root,
	 * this package becomes the root and packages are identified within it. */
	@SuppressWarnings("unused")
	private void determineInternalRootPackagesWithClasses() {
		internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
		SoftwareUnitDTO[] allRootUnits = queryService.getSoftwareUnitsInRoot(); // Get all root units
		for (SoftwareUnitDTO rootModule : allRootUnits) {
			if (!rootModule.uniqueName.equals(xLibrariesRootPackage)) { // Get all root units that are not libraries
				for (String internalPackage : queryService.getRootPackagesWithClass(rootModule.uniqueName)) { // Get root packages
					internalRootPackagesWithClasses.add(queryService.getSoftwareUnitByUniqueName(internalPackage));
				}
			}
		}
		if (internalRootPackagesWithClasses.size() == 1) {
			// Temporal solution useful for HUSACCT20 test. To be improved!
			// E.g., classes in root are excluded from the process.
			String newRoot = internalRootPackagesWithClasses.get(0).uniqueName;
			internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
			for (SoftwareUnitDTO child : queryService.getChildUnitsOfSoftwareUnit(newRoot)) {
				if (child.type.equalsIgnoreCase("package")) {
					internalRootPackagesWithClasses.add(child);
				}
			}
		}
	}

	/** Determine which packages form the root of the source code hierarchy, including single classes. If there is just a single package in that root,
	 * this package becomes the root and packages are identified within it. */
	@SuppressWarnings("unused")
	private void determineInternalRootPackagesWithClassesIncludingClasses() {
		internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
		SoftwareUnitDTO[] allRootUnits = queryService.getSoftwareUnitsInRoot(); // Get all root units
		for (SoftwareUnitDTO rootModule : allRootUnits) {
			if (!rootModule.uniqueName.equals(xLibrariesRootPackage)) { // Get all root units that are not libraries
				for (String internalPackage : queryService.getRootPackagesWithClass(rootModule.uniqueName)) { // Get root packages
					internalRootPackagesWithClasses.add(queryService.getSoftwareUnitByUniqueName(internalPackage));
				}
			}
		}
		if (internalRootPackagesWithClasses.size() == 1) {
			// Temporal solution useful for HUSACCT20 test. To be improved!
			// E.g., classes in root are excluded from the process.
			String newRoot = internalRootPackagesWithClasses.get(0).uniqueName;
			internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
			for (SoftwareUnitDTO child : queryService.getChildUnitsOfSoftwareUnit(newRoot)) {
				internalRootPackagesWithClasses.add(child);
			}
		}
	}

	// At the end, you want the very best solution to actually be placed in the intended architecture again, so that's what happens here.
	private void placeBestSolutionInIntendedArchitecture(Pattern currentPattern, int[] bestAlleles) {
		Map<Integer, ArrayList<String>> patternUnitNames = new HashMap<Integer, ArrayList<String>>();
		for (int i = 0; i < bestAlleles.length; i++) {
			if (bestAlleles[i] != 0) {
				ArrayList<String> temp = new ArrayList<String>(1);
				if (patternUnitNames.get(bestAlleles[i] - 1) != null) {
					temp = patternUnitNames.get(bestAlleles[i] - 1);
					temp.add(internalRootPackagesWithClasses.get(i).uniqueName);
					patternUnitNames.put(bestAlleles[i] - 1, temp);
				} else {
					temp.add(internalRootPackagesWithClasses.get(i).uniqueName);
					patternUnitNames.put(bestAlleles[i] - 1, temp);
				}
			}
		}
		if (patternUnitNames.keySet().size() == currentPattern.getNumberOfModules()) {
			currentPattern.mapPatternAllowingAggregates(patternUnitNames);
			validatePatternCandidateAllowingAggregates(patternUnitNames);
			System.out.println("Best candidate was successfully mapped and validated.");
		} else
			System.out.println("Failed to map best candidate");
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
		defineService.addRule(new RuleDTO(ruleType, true, domainParser.parseModule(moduleTo), domainParser.parseModule(moduleFrom), new String[0], "", null, false));
	}

	/** Automatic layering algorithm, to be improved. */
	private void identifyLayers() {
		// 1) Assign all internalRootPackages to bottom layer
		int layerId = 1;
		ArrayList<SoftwareUnitDTO> assignedUnits = new ArrayList<SoftwareUnitDTO>();
		assignedUnits.addAll(internalRootPackagesWithClasses);
		layers.put(layerId, assignedUnits);

		// 2) Identify the bottom layer. Look for packages with dependencies to
		// external systems only.
		identifyTopLayerBasedOnUnitsInBottomLayer(layerId);

		// 3) Look iteratively for packages on top of the bottom layer, et
		// cetera.
		while (layers.lastKey() > layerId) {
			layerId++;
			identifyTopLayerBasedOnUnitsInBottomLayer(layerId);
		}
		// mergeLayersPartiallyBasedOnSkipCallAvoidance();
		// Extra step to minimise the number of skip-calls by moving problematic
		// modules to lower layers.

		// 4) Add the layers to the intended architecture
		int highestLevelLayer = layers.size();
		if (highestLevelLayer > 1) {
			// Reverse the layer levels. The numbering of the layers within the
			// intended architecture is different: the highest level layer has
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
					int nrOfDependenciesFromOtherTosoftwareUnit = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(otherSoftwareUnit.uniqueName,
							softwareUnit.uniqueName).length;
					if (nrOfDependenciesFromsoftwareUnitToOther > ((nrOfDependenciesFromOtherTosoftwareUnit / 100) * layerThreshold)) {
						rootPackageDoesNotUseOtherPackage = false;
					}
				}
			}
			if (rootPackageDoesNotUseOtherPackage) { // Leave unit in the lower
														// layer
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
					nrOfDependenciesFromSoftwareUnitToOtherWithinLayer += queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName,
							otherSoftwareUnit.uniqueName).length;
				}
				for (int currentLowerLayerID = currentLayerID - 2; currentLowerLayerID < 0; currentLowerLayerID--) {
					unitsInCurrentLowerLayer = layers.get(currentLowerLayerID);
					for (SoftwareUnitDTO lowerSoftwareUnit : unitsInCurrentLowerLayer) {
						if (!lowerSoftwareUnit.uniqueName.equals(softwareUnit.uniqueName)) {
							nrOfSkipCallsFromSoftwareUnitToOtherLayers += queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName,
									lowerSoftwareUnit.uniqueName).length;
						}
					}
				}
				if (nrOfSkipCallsFromSoftwareUnitToOtherLayers < ((nrOfDependenciesFromSoftwareUnitToOtherWithinLayer / 100) * skipCallThreshold)) {
					assignedUnitsNewUpperLayer.add(softwareUnit);
					// Keep in current layer.
				} else {
					assignedUnitsNewLowerLayer.add(softwareUnit);
					// Move to one layer below.
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
}
