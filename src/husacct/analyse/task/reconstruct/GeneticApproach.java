package husacct.analyse.task.reconstruct;

import java.io.File;
import java.io.FileNotFoundException;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.Population;
import org.jgap.UnsupportedRepresentationException;
import org.jgap.audit.EvolutionMonitor;
import org.jgap.data.DataTreeBuilder;
import org.jgap.data.IDataCreators;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.xml.XMLDocumentBuilder;
import org.jgap.xml.XMLManager;
import org.w3c.dom.Document;

public class GeneticApproach {
	/** String containing the CVS revision. Read out via reflection! */
	private final static String CVS_REVISION = "$Revision: 1.27 $";
	
	/**
	 * The total number of times we'll let the population evolve.
	 */
	private static final int MAX_ALLOWED_EVOLUTIONS = 50;
	
	public static EvolutionMonitor m_monitor;
	
	/**
	 * Executes the genetic algorithm to determine the minimum number of coins necessary to make up the given target amount of change. The solution
	 * will then be written to System.out.
	 *
	 * @param a_targetChangeAmount
	 *            the target amount of change for which this method is attempting to produce the minimum number of coins
	 * @param a_doMonitor
	 *            true: turn on monitoring for later evaluation of evolution progress
	 * 			
	 * @throws Exception
	 * 			
	 * @author Neil Rotstan
	 * @author Klaus Meffert
	 * @since 1.0
	 */
	public static void makeChangeForAmount(int a_targetChangeAmount, boolean a_doMonitor) throws Exception {
		// Start with a DefaultConfiguration, which comes setup with the
		// most common settings.
		// -------------------------------------------------------------
		Configuration conf = new DefaultConfiguration();
		// Care that the fittest individual of the current population is
		// always taken to the next generation.
		// Consider: With that, the pop. size may exceed its original
		// size by one sometimes!
		// -------------------------------------------------------------
		conf.setPreservFittestIndividual(true);
		conf.setKeepPopulationSizeConstant(false);
		// Set the fitness function we want to use, which is our
		// GeneticFitnessFunction. We construct it with
		// the target amount of change passed in to this method.
		// ---------------------------------------------------------
		FitnessFunction myFunc = new GeneticFitnessFunction(a_targetChangeAmount);
		conf.setFitnessFunction(myFunc);
		if (a_doMonitor) {
			// Turn on monitoring/auditing of evolution progress.
			// --------------------------------------------------
			m_monitor = new EvolutionMonitor();
			conf.setMonitor(m_monitor);
		}
		// Now we need to tell the Configuration object how we want our
		// Chromosomes to be setup. We do that by actually creating a
		// sample Chromosome and then setting it on the Configuration
		// object. As mentioned earlier, we want our Chromosomes to each
		// have four genes, one for each of the coin types. We want the
		// values (alleles) of those genes to be integers, which represent
		// how many coins of that type we have. We therefore use the
		// IntegerGene class to represent each of the genes. That class
		// also lets us specify a lower and upper bound, which we set
		// to sensible values for each coin type.
		// --------------------------------------------------------------
		Gene[] sampleGenes = new Gene[4];
		sampleGenes[0] = new IntegerGene(conf, 0, 10); // Quarters
		sampleGenes[1] = new IntegerGene(conf, 0, 2); // Dimes
		sampleGenes[2] = new IntegerGene(conf, 0, 1); // Nickels
		sampleGenes[3] = new IntegerGene(conf, 0, 5); // Pennies
		IChromosome sampleChromosome = new Chromosome(conf, sampleGenes);
		conf.setSampleChromosome(sampleChromosome);
		// Finally, we need to tell the Configuration object how many
		// Chromosomes we want in our population. The more Chromosomes,
		// the larger number of potential solutions (which is good for
		// finding the answer), but the longer it will take to evolve
		// the population (which could be seen as bad).
		// ------------------------------------------------------------
		conf.setPopulationSize(20);
		
		// Create random initial population of Chromosomes.
		// Here we try to read in a previous run via XMLManager.readFile(..)
		// for demonstration purpose only!
		// -----------------------------------------------------------------
		Genotype population;
		try {
			Document doc = XMLManager.readFile(new File("JGAPExample32.xml"));
			population = XMLManager.getGenotypeFromDocument(conf, doc);
		} catch (UnsupportedRepresentationException uex) {
			// JGAP codebase might have changed between two consecutive runs.
			// --------------------------------------------------------------
			population = Genotype.randomInitialGenotype(conf);
		} catch (FileNotFoundException fex) {
			population = Genotype.randomInitialGenotype(conf);
		}
		// Now we initialize the population randomly, anyway (as an example only)!
		// If you want to load previous results from file, remove the next line!
		// -----------------------------------------------------------------------
		population = Genotype.randomInitialGenotype(conf);
		// Evolve the population. Since we don't know what the best answer
		// is going to be, we just evolve the max number of times.
		// ---------------------------------------------------------------
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < MAX_ALLOWED_EVOLUTIONS; i++) {
			if (!uniqueChromosomes(population.getPopulation())) {
				throw new RuntimeException("Invalid state in generation " + i);
			}
			if (m_monitor != null) {
				population.evolve(m_monitor);
			} else {
				population.evolve();
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total evolution time: " + (endTime - startTime) + " ms");
		// Save progress to file. A new run of this example will then be able to
		// resume where it stopped before! --> this is completely optional.
		// ---------------------------------------------------------------------
		
		// Represent Genotype as tree with elements Chromomes and Genes.
		// -------------------------------------------------------------
		DataTreeBuilder builder = DataTreeBuilder.getInstance();
		IDataCreators doc2 = builder.representGenotypeAsDocument(population);
		// create XML document from generated tree
		XMLDocumentBuilder docbuilder = new XMLDocumentBuilder();
		Document xmlDoc = (Document) docbuilder.buildDocument(doc2);
		XMLManager.writeFile(xmlDoc, new File("JGAPExample26.xml"));
		// Display the best solution we found.
		// -----------------------------------
		IChromosome bestSolutionSoFar = population.getFittestChromosome();
		double v1 = bestSolutionSoFar.getFitnessValue();
		System.out.println("The best solution has a fitness value of " + bestSolutionSoFar.getFitnessValue());
		bestSolutionSoFar.setFitnessValueDirectly(-1);
		System.out.println("It contains the following: ");
		System.out.println("\t" + GeneticFitnessFunction.getNumberOfCoinsAtGene(bestSolutionSoFar, 0) + " quarters.");
		System.out.println("\t" + GeneticFitnessFunction.getNumberOfCoinsAtGene(bestSolutionSoFar, 1) + " dimes.");
		System.out.println("\t" + GeneticFitnessFunction.getNumberOfCoinsAtGene(bestSolutionSoFar, 2) + " nickels.");
		System.out.println("\t" + GeneticFitnessFunction.getNumberOfCoinsAtGene(bestSolutionSoFar, 3) + " pennies.");
		System.out.println("For a total of " + GeneticFitnessFunction.amountOfChange(bestSolutionSoFar) + " cents in "
				+ GeneticFitnessFunction.getTotalNumberOfCoins(bestSolutionSoFar) + " coins.");
	}
	
	/**
	 * Main method. A single command-line argument is expected, which is the amount of change to create (in other words, 75 would be equal to 75
	 * cents).
	 *
	 * @param args
	 *            amount of change in cents to create
	 * @throws Exception
	 * 			
	 * @author Neil Rotstan
	 * @author Klaus Meffert
	 * @since 1.0
	 */
	public static void run(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Syntax: MinimizingMakeChange <amount>");
		} else {
			int amount = 0;
			try {
				amount = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("The <amount> argument must be a valid integer value");
				System.exit(1);
			}
			if (amount < 1 || amount >= GeneticFitnessFunction.MAX_BOUND) {
				System.out.println("The <amount> argument must be between 1 and " + (GeneticFitnessFunction.MAX_BOUND - 1) + ".");
			} else {
				boolean doMonitor = false;
				if (args.length > 1) {
					String monitoring = args[1];
					if (monitoring != null && monitoring.equals("MONITOR")) {
						doMonitor = true;
					}
				}
				makeChangeForAmount(amount, doMonitor);
			}
		}
	}
	
	/**
	 * @param a_pop
	 *            the population to verify
	 * @return true if all chromosomes in the populationa are unique
	 * 		
	 * @author Klaus Meffert
	 * @since 3.3.1
	 */
	public static boolean uniqueChromosomes(Population a_pop) {
		// Check that all chromosomes are unique
		for (int i = 0; i < a_pop.size() - 1; i++) {
			IChromosome c = a_pop.getChromosome(i);
			for (int j = i + 1; j < a_pop.size(); j++) {
				IChromosome c2 = a_pop.getChromosome(j);
				if (c == c2) {
					return false;
				}
			}
		}
		return true;
	}
}
