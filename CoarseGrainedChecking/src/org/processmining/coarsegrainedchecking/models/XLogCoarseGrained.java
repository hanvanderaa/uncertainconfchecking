package org.processmining.coarsegrainedchecking.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;

public class XLogCoarseGrained extends ArrayList<XTraceCoarseGrained> {

	private static final long serialVersionUID = 2215122359278709618L;
	XAttributeMap attributes;
	int maxPermutations;
	double avgPermutations;
	XLog originalLog;
	
	public XLogCoarseGrained(XLog originalLog, TimestampGranularity granularity) {
		this.originalLog = originalLog;
		this.attributes = originalLog.getAttributes();
		maxPermutations = 0;
		avgPermutations = 0.0;
		
		
		for (XTrace trace : originalLog) {
			XTraceCoarseGrained cgTrace = new XTraceCoarseGrained(trace, granularity);
			cgTrace.computePermutations();
			if (cgTrace.getPossibleEventSequences().size() > maxPermutations) {
				maxPermutations = cgTrace.getPossibleEventSequences().size();
			}
			avgPermutations += cgTrace.getPossibleEventSequences().size();
			this.add(cgTrace);
		}
		avgPermutations = (avgPermutations*1.0)/originalLog.size();
	}
	
	public XLogCoarseGrained(XLog originalLog, TimestampGranularity granularity, int sampleSize) {
		this.originalLog = originalLog;
		this.attributes = originalLog.getAttributes();
		maxPermutations = 0;
		avgPermutations = 0.0;
		
		for (XTrace trace : getTraceSample(originalLog, granularity, sampleSize)) {
			XTraceCoarseGrained cgTrace = new XTraceCoarseGrained(trace, granularity);
			cgTrace.computePermutations();
			if (cgTrace.getPossibleEventSequences().size() > maxPermutations) {
				maxPermutations = cgTrace.getPossibleEventSequences().size();
			}
			avgPermutations += cgTrace.getPossibleEventSequences().size();
			this.add(cgTrace);
		}
		avgPermutations = (avgPermutations*1.0)/originalLog.size();
	}
	
	private List<XTrace> getTraceSample(XLog originalLog, TimestampGranularity granularity, int sampleSize) {
		   Random rand = new Random(); 
		   
		   List<XTrace> allTraces = new ArrayList<XTrace>(originalLog);
	        List<XTrace> newList = new ArrayList<XTrace>(); 
	        for (int i = 0; i < sampleSize; i++) { 
	            int randomIndex = rand.nextInt(allTraces.size()); 
	            newList.add(allTraces.get(randomIndex)); 
	            allTraces.remove(randomIndex); 
	        } 
	        return newList; 
	}
	
	
	public XLog getOriginalLog() {
		return originalLog;
	}
	
	public int getMaxNoPermutations() {
		return maxPermutations;
	}
	
	public double getAvgNoPermutations() {
		return avgPermutations;
	}
	
	public XAttributeMap getAttributes() {
		return attributes;
	}
	
	public List<XTrace> getXTraces() {
		return new ArrayList<XTrace>(this);
	}
	
	public int uncertainTraceCount() {
		int res = 0;
		for (XTraceCoarseGrained cgTrace : this) {
			if (cgTrace.hasUncertainty()) {
				res++;
			}
		}
		return res;
	}
	
	public int certainTraceCount() {
		return this.size() - uncertainTraceCount();
	}
	
	public int getNoEvents() {
		int n = 0;
		for (XTraceCoarseGrained cgTrace : this) {
			n += cgTrace.size();
		}
		return n;
	}
	
	public int totalEventsInUncertainTraces() {
		int res = 0;
		for (XTraceCoarseGrained cgTrace : this) {
			if (cgTrace.hasUncertainty()) {
				res += cgTrace.size();
			}
		}
		return res;
	}
	
	public int totalEventsInUncertainSets() {
		int res = 0;
		for (XTraceCoarseGrained cgTrace : this) {
			for (Set<XEvent> uncertainSet : cgTrace.getUncertainSets()) {
				res += uncertainSet.size();
			}
		}
		return res;
	}
	

}