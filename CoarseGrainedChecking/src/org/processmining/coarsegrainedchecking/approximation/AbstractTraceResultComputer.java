package org.processmining.coarsegrainedchecking.approximation;

import org.processmining.coarsegrainedchecking.conformancechecker.ConformanceChecker;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public abstract class AbstractTraceResultComputer {

	
	public abstract SingleTraceResult computeTraceResult(XTraceCoarseGrained cgTrace, Petrinet net, 
			AbstractProbabilisticModel probModel, ConformanceChecker confChecker);
	
	public abstract String getName();
}
