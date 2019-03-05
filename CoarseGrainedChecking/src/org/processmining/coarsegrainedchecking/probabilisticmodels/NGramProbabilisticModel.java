package org.processmining.coarsegrainedchecking.probabilisticmodels;

import java.util.ArrayList;
import java.util.HashMap;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.plugins.log.XLogHelper;

public class NGramProbabilisticModel extends AbstractProbabilisticModel {

	int n;
	private HashMap<String, Integer> nGramMap;
	
	
	public NGramProbabilisticModel(XLogCoarseGrained cgLog, int n) {
		super(cgLog);
		this.n = n;
		this.nGramMap = new HashMap<String, Integer>();
		this.modelName = "NGramProbabilisticModel (" + n + ")";
		countNGrams(n);	
		countNGrams(n - 1);
	}
	
	public NGramProbabilisticModel(XLogCoarseGrained cgLog, int n, String modelName) {
		this(cgLog, n);
		this.modelName = modelName;
	}
	
	public int getN() {
		return n;
	}
	

	public double computeProbability(XTrace permutation) {
		if (n > permutation.size()) {
			return 0;
		}
		String numString = "<start," + XLogHelper.traceToString(permutation.subList(0, n - 1));
		String denomString = "<start," + XLogHelper.traceToString(permutation.subList(0, n - 2));
		
		int numerator = getNGramCount(numString);
		int denom = getNGramCount(denomString);
		if (denom == 0) {
			return 0;
		}
		double prob = 1.0 * numerator / denom;
		

		for (int i = 0; i < permutation.size() - n + 1; i++) {
			numString = XLogHelper.traceToString(permutation.subList(i, i + n));
			denomString = XLogHelper.traceToString(permutation.subList(i, i + n - 1));
			numerator = getNGramCount(numString);
			denom = getNGramCount(denomString);
			if (denom == 0) {
				return 0;
			}
			prob = prob * numerator / denom;

		}
		
		numString = XLogHelper.traceToString(permutation.subList(permutation.size() - n + 1, permutation.size())) + ",end>";
		denomString = XLogHelper.traceToString(permutation.subList(permutation.size() - n + 2, permutation.size())) + ",end>";
		
		numerator = getNGramCount(numString);
		denom = getNGramCount(denomString);
		
		if (denom == 0) {
			return 0;
		}
		prob = prob * numerator / denom;
		
		return prob;
	}
	
	private int getNGramCount(String ngramString) {
		if (nGramMap.containsKey(ngramString)) {
			return nGramMap.get(ngramString);
		}
		return 0;
	}
	

		
	private void countNGrams(int ngramsize) {
		
		for (XTraceCoarseGrained cgTrace : cgLog) {
			ArrayList<XEvent> start = cgTrace.getCertainTraceFromStart();
			if (!start.isEmpty() && start.size() >= (ngramsize - 1)) {
				ArrayList<XEvent> startNgram = new ArrayList<XEvent>(start.subList(0, ngramsize - 1));
				String ngramString = "<start," + XLogHelper.traceToString(startNgram);
				int count = 1;
				if (nGramMap.containsKey(ngramString)) {
					count = nGramMap.get(ngramString) + 1;
				}
				nGramMap.put(ngramString, count);
			}
		
		
			ArrayList<XEvent> end = cgTrace.getCertainTraceFromTail();
			if (!end.isEmpty() && end.size() >= (ngramsize - 1)) {
				ArrayList<XEvent> ngram = new ArrayList<XEvent>(end.subList(end.size() - ngramsize + 1, end.size()));
				String ngramString = XLogHelper.traceToString(ngram) + ",end>";
				
				int count = 1;
				if (nGramMap.containsKey(ngramString)) {
					count = nGramMap.get(ngramString) + 1;
				}
				nGramMap.put(ngramString, count);
			}
			
			for (ArrayList<XEvent> certainSubTrace : cgTrace.getCertainSubtraces()) {
				for (int i = 0; i < certainSubTrace.size() - ngramsize + 1; i++) {
					ArrayList<XEvent> ngram = new ArrayList<XEvent>(certainSubTrace.subList(i, i + ngramsize));
					String ngramString = XLogHelper.traceToString(ngram);
					int count = 1;
					if (nGramMap.containsKey(ngramString)) {
						count = nGramMap.get(ngramString) + 1;
					}
					nGramMap.put(ngramString, count);
				}
			}
		}
	}

}
