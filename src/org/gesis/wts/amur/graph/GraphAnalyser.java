package org.gesis.wts.amur.graph;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gesis.wts.amur.graph.objects.Node;
import org.gesis.wts.amur.graph.tools.GraphComparator;
import org.gesis.wts.amur.graph.tools.GraphJSONParser;
import org.gesis.wts.amur.graph.tools.NodeMerger;
import org.gesis.wts.amur.graph.tools.TheTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphAnalyser {
	final static Logger log = LoggerFactory.getLogger(GraphAnalyser.class);
	static String dataFolder;

	public GraphAnalyser() {
	}

	public void startAnalysis(String dataFolder) {
		GraphAnalyser.dataFolder = dataFolder;
		GraphJSONParser.init(dataFolder);

		List<Node> graphList = GraphJSONParser.getGraphs();
		Node resultGraph = new Node();

		log.info("merging graphs");
		long startTime = System.currentTimeMillis();
		log.info("timestamp startet: " + DateFormat.getTimeInstance().format(startTime));
		// mergeGraphs(graphList);
		if (Config.getInstance().getBooleanParameter("permutations")) {
			if (Config.getInstance().getBooleanParameter("threaded")) {
				mergeAllGraphPermutationsThreaded(graphList);
			} else {
				resultGraph = mergeAllGraphPermutations(graphList);
			}
		} else {
			if (Config.getInstance().getBooleanParameter("sorted")) {
				resultGraph = mergeGraphsSorted(graphList);
			} else {
				resultGraph = mergeGraphsEfficient(graphList);
			}

		}
		GraphJSONParser.serialiseGraph(resultGraph, true);
		long endTime = System.currentTimeMillis();
		long elapsed = System.currentTimeMillis() - startTime;
		log.info("timestamp finished: " + DateFormat.getTimeInstance().format(endTime));
		log.info("time taken: " + elapsed + "ms");
		log.info("threaded: " + Config.getInstance().getBooleanParameter("threaded"));
		log.info("nodeThreads: " + Config.getInstance().getBooleanParameter("nodeThreads"));
		log.info("threadPoolSize: " + Config.getInstance().getIntParameter("threadPoolSize"));
		log.info("permutations: " + Config.getInstance().getBooleanParameter("permutations"));
	}

	// sort modes are defined in settings with sortMode (ASC, DESC, SHUFFLE)
	public static Node mergeGraphsSorted(List<Node> graphs) {
		Node resultGraph = new Node();
		// System.out.println("Sorted merge with mode: " +
		// Config.getInstance().getStringParameter("sortMode"));
		TheTool.sort(graphs, Config.getInstance().getStringParameter("sortMode"));

		if (graphs.size() > 0) {
			resultGraph = graphs.get(0);
			for (int i = 1; i < graphs.size(); i++) {
				// System.out.println("Merging graph " + resultGraph.getType() +
				// " and graph " + graphs.get(i).getType());
				resultGraph = NodeMerger.mergeNodes(resultGraph, graphs.get(i));
				if (Config.getInstance().getBooleanParameter("writeIntermediateGraphs"))
					GraphJSONParser.serialiseGraph(resultGraph, true);
			}
		}
		return resultGraph;
	}

	private Node mergeGraphsEfficient(List<Node> graphs) {
		Node resultGraph = new Node();

		if (graphs.size() > 2) {
			int splitIndex = (graphs.size() / 2);
			Node firstSubgraph = mergeGraphsEfficient(graphs.subList(0, splitIndex));
			Node secondSubgraph = mergeGraphsEfficient(graphs.subList(splitIndex, graphs.size()));
			log.debug("Merging graph " + firstSubgraph.getType() + " and graph " + secondSubgraph.getType());
			resultGraph = NodeMerger.mergeNodes(firstSubgraph, secondSubgraph);
		} else {
			if (graphs.size() == 2) {
				Node firstGraph = graphs.get(0);
				Node secondGraph = graphs.get(1);
				log.debug("Merging graph " + firstGraph.getType() + " and graph " + secondGraph.getType());
				resultGraph = NodeMerger.mergeNodes(firstGraph, secondGraph);
			} else if (graphs.size() == 1) {
				resultGraph = graphs.get(0);
			}
		}
		if (Config.getInstance().getBooleanParameter("writeIntermediateGraphs"))
			GraphJSONParser.serialiseGraph(resultGraph, true);
		return resultGraph;
	}

	private Node mergeAllGraphPermutations(List<Node> graphs) {
		Node resultGraph = new Node();
		int numberOfGraphs = graphs.size();
		List<Integer> indexList = new ArrayList<Integer>();
		for (int i = 0; i < numberOfGraphs; i++) {
			indexList.add(i);
		}
		List<List<Integer>> permutations = TheTool.generatePerm(indexList);
		log.debug("calculated: " + permutations.size() + " permutations");
		for (int i = 0; i < permutations.size(); i++) {
			List<Integer> curPermutation = permutations.get(i);
			Node newGraph = new Node();
			for (int j = 0; j < curPermutation.size(); j++) {
				log.debug("merging graph " + newGraph.getType() + " and graph "
						+ graphs.get(curPermutation.get(j)).getType());
				newGraph = NodeMerger.mergeNodes(newGraph, graphs.get(curPermutation.get(j)));
			}
			if (Config.getInstance().getBooleanParameter("writeThreadResults")) {
				GraphJSONParser.serialiseGraph(newGraph, true);
			}
			if (resultGraph.getNumberOfNodes() > newGraph.getNumberOfNodes()) {
				resultGraph = newGraph;
			} else if (resultGraph.getNumberOfNodes() == newGraph.getNumberOfNodes()) {
				double resultGraphSubTreeWeight = resultGraph.getSubtreeWeight();
				double newGraphSubTreeWeight = newGraph.getSubtreeWeight();
				if (resultGraphSubTreeWeight < newGraphSubTreeWeight) {
					resultGraph = newGraph;
				}
			}
		}
		return resultGraph;
	}

	private Node mergeAllGraphPermutationsThreaded(List<Node> graphs) {
		Node resultGraph = new Node();
		int numberOfGraphs = graphs.size();
		List<Integer> indexList = new ArrayList<Integer>();
		for (int i = 0; i < numberOfGraphs; i++) {
			indexList.add(i);
		}

		List<List<Integer>> permutations = TheTool.generatePerm(indexList);
		ExecutorService executor = Executors.newFixedThreadPool(Config.getInstance().getIntParameter("threadPoolSize"));
		List<Future<Node>> listOfPermutations = new ArrayList<Future<Node>>();

		for (int i = 0; i < permutations.size(); i++) {
			Callable<Node> graphPermutation = new GraphPermutationMergerThread(graphs, permutations.get(i));
			// execute
			Future<Node> bufferGraph = executor.submit(graphPermutation);
			// remember
			listOfPermutations.add(bufferGraph);
		}

		// finish them all
		executor.shutdown();
		while (!executor.isTerminated()) {
			// wait to terminate
		}

		resultGraph = GraphComparator.getBestGraph(listOfPermutations);
		GraphJSONParser.serialiseGraph(resultGraph, true);
		return resultGraph;
	}

}
