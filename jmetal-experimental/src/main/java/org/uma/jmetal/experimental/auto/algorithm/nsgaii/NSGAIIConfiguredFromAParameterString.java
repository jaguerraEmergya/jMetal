package org.uma.jmetal.experimental.auto.algorithm.nsgaii;

import org.uma.jmetal.experimental.auto.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.observer.impl.EvaluationObserver;
import org.uma.jmetal.util.observer.impl.RunTimeChartObserver;

/**
 * Class configuring NSGA-II using arguments in the form <key, value> and the {@link AutoNSGAII} class.
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 */
public class NSGAIIConfiguredFromAParameterString {

  public static void main(String[] args) {
    String referenceFrontFileName = "DTLZ2.2D.csv" ;

    String[] parameters =
        ("--problemName org.uma.jmetal.problem.multiobjective.lsmop.LSMOP9_2_20 "
                + "--maximumNumberOfEvaluations 200000 "
                + "--referenceFrontFileName "+ referenceFrontFileName + " "
                + " --populationSize 100 --algorithmResult externalArchive --populationSizeWithArchive 96 --createInitialSolutions random --variation crossoverAndMutationVariation --offspringPopulationSize 9 --crossover BLX_ALPHA --crossoverProbability 0.1504 --crossoverRepairStrategy random --blxAlphaCrossoverAlphaValue 0.0333 --mutation polynomial --mutationProbability 0.1075 --mutationRepairStrategy random --polynomialMutationDistributionIndex 67.9223 --selection tournament --selectionTournamentSize 7")
            .split("\\s+");

    AutoNSGAII NSGAII = new AutoNSGAII();
    NSGAII.parseAndCheckParameters(parameters);

    AutoNSGAII.print(NSGAII.fixedParameterList);
    AutoNSGAII.print(NSGAII.autoConfigurableParameterList);

    EvolutionaryAlgorithm<DoubleSolution> nsgaII = NSGAII.create();

    EvaluationObserver evaluationObserver = new EvaluationObserver(1000);
    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>(
            "NSGA-II", 80, 1000,"resources/referenceFrontsCSV/" + referenceFrontFileName);
    //WriteSolutionsToFilesObserver writeSolutionsToFilesObserver = new WriteSolutionsToFilesObserver() ;

    nsgaII.getObservable().register(evaluationObserver);
    nsgaII.getObservable().register(runTimeChartObserver);
    //nsgaII.getObservable().register(writeSolutionsToFilesObserver);

    nsgaII.run();

    System.out.println("Total computing time: " + nsgaII.getTotalComputingTime()) ;

    new SolutionListOutput(nsgaII.getResult())
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();
  }
}
