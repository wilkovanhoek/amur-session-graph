package org.gesis.wts.amur.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gesis.wts.amur.graph.objects.Node;
import org.gesis.wts.amur.graph.tools.GraphComparator;
import org.gesis.wts.amur.graph.tools.NodeMerger;

public class ChildrenMergerThread implements Callable<List<Node>> {
	private Node startingNode;
	private List<Node> remainingNodeList;
	private List<Node> secondNodeList;

	public ChildrenMergerThread(Node startingNode, List<Node> remainingNodeList, List<Node> secondNodeList) {
		super();
		this.startingNode = startingNode;
		this.remainingNodeList = remainingNodeList;
		this.secondNodeList = secondNodeList;
	}

	@Override
	public List<Node> call() throws Exception {
//		System.out.println("Startet thread for Node: " + startingNode.getType() + " - subTreeWeight: "
//				+ startingNode.getSubtreeWeight() + " children: " + startingNode.getChildren());
		List<Node> resultNodeList = new ArrayList<Node>();
		for (Node curNode : secondNodeList) {
			List<Node> remainingSecondNodeList = new ArrayList<Node>(secondNodeList);
			remainingSecondNodeList.remove(curNode);
			Node mergedNode = NodeMerger.mergeNodes(startingNode, curNode);

			List<Node> bufferNodeList = new ArrayList<Node>();
			bufferNodeList.add(mergedNode);
			bufferNodeList.addAll(NodeMerger.mergeNodeChildrenThreaded(remainingNodeList, remainingSecondNodeList));
			resultNodeList = GraphComparator.getBetterSubGraph(resultNodeList, bufferNodeList);

		}
		return resultNodeList;
	}

}
