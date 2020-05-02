package org.processmining.coarsegrainedchecking.approximation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.TDistribution;
import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.conformancechecker.ConformanceChecker;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult.ConformanceMode;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class StatisticalTraceResultApproximator extends AbstractTraceResultComputer {

	
	ConformanceMode conformanceMode;
	double alpha;
	double deltaThreshold;
	Map<XTrace, Double> probabilityMap;
	
	public StatisticalTraceResultApproximator(ConformanceMode mode, double alpha, double deltaThreshold) {
		this.conformanceMode = mode;
		this.alpha = alpha;
		this.deltaThreshold = deltaThreshold;
	}
	
	public SingleTraceResult computeTraceResult(XTraceCoarseGrained cgTrace, Petrinet net,
			AbstractProbabilisticModel probModel, ConformanceChecker confChecker) {
		this.probabilityMap = probModel.computeTraceProbabilities(cgTrace);
		
		// sort resolutions by probability
		List<XTrace> sortedPossibleEventSequences = probabilityMap.entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		
		Map<XTrace, Double> resultMap = new HashMap<XTrace, Double>();
		List<Double> resultList = new ArrayList<Double>();
		for (XTrace permutation : sortedPossibleEventSequences) {
			if (probabilityMap.get(permutation) > 0.01) {
				double permConf = confChecker.computeConformance(permutation, conformanceMode);
				resultMap.put(permutation, permConf);
				resultList.add(permConf);

				if (isProperDistribution(resultMap)) {

					double knownConf = weightedConformance(resultMap);
					double estimatedConf = estimateConformance(resultMap);
					double errorMargin;
					if (conformanceMode == ConformanceMode.FITNESS) {
						errorMargin = errorMarginNormalDist(resultMap);
					} else {
						errorMargin = errorMarginBinomialDist(resultMap);
					}


					double uncheckedProb = 1 - checkedProbability(resultMap);

					if ( (uncheckedProb * errorMargin / estimatedConf) <= deltaThreshold ) {
						//					System.out.println("permutations: " + cgTrace.getPossibleResolutions().size());
						//					System.out.println("different fitness values:" + new HashSet<Double>(resultList).size() + " checked: " + resultList.size());


						SingleTraceResult traceResult = new SingleTraceResult(cgTrace);
						traceResult.setConformanceMode(conformanceMode);
						traceResult.setAsApproximated();

						traceResult.setWeightedFitness(estimatedConf);
						traceResult.setResultLowerbound(estimatedConf - uncheckedProb * errorMargin);
						traceResult.setResultLowerbound(estimatedConf + uncheckedProb * errorMargin);
						return traceResult;
					} 
				}
			}
		}
		
//		no approximation reached
		SingleTraceResult traceResult = new SingleTraceResult(cgTrace);
		traceResult.setConformanceMode(conformanceMode);
		traceResult.setWeightedFitness( weightedConformance(resultMap));
		return traceResult;

	}
	
	
	private boolean isProperDistribution(Map<XTrace, Double> resultMap) {
		return resultMap.size() > 20;
	}
	
	private double estimateConformance(Map<XTrace, Double> resultMap) {
		double knownConf = weightedConformance(resultMap);
		double uncheckedProb = 1 - checkedProbability(resultMap);
		double sampleAverage = sampleAverage(resultMap);
		
		return knownConf + uncheckedProb * sampleAverage;
	}
	
	private double checkedProbability(Map<XTrace, Double> resultMap) {
		double res = 0.0;
		for (XTrace permutation : resultMap.keySet()) {
			res += probabilityMap.get(permutation);
		}
		return res;
	}
	
	private double weightedConformance(Map<XTrace, Double> resultMap) {
		double res = 0.0;
		for (XTrace permutation : resultMap.keySet()) {
			res += probabilityMap.get(permutation) * resultMap.get(permutation);
		}
		return res;
	}

	private double sampleAverage(Map<XTrace, Double> resultMap) {
		return resultMap.values().stream().mapToDouble(val -> val).average().orElse(0.0);
	}
	
	private double sampleStdev(Map<XTrace, Double> resultMap) {
		double sum = 0;
		double avg = sampleAverage(resultMap);

		for (Double d : resultMap.values()) {
			sum += Math.pow((d - avg), 2);
		}
		return Math.sqrt( sum / ( resultMap.size() - 1 ) ); 
	}
	
	private double errorMarginBinomialDist(Map<XTrace, Double> resultMap) {
		return 0.0;
	}
	
	private double errorMarginNormalDist(Map<XTrace, Double> resultMap) {

		// Create T Distribution with N-1 degrees of freedom
		TDistribution tDist = new TDistribution(resultMap.size() - 1);
		// Calculate critical value
		double critVal = tDist.inverseCumulativeProbability(1.0 - alpha / 2);
		// Calculate confidence interval
		return critVal * sampleStdev(resultMap) / Math.sqrt(resultMap.size());
	}
    
	
	public String getName() {
		return "statistical approximator alpha:" + alpha + " delta: " + deltaThreshold ;
	}

}
