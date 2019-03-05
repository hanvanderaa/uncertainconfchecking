package org.processmining.coarsegrainedchecking.evaluation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class SingleModelLogResults {
	
	Petrinet net;
	XLogCoarseGrained cgLog;
	TimestampGranularity granularity;
	AbstractProbabilisticModel probModel;
	long runtime;
	
	Map<XTraceCoarseGrained, Double> traceConfProb = new LinkedHashMap<XTraceCoarseGrained, Double>();
	Map<XTraceCoarseGrained, Double> traceNonConfProb = new LinkedHashMap<XTraceCoarseGrained, Double>();
	Map<XTraceCoarseGrained, Double> traceFitness = new LinkedHashMap<XTraceCoarseGrained, Double>();
	Map<XTraceCoarseGrained, Boolean> traceResolved = new LinkedHashMap<XTraceCoarseGrained, Boolean>();
	Map<XTraceCoarseGrained, Boolean> traceConforming = new LinkedHashMap<XTraceCoarseGrained, Boolean>();
	Map<XTraceCoarseGrained, Integer> rankMap = new LinkedHashMap<XTraceCoarseGrained, Integer>();
	
	double logCoverage;
	double uncertaintyRatio;
	
	public SingleModelLogResults(Petrinet net, XLogCoarseGrained cgLog, 
			TimestampGranularity granularity, AbstractProbabilisticModel probModel) {
		this.net = net;
		this.cgLog = cgLog;
		this.granularity = granularity;
		this.probModel = probModel;
	}
	
	
	public int getRankOfTopPossibleEventSequence(XTraceCoarseGrained cgTrace) {
		if (rankMap.containsKey(cgTrace)) {
			return rankMap.get(cgTrace);
		} else {
			return Integer.MAX_VALUE;
		}
	}

	public void setRankOfTopPossibleEventSequence(int rankOfTopPossibleEventSequence, XTraceCoarseGrained cgTrace) {
		rankMap.put(cgTrace, rankOfTopPossibleEventSequence);
	}


	
	public void setRuntime(long runtime) {
		this.runtime = runtime;
	}
	
	public long getRuntime() {
		return runtime;
	}
	
	public double getFractionOfResolvedTraces() {
		double sum = 0.0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			if (traceResolved.get(cgTrace) == true) {
				sum++;
			}
		}
//		return (sum/getCGTraces().size());
		return sum;
	}
	
	public int getMaxNoPermutations() {
		int max = 0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			if (cgTrace.getPossibleEventSequences().size() > max) {
				max = cgTrace.getPossibleEventSequences().size();
			}
		}
		return max;
	}
	
	public double getAvgNoPermutations() {
		double sum = 0.0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			sum += cgTrace.getPossibleEventSequences().size();
		}
		return (sum * 1.0 /getCGTraces().size());
	}
	
	public int getOriginallyConfTraces() {
		int sum = 0;
		for (XTraceCoarseGrained cgTrace : cgLog) {
			if (cgTrace.originalIsConformant()) {
				sum++;
			}
		}
		return sum;
	}
	
	public int getOriginallyNonConfTraces() {
		return cgLog.size() - getOriginallyConfTraces();
	}
	
	
	public Petrinet getNet() {
		return net;
	}

	public XLogCoarseGrained getCgLog() {
		return cgLog;
	}

	public TimestampGranularity getGranularity() {
		return granularity;
	}
	
	public AbstractProbabilisticModel getProbModel() {
		return probModel;
	}
	
	public Collection<XTraceCoarseGrained> getCGTraces() {
		return traceConfProb.keySet();
	}
	
	public void setResolved(XTraceCoarseGrained cgTrace, boolean resolved) {
		traceResolved.put(cgTrace, resolved);
	}
	
	public void setConfProb(XTraceCoarseGrained cgTrace, double val) {
		traceConfProb.put(cgTrace, val);
	}

	public void setNonConfProb(XTraceCoarseGrained cgTrace, double val) {
		traceNonConfProb.put(cgTrace, val);
	}
	
	public double getConfProb(XTraceCoarseGrained cgTrace) {
		return traceConfProb.get(cgTrace);
	}
	
	public double getNonConfProb(XTraceCoarseGrained cgTrace) {
		return traceNonConfProb.get(cgTrace);
	}
	
	public double getMSE(XTraceCoarseGrained cgTrace) {
		if (cgTrace.originalIsConformant()) {
			return Math.pow((1-getConfProb(cgTrace)),2);
		} else {
			return Math.pow((1-getNonConfProb(cgTrace)),2);
		}
	}

	
	public double getLogConfProb() {
		double sum = 0.0;
		for (double val : traceConfProb.values()) {
			sum += val;
		}
		return sum / traceConfProb.size();
	}
	

	public double getLogConfProbBasedOnConformance(boolean conforming) {
		double sum = 0;
		int total = 0;
		for (XTraceCoarseGrained cgTrace: traceConfProb.keySet()) {
			if (cgTrace.originalIsConformant()==conforming) {
				sum += traceConfProb.get(cgTrace);
				total++;
			}
		}
		return sum / total;
	}
	
	public double getLogNonConfProbBasedOnConformance(boolean conforming) {
		double sum = 0;
		int total = 0;
		for (XTraceCoarseGrained cgTrace: traceNonConfProb.keySet()) {
			if (cgTrace.originalIsConformant()==conforming) {
				sum += traceNonConfProb.get(cgTrace);
				total++;
			}
		}
		return sum / total;
	}
	
	

	public double getLogRMSEBasedOnConformance(boolean conforming) {
		double sum = 0;
		int total = 0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			if (cgTrace.originalIsConformant()==conforming) {
				sum += Math.sqrt(getMSE(cgTrace));
				total++;
			}
		}
		return Math.sqrt(sum / total );
	}
	

	
	public double getLogNonConfProb() {
		double sum = 0;
		for (double val : traceNonConfProb.values()) {
			sum += val;
		}
		return sum / traceNonConfProb.size();
	}
	

	public double getLogRMSE() {
		double sum = 0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			sum += getMSE(cgTrace);
		}
		return Math.sqrt(sum / getCGTraces().size());
	}
	
	public double getLogRMSEFitness() {
		double sum = 0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			sum += Math.pow((cgTrace.getOriginalFitness() - getFitness(cgTrace)),2);
		}
		return Math.sqrt(sum / getCGTraces().size());
	}
	
	
	public double getNoOfLogsWithTopRank(int top) {
		int sum = 0;
		for (XTraceCoarseGrained cgTrace : getCGTraces()) {
			if (getRankOfTopPossibleEventSequence(cgTrace) <= top) {
				sum++;
			}
		}
		return sum;
	}

	public double getLogCoverage() {
		return logCoverage;
	}

	public void setLogCoverage(double logCoverage) {
		this.logCoverage = logCoverage;
	}

	public double getUncertaintyRatio() {
		return uncertaintyRatio;
	}

	public void setUncertaintyRatio(double uncertaintyRatio) {
		this.uncertaintyRatio = uncertaintyRatio;
	}


	public void setFitness(XTraceCoarseGrained cgTrace, double fitness) {
		this.traceFitness.put(cgTrace, fitness);		
	}
	
	public double getFitness(XTraceCoarseGrained cgTrace) {
		return traceFitness.get(cgTrace);
	}
	
	

}
