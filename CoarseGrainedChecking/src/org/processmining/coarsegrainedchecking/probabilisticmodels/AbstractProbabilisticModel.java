package org.processmining.coarsegrainedchecking.probabilisticmodels;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;

public abstract class AbstractProbabilisticModel {

	protected XLogCoarseGrained cgLog;
	protected String modelName;
	
	public AbstractProbabilisticModel(XLogCoarseGrained cgLog) {
		this.cgLog = cgLog;
	}
	
	public  String getName() {
		return modelName;
	}
	
	public boolean isMaxProbModel() {
		return false;
	}
	
	public abstract double computeProbability(XTrace permutation);
	
}
