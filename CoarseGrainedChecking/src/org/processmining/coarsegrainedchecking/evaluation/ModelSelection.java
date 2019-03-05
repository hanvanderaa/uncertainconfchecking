package org.processmining.coarsegrainedchecking.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.processmining.coarsegrainedchecking.models.XLogCoarseGrained;
import org.processmining.coarsegrainedchecking.models.XTraceCoarseGrained;
import org.processmining.coarsegrainedchecking.probabilisticmodels.AbstractProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.BinaryRelationProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.FullTraceEquivalenceProbabilisticModel;
import org.processmining.coarsegrainedchecking.probabilisticmodels.NGramProbabilisticModel;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.log.XLogHelper;

public class ModelSelection {
//	XLogCoarseGrained cgLog;
//	AbstractProbabilisticModel probModel;
//	
//	public ModelSelection(XLogCoarseGrained cgLog, AbstractProbabilisticModel probModel) {
//		this.cgLog = cgLog;
//		this.probModel = probModel;
//	}
	
	public double computeLogCoverage(XLogCoarseGrained cgLog, AbstractProbabilisticModel probModel) {
		int resolvedCount = 0;
		int unresolvedCount = 0;
		for (XTraceCoarseGrained cgTrace : cgLog) {
			if (cgTrace.hasUncertainty()) {
				if (probModel.computeProbability(cgTrace) > 0) {
					resolvedCount++;
				} else {
					unresolvedCount++;
				}
			}
		}
		
		return resolvedCount * 1.0 / (resolvedCount + unresolvedCount);
	}
	
	public double computeUncertaintyRatio(XLogCoarseGrained cgLog, AbstractProbabilisticModel probModel) {
		List<Double> ratios = new ArrayList<Double>();
		Set<String> eventClasses = XLogHelper.extractEventClasses(cgLog.getOriginalLog());
		for (String eventClass1 : eventClasses) {
			for (String eventClass2 : eventClasses) {
				if (eventClass1.equals(eventClass2)) {
					continue;
				}
				
				int support = support(cgLog, probModel, eventClass1, eventClass2);
				int uncertainTraces = uncertainTraces(cgLog, eventClass1, eventClass2);
				
				double ratio = 0;
				if (support != 0) {
					ratio = uncertainTraces * 1.0 / support;
				}
				ratios.add(ratio);
			}
		}
		
		double sum = 0;
		for (double ratio : ratios) {
			sum+= ratio;
		}
		return sum/ratios.size();
	}
	
	
	private int support(XLogCoarseGrained cgLog, AbstractProbabilisticModel probModel, String eventClass1, String eventClass2) {
		int count = 0;
		for (XTraceCoarseGrained cgTrace : cgLog) {
			if (supp(cgTrace, probModel, eventClass1, eventClass2)) {
				count++;
			}
		}
		return count;
	}
	
	private int uncertainTraces(XLogCoarseGrained cgLog, String eventClass1, String eventClass2) {
		int count = 0;
		for (XTraceCoarseGrained cgTrace : cgLog) {
			if (hasUncertainty(cgTrace, eventClass1, eventClass2)) {
				count++;
			}
		}
		return count;
	}
	
	private boolean supp(XTraceCoarseGrained cgTrace, AbstractProbabilisticModel probModel, String eventClass1, String eventClass2) {
		if (probModel.getClass() == FullTraceEquivalenceProbabilisticModel.class) {
			return suppFTE(cgTrace, eventClass1, eventClass2);
		}
		
		if (probModel.getClass() == NGramProbabilisticModel.class) {
			int ngramsize = ((NGramProbabilisticModel) probModel).getN();
			return suppNGram(ngramsize, cgTrace, eventClass1, eventClass2);
		}
		
		if (probModel.getClass() == BinaryRelationProbabilisticModel.class) {
			return suppBinaryRelation(cgTrace, eventClass1, eventClass2);
		}		
		
		return false;
	}
	
	
	private boolean suppFTE(XTraceCoarseGrained cgTrace, String eventClass1, String eventClass2) {
		if (cgTrace.hasUncertainty()) {
			return false;
		}
		
		String cgTraceString = XLogHelper.traceToString(cgTrace);
		String nGram12 = eventClass1 + XLogHelper.SEPARATOR_STRING + eventClass2;
		String nGram21 = eventClass1 + XLogHelper.SEPARATOR_STRING + eventClass2;
		
		return cgTraceString.contains(nGram12) || cgTraceString.contains(nGram21);
	}
	
	private boolean suppNGram(int ngramsize, XTraceCoarseGrained cgTrace, String eventClass1, String eventClass2) {
		String nGram12 = eventClass1 + XLogHelper.SEPARATOR_STRING + eventClass2;
		String nGram21 = eventClass1 + XLogHelper.SEPARATOR_STRING + eventClass2;
		
		ArrayList<XEvent> start = cgTrace.getCertainTraceFromStart();
		if (!start.isEmpty() && start.size() >= (ngramsize - 1)) {
			ArrayList<XEvent> startNgram = new ArrayList<XEvent>(start.subList(0, ngramsize - 1));
			String ngramString = "<start," + XLogHelper.traceToString(startNgram);
			if (ngramString.contains(nGram12) || ngramString.contains(nGram21)) {
				return true;
			}
		}

		ArrayList<XEvent> end = cgTrace.getCertainTraceFromTail();
		if (!end.isEmpty() && end.size() >= (ngramsize - 1)) {
			ArrayList<XEvent> ngram = new ArrayList<XEvent>(end.subList(end.size() - ngramsize + 1, end.size()));
			String ngramString = XLogHelper.traceToString(ngram) + ",end>";
			if (ngramString.contains(nGram12) || ngramString.contains(nGram21)) {
				return true;
			}
		}
		
		for (ArrayList<XEvent> certainSubTrace : cgTrace.getCertainSubtraces()) {
			for (int i = 0; i < certainSubTrace.size() - ngramsize + 1; i++) {
				ArrayList<XEvent> ngram = new ArrayList<XEvent>(certainSubTrace.subList(i, i + ngramsize));
				String ngramString = XLogHelper.traceToString(ngram);
				if (ngramString.contains(nGram12) || ngramString.contains(nGram21)) {
					return true;
				}
			}
		}
		
		if (cgTrace.size() < ngramsize) {
			return suppFTE(cgTrace, eventClass1, eventClass2);
		}
		
		return false;
	}
	
	private boolean suppBinaryRelation(XTraceCoarseGrained cgTrace, String eventClass1, String eventClass2) {
		for (int i = 0; i < cgTrace.size() - 1; i++) {
			String label1 = XConceptExtension.instance().extractName(cgTrace.get(i));
			for (int j = i + 1; j < cgTrace.size(); j++) {
				String label2 = XConceptExtension.instance().extractName(cgTrace.get(j));
				Pair<String, String> pair = new Pair<String,String>(label1, label2);
				if (!cgTrace.hasEqualTimestamps(cgTrace.get(i), cgTrace.get(j))) {
					if (label1.equals(eventClass1) && label2.equals(eventClass2)) {
						return true;
					}
					if (label2.equals(eventClass1) && label1.equals(eventClass2)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean hasUncertainty(XTraceCoarseGrained cgTrace, String eventClass1, String eventClass2) {
		for (Set<XEvent> uncertainSet : cgTrace.getUncertainSets()) {
			String labels = XLogHelper.traceToString(uncertainSet);
			if (labels.contains(eventClass1) && labels.contains(eventClass2)) {
				return true;
			}
		}
		return false;
	}

	
	
	
}

