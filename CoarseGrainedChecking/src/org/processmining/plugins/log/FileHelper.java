/**
 * 
 */
package org.processmining.plugins.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;
import org.processmining.plugins.pnml.importing.StochasticNetDeserializer;
import org.processmining.plugins.pnml.simple.PNMLRoot;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;



public class FileHelper {

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}

		}
		return (path.delete());
	}
	
	public static void copyFile(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
	
	public static void saveStringToFile(String outString, String filePath) throws IOException {
		File file = new File(filePath);
		File directory = new File(file.getParent());
		if(!directory.exists())
			directory.mkdirs();
		
		saveStringToFile(outString, file);
	}
	
	public static String listToCsv(List<String> list) {
		Iterator<String> iter = list.iterator();
		String csvRep = "";
		while (iter.hasNext()) {
			String value = iter.next();
			csvRep += (value != null) ? value : "";
			if(iter.hasNext()) csvRep += ";";
		}
		return csvRep;
	}
	
	public static void saveStringToFile(String outString, File file) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(outString);
		out.close();
	}

	public static String getValidFileName(String fileName) {
		String[] invalidCharInFileName = { "\\", "/", ":", "?", "\"", "<", ">", "|" };
		for (String invalidChar : invalidCharInFileName)
			fileName = fileName.replace(invalidChar, " ");
		return fileName;
	}

	public static void saveEventLog(XLog log, String absolutePath) {
		try {
			String logName = XConceptExtension.instance().extractName(log);
			if (logName.length() > 255) {
				logName = logName.substring(0, 248);
			}

			File logFile = new File(absolutePath);
			FileOutputStream logOut = new FileOutputStream(logFile);
//			XSerializer logSerializer = new XesXmlGZIPSerializer();
			XSerializer logSerializer = new XesXmlSerializer();
			logSerializer.serialize(log, logOut);
			logOut.close();

			System.out.println("File is written");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Petrinet importPNML(String fileName, PluginContext context) throws Exception {
		return (Petrinet) (importPNMLObjects(fileName, context))[0];
	}
	
	public static Object[] importPNMLObjects(String fileName, PluginContext context) throws Exception {
		File netFile = new File(fileName);
		PnmlImportUtils utils = new PnmlImportUtils();
		Pnml pnml = utils.importPnmlFromStream(context, new FileInputStream(netFile), netFile.getName(),
				netFile.length());
		//			net = (Petrinet) netImport.importFromStream(

		PetrinetGraph netGraph = PetrinetFactory.newPetrinet(pnml.getLabel() + " (imported from " + netFile.getName()
				+ ")");

		Marking marking = new Marking();
		GraphLayoutConnection layout = new GraphLayoutConnection(netGraph);
		pnml.convertToNet(netGraph, marking, layout);
		if(context!=null) {
			context.addConnection(new InitialMarkingConnection(netGraph, marking));
			context.addConnection(layout);
		}
		Petrinet net = (Petrinet) netGraph;
		
		for(org.processmining.models.graphbased.directed.petrinet.elements.Transition t : net.getTransitions())
			if(t.getLabel().equals(""))
				t.setInvisible(true);

		Object[] netObjects = new Object[]{net, marking};
		return netObjects;
	}

	public static Petrinet importStochasticPNML(String filePath, String filename) throws FileNotFoundException,
			Exception {
		Object[] netObjects = importStochasticPNMLwithMarking(filePath, filename);
		return ((Petrinet) netObjects[0]);
	}

	public static Object[] importStochasticPNMLwithMarking(String filePath, String filename)
			throws FileNotFoundException {
		//		Serializer serializer = new Persister();
		//		PNMLRoot pnml = serializer.read(PNMLRoot.class, new FileInputStream(new File(filePath)));
		//
		//		StochasticNetDeserializer converter = new StochasticNetDeserializer();
		//		return converter.convertToNet(context, pnml, filename, true);

		
		Object[] netObjects = null;
		
		try {
			Serializer serializer = new Persister();
			PNMLRoot pnml = serializer.read(PNMLRoot.class, new FileInputStream(new File(filePath)));
	
			StochasticNetDeserializer converter = new StochasticNetDeserializer();
			netObjects = converter.convertToNet(null, pnml, filename, true);
			Petrinet net = (Petrinet) netObjects[0];
			Marking marking = (Marking) netObjects[1];
			if (marking == null || marking.size() == 0) {
				marking = StochasticNetUtils.getInitialMarking(null, net);
				netObjects[1] = marking;
			}
		} catch (Exception e) {
			try {
				netObjects = importPNMLObjects(filePath, null);
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		Petrinet net = (Petrinet) netObjects[0];
		Marking marking = (Marking) netObjects[1];
		for(Transition t : net.getTransitions())
			if(NetHelper.isSilentTransition(t)) t.setInvisible(true);
		
		return netObjects;
	}

	

	public static void savePetrinetToPnml(Petrinet net, Marking marking, File file) throws IOException {

		if(marking==null)
			marking = new Marking();

		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		HashMap<PetrinetGraph, Marking> markedNets = new HashMap<PetrinetGraph, Marking>();
		markedNets.put(net, marking);
		Pnml pnml = new Pnml().convertFromNet(markedNets, layout);
		pnml.setType(Pnml.PnmlType.PNML);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		bw.write(text);
		bw.close();
	}



	public static void createDirectory(String dirPath) {
		File dir = new File(dirPath);
		if(!dir.exists())
			dir.mkdirs();
		
	}
}
