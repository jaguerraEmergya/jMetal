package org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.SolutionsCreation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class that creates a list of randomly instantiated solutions
 *
 * @param <S>
 */
public class StaticSolutionsCreation<S extends Solution<?>> implements SolutionsCreation<S> {
  private final int numberOfSolutionsToCreate;
  private final Problem<S> problem;
  private List<DoubleSolution> inputSolutionPopulation = new ArrayList<>();

  /**
   * Creates the list of solutions
   * @param problem Problem defining the solutions
   * @param numberOfSolutionsToCreate
   */
  public StaticSolutionsCreation(Problem<S> problem, int numberOfSolutionsToCreate, List<DoubleSolution> solutionPopulation) {
    this.problem = problem;
    this.numberOfSolutionsToCreate = numberOfSolutionsToCreate;
    this.inputSolutionPopulation = solutionPopulation;
  }

  public List<S> create() {
    List<S> solutionList = new ArrayList<>(numberOfSolutionsToCreate);
    //IntStream.range(0, numberOfSolutionsToCreate).forEach(i -> solutionList.add(problem.createSolution()));

    solutionList = (List<S>) inputSolutionPopulation;

    return solutionList;
  }
}
