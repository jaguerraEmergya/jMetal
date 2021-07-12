package org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.ComponentBasedEvolutionaryAlgorithm;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.evaluation.Evaluation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.evaluation.impl.SequentialEvaluation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.impl.RankingAndDensityEstimatorReplacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.selection.MatingPoolSelection;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.selection.impl.NaryTournamentMatingPoolSelection;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.SolutionsCreation;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.variation.impl.CrossoverAndMutationVariation;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.comparator.MultiComparator;
import org.uma.jmetal.util.densityestimator.DensityEstimator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.observable.impl.DefaultObservable;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;
import org.uma.jmetal.util.termination.Termination;

import java.util.*;

/** @author Antonio J. Nebro <antonio@lcc.uma.es> */
@SuppressWarnings("serial")
public class WOF_NSGAII<S extends Solution<?>> extends ComponentBasedEvolutionaryAlgorithm<S> {

  /**
   * Constructor
   *
   * @param evaluation
   * @param initialPopulationCreation
   * @param termination
   * @param selection
   * @param variation
   * @param replacement
   */
  public WOF_NSGAII(
      Evaluation<S> evaluation,
      SolutionsCreation<S> initialPopulationCreation,
      Termination termination,
      MatingPoolSelection<S> selection,
      CrossoverAndMutationVariation<S> variation,
      RankingAndDensityEstimatorReplacement<S> replacement) {
    super(
        "NSGA-II",
        evaluation,
        initialPopulationCreation,
        termination,
        selection,
        variation,
        replacement);
  }

  /** Constructor */
  public WOF_NSGAII(
      Problem<S> problem,
      int populationSize,
      int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator,
      Termination termination,
      Ranking<S> ranking,
      DoubleSolution solution) {
    this.name = "NSGA-II";
    this.problem = problem;
    this.observable = new DefaultObservable<>(name);
    this.attributes = new HashMap<>();

    DensityEstimator<S> densityEstimator = new CrowdingDistanceDensityEstimator<>();
    List<DoubleSolution> randomWeightVector = new ArrayList<>(); // población de 10 que debe ir evolucionando

    int chunksSize = solution.variables().size()/4; // 100/4, 200/4
    int numOfGroups = 4;
    int cont = 0;

    List<Bounds<Double>> bounds = new ArrayList<>(chunksSize);

    Bounds<Double> [] boundsArray = new Bounds[chunksSize];
    for(int i = 0; i<chunksSize-1; i++){
        boundsArray[i] = Bounds.create(0.0, 1.0);
    }
    /*System.out.println("boundsArray");
    for(int i = 0; i<chunksSize-1; i++){
      System.out.println(boundsArray[i].getLowerBound());
    }*/

    bounds = Arrays.asList(boundsArray);
    /*System.out.println("bounds");
    System.out.println(bounds.size());
    for(int i = 0; i<chunksSize-1; i++){
      System.out.println(bounds.get(i).getLowerBound());
    }*/


    //System.out.println("bounds size: " + bounds.size());
    DoubleSolution newWeightVector = new DefaultDoubleSolution(2, bounds);

    for(int j = 0; j<numOfGroups; j++){
      newWeightVector.variables().clear();
      for(int i = 0; i<chunksSize-1; i++){
        Double newValue = solution.variables().get(cont) * (j+1);
        newWeightVector.variables().add(newValue);
        cont++;
      }
      // Guardar los 4 vectores generados con pesos (soluciones) en un Wk de tamaño 4, 4 variables
      // Yo veo mas bien 4 soluciones de 25 variables, hablar con Antonio
      // Uso despues población de esos 4 indivíduos/soluciones para optimizar
      randomWeightVector.add(newWeightVector);
    }

    this.createInitialPopulation = new StaticSolutionsCreation<>(problem, populationSize, randomWeightVector);
    //this.createInitialPopulation = new RandomSolutionsCreation<>(problem, populationSize);

    this.replacement =
        new RankingAndDensityEstimatorReplacement<>(
            ranking, densityEstimator, Replacement.RemovalPolicy.oneShot);

    this.variation =
        new CrossoverAndMutationVariation<>(
            offspringPopulationSize, crossoverOperator, mutationOperator);

    this.selection =
        new NaryTournamentMatingPoolSelection<>(
            2,
            variation.getMatingPoolSize(),
            new MultiComparator<>(
                Arrays.asList(
                    Comparator.comparing(ranking::getRank), Comparator.comparing(densityEstimator::getValue).reversed())));

    this.termination = termination;

    this.evaluation = new SequentialEvaluation<>(problem);

    this.archive = null;
  }

  /** Constructor */
  public WOF_NSGAII(
      Problem<S> problem,
      int populationSize,
      int offspringPopulationSize,
      CrossoverOperator<S> crossoverOperator,
      MutationOperator<S> mutationOperator,
      Termination termination,
      DoubleSolution solution) {
    this(
        problem,
        populationSize,
        offspringPopulationSize,
        crossoverOperator,
        mutationOperator,
        termination,
        new FastNonDominatedSortRanking<>(),
            solution);
  }
}
