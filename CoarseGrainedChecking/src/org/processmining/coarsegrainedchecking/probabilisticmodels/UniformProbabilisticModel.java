package org.processmining.coarsegrainedchecking.probabilisticmodels;

import java.util.HashMap;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;

public class UniformProbabilisticModel extends AbstractProbabilisticModel {

	private HashMap<XTrace, Integer> permCountMap;
	
	public UniformProbabilisticModel() {
		this.modelName = "UniformProbabilisticModel";
	}
	
	public void initialize(XLogCoarseGrained cgLog) {
		this.cgLog = cgLog;
		permCountMap = new HashMap<XTrace, Integer>();
		for (XTraceCoarseGrained cgTrace : cgLog) {
			for (XTrace perm : cgTrace.getPossibleResolutions()) {
				permCountMap.put(perm, cgTrace.getPossibleResolutions().size());
			}
		}
	}
	
	public double computeProbability(XTrace permutation) {
		return 1.0 / permCountMap.get(permutation);
	}
	

}
