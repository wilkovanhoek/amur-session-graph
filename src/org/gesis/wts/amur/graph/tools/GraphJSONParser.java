package org.gesis.wts.amur.graph.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gesis.wts.amur.graph.Config;
import org.gesis.wts.amur.graph.objects.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class GraphJSONParser {
	final static Logger log = LoggerFactory.getLogger(GraphJSONParser.class);
	private static Gson gson;
	private static String dataFolder;

	public static File[] getJSONFiles(String dirName) {
		File dir = new File(dirName);
		return dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".json");
			}
		});

	}

	public static List<Node> getGraphs() {
		log.info("reading graphs");
		List<Node> graphList = new ArrayList<Node>();

		File resultsFolder = new File(dataFolder + "/results");
		if (!resultsFolder.exists())
			resultsFolder.mkdirs();
		try {
			File[] jsonFiles = getJSONFiles(dataFolder + "/graphs");
			for (int i = 0; i < jsonFiles.length; i++) {
				log.debug("reading file: " + jsonFiles[i].getName());
				BufferedReader br = new BufferedReader(new FileReader(jsonFiles[i]));
				Node newGraph = gson().fromJson(br, Node.class);
				// newGraph.initSubTreeWeight();
				newGraph.init();

				if (Config.getInstance().getBooleanParameter("writeParsedGraphs"))
					serialiseGraph(newGraph, null, true);
				graphList.add(newGraph);
			}
//			TheTool.sort(graphList,"ASC");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		if (Config.getInstance().getBooleanParameter("writeSequences")){
			Graph2CSVWriter.generateSequenceCSV(graphList, "sequence");
		}
		log.info("reading of graphs is done!");
		return graphList;
	}

	public static void serialiseGraph(Node graph, String fileName,  boolean writeCSV) {
		serialiseGraph(graph, fileName);
		if (writeCSV)
			Graph2CSVWriter.generateEdgelistCSV(graph, fileName);
	}
	
	public static void serialiseGraph(Node graph, boolean writeCSV) {
		serialiseGraph(graph, null);
		if (writeCSV)
			Graph2CSVWriter.generateEdgelistCSV(graph, null);
	}

	public static void serialiseGraph(Node graph, String fileName) {
		TheTool.sortRecursive(graph,"DESC");
		String outputFile = fileName!=null?fileName:graph.getType();

		String json = gson().toJson(graph);
		try {
			String filePath = dataFolder + "/results/" + outputFile+ ".json";
			log.debug("writing result graph to file: " + filePath);
			// write converted json data to a file named "CountryGSON.json"
			FileWriter writer = new FileWriter(filePath);
			writer.write(json);
			writer.close();
			log.debug("writing result graph is done!");
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	private static Gson gson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	public static void init(String dataFolder) {
		GraphJSONParser.dataFolder = dataFolder;
		Graph2CSVWriter.init(dataFolder);
	}
	
	public static void serialiseGraphs(List<Node> graphList, String fileName, boolean writeCSV) {
		for(Node curGaph: graphList){
			GraphJSONParser.serialiseGraph(curGaph, fileName + "_" + curGaph.getType(), writeCSV);
		}

		
	}

	public static void serialiseGraphs(List<Node> graphList, boolean writeCSV) {
		for(Node curGaph: graphList){
			GraphJSONParser.serialiseGraph(curGaph, null, writeCSV);
		}

		
	}

}
