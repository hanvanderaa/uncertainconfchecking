/**
 * 
 */
package org.processmining.coarsegrainedchecking.utils;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

public class NoiseUtils {
	
	private static Random random = new Random(); 
	
	
	/**
	 * @return the random
	 */
	public static Random getRandom() {
		return random;
	}


	/**
	 * @param random the random to set
	 */
	public static void setRandom(Random random) {
		NoiseUtils.random = random;
	}


	public static XLog drawNTracesOutOfOtherLog(XLog log, int traceSize) {
		XLog result = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		for (int i = 0; i < traceSize; i++){
			XTrace traceToCloneFrom = log.get(random.nextInt(log.size()));
			XTrace copy = XFactoryRegistry.instance().currentDefault().createTrace((XAttributeMap) traceToCloneFrom.getAttributes().clone());
			
			// copy trace
			for (XEvent e : traceToCloneFrom){
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap) e.getAttributes().clone());
				copy.insertOrdered(copyEvent);
			}
			result.add(copy);
		}
		return result;
	}


	public static XLog introduceNoise(XLog log, int noise) throws IOException {
		XLog result = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		
		int i = 1;
		
		for (XTrace t : log) {
			int removeEvent = 0;
			int duplicateEvent = 0;
			int swapEvents = 0;
				
			XTrace copy = XFactoryRegistry.instance().currentDefault().createTrace((XAttributeMap) t.getAttributes().clone());
			Pair<Long,Long> traceBounds = StochasticNetUtils.getBufferedTraceBounds(t);
			// copy trace
			for (XEvent e : t){
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent((XAttributeMap) e.getAttributes().clone());
				copy.add(copyEvent);
//				copy.insertOrdered(copyEvent);
			}
			if (i++ % 100 < noise){ // we have 1000 traces <- deterministically insert noise into the given percentage 
//			if (random.nextDouble()*100 < noise){
				String originalTrace = StochasticNetUtils.debugTrace(copy);
				// insert Noise:
				do {
					switch(random.nextInt(3)){
						case 0: // remove an event:
							if (copy.size() > 1){
								copy.remove(random.nextInt(copy.size()));
								removeEvent++;
							} else {
								insertDuplicateSomewhereInTrace(copy);
								duplicateEvent++;
							}
							break;
						case 1: // add a duplicate:
							insertDuplicateSomewhereInTrace(copy);
							duplicateEvent++;
							break;
						case 2: // swap events:
							if (copy.size()>=2){
								swapEvents(copy);
								swapEvents++;
							} else {
								insertDuplicateSomewhereInTrace(copy);
								duplicateEvent++;
							}
							break;
						default:
							insertDuplicateSomewhereInTrace(copy);
							duplicateEvent++;
					}
				} while (random.nextDouble() <= 0.5); // was "< 0.1" before
			}
			result.add(copy);
		}
		return result;
	}


	public static void swapEvents(XTrace copy) {
		XEvent firstEvent = copy.get(random.nextInt(copy.size()/2));
		XEvent secondEvent = copy.get(copy.size()-1-random.nextInt(copy.size()/2));
		// swap time stamps:
		Date firstTimeStamp = XTimeExtension.instance().extractTimestamp(firstEvent);
		XTimeExtension.instance().assignTimestamp(firstEvent, XTimeExtension.instance().extractTimestamp(secondEvent));
		XTimeExtension.instance().assignTimestamp(secondEvent, firstTimeStamp);
		// reorder trace:
		copy.remove(firstEvent);
		copy.remove(secondEvent);
		copy.insertOrdered(firstEvent);
		copy.insertOrdered(secondEvent);
	}


	public static void insertDuplicateSomewhereInTrace(XTrace copy) {
		// add a duplicate:
		// duplicate event, such that it can be randomly added to the trace later on
		XEvent eventToCopy = copy.get(random.nextInt(Math.max(1,copy.size()/2)));
		XEvent copiedEvent = XFactoryRegistry.instance().currentDefault().createEvent(eventToCopy.getAttributes());
		long timeDiff = XTimeExtension.instance().extractTimestamp(eventToCopy).getTime(); 
		if (copy.indexOf(eventToCopy) > 0) {
			XEvent pred = copy.get(copy.indexOf(eventToCopy) - 1);
			timeDiff = XTimeExtension.instance().extractTimestamp(eventToCopy).getTime() -   
					XTimeExtension.instance().extractTimestamp(pred).getTime();
		}		
		XEvent newPred = copy.get(copy.size()-1-random.nextInt(Math.max(1,copy.size()/2)));
		long newTimestamp = XTimeExtension.instance().extractTimestamp(newPred).getTime() + timeDiff;
		
		XTimeExtension.instance().assignTimestamp(copiedEvent,newTimestamp);
		copy.insertOrdered(copiedEvent);
	}

}
