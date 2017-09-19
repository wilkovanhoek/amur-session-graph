package org.gesis.wts.amur.graph.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gesis.wts.amur.graph.objects.Connection;
import org.gesis.wts.amur.graph.objects.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Graph2CSVWriter {
	final static Logger log = LoggerFactory.getLogger(Graph2CSVWriter.class);
	// Delimiter used in CSV file
	public static final String NEW_LINE_SEPARATOR = "\n";
	private static final Object[] FILE_HEADER = { "from", "to", "weight" };
	private static String outputFolder = "data/results/";
	private static final String DELIMITER = "._";
	

	public static void init(String outputFolder) {
		Graph2CSVWriter.outputFolder = outputFolder + "/results/";
	}

	public static void generateEdgelistCSV(Node graph, String fileName) {
		String outputFile = fileName!=null?fileName:graph.getType();
		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;
		List<Connection> connectionList = convertNode2ConnectionList(graph, 0);

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

		try {
			String filePath = outputFolder + "" + outputFile + ".csv";
			fileWriter = new FileWriter(filePath);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			csvFilePrinter.printRecord(FILE_HEADER);

			for (Connection curConnection : connectionList) {
				List<String> connection = new ArrayList<String>();
				connection.add(curConnection.getFirstNode());
				connection.add(curConnection.getSecondNode());
				connection.add(String.valueOf(curConnection.getWeight()));
				csvFilePrinter.printRecord(connection);
			}

			log.debug("csv file " + filePath + " was created successfully !!!");

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
	
	private static List<Connection> convertNode2ConnectionList(Node graph, int curId) {
		List<Connection> connectionList = new ArrayList<Connection>();
		String firstNode = curId + DELIMITER + graph.getType();
		for (Node curChild : graph.getChildrenWithKids()) {
			curId++;
			String secondNode = curId + DELIMITER + curChild.getType();
			Connection newConnection = new Connection(firstNode, secondNode, curChild.getWeight());
			connectionList.add(newConnection);
			connectionList.addAll(convertNode2ConnectionList(curChild, curId));
			curId += curChild.getNumberOfNodes();
		}
		
		for (Node curChild : graph.getChildlessChildren()) {
			curId++;
			String secondNode = curId + DELIMITER + curChild.getType();
			Connection newConnection = new Connection(firstNode, secondNode, curChild.getWeight());
			connectionList.add(newConnection);
		}

		return connectionList;

	}

}
