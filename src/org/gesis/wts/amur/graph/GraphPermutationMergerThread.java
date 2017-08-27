package org.gesis.wts.amur.graph;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.gesis.wts.amur.graph.objects.Node;
import org.gesis.wts.amur.graph.tools.GraphJSONParser;
import org.gesis.wts.amur.graph.tools.NodeMerger;

public class GraphPermutationMergerThread implements Callable<Node> {
	private List<Node> graphs;
	private List<Integer> graphPermutation;

	public GraphPermutationMergerThread(List<Node> graphs, List<Integer> graphPermutation) {
		super();
		this.graphs = graphs;
		this.graphPermutation = graphPermutation;
	}

	@Override
	public Node call() throws Exception {
		System.out.println("new Thread for permutation: " + Arrays.toString(graphPermutation.toArray()));
		Node resultGraph = new Node();
		for (int i = 0; i < graphPermutation.size(); i++) {
			System.out.println("Merging graph " + resultGraph.getType() + " and graph "
					+ graphs.get(graphPermutation.get(i)).getType());
			resultGraph = NodeMerger.mergeNodes(resultGraph, graphs.get(graphPermutation.get(i)));
		}
		System.out.println("Thread "+  Arrays.toString(graphPermutation.toArray()) + " done!");
		if(Config.getInstance().getBooleanParameter("writeThreadResults")){
			GraphJSONParser.serialiseGraph(resultGraph, true);			
//			Graph2CSVWriter.generateEdgelistCSV(resultGraph);
		}
		return resultGraph;
	}

}
