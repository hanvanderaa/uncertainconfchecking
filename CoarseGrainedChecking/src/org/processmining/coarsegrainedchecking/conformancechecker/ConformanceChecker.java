package org.processmining.coarsegrainedchecking.conformancechecker;

import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult.ConformanceMode;

public interface ConformanceChecker {

	
	public double traceFitness(XTrace trace);
	
	public boolean isConformant(XTrace trace);
	
	public double computeConformance(XTrace trace, ConformanceMode conformanceMode);
	
	
}
