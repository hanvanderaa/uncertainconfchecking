package org.processmining.coarsegrainedchecking.plugins;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.coarsegrainedchecking.IO.ResultsWriter;
import org.processmining.coarsegrainedchecking.conformancechecker.AlignmentBasedChecker;
import org.processmining.coarsegrainedchecking.evaluation.LogGenerator;
import org.processmining.coarsegrainedchecking.evaluation.ModelSelection;
import org.processmining.coarsegrainedchecking.evaluation.SingleModelLogResults;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.BinaryRelationProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.FullTraceEquivalenceProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.NGramMaxProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.NGramProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.UniformProbabilisticModel;
import org.processmining.coarsegrainedchecking.utils.IOHelper;
import org.processmining.coarsegrainedchecking.utils.NoiseUtils;
import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;


@Plugin(name = "0000 Coarse grained conformance checking workflow", parameterLabels = {"Parameters"}, 
returnLabels = { "Probabilistic conformance checking results" }, returnTypes = {SingleModelLogResults.class},  userAccessible = true)
public class CoarseGrainedConformanceCheckingPlugin {

		CoarseGrainedConformanceCheckingParameters parameters;
	
		Map<String, StochasticNet> netMap;
		
		public static void main(String[] args) throws Exception {
			CoarseGrainedConformanceCheckingPlugin plugin = new CoarseGrainedConformanceCheckingPlugin();
			CoarseGrainedConformanceCheckingParameters parameters = new CoarseGrainedConformanceCheckingParameters();
			plugin.exec(null, parameters);
			System.out.println("Done.");
		}
		
		@UITopiaVariant(affiliation = "", author = "Han van der Aa, Henrik Leopold", email = "han.van.der.aa@hu-berlin.de, h.leopold@vu.nl")
		@PluginVariant(variantLabel = "Run based on default parameters", requiredParameterLabels = {})
		public void exec(UIPluginContext context) throws Exception {
			CoarseGrainedConformanceCheckingParameters parameters = new CoarseGrainedConformanceCheckingParameters();
			
			exec(context, parameters);
			
		}
				
		@UITopiaVariant(affiliation = "", author = "Han van der Aa, Henrik Leopold", email = "han.van.der.aa@hu-berlin.de, henrik.leopold@the-klu.org")
		@PluginVariant(variantLabel = "Run based on specified parameters", requiredParameterLabels = {0})
		public void exec(PluginContext context, CoarseGrainedConformanceCheckingParameters parameters) throws Exception {
			
			this.parameters = parameters;
			
			// Load models and initialize CSV writer
			loadModels(context);
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
			ResultsWriter writer = new ResultsWriter(parameters.OUTPUT_FOLDER + parameters.OUTPUT_FILE + "_" + timeStamp + ".csv", parameters.AGGREGATE_ON_LOG_LEVEL);

			int modelID = 0;
			
			// For each model in the input folder ...
			for (String netName : netMap.keySet()) {
				modelID++;
				System.out.println("Model " + modelID + " " + netName);
				
				// Extract Petri Net object
				StochasticNet net = netMap.get(netName);

				// Obtain logs for net based on name of respective PNML file
				HashSet<XLog> logs = obtainLogs(context, net);
				
				// For each identified log file ...
				for (XLog log: logs) {
					
					// Create course-grained log
					for (TimestampGranularity granularity: parameters.GRANULARITY_LEVELS) {
						XLogCoarseGrained cgLog = new XLogCoarseGrained(log, granularity);
						
						// Instantiate conformance checker
						AlignmentBasedChecker confChecker = new AlignmentBasedChecker(net, log, context);
						
						// Check conformance of original trace 
						for (XTraceCoarseGrained cgTrace : cgLog) {
							double traceFitness = confChecker.traceFitness(cgTrace);
							cgTrace.setOriginalFitness(traceFitness);
							cgTrace.setOriginalConformance( traceFitness == 1.0 );
						}
					
						
						AbstractProbabilisticModel probModel;
						// Run Full Trace Equivalence 
						probModel = new FullTraceEquivalenceProbabilisticModel(cgLog);
						runSingleLogAndNet(log, cgLog, net, writer, probModel, granularity, confChecker);
						
						// Run N-Gram Probabilistic Models 
						for (int n=4;n>1;n--) {
							probModel = new NGramProbabilisticModel(cgLog, n);
							runSingleLogAndNet(log, cgLog, net, writer, probModel, granularity, confChecker);
						}
							
						// Run Binary Relation Probabilistic Model
						probModel = new BinaryRelationProbabilisticModel(cgLog);
						runSingleLogAndNet(log, cgLog, net, writer, probModel, granularity, confChecker);
						
						// Run 2-Gram max model (baseline1)
						probModel = new NGramMaxProbabilisticModel(cgLog, 2);
						runSingleLogAndNet(log, cgLog, net, writer, probModel, granularity, confChecker);
						
						// Run uniform prob model (baseline2)
						probModel = new UniformProbabilisticModel(cgLog);
						runSingleLogAndNet(log, cgLog, net, writer, probModel, granularity, confChecker);
					}
				}
			}
			writer.close();
		}

	private void runSingleLogAndNet(XLog originalLog, XLogCoarseGrained cgLog, Petrinet net, ResultsWriter writer, AbstractProbabilisticModel probModel, TimestampGranularity granularity, AlignmentBasedChecker confChecker) {
		SingleModelLogResults results = new SingleModelLogResults(net, cgLog, granularity, probModel);
		
		double confProb;
		double nonConfProb;
		double naiveConf;
		double fitness;
		long starttime = System.currentTimeMillis();
	
		Iterator<XTraceCoarseGrained> iter = cgLog.iterator();
		while (iter.hasNext()) {
			XTraceCoarseGrained cgTrace = iter.next();
			confProb = 0.0;
			nonConfProb = 0.0;
			naiveConf = 0.0;
			fitness = 0.0;
			
			// Trace is actually uncertain ...
			if (cgTrace.getUncertainSets().size() >= 0) {
				
				// Create map for probabilities of possible event sequences
				HashMap<XTrace, Double> probabilityMap = new HashMap<>();
				
				// Store probabilities
				double totalProb = 0.0;
				boolean resolved = false;
				for (XTrace possibleEventSequence : cgTrace.getPossibleEventSequences()) {
					double sequenceProb;
					if (probModel.isMaxProbModel()) {
						sequenceProb = ((NGramMaxProbabilisticModel) probModel).computeProbability(cgTrace, possibleEventSequence);
					} else {
						sequenceProb = probModel.computeProbability(possibleEventSequence);
					}
					if (sequenceProb > 0) {
						resolved = true;
						probabilityMap.put(possibleEventSequence, sequenceProb);
						totalProb += sequenceProb;
					}
				}
				
				// Derive sorted list (most likely possible event sequence first)
				List<XTrace> sortedPossibleEventSequences = probabilityMap.entrySet().stream()
					    .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
					    .map(Map.Entry::getKey)
					    .collect(Collectors.toList());
				
				// Normalize probabilities to a sum of 1.0
				if (totalProb != 0) {
					for (XTrace t: sortedPossibleEventSequences) {
						probabilityMap.put(t,probabilityMap.get(t)/totalProb);
					}
				}	
				
				double currentTotalConfidence = 0.0;
				double currentTotalConfidence_conf = 0.0;
				double currentTotalConfidence_nonConf = 0.0;
				
				int i = 0;
				boolean identifiedOriginalTrace = false;
				for (XTrace t: sortedPossibleEventSequences) {
					i++;
					if (!identifiedOriginalTrace && cgTrace.matchesOrignialTrace(t)) {
						results.setRankOfTopPossibleEventSequence(i,cgTrace);
						identifiedOriginalTrace = true;
					}
					currentTotalConfidence += probabilityMap.get(t);
					
					double permProb = probabilityMap.get(t);
					
					// Check if possible event sequence is conforming and record probabilities
					cgTrace.setPermutationConformance(t, confChecker.isConformant(t));
					if (cgTrace.getPermutationConformance(t)) {
						confProb += permProb;
						currentTotalConfidence_conf += permProb;
						naiveConf =+ 1.0;
					} else {
						nonConfProb = nonConfProb + permProb;
						currentTotalConfidence_nonConf += permProb;
					}
					
					if (parameters.SCALABILITY_TOP_K && i > parameters.TOP_K) {
						confProb = confProb / (confProb + nonConfProb);
						nonConfProb= nonConfProb / (confProb + nonConfProb);
						break;
					}
					
					if (parameters.SCALABILITY_OPTIMZATION_MAJORITY  && (currentTotalConfidence_conf > 0.5 || currentTotalConfidence_nonConf > 0.5)) {
						confProb = confProb / (confProb + nonConfProb);
						nonConfProb= nonConfProb / (confProb + nonConfProb);
						break;
					}
					
					// Stop considering traces once the Scalability Threshold has been reached
					if (!parameters.SCALABILITY_OPTIMZATION_MAJORITY && parameters.SCALABILITY_OPTIMZATION && currentTotalConfidence >= parameters.SCALABILITY_TRHESHOLD) {
						confProb = confProb / (confProb + nonConfProb);
						nonConfProb= nonConfProb / (confProb + nonConfProb);
						break;
					}
					
					double permFit = confChecker.traceFitness(t); 
					fitness += permProb * permFit;
				}
				
				if (cgTrace.getPossibleEventSequences().size() > 0) {
					
					// Assign naive confProb / nonConfProb in case there was no possible event sequence with a probability of greater than 0
					naiveConf  = naiveConf / cgTrace.getPossibleEventSequences().size();
					if (resolved == false) {
						confProb = naiveConf;
						nonConfProb = 1-naiveConf;
					}
					results.setConfProb(cgTrace, confProb);
					results.setNonConfProb(cgTrace, nonConfProb);
//					results.setNaiveConfProb(cgTrace, naiveConf);
					results.setResolved(cgTrace, resolved);
					results.setFitness(cgTrace, fitness);
				} else {
					iter.remove();
				}
			}
		}
		long runtime = System.currentTimeMillis() - starttime;
		results.setRuntime(runtime);
		
		
		ModelSelection modelSelection = new ModelSelection();
		results.setLogCoverage(modelSelection.computeLogCoverage(cgLog, probModel));
		results.setUncertaintyRatio(modelSelection.computeUncertaintyRatio(cgLog, probModel));
		
		writer.writeResults(results);
	}
	
		
	private Map<String, StochasticNet> loadModels(PluginContext context) {
		netMap = new HashMap<String, StochasticNet>();

		List<File> pnmFiles = IOHelper.getFilesWithExtension(new File(parameters.MODEL_FOLDER), ".pnml");
		for (int i = parameters.MODEL_RANGE_START; i < Math.min(parameters.MODEL_RANGE_END, pnmFiles.size()); i++) {
			File pnmlFile = pnmFiles.get(i);
			StochasticNet net = null;
			try {
				net = IOHelper.importStochasticNet(context, pnmlFile.getAbsolutePath());
			} catch (Exception e) {
				//				e.printStackTrace();
				System.err.println("FAILED TO LOAD MODEL FROM:" + pnmlFile);
			}
			if (net != null) {
				netMap.put(IOHelper.getFileName(pnmlFile), net);
			} else {
				System.out.println("FAILED TO LOAD MODEL FROM:" + pnmlFile);
			}
		}
		System.out.println("Loaded " + netMap.size() + " models from " + parameters.MODEL_FOLDER);
		return netMap;
	}
	
	private HashSet<XLog> obtainLogs(PluginContext context, StochasticNet sNet) {
		HashSet<XLog> logs = new HashSet<>();
		
		if (parameters.GENERATE_NEW_LOGS) {
			LogGenerator logGenerator = new LogGenerator(parameters.LOG_SIZE, parameters.SIMULATION_TIME_UNIT, parameters.SIMULATION_SEED, parameters.ARRIVAL_RATE, parameters.THROUGHPUT_RATE, parameters.MAX_TRACE_LENGTH);
			XLog log = logGenerator.generateLogForNet(context, sNet);
			XAttribute source = new XAttributeLiteralImpl("source", "generated");
			log.getAttributes().put("source", source);
			for (int i = 0; i < parameters.NOISE_LEVELS.length; i++) {
				int noise = parameters.NOISE_LEVELS[i];
				XAttribute noiseAttr = new XAttributeContinuousImpl("noiseLevel", noise);
				XLog noisyLog = null;
				try {
					noisyLog = NoiseUtils.introduceNoise(log, noise);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (noisyLog != null) {
					noisyLog.getAttributes().put("noiseLevel", noiseAttr);
					logs.add(noisyLog);
				}
			}
		}
		
	
		if (!parameters.GENERATE_NEW_LOGS) {
			XLog log = null;
			XFactory  factory = XFactoryRegistry.instance().currentDefault();
			XParser parser = new XesXmlParser(factory);
			
			// Get all files from log folder
			File[] files = new File(parameters.LOG_FOLDER).listFiles();
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".xes")) {
				    
			    	try {
			    		log = parser.parse(file).get(0);
			    		XAttribute source = new XAttributeLiteralImpl("source",file.getName());
			    		log.getAttributes().put("source", source);
			    		System.out.println("Loaded log with: " + log.size() + " traces.");
					} catch (Exception e) {
						System.err.println("FAILED TO LOAD LOG FROM: " + file);
					}
			    	if (log!=null) {
			    		logs.add(log);
			    	}
			    }
			}
		}
		return logs;
	}
	
	
}
