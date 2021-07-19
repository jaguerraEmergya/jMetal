package org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.impl;

import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.solutionscreation.SolutionsCreation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class that returns a solution list that has been previously created.
 *
 * @param <S>
 */
public class ExistingListSolutionsCreation<S extends Solution<?>> implements SolutionsCreation<S> {
  private List<S> solutionList;

  /** Creates the list of solutions */
  public ExistingListSolutionsCreation(List<S> solutionList) {
    this.solutionList = solutionList;
  }

  public List<S> create() {
    return solutionList;
  }
}
