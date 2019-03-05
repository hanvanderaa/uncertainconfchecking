package org.processmining.coarsegrainedchecking.probabilisticmodels;

import java.util.HashMap;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;

public class UniformProbabilisticModel extends AbstractProbabilisticModel {

	private HashMap<XTrace, Integer> permCountMap;
	
	public UniformProbabilisticModel(XLogCoarseGrained cgLog) {
		super(cgLog);
		this.modelName = "UniformProbabilisticModel";
		permCountMap = new HashMap<XTrace, Integer>();
		for (XTraceCoarseGrained cgTrace : cgLog) {
			for (XTrace perm : cgTrace.getPossibleEventSequences()) {
				permCountMap.put(perm, cgTrace.getPossibleEventSequences().size());
			}
		}
	}
	
	public double computeProbability(XTrace permutation) {
		return 1.0 / permCountMap.get(permutation);
	}
	

}
