package org.processmining.coarsegrainedchecking.probabilisticmodels;

import java.util.HashMap;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.plugins.log.XLogHelper;

public class FullTraceEquivalenceProbabilisticModel extends AbstractProbabilisticModel {

	private HashMap<String, Integer> traceStringCountMap;
	private int certainTraceCount;
	
	
	public FullTraceEquivalenceProbabilisticModel() {
		this.modelName = "FullTraceEquivalenceProbabilisticModel";
	}
	
	public void initialize(XLogCoarseGrained cgLog) {
		this.cgLog = cgLog;
		traceStringCountMap = new HashMap<String, Integer>();
		certainTraceCount = 0;
		for (XTraceCoarseGrained cgTrace : cgLog) {
			if (!cgTrace.hasUncertainty()) {
				String cgTraceString = XLogHelper.traceToString(cgTrace);
				int count = 0;
				if (traceStringCountMap.containsKey(cgTraceString)) {
					count = traceStringCountMap.get(cgTraceString);
				}
				count++;
				traceStringCountMap.put(cgTraceString, count);
				certainTraceCount++;
			}
		}
		
	}
	
	
	public double computeProbability(XTraceCoarseGrained cgTrace, XTrace permutation) {
		if (certainTraceCount == 0) {
			return 0;
		}
		return getTraceCount(permutation) * 1.0 / certainTraceCount;
	}
	
	private int getTraceCount(XTrace permutation) {
		String traceString = XLogHelper.traceToString(permutation);
		if (!traceStringCountMap.containsKey(traceString)) {
			return 0;
		}
		return traceStringCountMap.get(traceString);
	}





}
