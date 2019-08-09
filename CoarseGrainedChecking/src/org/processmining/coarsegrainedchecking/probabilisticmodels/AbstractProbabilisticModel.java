package org.processmining.coarsegrainedchecking.probabilisticmodels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;

public abstract class AbstractProbabilisticModel {

	protected XLogCoarseGrained cgLog;
	protected String modelName;
	private Set<XTraceCoarseGrained> resolvedTraces = new HashSet<XTraceCoarseGrained>();

	
	public  String getName() {
		return modelName;
	}
	
	public abstract void initialize(XLogCoarseGrained cgLog);
	
	public Map<XTrace, Double> computeTraceProbabilities(XTraceCoarseGrained cgTrace) {
		HashMap<XTrace, Double> probabilityMap = new HashMap<>();

		for (XTrace resolution : cgTrace.getPossibleResolutions()) {
			double sequenceProb = computeProbability(resolution);
			probabilityMap.put(resolution, sequenceProb);
		}
		if (totalProbability(probabilityMap) > 0) {
			resolvedTraces.add(cgTrace);
		}
		
		normalizeProbabilities(probabilityMap);
		return probabilityMap;
	}

	
	protected abstract double computeProbability(XTrace resolution);
	
	
	public void normalizeProbabilities(Map<XTrace, Double> probabilityMap) {
		double total = totalProbability(probabilityMap);

		// Normalize probabilities to a sum of 1.0
		if (total > 0) {
			for (XTrace t: probabilityMap.keySet()) {
				probabilityMap.put(t, probabilityMap.get(t)/total);
			}
		} 
		// Set uniform probabilities 
		else {
			for (XTrace t: probabilityMap.keySet()) {
				probabilityMap.put(t, 1.0 / probabilityMap.keySet().size() );
			}
		}
	}

		
	private double totalProbability(Map<XTrace, Double> probabilityMap) {
		double sum = 0.0;
		for (Double d : probabilityMap.values()) {
			sum += d;
		}
		return sum;
	}
	
	public boolean wasResolved(XTraceCoarseGrained cgTrace) {
		return resolvedTraces.contains(cgTrace);
	}
	
}
	

