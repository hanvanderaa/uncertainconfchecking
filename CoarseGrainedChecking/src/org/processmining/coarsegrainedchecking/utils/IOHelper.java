package org.processmining.coarsegrainedchecking.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.ToStochasticNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.log.FileHelper;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;

public class IOHelper {
	
	public static String getStringFromXTrace(XTrace trace) {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		XEvent event = null;
		Iterator <XEvent> iter = trace.iterator();
		while (iter.hasNext()) {
			event = iter.next();
			String label = XConceptExtension.instance().extractName(event);
			sb.append(label);
			sb.append(",");
		}
		sb.replace(sb.length() - 1, sb.length(), ">");
		return sb.toString();
	}

	public static Petrinet importPNML(PluginContext context, String filepath) throws Exception {
		Petrinet net = null;
		try {
			net = FileHelper.importPNML(filepath, context);
		} catch (NullPointerException e) {
			
		}
		// net is stochastic
		if (net == null) {
			net = FileHelper.importStochasticPNML(filepath, getFileName(new File(filepath)));
			
			Marking marking = StochasticNetUtils.getInitialMarking(context, net); 
			net = (Petrinet) ToStochasticNet.asPetriNet(context, (StochasticNet) net, marking)[0]; 
		}
		return net;
	}
	
	public static StochasticNet importStochasticNet(PluginContext context, String filepath)  {
		Petrinet net = null;
		StochasticNet snet = null;
		try {
			net = FileHelper.importPNML(filepath, context);
			Marking marking = StochasticNetUtils.getInitialMarking(context, net);
			
			// MODIFIED - BEGIN
			Object[] transformed = ToStochasticNet.fromPetriNetExternal(context, net, marking);
			snet = (StochasticNet) transformed[0];
			// MODIFIED - END
		
		} catch (Exception e) {
//			e.printStackTrace();
		}
//		 net is stochastic
		if (net == null) {
		try {
			net = FileHelper.importStochasticPNML(filepath, getFileName(new File(filepath)));
			snet = (StochasticNet) net;
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

		for (Transition t : snet.getTransitions()) {
			t.setInvisible(false);
		}
		
		return snet;
	}
	
	public static void serializeObject(String folderpath, String filename, Object object) {
		if (!folderpath.endsWith("/")) {
			folderpath += "/";
		}
		File folder = new File(folderpath);
		if (!folder.exists()) {
			folder.mkdir();
		}
	   serializeObject(folderpath + filename, object);
	   }
	
	public static void serializeObject(String filepath, Object object) {
		try
	      {
	         FileOutputStream fileOut =
	         new FileOutputStream(filepath);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(object);
	         out.close();
	         fileOut.close();
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
	}
	
	 public static Object deserializeObject(String filepath)
	   {
	      Object o = null;
	      try
	      {
	         FileInputStream fileIn = new FileInputStream(filepath);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         o = in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	      }catch(ClassNotFoundException c)
	      {
	         c.printStackTrace();
	      }
	      return o;
	   }


	public static List<File> getFilesWithExtension(File directory, String ext) {
		List<File> filtered = new ArrayList<File>();
		for (File file : directory.listFiles()) {
			if (file.getName().endsWith(ext)) {
				filtered.add(file);
			}
		}
		Collections.sort(filtered);
		return filtered;
	}
	
	
	public static String readFile(File file) throws IOException {
		Charset encoding = Charset.defaultCharset();
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		return new String(encoded, encoding);
	}
	
	
	public static String getFileName(File file) {
		return FilenameUtils.removeExtension(file.getName()).toLowerCase();
	}
	
	public static double round(double value, int places) {
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
}
