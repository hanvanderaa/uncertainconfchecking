package org.processmining.coarsegrainedchecking.probabilisticmodels;

import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.framework.util.Pair;

public class BinaryRelationProbabilisticModel extends AbstractProbabilisticModel {

	private HashMap<Pair<String, String>, Integer> orderedPairCount;
	private HashMap<Pair<String, String>, Integer> pairCount;
	
	public BinaryRelationProbabilisticModel() {
		this.modelName = "BinaryRelationProbabilisticModel";
	}
	
	public void initialize(XLogCoarseGrained cgLog) {
		this.cgLog = cgLog;
		countPairs();
	}

	public double computeProbability(XTraceCoarseGrained cgTrace, XTrace permutation) {
		double prob = 1.0;
		for (int i = 0; i < permutation.size() - 1; i++) {
			String label1 = XConceptExtension.instance().extractName(permutation.get(i)); 
			for (int j = i + 1; j < permutation.size(); j++) {
				String label2 = XConceptExtension.instance().extractName(permutation.get(j));
				Pair<String, String> pair = new Pair<String,String>(label1, label2);
				Pair<String, String> pair2 = new Pair<String,String>(label2, label1);
				
				if (!label1.equals(label2)) {
					int orderedCount = 0;
					if (orderedPairCount.containsKey(pair)) {
						orderedCount = orderedPairCount.get(pair);
					}
					int totalCount = 0;
					if (pairCount.containsKey(pair)) {
						totalCount += pairCount.get(pair);
					}
					if (pairCount.containsKey(pair2)) {
						totalCount += pairCount.get(pair2);
					}
					if (totalCount == 0) {
						return 0.0;
					}
					prob = prob * (orderedCount * 1.0 / totalCount);
				}	
			}
		}
		return prob;
	}
	
	private void countPairs() {
		orderedPairCount = new HashMap<Pair<String, String>, Integer>();
		pairCount = new HashMap<Pair<String, String>, Integer>();
		
		for (XTraceCoarseGrained cgTrace : cgLog) {
			HashSet<Pair<String, String>> seenInTrace = new HashSet<Pair<String, String>>();
			HashSet<Pair<String, String>> seenOrderedInTrace = new HashSet<Pair<String, String>>(); 
			
			for (int i = 0; i < cgTrace.size() - 1; i++) {
				String label1 = XConceptExtension.instance().extractName(cgTrace.get(i)); 
				for (int j = i + 1; j < cgTrace.size(); j++) {
					String label2 = XConceptExtension.instance().extractName(cgTrace.get(j));
					Pair<String, String> pair = new Pair<String,String>(label1, label2);
					
					if (!seenInTrace.contains(pair)) {
						if (pairCount.containsKey(pair)) {
							pairCount.put(pair, pairCount.get(pair) + 1);
						} else {
							pairCount.put(pair, 1);
						}
						seenInTrace.add(pair);
					}
					if (!seenOrderedInTrace.contains(pair)) {
						if (!cgTrace.hasEqualTimestamps(cgTrace.get(i), cgTrace.get(j))) {
							if (orderedPairCount.containsKey(pair)) {
								orderedPairCount.put(pair, orderedPairCount.get(pair) + 1);
							} else {
								orderedPairCount.put(pair, 1);
							}
							seenOrderedInTrace.add(pair);
						}
					}
				}
			}
		}
	}


	

}
