package org.processmining.coarsegrainedchecking.probabilisticmodels;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;

public class UniformProbabilisticModel extends AbstractProbabilisticModel {
	
	public UniformProbabilisticModel() {
		this.modelName = "UniformProbabilisticModel";
	}
	
	public void initialize(XLogCoarseGrained cgLog) {
		this.cgLog = cgLog;
	}
	
	public double computeProbability(XTraceCoarseGrained cgTrace, XTrace permutation) {
		return 1.0 / cgTrace.getPossibleResolutions().size();
	}
	

}
