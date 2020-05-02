package org.processmining.coarsegrainedchecking.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.plugins.CoarseGrainedConformanceCheckingParameters;
import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;

public class XLogCoarseGrained extends ArrayList<XTraceCoarseGrained> {

	private static final long serialVersionUID = 2215122359278709618L;
	XAttributeMap attributes;
	XLog originalLog;
	
	public XLogCoarseGrained(XLog originalLog, TimestampGranularity granularity, CoarseGrainedConformanceCheckingParameters parameters) {
		this.originalLog = originalLog;
		this.attributes = originalLog.getAttributes();
		
		
		for (XTrace trace : originalLog) {
			XTraceCoarseGrained cgTrace = new XTraceCoarseGrained(trace, granularity, parameters);
			cgTrace.computePossibleResolutions();
			this.add(cgTrace);
		}
	}
	
	public XLogCoarseGrained(XLog originalLog, TimestampGranularity granularity, CoarseGrainedConformanceCheckingParameters parameters, int sampleSize) {
		this.originalLog = originalLog;
		this.attributes = originalLog.getAttributes();
		
		for (XTrace trace : getTraceSample(originalLog, granularity, sampleSize)) {
			XTraceCoarseGrained cgTrace = new XTraceCoarseGrained(trace, granularity, parameters);
			cgTrace.computePossibleResolutions();
			this.add(cgTrace);
		}
	}
	
	private List<XTrace> getTraceSample(XLog originalLog, TimestampGranularity granularity, int sampleSize) {
		   Random rand = new Random(); 
		   
		   List<XTrace> allTraces = new ArrayList<XTrace>(originalLog);
	        List<XTrace> newList = new ArrayList<XTrace>(); 
	        for (int i = 0; i < Math.min(sampleSize, originalLog.size()); i++) { 
	            int randomIndex = rand.nextInt(allTraces.size()); 
	            newList.add(allTraces.get(randomIndex)); 
	            allTraces.remove(randomIndex); 
	        } 
	        return newList; 
	}
	
	
	public XLog getOriginalLog() {
		return originalLog;
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