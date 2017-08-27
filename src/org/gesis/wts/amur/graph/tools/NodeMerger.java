package org.gesis.wts.amur.graph.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gesis.wts.amur.graph.ChildrenMergerThread;
import org.gesis.wts.amur.graph.Config;
import org.gesis.wts.amur.graph.GraphAnalyser;
import org.gesis.wts.amur.graph.GraphClusterer;
import org.gesis.wts.amur.graph.objects.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeMerger {
	final static Logger log = LoggerFactory.getLogger(NodeMerger.class);
	
	public static Node mergeNodes(Node firstNode, Node secondNode) {
		Node resultNode = new Node();
		String mergedType = firstNode.getType() + "-" + secondNode.getType();
		resultNode.setType(mergedType);
		int mergedWeight = firstNode.getWeight() + secondNode.getWeight();
		resultNode.setWeight(mergedWeight);

		// merge children
		Node bufferNode = mergeChildren(firstNode, secondNode);
		resultNode.setChildrenWithKids(bufferNode.getChildrenWithKids());
		resultNode.setChildlessChildren(bufferNode.getChildlessChildren());
		resultNode.update();

		return resultNode;
	}

	private static Node mergeChildren(Node firstNode, Node secondNode) {
		Node resultNode = new Node();
		List<Node> firstNodeChildren = firstNode.getChildrenWithKids();
		List<Node> secondNodeChildren = secondNode.getChildrenWithKids();

		if (!firstNodeChildren.isEmpty()) {
			if (!secondNodeChildren.isEmpty()) {
				for (Node firstChild : firstNodeChildren) {
					Node newNode = new Node();
					Node bufferFirstNode = new Node(firstNode);
					bufferFirstNode.removeChildWithKids(firstChild);
					for (Node secondChild : secondNodeChildren) {
						Node bufferSecondNode = new Node(secondNode);
						bufferSecondNode.removeChildWithKids(secondChild);
						Node mergedNode = mergeNodes(firstChild, secondChild);

						Node bufferNode = new Node();
						bufferNode.addChildWithKids(mergedNode);

						Node mergedChildren = mergeChildren(bufferFirstNode, bufferSecondNode);
						bufferNode.addChildrenWithKids(mergedChildren.getChildrenWithKids());
						bufferNode.addChildlessChildren(mergedChildren.getChildlessChildren());
						bufferNode.update();
						newNode = GraphComparator.getBetterSubGraph(newNode, bufferNode);

					}
					resultNode = GraphComparator.getBetterSubGraph(resultNode, newNode);
				}
			} else {
				return mergeChildlessAndWithKids(firstNodeChildren, firstNode.getChildlessChildren(), secondNode.getChildlessChildren());
			}
		} else if (!secondNodeChildren.isEmpty()) {
			return mergeChildlessAndWithKids(secondNodeChildren, secondNode.getChildlessChildren(), firstNode.getChildlessChildren());
		} else {
			resultNode.setChildlessChildren(mergeChildlessChildren(firstNode.getChildlessChildren(), secondNode.getChildlessChildren()));
		}
		return resultNode;
	}

	private static Node mergeChildlessAndWithKids(List<Node> firstChildrenWithKids, List<Node> firstChildlessChildren, List<Node> secondChildless) {
		Node resultNode = new Node();
		// TODO: merge rest with childless and the remaining childless
		List<Node> bufferWithKids = new ArrayList<Node>();
		// Collections.sort(firstChildrenWithKids, Collections.reverseOrder());
		List<Node> bufferChildless = new ArrayList<Node>(secondChildless);
		Collections.sort(bufferChildless, Collections.reverseOrder());
		for (Node curWithKids : firstChildrenWithKids) {
			if (bufferChildless.size() > 0) {
				Node childlessNode = bufferChildless.get(0);
				bufferWithKids.add(mergeNodes(curWithKids, childlessNode));
				bufferChildless.remove(0);
			} else {
				bufferWithKids.add(curWithKids);
			}
		}
		resultNode.setChildlessChildren(mergeChildlessChildren(firstChildlessChildren, bufferChildless));
		resultNode.setChildrenWithKids(bufferWithKids);
		return resultNode;
	}

	private static List<Node> mergeChildlessChildren(List<Node> firstChildlessChildren, List<Node> secondChildless) {
		List<Node> resultNodeList = new ArrayList<Node>();
		Collections.sort(firstChildlessChildren, Collections.reverseOrder());
		List<Node> bufferChildless = new ArrayList<Node>(secondChildless);
		Collections.sort(bufferChildless, Collections.reverseOrder());

		for (Node curChild : firstChildlessChildren) {
			if (bufferChildless.size() > 0) {
				Node childlessNode = bufferChildless.get(0);
				resultNodeList.add(mergeNodes(curChild, childlessNode));
				bufferChildless.remove(0);
			} else {
				resultNodeList.add(curChild);
			}

		}
		for (Node curChild : bufferChildless) {
			resultNodeList.add(curChild);
		}
		return resultNodeList;
	}

	public static List<Node> mergeNodeChildrenThreaded(List<Node> firstNodeChildren, List<Node> secondNodeChildren) {
		List<Node> resultNodeList = new ArrayList<Node>();
		// executor for threading
		ExecutorService executor = Executors.newFixedThreadPool(Config.getInstance().getIntParameter("threadPoolSize"));
		List<Future<List<Node>>> listOfPermutations = new ArrayList<Future<List<Node>>>();

		if (!firstNodeChildren.isEmpty()) {
			if (!secondNodeChildren.isEmpty()) {

				for (Node firstChild : firstNodeChildren) {
					// List<Node> newNodeList = new ArrayList<Node>();
					List<Node> remainingFirstChildren = new ArrayList<Node>(firstNodeChildren);
					remainingFirstChildren.remove(firstChild);

					Callable<List<Node>> childrenMergerThread = new ChildrenMergerThread(firstChild, remainingFirstChildren, secondNodeChildren);

					// execute
					Future<List<Node>> permutation = executor.submit(childrenMergerThread);

					// remember
					listOfPermutations.add(permutation);

				}

				// finish them all
				executor.shutdown();

				while (!executor.isTerminated()) {
					// wait to terminate
				}

				resultNodeList = GraphComparator.getBestSubGraph(listOfPermutations);
			} else {
				return firstNodeChildren;
			}
		} else {
			return secondNodeChildren;
		}
		return resultNodeList;
	}
	
	public static List<Node> mergeCombinations(List<Node> graphList, List<Map<Byte, List<Byte>>> grouping, int id) {
		List<Node> bestResultGraphList = new ArrayList<>();
		// iterate all combinations of graph groups
		for (Map<Byte, List<Byte>> curGrouping : grouping) {
			log.info("thread " + id + " - new combination: " + curGrouping.toString());
			List<Node> resultGraphsList = new ArrayList<>();
			// iterate the groups. They are used as map keys
			for (Byte curGroup : curGrouping.keySet()) {
				List<Node> curGraphList = new ArrayList<>();
				// iterate over all graphs in the current group and add them to
				// the graphList
				for (Byte curGraph : curGrouping.get(curGroup)) {
					curGraphList.add(graphList.get(curGraph));
				}
				Node newNode = GraphAnalyser.mergeGraphsSorted(curGraphList);
				resultGraphsList.add(newNode);
			}
			bestResultGraphList = GraphComparator.getBetterGraphGrouping(bestResultGraphList, resultGraphsList);
		}
		return bestResultGraphList;
		
	}
}
