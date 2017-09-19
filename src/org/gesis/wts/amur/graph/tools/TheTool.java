package org.gesis.wts.amur.graph.tools;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gesis.wts.amur.graph.Config;
import org.gesis.wts.amur.graph.objects.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheTool {
	final static Logger log = LoggerFactory.getLogger(TheTool.class);

	public static List<List<Integer>> generatePerm(List<Integer> original) {
		if (original.size() == 0) {
			List<List<Integer>> result = new ArrayList<List<Integer>>();
			result.add(new ArrayList<Integer>());
			return result;
		}
		int firstElement = original.remove(0);
		List<List<Integer>> returnValue = new ArrayList<List<Integer>>();
		List<List<Integer>> permutations = generatePerm(original);
		for (List<Integer> smallerPermutated : permutations) {
			for (int index = 0; index <= smallerPermutated.size(); index++) {
				List<Integer> temp = new ArrayList<Integer>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

	public static List<Map<Byte, List<Byte>>> grouping(Byte numberOrGraphs, Byte numberOfGroups) {
		// List<Integer> graphs = IntStream.rangeClosed(0, numberOrGraphs -
		// 1).boxed().collect(Collectors.toList());
		List<Byte> graphs = new ArrayList<>();
		for (Byte i = 0; i < numberOrGraphs; i++) {
			graphs.add(i);
		}
		// List<Integer> groups = IntStream.rangeClosed(0, numberOfGroups -
		// 1).boxed().collect(Collectors.toList());
		List<Byte> groups = new ArrayList<>();
		for (Byte i = 0; i < numberOfGroups; i++) {
			groups.add(i);
		}

		// List<Map<Integer, List<Integer>>> resultGrouping = grouping(graphs,
		// groups);
		List<Map<Byte, List<Byte>>> resultGrouping = groupingIterative(graphs, groups);

		// remove all symmetric groupings and groupings with empty groups
		List<Map<?, ?>> toBeRemoved = new ArrayList<Map<?, ?>>();
		for (Map<?, ?> curGroupping : resultGrouping) {
			if (curGroupping.size() < numberOfGroups)
				toBeRemoved.add(curGroupping);
		}
		resultGrouping.removeAll(toBeRemoved);
		log.info("grouping is done!");
		log.debug("final grouping is: \n\n" + resultGrouping.toString());

		return resultGrouping;
	}

	public static List<Map<Integer, List<Integer>>> grouping(List<Integer> graphs, List<Integer> groups) {
		log.info("starting recursive grouping for groups (" + groups.toString() + ") and graphs (" + graphs.toString() + ")");
		if (graphs.isEmpty()) {
			// Map<Integer, List<Integer>> empty = Collections.emptyMap();
			// return Collections.singletonList(empty);
			return new ArrayList<Map<Integer, List<Integer>>>();
		} else {
			Integer graph = graphs.get(0);
			List<Map<Integer, List<Integer>>> subs = grouping(graphs.subList(1, graphs.size()), groups);

			List<Map<Integer, List<Integer>>> solutions = new ArrayList<>();
			int subsSize = subs.size();
			if (subs.isEmpty())
				subs.add(new HashMap<Integer, List<Integer>>());

			for (Integer group : groups) {
				// only create unique combinations.
				// TODO: check if this works correct. NEEDED: number of distinct
				// combinations of n element into k groups
				if (group <= subsSize) {
					for (Map<Integer, List<Integer>> sub : subs) {
						// Map<Integer, List<Integer>> m = new HashMap<>(sub);
						// shallow copy does not suffice for a Map in this case,
						// therefore this manual cloning
						Map<Integer, List<Integer>> m = new HashMap<>();
						for (Integer curKey : sub.keySet()) {
							m.put(curKey, new ArrayList<>(sub.get(curKey)));
						}

						if (m.containsKey(group)) {
							m.get(group).add(graph);
						} else {
							m.put(group, new ArrayList<Integer>());
							m.get(group).add(graph);
						}
						solutions.add(m);
					}

				}

			}
			subs = null;
			System.gc();
			return solutions;
		}
	}

	public static List<Map<Byte, List<Byte>>> groupingIterative(List<Byte> graphs, List<Byte> groups) {
		log.info("starting iterative grouping for groups (" + groups.toString() + ") and graphs (" + graphs.toString() + ")");
		List<Map<Byte, List<Byte>>> resultList = new ArrayList<>();

		for (Byte curGraph : graphs) {
			log.debug("curGraph: " + curGraph + " - resultList size: " + resultList.size());

			List<Map<Byte, List<Byte>>> newElements = new ArrayList<>();
			// if there are already results in the list
			if (!resultList.isEmpty()) {
				for (Map<Byte, List<Byte>> curMap : resultList) {
					// System.out.println("curMap: " + curMap.toString());
					for (Byte curGroup : groups) {
						// maybe +1
						if (curGroup <= curMap.size()) {
							if (curGroup < groups.size() - 1) {
								// create new Map and add it
								Map<Byte, List<Byte>> newMap = new HashMap<>();

								// create a copy of the current map
								for (Byte curKey : curMap.keySet()) {
									newMap.put(curKey, new ArrayList<>(curMap.get(curKey)));
								}

								// add a new group if needed
								if (!newMap.containsKey(curGroup)) {
									List<Byte> newList = new ArrayList<>();
									newList.add(curGraph);
									newMap.put(curGroup, newList);
								} else {
									newMap.get(curGroup).add(curGraph);
								}

								// add newMap to resultList
								newElements.add(newMap);
								newMap = null;
							} else {
								if (!curMap.containsKey(curGroup)) {
									List<Byte> newList = new ArrayList<>();
									newList.add(curGraph);
									curMap.put(curGroup, newList);
								} else {
									curMap.get(curGroup).add(curGraph);
								}
							}

						} else {
							if (!curMap.containsKey(curGroup)) {
								List<Byte> newList = new ArrayList<>();
								newList.add(curGraph);
								curMap.put(curGroup, newList);
							} else {
								curMap.get(curGroup).add(curGraph);
							}
						}
					}
				}
				resultList.addAll(newElements);
				newElements = null;
				System.gc();
			} else {
				Map<Byte, List<Byte>> newMap = new HashMap<>();
				List<Byte> newList = new ArrayList<>();
				newList.add(curGraph);
				newMap.put(groups.get(0), newList);
				resultList.add(newMap);
			}
		}
		return resultList;

	}

	public static void sortRecursive(Node node, String order) {
		for (Node curChild : node.getChildrenWithKids()) {
			sortRecursive(curChild, order);
		}
		sort(node.getChildrenWithKids(), order);
		sort(node.getChildlessChildren(), order);
	}

	public static void sortRecursive(Node node) {
		sortRecursive(node, Config.getInstance().getStringParameter("sortMode"));
	}

	public static void sort(Node node) {
		sort(node, Config.getInstance().getStringParameter("sortMode"));
	}

	public static void sort(Node node, String order) {
		sort(node.getChildlessChildren(), order);
		sort(node.getChildrenWithKids(), order);
	}

	public static void sort(List<Node> nodes) {
		sort(nodes, Config.getInstance().getStringParameter("sortMode"));
	}

	public static void sort(List<Node> nodes, String order) {

		switch (order) {
		case "ASC":
			Collections.sort(nodes);
			break;
		case "DESC":
			Collections.sort(nodes, Collections.reverseOrder());

			break;
		case "SHUFFLE":
			Collections.shuffle(nodes);
			break;
		default:
			// do not sort specifically
			break;
		}
	}

	public static Node thresholdGraph(Node graph, int threshold) {
		return thresholdGraph(graph, threshold, false);
	}

	public static Node thresholdGraph(Node node, int threshold, boolean resetWeight) {
		if (node.getWeight() >= threshold) {
			Node newNode = new Node(node.getType(), node.getWeight());
			if (resetWeight)
				newNode.setWeight(1);

			for (Node curChild : node.getChildlessChildren()) {
				if (curChild.getWeight() >= threshold) {
					Node newChild = new Node(curChild.getType(), curChild.getWeight());
					if (resetWeight)
						newChild.setWeight(1);
					newNode.getChildren().add(newChild);
				}

			}
			for (Node curChild : node.getChildrenWithKids()) {
				if (curChild.getWeight() >= threshold) {
					Node newChild = thresholdGraph(curChild, threshold, resetWeight);
					if (newChild != null)
						newNode.getChildren().add(newChild);
				}
			}
			return newNode;
		} else
			return null;

	}

}
