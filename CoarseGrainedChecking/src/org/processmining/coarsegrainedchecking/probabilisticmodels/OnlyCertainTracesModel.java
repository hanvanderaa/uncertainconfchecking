package org.processmining.coarsegrainedchecking.probabilisticmodels;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;

public class OnlyCertainTracesModel extends AbstractProbabilisticModel {
	
	public OnlyCertainTracesModel() {
		this.modelName = "OnlyCertainTracesModel";
	}
	
	public void initialize(XLogCoarseGrained cgLog) {
		this.cgLog = cgLog;
	}
	
	public double computeProbability(XTraceCoarseGrained cgTrace, XTrace permutation) {
		if (cgTrace.hasUncertainty()) {
			return 0.0;
		}
		return 1.0;
	}
	

}
