package org.uma.jmetal.component.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;
import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.component.algorithm.multiobjective.NSGAIIBuilder;
import org.uma.jmetal.component.catalogue.common.termination.Termination;
import org.uma.jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.PMXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation;
import org.uma.jmetal.problem.multiobjective.MultiobjectiveTSP;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * Class to configure and run the NSGA-II algorithm to solve a bi-objective TSP.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIITSPExample {
  public static void main(String[] args) throws JMetalException, IOException {

    PermutationProblem<PermutationSolution<Integer>> problem =
        new MultiobjectiveTSP(
            "resources/tspInstances/kroA100.tsp", "resources/tspInstances/kroB100.tsp");

    CrossoverOperator<PermutationSolution<Integer>> crossover = new PMXCrossover(0.9);

    double mutationProbability = 0.2;
    MutationOperator<PermutationSolution<Integer>> mutation = new PermutationSwapMutation<>(mutationProbability);

    int populationSize = 100;
    int offspringPopulationSize = 100;

    Termination termination = new TerminationByEvaluations(125000);

    EvolutionaryAlgorithm<PermutationSolution<Integer>> nsgaii = new NSGAIIBuilder<>(
                    problem,
                    populationSize,
                    offspringPopulationSize,
                    crossover,
                    mutation)
        .setTermination(termination)
        .build();

    nsgaii.run();

    List<PermutationSolution<Integer>> population = nsgaii.result();
    JMetalLogger.logger.info("Total execution time : " + nsgaii.totalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + nsgaii.numberOfEvaluations());

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
            .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
  }
}
