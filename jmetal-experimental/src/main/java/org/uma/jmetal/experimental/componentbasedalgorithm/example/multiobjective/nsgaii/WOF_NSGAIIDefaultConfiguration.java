package org.uma.jmetal.experimental.componentbasedalgorithm.example.multiobjective.nsgaii;

import org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.multiobjective.nsgaii.WOF_NSGAII;
import org.uma.jmetal.experimental.componentbasedalgorithm.algorithm.multiobjective.nsgaii.populatedNSGAII;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.impl.RankingAndDensityEstimatorReplacement;
import org.uma.jmetal.experimental.qualityIndicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT1;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.MultiComparator;
import org.uma.jmetal.util.densityestimator.DensityEstimator;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.ranking.Ranking;
import org.uma.jmetal.util.ranking.impl.FastNonDominatedSortRanking;
import org.uma.jmetal.util.termination.impl.TerminationByQualityIndicator;

import java.util.*;
import java.util.stream.Collectors;

import java.io.IOException;

import static org.uma.jmetal.util.VectorUtils.readVectors;

/**
 * Class to configure and run the NSGA-II algorithm configured with standard settings.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class WOF_NSGAIIDefaultConfiguration extends AbstractAlgorithmRunner {
  public static void main(String[] args) throws JMetalException, IOException {
    long initComputingTime = System.currentTimeMillis();


    Problem<DoubleSolution> problem;
    NSGAII<DoubleSolution> originalAlgorithm;
    NSGAII<DoubleSolution> finalAlgorithm = null;
    populatedNSGAII<DoubleSolution> populatedAlgorithm;
    WOF_NSGAII<DoubleSolution> weightedAlgorithm;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;

    int numDecVars = 4096;
    problem = new ZDT1(numDecVars);
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT1.csv";
    double crossoverProbability = 0.95;
    double crossoverDistributionIndex = 20.0;
    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
    double mutationProbability = 1.0 / problem.getNumberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
    int populationSize = 100;
    int offspringPopulationSize = 100;
    Double maxEvaluations = 1000000.0;
    double currentEvaluations = 0.0;
    int numOfVarGroups = 4;
    int chunks = 3; // q from WOF definition
    Double evalsPerIteration = 10000.0;
    Double iterations = maxEvaluations/evalsPerIteration;
    double iteration = 0.0;
    Double mixedEvalsPercent = 0.5;
    double p = 0.2;

    // Al iniciarse añade 100 evaluaciones
    TerminationByQualityIndicator originalTermination = new TerminationByQualityIndicator(
            new PISAHypervolume(), readVectors(referenceParetoFront, ","),0.9, 7000);
    TerminationByQualityIndicator weightedTermination = new TerminationByQualityIndicator(
            new PISAHypervolume(), readVectors(referenceParetoFront, ","),0.9, 1000);
    TerminationByQualityIndicator finalTermination = new TerminationByQualityIndicator(
            new PISAHypervolume(), readVectors(referenceParetoFront, ","),0.99, (int) (maxEvaluations*mixedEvalsPercent)); //(int)(maxEvaluations * mixedEvalsPercent));
    //System.out.println("final termination evals: " + maxEvaluations*mixedEvalsPercent);

    List<DoubleSolution> originalPopulation = new ArrayList<>();
    List<DoubleSolution> finalPopulation = new ArrayList<>();
    List<DoubleSolution> selectedPopulation = new ArrayList<>();
    List<DoubleSolution> weightedPopulation = new ArrayList<>();
    List<DoubleSolution> chosenSolutions = new ArrayList<>();
    List<DoubleSolution> modifiedPopulation = new ArrayList<>();
    List<DoubleSolution> combinedModifiedPopulations = new ArrayList<>();

    // Crear población original, primero aleatoria, siguientes iteraciones a partir de la población recibida de weighted optimization
      while(currentEvaluations < maxEvaluations * mixedEvalsPercent){ // Si sigue en la primera fase
        if(iteration == 0.0){
          originalAlgorithm = new NSGAII<>(problem, populationSize, offspringPopulationSize, crossover, mutation, originalTermination);
          originalAlgorithm.run();
          originalPopulation = originalAlgorithm.getResult();
        }else{
          populatedAlgorithm = new populatedNSGAII<>(problem, populationSize, offspringPopulationSize, crossover, mutation, originalTermination, weightedPopulation);
          populatedAlgorithm.run();
          originalPopulation = populatedAlgorithm.getResult();
        }

        currentEvaluations += originalTermination.getEvaluations();
        chosenSolutions.add(originalPopulation.get(0));
        chosenSolutions.add(originalPopulation.get((populationSize/2-1)));
        chosenSolutions.add(originalPopulation.get(populationSize-1));

        List<DoubleSolution> newWeightedIndividuals = new ArrayList<>();
          for(int q=0; q<chunks;q++){
            weightedAlgorithm = new WOF_NSGAII<>(problem, populationSize/4, offspringPopulationSize/4, crossover, mutation, weightedTermination, chosenSolutions.get(q));
            weightedAlgorithm.run();
            weightedPopulation = weightedAlgorithm.getResult();
            newWeightedIndividuals.add(weightedPopulation.get(3)); // Extraigo el tercero, por ejemplo, de los tres Wk para obtener 3 vectores de 25 pesos
            currentEvaluations += weightedTermination.getEvaluations();

            modifiedPopulation = originalPopulation.stream().map(solution -> (DoubleSolution)solution.copy()).collect(Collectors.toList());
            // Crear tres nuevas poblaciones S' con los vectores wk sacados de las poblaciones Wk. "aplicar wk a S" (S'=S*W ¿?)(añádir el indivíduo¿? 25 vs 100)
            for(int n=0;n<100; n++){ // 100 indivíduos en la población
              int weightPosition = 0;
                //for(int m=0;m<100; m++){ // 100 variables en cada indivíduo
                for(int m=0;m<numDecVars-1; m++){ // 100 variables en cada indivíduo, o más
                  //System.out.println("var number");
                  //System.out.println(m);
                  Double oldDecVarValue = originalPopulation.get(n).variables().get(m);
                  int mult = originalPopulation.get(0).variables().size()/numOfVarGroups;
                    if(m!=0 && m%mult == 0){ // Si es múltiplo de 25 cambio el peso, para que sean 3
                      weightPosition++;
                    }
                    // Lo que había por un peso
                    //Double newDecVarValue = oldDecVarValue * newWeightedIndividuals.get(0).variables().get(weightPosition);
                    //Double newDecVarValue = oldDecVarValue * newWeightedIndividuals.get(q).variables().get(weightPosition); // 0 -- q
                    Double newDecVarValue = oldDecVarValue + p * (newWeightedIndividuals.get(q).variables().get(weightPosition) - 1.0); // mx - min es 1-0
                    /*System.out.println("weight");
                    System.out.println(newWeightedIndividuals.get(0).variables().get(weightPosition));
                    System.out.println("oldDecVarValue");
                    System.out.println(oldDecVarValue);
                    System.out.println("newDecVarValue");
                    System.out.println(newDecVarValue);*/

                    //modifiedPopulation.get(n).variables().set(m, oldDecVarValue * newWeightedIndividuals.get(0).variables().get(weightPosition));
                    modifiedPopulation.get(n).variables().set(m, newDecVarValue);
                }
            }
            combinedModifiedPopulations.addAll(modifiedPopulation.stream().map(solution -> (DoubleSolution)solution.copy()).collect(Collectors.toList()));
          }
        // Aplicar Ranking y Crowding en la población combinada de soluciones generadas y la original
        combinedModifiedPopulations.addAll(originalPopulation.stream().map(solution -> (DoubleSolution)solution.copy()).collect(Collectors.toList()));
        FastNonDominatedSortRanking testRanking = new FastNonDominatedSortRanking<>();
        DensityEstimator<DoubleSolution> densityEstimator = new CrowdingDistanceDensityEstimator<>();
        RankingAndDensityEstimatorReplacement replacement = new RankingAndDensityEstimatorReplacement(testRanking, densityEstimator, Replacement.RemovalPolicy.oneShot);
        selectedPopulation = replacement.replace2(combinedModifiedPopulations, modifiedPopulation.size());
        weightedPopulation = selectedPopulation; // new originalPopulation has mixed solutions from weightedPopulation
        combinedModifiedPopulations.clear();
        iteration++;
      }// Final de primera fase
    finalAlgorithm = new NSGAII<>(problem, populationSize, offspringPopulationSize, crossover, mutation, finalTermination);
    finalAlgorithm.run();
    finalPopulation = finalAlgorithm.getResult();
    currentEvaluations += finalTermination.getEvaluations();


    long totalComputingTime = System.currentTimeMillis() - initComputingTime ;
    System.out.println("Total computing time: " + totalComputingTime);
    JMetalLogger.logger.info("Successful termination: " + !finalTermination.evaluationsLimitReached()) ;
    JMetalLogger.logger.info("Last quality indicator value: " + finalTermination.getComputedIndicatorValue()) ;
    JMetalLogger.logger.info("Reference front indicator value: " + finalTermination.getReferenceFrontIndicatorValue()) ;
    System.out.println("Total evaluations: " + currentEvaluations);
    System.out.println("Total iterations: " + iteration);
    new SolutionListOutput(weightedPopulation).setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
                                              .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ",")).print();
    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");
    if (!referenceParetoFront.equals("")) {
      printQualityIndicators(finalPopulation, referenceParetoFront);
    }
  }
}
