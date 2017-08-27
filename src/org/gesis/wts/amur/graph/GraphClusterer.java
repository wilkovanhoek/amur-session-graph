package org.gesis.wts.amur.graph;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class GraphClusterer {
	final static Logger log = LoggerFactory.getLogger(GraphClusterer.class);
	static String dataFolder;

	public void startClustering(String dataFolder, Byte numberOfGroups) {
		GraphClusterer.dataFolder = dataFolder;
		GraphJSONParser.init(dataFolder);

		log.info("clustering of graphs");
		long startTime = System.currentTimeMillis();
		log.info("timestamp startet: " + DateFormat.getTimeInstance().format(startTime));

		List<Node> graphList = GraphJSONParser.getGraphs();

		Byte maxGroups = numberOfGroups;

		Map<Byte, List<Map<Byte, List<Byte>>>> groups = new HashMap<Byte, List<Map<Byte, List<Byte>>>>();
		// change i for having all groupings calculated
		for (byte i = maxGroups; i <= maxGroups; i++) {
			if(graphList.size() < 128){
				byte graphs = (byte)graphList.size();
				groups.put(i, TheTool.grouping(graphs, i));

				List<Node> bestResultGraphList = new ArrayList<>();
				if (Config.getInstance().getBooleanParameter("threaded")) {
					bestResultGraphList = mergeCombinationsThreaded(graphList, groups.get(i));
				} else {
					bestResultGraphList = NodeMerger.mergeCombinations(graphList, groups.get(i), i);
				}

				GraphJSONParser.serialiseGraphs(bestResultGraphList, "best_results", true);	
			}else{
				
			}
			
		}
		log.info("clustering is done!");

		long elapsed = System.currentTimeMillis() - startTime;
		log.info("timestamp finished: " + DateFormat.getTimeInstance().format(System.currentTimeMillis()));
		log.info("time taken: " + elapsed);
		log.info("threaded: " + Config.getInstance().getBooleanParameter("threaded"));
		log.info("nodeThreads: " + Config.getInstance().getBooleanParameter("nodeThreads"));
		log.info("threadPoolSize: " + Config.getInstance().getIntParameter("threadPoolSize"));
		log.info("permutations: " + Config.getInstance().getBooleanParameter("permutations"));
	}

	private List<Node> mergeCombinationsThreaded(List<Node> graphs, List<Map<Byte, List<Byte>>> grouping) {
		List<Node> bestResultGraphList = new ArrayList<>();
		int chunkSize = grouping.size() / Config.getInstance().getIntParameter("threadPoolSize");

		ExecutorService executor = Executors.newFixedThreadPool(Config.getInstance().getIntParameter("threadPoolSize"));
		List<Future<List<Node>>> listOfBestCombinations = new ArrayList<Future<List<Node>>>();

		log.info("Merging threaded - chunk size: " + chunkSize);
		for (int i = 0; i < grouping.size(); i += chunkSize) {
			int listEnd = (i + chunkSize) <= grouping.size() ? i + chunkSize : grouping.size();
			List<Node> graphsCopy = Node.getCopies(graphs);
			Callable<List<Node>> graphCombination = new GraphCombinationMergerThread(graphsCopy, grouping.subList(i, listEnd), i / chunkSize);
			// execute
			Future<List<Node>> bufferGraph = executor.submit(graphCombination);
			// remember
			listOfBestCombinations.add(bufferGraph);
		}

		// finish them all
		executor.shutdown();

		while (!executor.isTerminated()) {
			// wait to terminate
		}

		bestResultGraphList = GraphComparator.getBetterGraphGroupingThreaded(listOfBestCombinations);

		return bestResultGraphList;
	}

}
