package org.processmining.coarsegrainedchecking.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.processmining.coarsegrainedchecking.approximation.AbstractTraceResultComputer;
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
	AbstractTraceResultComputer resultComputer;
	long runtime;
	
	List<SingleTraceResult> traceResults;
	
	
	public SingleModelLogResults(Petrinet net, XLogCoarseGrained cgLog, 
			TimestampGranularity granularity, AbstractProbabilisticModel probModel, AbstractTraceResultComputer resultComputer) {
		this.net = net;
		this.cgLog = cgLog;
		this.granularity = granularity;
		this.probModel = probModel;
		this.resultComputer = resultComputer;
		traceResults = new ArrayList<SingleTraceResult>();
	}
	
	public void addTraceResult(SingleTraceResult traceResult) {
		traceResults.add(traceResult);
	}
	

	public void setRuntime(long runtime) {
		this.runtime = runtime;
	}
	
	public long getRuntime() {
		return runtime;
	}
	
	public int getNumberOfResolvedTraces() {
		int sum = 0;
		for (SingleTraceResult tr : traceResults) {
			if (tr.wasResolved()) {
				sum++;
			}
		}
		return sum;
	}
	
	public int getNumberOfApproximatedTraces() {
		int sum = 0;
		for (SingleTraceResult tr : traceResults) {
			if (tr.isApproximated()) {
				sum++;
			}
		}
		return sum;
	}
	
	public int getNumberOfCheckedTraces() {
		return traceResults.size();
	}
	
	public int getMaxNoPermutations() {
		int max = 0;
		for (SingleTraceResult tr : traceResults) {
			if (tr.getCgTrace().getPossibleResolutions().size() > max) {
				max = tr.getCgTrace().getPossibleResolutions().size();
			}
		}
		return max;
	}
	
	public double getAvgNoPermutations() {
		double sum = 0.0;
		for (SingleTraceResult tr : traceResults) {
			sum += tr.getCgTrace().getPossibleResolutions().size();
		}
		return (sum * 1.0 /traceResults.size());
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
			

	public double getLogConfProb() {
		double sum = 0.0;
		for (SingleTraceResult tr : traceResults) {
			sum += tr.getProbConf();
		}
		return sum / traceResults.size();
	}

	public double getLogRMSE() {
		double sum = 0;
		for (SingleTraceResult tr : traceResults) {
			int originalConf = 0;
			if (tr.getCgTrace().originalIsConformant()) {
				originalConf = 1;
			}
			double error = Math.pow(originalConf - tr.getProbConf(),2);
			sum += error;
		}
		return Math.sqrt(sum / traceResults.size());
	}
	
	public double getLogRMSEFitness() {
		double sum = 0;
		for (SingleTraceResult tr : traceResults) {
			double error = Math.pow(tr.getOriginalFitness() - tr.getWeightedFitness(), 2);
			sum += error;
		}
		return Math.sqrt(sum / traceResults.size());
	}
	
	public AbstractTraceResultComputer getResultComputer() {
		return resultComputer;
	}

	public double getPredictedLogFitness() {
		double sum  = 0;
		for (SingleTraceResult tr : traceResults) {
			sum += tr.getWeightedFitness();
		}
		return sum / traceResults.size();
	}


	public double getOriginalLogFitness() {
		double sum  = 0;
		for (SingleTraceResult tr : traceResults) {
			sum += tr.getCgTrace().getOriginalFitness();
		}
		return sum / traceResults.size();
	}

	public int getOverflownTraces() {
		int sum = 0;
		for (XTraceCoarseGrained cgTrace : cgLog) {
			if (cgTrace.hasResolutionOverflow()) {
				sum++;
			}
		}
		return sum;
	}
	

}
