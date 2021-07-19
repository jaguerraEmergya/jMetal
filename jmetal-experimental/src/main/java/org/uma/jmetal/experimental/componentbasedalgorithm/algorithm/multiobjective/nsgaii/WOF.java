package org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.ComponentBasedEvolutionaryAlgorithm;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.evaluation.Evaluation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.evaluation.impl.SequentialEvaluation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.impl.RankingAndDensityEstimatorReplacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.selection.MatingPoolSelection;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.selection.impl.NaryTournamentMatingPoolSelection;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.SolutionsCreation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.impl.ExistingListSolutionsCreation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.impl.RandomSolutionsCreation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.variation.impl.CrossoverAndMutationVariation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.variation.impl.ParallelCrossoverAndMutationVariation;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.MultiComparator;
import org.uma.jmetal.util.densityestimator.DensityEstimator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.grouping.CollectionGrouping;
import org.uma.jmetal.util.grouping.impl.ListLinearGrouping;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.MergeNonDominatedSortRanking;
import org.uma.jmetal.util.termination.Termination;
import org.uma.jmetal.util.termination.impl.TerminationByEvaluations;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WOF implements Algorithm<List<DoubleSolution>> {
  ComponentBasedEvolutionaryAlgorithm<DoubleSolution> algorithm;
  CollectionGrouping<List<Double>> groupingMechanism;
  DoubleProblem problem;

  int populationSize;
  int nsgaIIEvaluations;
  int maxEvaluations;
  List<DoubleSolution> population;
  int evaluations;

  public WOF(DoubleProblem problem) {
    this.problem = problem;
    this.groupingMechanism = new ListLinearGrouping<>(4);

    nsgaIIEvaluations = 1000;
    maxEvaluations = 1000000;

    populationSize = 100;
  }

  private NSGAII<DoubleSolution> getNSGAII(
      DoubleProblem problem, List<DoubleSolution> population, int evaluations) {
    int populationSize = 100;
    int offspringPopulationSize = 100;
    int maxNumberOfEvaluations = evaluations;

    DensityEstimator<DoubleSolution> densityEstimator = new CrowdingDistanceDensityEstimator<>();
    Ranking<DoubleSolution> ranking = new MergeNonDominatedSortRanking<>();

    SolutionsCreation<DoubleSolution> initialSolutionsCreation =
        new ExistingListSolutionsCreation<>(population);

    RankingAndDensityEstimatorReplacement<DoubleSolution> replacement =
        new RankingAndDensityEstimatorReplacement<>(
            ranking, densityEstimator, Replacement.RemovalPolicy.oneShot);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    CrossoverOperator<DoubleSolution> crossover =
        new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.getNumberOfVariables();
    double mutationDistributionIndex = 20.0;
    MutationOperator<DoubleSolution> mutation =
        new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    CrossoverAndMutationVariation<DoubleSolution> variation =
        new CrossoverAndMutationVariation<>(offspringPopulationSize, crossover, mutation);

    MatingPoolSelection<DoubleSolution> selection =
        new NaryTournamentMatingPoolSelection<>(
            2,
            variation.getMatingPoolSize(),
            new MultiComparator<>(
                Arrays.asList(
                    Comparator.comparing(ranking::getRank),
                    Comparator.comparing(densityEstimator::getValue).reversed())));

    Termination termination = new TerminationByEvaluations(maxNumberOfEvaluations);

    Evaluation<DoubleSolution> evaluation = new SequentialEvaluation<>(problem);

    NSGAII<DoubleSolution> nsgaII =
        new NSGAII<>(
            evaluation, initialSolutionsCreation, termination, selection, variation, replacement);
    return nsgaII;
  }

  @Override
  public void run() {
    population = new RandomSolutionsCreation<>(problem, populationSize).create();

    this.algorithm = getNSGAII(problem, population, nsgaIIEvaluations);
    evaluations = 0;

    while (evaluations < maxEvaluations) {
      algorithm.run();
      population = algorithm.getPopulation();
      evaluations += nsgaIIEvaluations;

      int q = 10;
      List<DoubleSolution> listOfQSolutions =
          new RankingAndCrowdingSelection<DoubleSolution>(q, new DominanceComparator<>())
              .execute(population);
    }
  }

  private List<DoubleSolution> weightingOptimization(
      DoubleSolution solution,
      DoubleProblem problem,
      ComponentBasedEvolutionaryAlgorithm<DoubleSolution> algorithm,
      CollectionGrouping<List<Double>> grouping) {
    List<DoubleSolution> weightSolutionList = null;

    grouping.computeGroups(solution.variables());

    return weightSolutionList;
  }

  @Override
  public List<DoubleSolution> getResult() {
    return null;
  }

  @Override
  public String getName() {
    return "WOF";
  }

  @Override
  public String getDescription() {
    return "WOF";
  }
}
