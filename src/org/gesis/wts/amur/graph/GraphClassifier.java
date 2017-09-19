package org.gesis.wts.amur.graph;

import java.awt.RadialGradientPaint;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.gesis.wts.amur.graph.objects.Connection;
import org.gesis.wts.amur.graph.objects.Node;
import org.gesis.wts.amur.graph.tools.Graph2CSVWriter;
import org.gesis.wts.amur.graph.tools.GraphJSONParser;
import org.gesis.wts.amur.graph.tools.TheTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraphClassifier classifies session trees into given groups. For each group, a
 * archetype is needed. In general, this archetype is created by merging similar
 * graph, and extracting a sub graph based on a threshold of the minimal node
 * weight.
 *
 **/

public class GraphClassifier {
	final static Logger log = LoggerFactory.getLogger(GraphClassifier.class);
	static String dataFolder;
	private GraphAnalyser graphAnalyser;
	private static final Object[] FILE_HEADER = { "curGraph", "archetype", "curNumNodes", "archeNumNodes", "sumNodes", "mergedNumNodes", "numMergedNodes",
			"max", "numMergedNodes / max", "unmatchCur", "unmatchArche" };

	public void startClassifying(String dataFolder) {
		GraphClassifier.dataFolder = dataFolder;
		graphAnalyser = new GraphAnalyser();
		GraphJSONParser.init(dataFolder);
		List<Node> graphList = GraphJSONParser.getGraphs();
		List<Node> archetypes = GraphJSONParser.getGraphs(dataFolder + "/archetypes");

		log.info("classifying of graphs");
		long startTime = System.currentTimeMillis();
		log.info("timestamp startet: " + DateFormat.getTimeInstance().format(startTime));

		archetypes = prepareArchetypes(archetypes);

		classifyGraphs(archetypes, graphList);

		// GraphJSONParser.serialiseGraphs(archetypes, true);

		log.info("classifying is done!");

		long elapsed = System.currentTimeMillis() - startTime;
		log.info("timestamp finished: " + DateFormat.getTimeInstance().format(System.currentTimeMillis()));
		log.info("time taken: " + elapsed);
		log.info("threaded: " + Config.getInstance().getBooleanParameter("threaded"));
		log.info("nodeThreads: " + Config.getInstance().getBooleanParameter("nodeThreads"));
		log.info("threadPoolSize: " + Config.getInstance().getIntParameter("threadPoolSize"));
		log.info("permutations: " + Config.getInstance().getBooleanParameter("permutations"));
	}

	private void classifyGraphs(List<Node> archetypes, List<Node> graphList) {
		List<List<String>> csvOutput = new ArrayList<>();
		for (Node curGraph : graphList) {
			log.info(Config.LINEDELIMITER + "archetyping graph " + curGraph.getType());
			// int curNumOfNodes = curGraph.getNumberOfNodes();
			for (Node curArchetype : archetypes) {
				Node mergedGraph = graphAnalyser.mergeGraphs(curGraph, curArchetype);
				csvOutput.add(getCSVLine(curGraph, curArchetype, mergedGraph));
			}
		}
		writeCSVFile(csvOutput);

	}

	private List<String> getCSVLine(Node graph, Node archetype, Node mergedGraph) {
		List<String> resultList = new ArrayList<>();

		resultList.add(graph.getType());
		resultList.add(archetype.getType());

		resultList.add(String.valueOf(graph.getNumberOfNodes()));
		resultList.add(String.valueOf(archetype.getNumberOfNodes()));

		int sumOfNodes = graph.getNumberOfNodes() + archetype.getNumberOfNodes();
		resultList.add(String.valueOf(sumOfNodes));
		resultList.add(String.valueOf(mergedGraph.getNumberOfNodes()));

		int numOfMergedNodes = sumOfNodes - mergedGraph.getNumberOfNodes();
		resultList.add(String.valueOf(numOfMergedNodes));

		int max = Math.max(graph.getNumberOfNodes(), archetype.getNumberOfNodes());
		resultList.add(String.valueOf(max));
		double mergeRatio = ((double)numOfMergedNodes) / ((double)max);
		resultList.add(String.valueOf(mergeRatio));
		resultList.add(String.valueOf(graph.getNumberOfNodes() - numOfMergedNodes));
		resultList.add(String.valueOf(archetype.getNumberOfNodes() - numOfMergedNodes));

		return resultList;
	}

	private List<Node> prepareArchetypes(List<Node> archetypes) {
		List<Node> newArchetypes = new ArrayList<>();
		for (Node curArchetype : archetypes) {
			Node newNode = TheTool.thresholdGraph(curArchetype, Config.getInstance().getIntParameter("archetypeThreshold"),
					Config.getInstance().getBooleanParameter("resetWeight"));
			newNode.init();
			newArchetypes.add(newNode);
		}

		return newArchetypes;
	}

	private void writeCSVFile(List<List<String>> data) {
		String outputFile = dataFolder + "/results/classifier.csv";
		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(Graph2CSVWriter.NEW_LINE_SEPARATOR);

		try {
			// String filePath = outputFolder + "" + outputFile + ".csv";
			fileWriter = new FileWriter(outputFile);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			csvFilePrinter.printRecord(FILE_HEADER);

			for (List<String> curLine : data) {
				csvFilePrinter.printRecord(curLine);
			}

			log.debug("csv file " + outputFile + " was created successfully !!!");

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);

		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}

		}
	}

}
