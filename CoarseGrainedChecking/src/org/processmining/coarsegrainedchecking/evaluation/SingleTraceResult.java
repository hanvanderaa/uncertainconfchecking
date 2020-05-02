package org.processmining.coarsegrainedchecking.evaluation;

import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;

public class SingleTraceResult {
	

	public enum ConformanceMode  { 
		BINARY_CONF, FITNESS
	} ;

	private XTraceCoarseGrained cgTrace;
	private ConformanceMode conformanceMode;
	private boolean approximated;
	private double probConf;
	private double weightedFitness;
	private double resultLowerbound;
	private double resultUpperbound;
	private boolean wasResolved;
	
	
	public SingleTraceResult(XTraceCoarseGrained cgTrace) {
		this.cgTrace = cgTrace;
		approximated = false;
	}
	
	public void setAsApproximated() {
		this.approximated = true;
	}
	
	public boolean isApproximated() {
		return approximated;
	}

	public void setProbabilisticConformance(double probConf) {
		this.probConf = probConf;
	}

	public XTraceCoarseGrained getCgTrace() {
		return cgTrace;
	}

	public double getProbConf() {
		return probConf;
	}


	public double getWeightedFitness() {
		return weightedFitness;
	}

	public double getOriginalFitness() {
		return cgTrace.getOriginalFitness();
	}

	public boolean isOriginalConformant() {
		return cgTrace.originalIsConformant();
	}

	public void setIfResolved(boolean wasResolved) {
		this.wasResolved = wasResolved;
	}
	
	public boolean wasResolved() {
		return wasResolved;
	}

	public double getResultLowerbound() {
		return resultLowerbound;
	}

	public void setResultLowerbound(double resultLowerbound) {
		this.resultLowerbound = resultLowerbound;
	}

	public double getResultUpperbound() {
		return resultUpperbound;
	}

	public void setResultUpperbound(double resultUpperbound) {
		this.resultUpperbound = resultUpperbound;
	}

	public void setConformanceMode(ConformanceMode conformanceMode) {
		this.conformanceMode = conformanceMode;
	}
	
	public ConformanceMode getConformanceMode() {
		return conformanceMode;
	}
	
	public void setWeightedFitness(double fitness) {
		this.weightedFitness = fitness;
	}
			
	
	
}
