package husacct.analyse.task.reconstruct.genetic;

import java.util.ArrayList;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.Population;
import org.jgap.audit.EvolutionMonitor;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;

import husacct.analyse.task.reconstruct.ReconstructArchitecture;
import husacct.analyse.task.reconstruct.patterns.Pattern;

public class GeneticAlgorithm {

	private static final int MAX_ALLOWED_EVOLUTIONS = 1;
	private static int numberOfGenes;
	private static int numberOfModules;
	public static EvolutionMonitor m_monitor;
	private static ArrayList<Chromosome> bestSolutions;
	private static ReconstructArchitecture reconstructArchitecture;
	private static Pattern pattern;
	private static int allowRemainder;

	@SuppressWarnings("unchecked")
	public static void determineBestCandidates(boolean a_doMonitor) throws Exception {
		Configuration conf = new DefaultConfiguration();
		// Care that the fittest individual of the current population is always taken to the next generation. With that, the population size may
		// exceed its original size by one.
		conf.setPreservFittestIndividual(true);
		conf.setKeepPopulationSizeConstant(false);
		// Set the fitness function we want to use, which is our GeneticFitnessFunction. We construct it with the target amount of change passed in to
		// this method.
		FitnessFunction myFunc = new GeneticFitnessFunction(pattern, reconstructArchitecture);
		conf.setFitnessFunction(myFunc);
		if (a_doMonitor) {
			// Turn on monitoring/auditing of evolution progress.
			m_monitor = new EvolutionMonitor();
			conf.setMonitor(m_monitor);
		}
		Gene[] sampleGenes = new Gene[numberOfGenes];
		for (int i = 0; i < numberOfGenes; i++) {
			sampleGenes[i] = new IntegerGene(conf, allowRemainder, numberOfModules);
		}
		IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
		conf.setSampleChromosome(sampleChromosome);
		conf.setPopulationSize(100);
		// Create random initial population of Chromosomes.
		Genotype population = Genotype.randomInitialGenotype(conf);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {
			if (!uniqueChromosomes(population.getPopulation()))
				throw new RuntimeException("Invalid state in generation " + i);
			if (m_monitor != null) {
				System.out.println("Begin evolution iteration " + i);
				population.evolve(m_monitor);
				System.out.println("End evolution iteration " + i);
			} else {
				System.out.println("Begin evolution iteration " + i);
				population.evolve();
				System.out.println("End evolution iteration " + i);
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total evolution time: " + (endTime - startTime) + " ms");
		bestSolutions = new ArrayList<Chromosome>(10);
		bestSolutions.addAll(population.getFittestChromosomes(10));
	}

	public static ArrayList<Chromosome> run(Pattern currentPattern, String[] softwareUnits, boolean monitor, ReconstructArchitecture reconstruct,
			boolean remainder) throws Exception {
		if (softwareUnits.length < 2) {
			System.out.println("Too few software units. ");
		} else if (softwareUnits.length > GeneticFitnessFunction.getMaxBounds())
			System.out.println("Too many software units. ");
		else if (currentPattern.getNumberOfModules() > softwareUnits.length)
			System.out.println("Too few pattern modules. ");
		else {
			numberOfGenes = softwareUnits.length;
			numberOfModules = currentPattern.getNumberOfModules();
			reconstructArchitecture = reconstruct;
			pattern = currentPattern;
			if (remainder)
				allowRemainder = 0;
			else
				allowRemainder = 1; // Allowing a Remainder (software units not mapped to any of the pattern's module) means that genes should have
									// allele values starting with 0, otherwise they should start with 1. Hence the integer value here.
		}
		determineBestCandidates(monitor);
		System.out.println("Presenting the ordering of software units mapped in the chromosomes: \n");
		for (int i = 1; i <= softwareUnits.length; i++) {
			System.out.println(i + ": " + softwareUnits[i-1]);
		}
		System.out.println();
		return bestSolutions;
	}

	// Check that all chromosomes are unique
	public static boolean uniqueChromosomes(Population a_pop) {
		for (int i = 0; i < a_pop.size() - 1; i++) {
			IChromosome c = a_pop.getChromosome(i);
			for (int j = i + 1; j < a_pop.size(); j++) {
				IChromosome c2 = a_pop.getChromosome(j);
				if (c == c2)
					return false;
			}
		}
		return true;
	}
}
