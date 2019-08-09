package org.processmining.coarsegrainedchecking.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;

import org.processmining.coarsegrainedchecking.evaluation.SingleModelLogResults;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.analysis.NonFreeChoiceClustersSet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.log.XLogHelper;
import org.processmining.plugins.petrinet.structuralanalysis.FreeChoiceAnalyzer;

import au.com.bytecode.opencsv.CSVWriter;

public class ResultsWriter {
 
//	private static final NumberFormat formatter = new DecimalFormat("#0.000");
	

	private static final String[] LOG_LEVEL_HEADER = {"Net", "Log", "Prob. Model", "Approximation", "Granularity", "noise",
			"places", "transitions", "silent trans.", "duplicate trans.", "xorsplits", "andsplits", "loops", "skips", "NFC size",
			"conf. traces", "nonconf traces",
			"certain traces", "uncertain traces", "total no. events ", "events in uncertain sets", "avg. no. perm.", "max. no. perm.", "overflow traces", "resolved traces",
			"checked traces", "approximated traces",			
			"confProb", "RMSE (probConf)", "original fitness", "predicted fitnes", "RMSE (fitness)",   "Runtime"};
	
	CSVWriter writer;
	boolean aggregateLogLevel;
	DecimalFormat formatter;
	
	public ResultsWriter(String outFilePath) throws IOException {
		writer = new CSVWriter(new FileWriter(outFilePath), ';');
		writer.writeNext(LOG_LEVEL_HEADER);
				
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.');
//		otherSymbols.setGroupingSeparator('.'); 
		formatter = new DecimalFormat("#0.000", otherSymbols);
		
		
	}
	
	public void writeResults(SingleModelLogResults results) {

			Petrinet net = results.getNet();
			ArrayList <String> csvLine = new ArrayList<String>();
			csvLine.add(net.getLabel());
			csvLine.add(results.getCgLog().getAttributes().get("source").toString());
			csvLine.add(results.getProbModel().getName());
			csvLine.add(results.getResultComputer().getName());
			csvLine.add(String.valueOf(results.getGranularity()));
			try {
				String noise = results.getCgLog().getAttributes().get("noiseLevel").toString();
				csvLine.add(noise);
			} catch (Exception e) {
				csvLine.add("unspecified");
			}
			csvLine.add(String.valueOf(net.getPlaces().size()));
			csvLine.add(String.valueOf(net.getTransitions().size()));
			csvLine.add(String.valueOf(silentTasks(net)));
			csvLine.add(String.valueOf(duplicateTasks(net)));
			csvLine.add(String.valueOf(countXorSplits(net)));
			csvLine.add(String.valueOf(countAndSplits(net)));
			csvLine.add(String.valueOf(XLogHelper.hasLoopsApproximation(results.getCgLog().getXTraces(), net)));
			csvLine.add(String.valueOf(countSkips(net)));
			csvLine.add(String.valueOf(NFCSize(net)));
			
			csvLine.add(String.valueOf(results.getOriginallyConfTraces()));
			csvLine.add(String.valueOf(results.getOriginallyNonConfTraces()));
			csvLine.add(String.valueOf(results.getCgLog().certainTraceCount()));
			csvLine.add(String.valueOf(results.getCgLog().uncertainTraceCount()));
			csvLine.add(String.valueOf(results.getCgLog().getNoEvents()));
			csvLine.add(String.valueOf(results.getCgLog().totalEventsInUncertainSets()));
			csvLine.add(String.valueOf(formatter.format(results.getAvgNoPermutations())));
			csvLine.add(String.valueOf(results.getMaxNoPermutations()));
			csvLine.add(String.valueOf(results.getOverflownTraces()));
			csvLine.add(String.valueOf(results.getNumberOfResolvedTraces()));
			csvLine.add(String.valueOf(results.getNumberOfCheckedTraces()));
			csvLine.add(String.valueOf(results.getNumberOfApproximatedTraces()));
			
			csvLine.add(formatter.format(results.getLogConfProb()));
			csvLine.add(formatter.format(results.getLogRMSE()));
			csvLine.add(formatter.format(results.getOriginalLogFitness()));
			csvLine.add(formatter.format(results.getPredictedLogFitness()));
			csvLine.add(formatter.format(results.getLogRMSEFitness()));
			
			csvLine.add(formatter.format(results.getRuntime()));		
			writer.writeNext(csvLine.stream().toArray(String[]::new));
			System.out.println(csvLine.subList(20, csvLine.size()));
		
		try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int countXorSplits(Petrinet net) {
		int res = 0;
	
		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).size() > 1) {
				res++;
			}
		}
		return res;
	}
	
	private int countAndSplits(Petrinet net) {
		int res = 0;
		for (Transition t : net.getTransitions()) {
			if (net.getOutEdges(t).size() > 1) {
				res++;
			}
		}
		return res;
	}
	
	private int silentTasks(Petrinet net) {
		int n = 0;
		for (Transition t : net.getTransitions()) {
			if (isSilent(t)) {
				n++;
			}
		}
		return n;
	}
	
	private boolean isSilent(Transition t) {
		return (t.getLabel().isEmpty() || t.getLabel().startsWith("tau"));
	}
	
	private int duplicateTasks(Petrinet net) {
		int res = 0;
		
		Set<String> seen = new HashSet<String>();
		for (Transition t : net.getTransitions()) {
			if (!t.getLabel().isEmpty()) {
				if (!seen.add(t.getLabel())) {
					res++;
				}
			}
		}
		return res;
	}
	
	private int countSkips(Petrinet net) {
		int res = 0;
		for (Place p : net.getPlaces()) {
			boolean hasLabeled = false;
			boolean hasSilent = false;
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getOutEdges(p)) {
				Transition t = (Transition) e.getTarget();
				if (isSilent(t)) {
					hasSilent = true;
				} else {
					hasLabeled = true;
				}
			}
			if (hasLabeled && hasSilent) {
				res++;
			}
		}
		return res;
	}
	
	private int NFCSize(Petrinet net) {
		NonFreeChoiceClustersSet clusters = FreeChoiceAnalyzer.getNFCClusters(net);
		int res = 0;
		for (SortedSet<PetrinetNode> cluster : clusters) {
			res = res + cluster.size();
		}
		return res;
	}
}
