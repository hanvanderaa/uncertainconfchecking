/**
 * 
 */
package org.processmining.plugins.log;

import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.DistributionType;
import org.processmining.models.graphbased.directed.petrinet.elements.TimedTransition;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class NetHelper {

	public static boolean isSilentTransition(Transition t) {
		return t.getLabel() == null
				|| t.getLabel().trim().equals("")
				|| t.getLabel().trim().startsWith("tr")
				|| t.isInvisible()
				|| (t instanceof TimedTransition && ((TimedTransition) t).getDistributionType().equals(
						DistributionType.IMMEDIATE));
	}

	public static Set<String> getActivities(Petrinet net) {
		Set<String> activities = new HashSet<>();
		for(Transition t : net.getTransitions())
			if(!NetHelper.isSilentTransition(t)) activities.add(t.getLabel());
		
		return activities;
	}
	
	
	public static int getNumberOfActivities(Petrinet net) {
		int numberOfActivities = 0;
		for(Transition t : net.getTransitions())
			if(!NetHelper.isSilentTransition(t)) numberOfActivities++;
		
		return numberOfActivities;
	}
}
