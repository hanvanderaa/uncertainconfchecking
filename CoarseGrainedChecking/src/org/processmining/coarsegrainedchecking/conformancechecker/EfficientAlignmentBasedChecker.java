package org.processmining.coarsegrainedchecking.conformancechecker;


	import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult.ConformanceMode;
import org.processmining.coarsegrainedchecking.plugins.CoarseGrainedConformanceCheckingParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.log.XLogHelper;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import nl.tue.alignment.Progress;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

	public class EfficientAlignmentBasedChecker implements ConformanceChecker  {

	
		Petrinet net;
		PluginContext context;
		private TransEvClassMapping transEventMap;
		
		ReplayerParameters replayParameters;
		Map<List<String>, Double> fitnessMap;
		private Replayer replayer;
		XAttributeMap logAttributes;
//		long preProcessTimeNanoseconds = 0;
//		int toms = 20000;

		
		public EfficientAlignmentBasedChecker(CoarseGrainedConformanceCheckingParameters pluginParameters, Petrinet net, XLog log, PluginContext context) {
			this.net = net;
			this.context = context;
			this.fitnessMap = new HashMap<List<String>, Double>();
			
			AcceptingPetriNet apn = AcceptingPetriNetFactory.createAcceptingPetriNet(net);
			
			XEventClassifier eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
			XEventClasses classes = logInfo.getEventClasses();
			logAttributes = log.getAttributes();
			transEventMap = computeTransEventMapping(log, net);
			Marking initMarking = apn.getInitialMarking();
			Marking finalMarking = getFinalMarking(net);
			
			
			replayParameters = new ReplayerParameters.Default(2, Debug.NONE);
			replayer = new Replayer(replayParameters, net, initMarking, finalMarking, classes, transEventMap, false);
		}
		
		
		private Marking getFinalMarking(PetrinetGraph net) {
			Marking finalMarking = new Marking();

			for (Place p : net.getPlaces()) {
				if (net.getOutEdges(p).isEmpty())
					finalMarking.add(p);
			}

			return finalMarking;
		}

		
	public double traceFitness(XTrace trace) {
		List<String> traceLabelList = XLogHelper.traceToLabelList(trace);
		if (fitnessMap.containsKey(traceLabelList)) {
			return fitnessMap.get(traceLabelList);
		}
		
		XLog log2=new XLogImpl(logAttributes);
		log2.add(trace);

//		ExecutorService service = Executors.newFixedThreadPool(replayParameters.nThreads);

		try {
			PNRepResult pnrresult  = replayer.computePNRepResult(Progress.INVISIBLE, log2);
			double fitness = (double) pnrresult.getInfo().get(PNRepResult.TRACEFITNESS);
			fitnessMap.put(traceLabelList, fitness);
			return fitness;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		// Setup an array to store the results
//		@SuppressWarnings("unchecked")
//		Future<TraceReplayTask>[] futures = new Future[log2.size()];
//		for (int i = 0; i < log2.size(); i++) {
//			// Setup the trace replay task
//			TraceReplayTask task = new TraceReplayTask(replayer, replayParameters, log2.get(i), i, toms,
//					replayParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
//			// submit for execution
//			futures[i] = service.submit(task);
//		}
//
//		// initiate shutdown and wait for termination of all submitted tasks and obtain results.
//		service.shutdown();
//		for (int i = 0; i < log2.size(); i++) {
//			TraceReplayTask result;
//			try {
//				result = futures[i].get();
//				double fitness = result.getSuccesfulResult().getInfo().get(PNRepResult.RAWFITNESSCOST);
//				fitnessMap.put(traceLabelList, fitness);
//				return fitness;
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}

		return -1.0;

	}




	public boolean isConformant(XTrace trace) {
		return (traceFitness(trace) == 1.0);
	}


	public double computeConformance(XTrace trace, ConformanceMode conformanceMode) {
		if (conformanceMode == ConformanceMode.FITNESS) {
			return traceFitness(trace);
		}
		if (isConformant(trace)) {
			return 1.0;
		}
		return 0.0;
	}


	public static  TransEvClassMapping computeTransEventMapping(XLog log, PetrinetGraph net) {
		XEventClass evClassDummy = EvClassLogPetrinetConnectionFactoryUI.DUMMY;
		TransEvClassMapping mapping = new TransEvClassMapping(XLogInfoImpl.STANDARD_CLASSIFIER, evClassDummy);
		XEventClasses ecLog = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.STANDARD_CLASSIFIER).getEventClasses();
		for (Transition t : net.getTransitions()) {
			//TODO: this part is rather hacky, I'll admit.
			XEventClass eventClass = ecLog.getByIdentity(t.getLabel() + "+complete");
			if (eventClass == null) {
				eventClass = ecLog.getByIdentity(t.getLabel() + "+COMPLETE");
			}
			if (eventClass == null) {
				eventClass = ecLog.getByIdentity(t.getLabel());
			}
			
			if (eventClass != null) {
				mapping.put(t, eventClass);
			} else {
				mapping.put(t, evClassDummy);
				t.setInvisible(true);
			}
		}
		return mapping;
	}
	
//	private static TransEvClassMapping computeTransEventMapping(XLog log, Petrinet net) {
//		XEventClass evClassDummy = EvClassLogPetrinetConnectionFactoryUI.DUMMY;
//		TransEvClassMapping mapping = new TransEvClassMapping(XLogInfoImpl.STANDARD_CLASSIFIER, evClassDummy);
//		XEventClasses ecLog = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.STANDARD_CLASSIFIER).getEventClasses();
//		for (Transition t : net.getTransitions()) {
//			XEventClass eventClass = ecLog.getByIdentity(t.getLabel() + "+complete");
//			if (eventClass != null) {
//				mapping.put(t, eventClass);
//			} else {
//				t.setInvisible(true);
//			}
//		}
//		return mapping;
//	}




	}



