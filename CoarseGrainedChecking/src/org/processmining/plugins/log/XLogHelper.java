/**
 * 
 */
package org.processmining.plugins.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class XLogHelper {
	// complex separator to avoid any accidental collisions
	public static final String SEPARATOR_STRING = ",.,"; 
	
	public static String traceToString(Collection<XEvent> events) {
		StringBuilder sb = new StringBuilder();
		for (XEvent event : events) {
			String label = XConceptExtension.instance().extractName(event); 
			sb.append(label);
			sb.append(SEPARATOR_STRING);
		}
		return sb.toString();
	}
	 
	
	public static List<String> traceToLabelList(XTrace trace) {
		List<String> res = new ArrayList<String>();
		Iterator<XEvent> eventIter = trace.iterator();
		while (eventIter.hasNext()) {
			XEvent xEvent = eventIter.next();
			res.add(XConceptExtension.instance().extractName(xEvent));
		}
		return res;
	}

	public static XTrace findTraceByName(XLog log, String traceName) {
		Iterator<XTrace> traceIter = log.iterator();
		while (traceIter.hasNext()) {
			XTrace xTrace = traceIter.next();
			if (XConceptExtension.instance().extractName(xTrace).equals(traceName)) {
				return xTrace;
			}
		}
		return null;
	}

	public static String getAttribute(XEvent xEvent, String attributeKey) {
		if (xEvent.getAttributes().get(attributeKey) != null) {
			return ((XAttributeLiteralImpl) xEvent.getAttributes().get(attributeKey)).getValue();
		}

		return "";
	}

	public static XLog initializeLog(XLog inputLog) {

		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog initializedLog = factory.createLog();
		initializedLog.setAttributes(inputLog.getAttributes());
		for (XExtension extension : inputLog.getExtensions())
			initializedLog.getExtensions().add(extension);

		for (XEventClassifier classifier : inputLog.getClassifiers())
			initializedLog.getClassifiers().add(classifier);

		return initializedLog;
	}

	public static void checkForActivityClassifier(XLog newLog) {
		XEventClassifier activityClassifier = null;
		XEventClassifier lifeCycleClassifier = null;
		XEventClassifier actLifeCycleClassifier = null;
		for (XEventClassifier classifier : newLog.getClassifiers()) {

			List<String> keyList = Arrays.asList(classifier.getDefiningAttributeKeys());
			if ((keyList.contains("concept:name") || classifier.name().equals("Activity")) && !keyList.contains("lifecycle:transition")) {
				activityClassifier = classifier;
			} else if (keyList.contains("lifecycle:transition") && !keyList.contains("concept:name")) {
				lifeCycleClassifier = classifier;
			} else if(keyList.contains("lifecycle:transition") && keyList.contains("concept:name")) {
				actLifeCycleClassifier = classifier;
			}

		}

		if (activityClassifier == null) {
			activityClassifier = new XEventNameClassifier();
			newLog.getClassifiers().add(activityClassifier);
		}
		
		if(lifeCycleClassifier == null) {
			lifeCycleClassifier = new XEventLifeTransClassifier();
			newLog.getClassifiers().add(lifeCycleClassifier);
		}
		
		if(actLifeCycleClassifier == null) {
			actLifeCycleClassifier = new XEventAttributeClassifier("ActLifeCylce", XConceptExtension.KEY_NAME, 
					XLifecycleExtension.KEY_TRANSITION);
			newLog.getClassifiers().add(actLifeCycleClassifier);
		}
		

		activityClassifier.setName("Activity");
		activityClassifier.getDefiningAttributeKeys()[0] = "concept:name";
		
	}

	public static String[] extractEventClassesSorted(XLog log) {
		//		XLogInfo summary = XLogInfoFactory.createLogInfo(log, selectedClassifier);
		//		XEventClasses eventClasses = summary.getEventClasses();

		Set<String> eventClasses = extractEventClasses(log);

		// create possible event classes
		String[] arrEvClass = eventClasses.toArray(new String[0]);
		Arrays.sort(arrEvClass);

		//		for(Object eventClass : arrEvClass) {
		//			System.out.println(eventClass.toString());
		//		}
		//		System.out.println(Arrays.toString(arrEvClass));

		return arrEvClass;
	}

	public static Set<String> extractEventClasses(XLog log) {
		HashSet<String> eventClasses = new HashSet<String>();
		for (XTrace xTrace : log) {
			for (XEvent xEvent : xTrace)
				eventClasses.add(XConceptExtension.instance().extractName(xEvent));
		}
		return eventClasses;
	}

	public static XLog importLog(String pathToLog) throws Exception {
		return importLog(new File(pathToLog));
	}
		
	public static XLog importLog(File logFile) throws Exception {	
//		XParser parser = new XesXmlParser(new XFactoryNaiveImpl());
//		Collection<XLog> logs = null;
//		File logFile = new File(pathToLog);
//		logs = parser.parse(logFile);
//		XLog xLog = logs.iterator().next();
//		XConceptExtension.instance().assignName(xLog, logFile.getName());
		XUniversalParser parser = new XUniversalParser();
		Collection<XLog> logs = parser.parse(logFile);
		if (logs.size() > 0){
			return logs.iterator().next();
		}
		return null;
	}
	
	
	
	public static boolean hasLoopsApproximation(Collection<XTrace> log, Petrinet net) {
//		if (!hasDuplicateTasks(net)) {
//			for (XTrace t : log) {
//				if (hasRepeatedEventNames(t)) {
//					return true;
//				}
//			}
//			return false;
//		}
		for (XTrace t : log) {
			if (t.size() > net.getTransitions().size() * 2) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasRepeatedEventNames(XTrace trace) {
		Set<String> seen = new HashSet<String>();
		for (XEvent e : trace) {
			if (!seen.add(XConceptExtension.instance().extractName(e))) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean hasDuplicateTasks(Petrinet net) {
		Set<String> seen = new HashSet<String>();
		for (Transition t : net.getTransitions()) {
			if (!t.getLabel().isEmpty()) {
				if (!seen.add(t.getLabel())) {
					return true;
				}
			}
		}
		return false;
	}

}
