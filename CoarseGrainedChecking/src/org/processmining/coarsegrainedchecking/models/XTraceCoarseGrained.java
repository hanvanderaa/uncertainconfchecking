package org.processmining.coarsegrainedchecking.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.coarsegrainedchecking.plugins.CoarseGrainedConformanceCheckingParameters;
import org.processmining.coarsegrainedchecking.utils.TimestampGranularity;

import com.google.common.collect.Lists;

public class XTraceCoarseGrained extends XTraceImpl {
	
	CoarseGrainedConformanceCheckingParameters parameters;
	private static final long serialVersionUID = 1L;
	private TimestampGranularity granularity = null;
	private ArrayList<HashSet<XEvent>> uncertainTrace = null;
	private HashSet<HashSet<XEvent>> uncertainSets = null;
	private HashMap<XEvent,HashSet<XEvent>> uncertainSetMap = null;
	private HashSet<ArrayList<XEvent>> certainSubtraces = null;
	ArrayList<XTrace> possibleResolutions;
	private boolean originalIsConformant;
	private double originalFitness;
	private boolean resolutionOverflow = false;
	
	public XTraceCoarseGrained(XTrace trace, TimestampGranularity granularity, CoarseGrainedConformanceCheckingParameters parameters) {
		super(trace.getAttributes());
		this.parameters = parameters;
		this.addAll(trace);
		this.granularity = granularity;
		computeMetaInformation();
	}
	
	public XTraceCoarseGrained(TimestampGranularity granularity, CoarseGrainedConformanceCheckingParameters parameters) {
		super(new XAttributeMapImpl());
		this.parameters = parameters;
		this.granularity = granularity;
	}
	
	
	public XTraceCoarseGrained(XAttributeMap attributeMap, TimestampGranularity granularity, CoarseGrainedConformanceCheckingParameters parameters) {
		super(attributeMap);
		this.granularity = granularity;
		this.parameters = parameters;
		computeMetaInformation();
	}
	
	
	
	
	public String getOriginalTraceString() {
		TimestampGranularity temp = this.granularity;
		this.granularity = TimestampGranularity.MILLISECONDS;
		computeMetaInformation();
		String originalTraceString = getTraceString();
		this.granularity = temp;
		computeMetaInformation();
		return originalTraceString;
	}
	
	public boolean matchesOrignialTrace(XTrace posssibleEventSequence) {
		for (int i = 0; i<this.size();i++) {
			if (posssibleEventSequence.get(i) != null) {
				String label1 = XConceptExtension.instance().extractName(posssibleEventSequence.get(i));
				String label2 = XConceptExtension.instance().extractName(this.get(i));
				if (!label1.equals(label2)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	

	
	public void addEvent(String name, int year, int month, int day, int hour, int minute, int second) {
		XEvent event = new XEventImpl();
		XTimeExtension.instance().assignTimestamp(event, new GregorianCalendar(year, month, day, hour, minute, second).getTime());
		XConceptExtension.instance().assignName(event, name);
		this.add(event);
	}
	
	public void update() {
		computeMetaInformation();
	}
	
	public boolean hasUncertainty() {
		return (uncertainTrace.size() != this.size());
	}
	
	public HashSet<HashSet<XEvent>> getUncertainSets() {
		if (uncertainSetMap == null) {
			computeMetaInformation();
		}
		return uncertainSets;
	}
	
	public HashSet<ArrayList<XEvent>> getCertainSubtraces() {
		return certainSubtraces;
	}
	
	public void setOriginalConformance(boolean conformance) {
		this.originalIsConformant = conformance;
	}
	
	public boolean originalIsConformant() {
		return originalIsConformant;
	}
	
		
	public double getOriginalFitness() {
		return originalFitness;
	}

	public void setOriginalFitness(double originalFitness) {
		this.originalFitness = originalFitness;
	}

	public ArrayList<XEvent> getCertainTraceFromStart() {
		ArrayList<XEvent> startTrace = new ArrayList<XEvent>();
	
		for (int i = 1; i < this.size(); i++) {
			if (!hasEqualTimestamps(this.get(i - 1), this.get(i))) {
				startTrace.add(this.get(i - 1));
			}
			else {
				return startTrace;
			}
		
		}
		startTrace.add(this.get(this.size() - 1));
		return startTrace;
	}
	
	public ArrayList<XEvent> getCertainTraceFromTail() {
		ArrayList<XEvent> endTrace = new ArrayList<XEvent>();
		
		for (int i = this.size() - 1; i > 0; i--) {
			if (!hasEqualTimestamps(this.get(i - 1), this.get(i))) {	
				endTrace.add(this.get(i));
			}
			else {
				return new ArrayList<XEvent>(Lists.reverse(endTrace));
			}
			
		}
		endTrace.add(this.get(0));
		return new ArrayList<XEvent>(Lists.reverse(endTrace));
	}

	public String getCertainSubtraceString () {
		String subtraceString = "{";
		for (ArrayList<XEvent> certainSubtrace: certainSubtraces) {
			String subtrace = "<";
			for (XEvent e: certainSubtrace) {
				String label = XConceptExtension.instance().extractName(e); 
				subtrace += label + ",";
			}
			subtrace = subtrace.substring(0, subtrace.length()-1) + ">,";
			subtraceString += subtrace;
		}
		subtraceString = subtraceString.substring(0, subtraceString.length()-1) + "}";
		return subtraceString;
	}
	
	public String getTraceString() {
		if (uncertainSetMap == null) {
			computeMetaInformation();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		for (HashSet<XEvent> eventSet : uncertainTrace) {
			sb.append(eventSetToString(eventSet));
			sb.append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), ">");
		return sb.toString();
	}
	
	private String eventSetToString(HashSet<XEvent> events) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (XEvent event : events) {
			String label = XConceptExtension.instance().extractName(event);
			sb.append(label);
			sb.append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), "}");
		return sb.toString();
		
	}
	
	private void computeMetaInformation() {
		uncertainTrace = new ArrayList<HashSet<XEvent>>();
		uncertainSets = new HashSet<HashSet<XEvent>>();
		uncertainSetMap = new HashMap<XEvent,HashSet<XEvent>>();
		certainSubtraces = new HashSet<ArrayList<XEvent>>();
		HashSet<XEvent> currentSet = null;
		ArrayList<XEvent> currentSubtrace = null;
		XEvent currentEvent = null;
		XEvent lastEvent = null;
		Iterator<XEvent> iter = this.iterator();
		
		// Iterate over set of events from trace
		int i = 0;
		while (iter.hasNext()) {
			i++;
			lastEvent = currentEvent;
			currentEvent = iter.next();
			
			// If we are looking at the first event of the trace
			if (currentSet == null) {
				currentSet = new HashSet<XEvent>();
				currentSet.add(currentEvent);
				currentSubtrace = new ArrayList<XEvent>();
				currentSubtrace.add(currentEvent);
				uncertainTrace.add(currentSet);

			// If we are looking at element 2 to n	
			} else {
				if (hasEqualTimestamps(currentEvent, currentSet.iterator().next())) {
					currentSet.add(currentEvent);
					
					if (currentSubtrace.size() > 1) {
						certainSubtraces.add(currentSubtrace);
						currentSubtrace = new ArrayList<XEvent>();
					} else {
						currentSubtrace = new ArrayList<XEvent>();
					}
					
					// If we are looking at last event
					if (!iter.hasNext()) {
						if (currentSet.size() > 1) {
							uncertainSets.add(currentSet);
							for (XEvent e: currentSet) {
								uncertainSetMap.put(e,currentSet);
							}
						} else {
							uncertainSetMap.put(currentEvent,null);
						}
					}
				} else {
					if (currentSet.size() == 1 && !currentSubtrace.contains(lastEvent)) {
						currentSubtrace.add(lastEvent);
					}
					if (currentSet.size() > 1) {
						uncertainSets.add(currentSet);
						for (XEvent e: currentSet) {
							uncertainSetMap.put(e,currentSet);
						}
					} else {
						uncertainSetMap.put(lastEvent,null);
					}
					currentSet = new HashSet<XEvent>();
					uncertainTrace.add(currentSet);
					currentSet.add(currentEvent);
					
					if (!iter.hasNext() && currentSubtrace.size() >= 1) {
						currentSubtrace.add(currentEvent);
						certainSubtraces.add(currentSubtrace);
					}
				}
			}
		}
	}
	
	public void computePossibleResolutions() {
		ArrayList<XTrace> currentTraces = new ArrayList<XTrace>(); 
		currentTraces.add(new XTraceImpl(this.getAttributes()));
		possibleResolutions =  computePermutations(currentTraces, 0, 0);

	}
	
	public boolean hasEqualTimestamps(XEvent event1, XEvent event2) {
		SimpleDateFormat timestampFormat = TimestampGranularity.getCorrespondingFormat(granularity);
		String timestamp1 = timestampFormat.format(XTimeExtension.instance().extractTimestamp(event1));
		String timestamp2 = timestampFormat.format(XTimeExtension.instance().extractTimestamp(event2));
		return timestamp1.equals(timestamp2);
	}
	
	public Collection<XTrace> getPossibleResolutions() {
		return possibleResolutions;
	}
	

	private ArrayList<XTrace> computePermutations(ArrayList<XTrace> currentTraces, int setIndex, int addedFromSet) {				
		if (currentTraces.size() >= parameters.MAX_RESOLUTIONS_PER_TRACE) {
			this.resolutionOverflow = true;
			return new ArrayList<XTrace>();
		}
		if (addedFromSet == uncertainTrace.get(setIndex).size()) {
			setIndex++;
			addedFromSet = 0;
		}
		if (setIndex == uncertainTrace.size()) {
			return currentTraces;
		}
		ArrayList<XTrace> newTraces = new ArrayList<XTrace>();
		for (XTrace partialTrace : currentTraces) {
			for (XEvent event : uncertainTrace.get(setIndex)) {
				if (!partialTrace.contains(event)) {
					XTrace newTrace = new XTraceImpl(partialTrace.getAttributes());
					newTrace.addAll(partialTrace);
					newTrace.add(event);
					newTraces.add(newTrace);
				}
			}
		}
		addedFromSet++;
		return computePermutations(newTraces, setIndex, addedFromSet);
	}

	public boolean hasResolutionOverflow() {
		return resolutionOverflow;
	}
	

	public String toString() {
		return getTraceString();
	}


	
}
