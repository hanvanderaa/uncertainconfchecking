package org.processmining.coarsegrainedchecking.plugins;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.coarsegrainedchecking.IO.ResultsWriter;
import org.processmining.coarsegrainedchecking.approximation.AbstractTraceResultComputer;
import org.processmining.coarsegrainedchecking.conformancechecker.AlignmentBasedChecker;
import org.processmining.coarsegrainedchecking.conformancechecker.ConformanceChecker;
import org.processmining.coarsegrainedchecking.conformancechecker.EfficientAlignmentBasedChecker;
import org.processmining.coarsegrainedchecking.evaluation.LogGenerator;
import org.processmining.coarsegrainedchecking.evaluation.SingleModelLogResults;
import org.processmining.coarsegrainedchecking.evaluation.SingleTraceResult;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
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


@Plugin(name = "Coarse grained conformance checking workflow", parameterLabels = {"Parameters"}, 
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
		
		@UITopiaVariant(affiliation = "", author = "Han van der Aa, Henrik Leopold", email = "han.van.der.aa@hu-berlin.de, henrik.leopold@the-klu.org")
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
			String outfile = parameters.OUTPUT_FOLDER + parameters.OUTPUT_FILE + "_" + timeStamp + ".csv";
			ResultsWriter writer = new ResultsWriter(outfile);

			int modelID = 0;
		
			// For each model in the input folder ...
			for (String netName : netMap.keySet()) {
				modelID++;
				System.out.println("Model " + modelID + " " + netName);
				
				// Extract Petri Net object
				StochasticNet net = netMap.get(netName);

				// Obtain logs for net based on name of respective PNML file
				HashSet<XLog> logs = obtainLogs(context, netName, net);
				
				
				
				// For each identified log file ...
				for (XLog log: logs) {
					

					// Create course-grained log
					for (TimestampGranularity granularity: parameters.GRANULARITY_LEVELS) {
						XLogCoarseGrained cgLog = new XLogCoarseGrained(log, granularity, parameters);

						System.out.println("uncertain traces: " + cgLog.uncertainTraceCount());
						System.out.println("uncertain events: " + cgLog.totalEventsInUncertainSets() + " ratio: " + ( (double) cgLog.totalEventsInUncertainSets() / cgLog.getNoEvents()));
						
						// Instantiate conformance checker
						ConformanceChecker confChecker = obtainConformanceChecker(parameters, net, log, context);
						
						// Check conformance of original traces
						Iterator<XTraceCoarseGrained> iter = cgLog.iterator();
						int done = 0;
						while (iter.hasNext() && done < parameters.MAX_TRACES_TO_CHECK) {
							XTraceCoarseGrained cgTrace = iter.next();

							if (cgTrace.hasUncertainty() && !cgTrace.hasResolutionOverflow()) {

								double traceFitness = confChecker.traceFitness(cgTrace);
								cgTrace.setOriginalFitness(traceFitness);
								cgTrace.setOriginalConformance( traceFitness == 1.0 );
								done++;								
								if (done % parameters.TRACE_PROGRESS_DEBUG == 0) {
									System.out.println("Traces done in original log: " + done);
								}
							}		

						}

						
						// loop over different approximation methods. reset confChecker each time for proper time measurement
						int comp = 1;
						for (AbstractTraceResultComputer resultComputer : Arrays.asList(parameters.resultComputers)) {
							confChecker = obtainConformanceChecker(parameters, net, log, context);
							// loop over probabilistic models
							int model = 1;
							for (AbstractProbabilisticModel probModel : Arrays.asList(parameters.probModels)) {
								probModel.initialize(cgLog);
								runSingleLogAndNet(cgLog.getOriginalLog(), cgLog, net, writer, probModel, granularity, confChecker, resultComputer);
								System.out.println("case: " + modelID + " computer: " + comp + " prob. model: " + model + " done");
								model++;
							}
							comp++;
						}
					}
				}
			}
			writer.close();
		}

	private void runSingleLogAndNet(XLog originalLog, XLogCoarseGrained cgLog, Petrinet net, ResultsWriter writer, AbstractProbabilisticModel probModel, 
			TimestampGranularity granularity, ConformanceChecker confChecker, AbstractTraceResultComputer resultComputer) {
		SingleModelLogResults logResults = new SingleModelLogResults(net, cgLog, granularity, probModel, resultComputer);

		long starttime = System.currentTimeMillis();

		Iterator<XTraceCoarseGrained> iter = cgLog.iterator();
		int done = 0;
		while (iter.hasNext() && done < parameters.MAX_TRACES_TO_CHECK) {
			XTraceCoarseGrained cgTrace = iter.next();

			if (cgTrace.hasUncertainty() && !cgTrace.hasResolutionOverflow()) {
				SingleTraceResult traceResult = resultComputer.computeTraceResult(cgTrace, net, probModel, confChecker);
				logResults.addTraceResult(traceResult);
				done++;
				
				if (done % parameters.TRACE_PROGRESS_DEBUG == 0) {
					System.out.println("Traces done: " + done);
				}
			}		
		
		}

		long runtime = System.currentTimeMillis() - starttime;
		logResults.setRuntime(runtime);


		writer.writeResults(logResults);
	}
	
	private ConformanceChecker obtainConformanceChecker(CoarseGrainedConformanceCheckingParameters pluginParameters, Petrinet net, XLog log, PluginContext context) {
		if (pluginParameters.OLD_CONF_CHECKER) {
			return new AlignmentBasedChecker(pluginParameters, net, log, context);
		} else {
			return new EfficientAlignmentBasedChecker(pluginParameters, net, log, context);
		}
	}
	
	
	private Map<String, StochasticNet> loadModels(PluginContext context) {
		netMap = new HashMap<String, StochasticNet>();

		List<File> pnmFiles = IOHelper.getFilesWithExtension(new File(parameters.DATA_PATH), ".pnml");
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
		System.out.println("Loaded " + netMap.size() + " models from " + parameters.DATA_FOLDER);
		return netMap;
	}
	
	private HashSet<XLog> obtainLogs(PluginContext context, String modelName, StochasticNet sNet) {
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
			
			// Get all files from log folder with matching name
			File[] files = new File(parameters.DATA_PATH).listFiles();
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".xes") && IOHelper.getFileName(file).equals(modelName)) {
				    
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
