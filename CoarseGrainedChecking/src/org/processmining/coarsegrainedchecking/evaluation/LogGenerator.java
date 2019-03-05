package org.processmining.coarsegrainedchecking.evaluation;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.TimeUnit;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.StochasticNetSemantics;
import org.processmining.models.semantics.petrinet.impl.EfficientStochasticNetSemanticsImpl;
import org.processmining.plugins.stochasticpetrinet.simulator.PNSimulator;
import org.processmining.plugins.stochasticpetrinet.simulator.PNSimulatorConfig;

public class LogGenerator {

	int logSize;
	TimeUnit timeUnit;
	int seed;
	int arrivalRate;
	int throughputRate;
	int maxTraceSize;

	
	
public LogGenerator(int logSize, TimeUnit timeUnit, int seed, int arrivalRate, int throughputRate, int maxTraceSize) {
		super();
		this.logSize = logSize;
		this.timeUnit = timeUnit;
		this.seed = seed;
		this.arrivalRate = arrivalRate;
		this.throughputRate = throughputRate;
		this.maxTraceSize = maxTraceSize;
	}

public XLog generateLogForNet(PluginContext context, StochasticNet net) {

		assignProcessingTimesToNet(net);
		
		AcceptingPetriNet acceptingNet = AcceptingPetriNetFactory.createAcceptingPetriNet(net);
		Marking initialMarking = acceptingNet.getInitialMarking();
		PNSimulator simulator = new PNSimulator();
		PNSimulatorConfig simConfig = new PNSimulatorConfig(logSize, timeUnit, seed, arrivalRate, maxTraceSize);
		simConfig.setDeterministicBoundedStateSpaceExploration(false);
		
		StochasticNetSemantics semantics = new EfficientStochasticNetSemanticsImpl();
		semantics.initialize(net.getTransitions(), initialMarking);
		XLog log = null;
		try {
			log = simulator.simulate(null, net, semantics, simConfig, initialMarking);
//			System.out.println("number of traces in originally generated log: " +log.size());
			
		} catch (Exception e) {
			System.out.println("FAILED TO GENERATE LOG FOR" + net.getLabel());
		} catch (OutOfMemoryError er) {
			System.out.println("FAILED TO GENERATE LOG FOR" + net.getLabel());
		}
		
		return log;
	}
	
	private void assignProcessingTimesToNet(StochasticNet net ) {
		
		for (Transition t : net.getTransitions()) {
			if (!t.isInvisible()) {
				TimedTransition tt = (TimedTransition) t;
				tt.setDistributionType(DistributionType.EXPONENTIAL);
				tt.setDistribution(new ExponentialDistribution(throughputRate));
				//			tt.setInvisible(false);
			}
		}
	}
	
	

}
