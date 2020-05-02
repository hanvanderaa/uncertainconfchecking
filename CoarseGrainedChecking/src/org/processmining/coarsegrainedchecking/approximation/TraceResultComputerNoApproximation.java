package org.processmining.coarsegrainedchecking.approximation;

import java.util.Map;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.conformancechecker.ConformanceChecker;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class TraceResultComputerNoApproximation extends AbstractTraceResultComputer {
	
	String name = "no approximation";

	public SingleTraceResult computeTraceResult(XTraceCoarseGrained cgTrace, Petrinet net, 
			AbstractProbabilisticModel probModel, ConformanceChecker confChecker) {
		
		Map<XTrace, Double> probabilityMap = probModel.computeTraceProbabilities(cgTrace);
		
		double probConf = 0.0;
		double weightedFitness = 0.0;
		for (XTrace resolution: probabilityMap.keySet()) {
			double resolutionProb = probabilityMap.get(resolution);
			if (resolutionProb > 0.01) {
				if (confChecker.isConformant(resolution)) {
					probConf += resolutionProb;
				}
				double permFit = confChecker.traceFitness(resolution);
				weightedFitness += resolutionProb * permFit;	
			}
		}
		SingleTraceResult traceResult = new SingleTraceResult(cgTrace);
		traceResult.setProbabilisticConformance(probConf);
		traceResult.setWeightedFitness(weightedFitness);
		traceResult.setIfResolved(probModel.wasResolved(cgTrace));
		
		return traceResult;
	}


	public String getName() {
		return name;
	}
}
