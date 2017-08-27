package org.gesis.wts.amur.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gesis.wts.amur.graph.objects.Node;
import org.gesis.wts.amur.graph.tools.GraphJSONParser;
import org.gesis.wts.amur.graph.tools.NodeMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphCombinationMergerThread implements Callable<List<Node>> {
	final static Logger log = LoggerFactory.getLogger(GraphCombinationMergerThread.class);
	private List<Node> graphs;
	private List<Map<Byte, List<Byte>>> graphCombinations;
	private int id = -1;

	public GraphCombinationMergerThread(List<Node> graphList, List<Map<Byte, List<Byte>>> combinations, int id) {
		super();
		this.graphs = graphList;
		this.graphCombinations = combinations;
		this.id = id;
	}

	@Override
	public List<Node> call() throws Exception {
		log.info("new Thread for " + graphCombinations.size() + " groupings");
		List<Node> resultGraphList = new ArrayList<Node>();
		resultGraphList = NodeMerger.mergeCombinations(graphs, graphCombinations, id);		
		if(Config.getInstance().getBooleanParameter("writeThreadResults")){
			GraphJSONParser.serialiseGraphs(resultGraphList,"thread_" + id + "_result",  true);
		}
		return resultGraphList;
	}

}
