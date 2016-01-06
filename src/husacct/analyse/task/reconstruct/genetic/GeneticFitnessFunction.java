package husacct.analyse.task.reconstruct.genetic;

import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

import husacct.analyse.task.reconstruct.ReconstructArchitecture;
import husacct.analyse.task.reconstruct.patterns.Pattern;

public class GeneticFitnessFunction extends FitnessFunction {
	
	private static final long serialVersionUID = 1L;
	public final static int MAX_BOUND = 4000;
	private ReconstructArchitecture reconstruct;
	private Pattern pattern;

	public GeneticFitnessFunction(Pattern currentPattern, ReconstructArchitecture reconstructArchitecture) {
		reconstruct = reconstructArchitecture;
		pattern = currentPattern;
	}

	// Determine fitness value for given a chromosome (pattern candidate). Higher fitness should mean a better candidate throughout this class.

	public double evaluate(IChromosome a_subject) {
		// Take care of the fitness evaluator. It could either be weighting higher fitness values higher (e.g.DefaultFitnessEvaluator). Or it could
		// weigh lower fitness values higher, because the fitness value is seen as a defect rate (e.g. DeltaFitnessEvaluator)
		boolean defaultComparation = a_subject.getConfiguration().getFitnessEvaluator().isFitter(2, 1);
		if (defaultComparation == false) {
			return 1.0;
		}
		Gene[] genes = a_subject.getGenes();
		int[] alleles = new int[genes.length];
		for (int i = 0; i < genes.length; i++) {
			alleles[i] = (int) genes[i].getAllele();
		}
		double fitness = reconstruct.getFitnessScore(pattern, alleles);
//		System.out.println("Fitness score: " + fitness);
		return Math.max(0.0d, fitness);
	}

	public static int getMaxBounds() {
		return MAX_BOUND;
	}
}
