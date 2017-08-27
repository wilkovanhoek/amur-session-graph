package org.gesis.wts.amur.graph.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.gesis.wts.amur.graph.objects.Node;

public class GraphComparator {
	
	
	public static Node getBestGraph(List<Future<Node>> listOfGraphs) {
		Node resultGraph = new Node();

		for (int i = 0; i < listOfGraphs.size(); i++) {
			Future<Node> curGraphObject = listOfGraphs.get(i);
			Node curGraph;
			try {
				curGraph = curGraphObject.get();
				if (curGraph.getSubtreeWeight() < curGraph.getSubtreeWeight()) {
					resultGraph = curGraph;
				}
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return resultGraph;
	}

	public static List<Node> getBestSubGraph(List<Future<List<Node>>> listOfPermutations) {
		List<Node> resultList = new ArrayList<>();

		for (int i = 0; i < listOfPermutations.size(); i++) {
			Future<List<Node>> curPermutation = listOfPermutations.get(i);
			List<Node> curList;
			try {
				curList = curPermutation.get();
				resultList = getBetterSubGraph(resultList, curList);

			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		return resultList;
	}

	public static int sumSubTreeWeights(List<Node> nodeList) {
		int sum = 0;
		for (Node curNode : nodeList) {
			sum += curNode.getSubtreeWeight();
		}
		return sum;
	}

	public static List<Node> getBetterSubGraph(List<Node> firstGraphList, List<Node> secondGraphList) {
		List<Node> resultNodeList = firstGraphList;
		if (firstGraphList.isEmpty()) {
			resultNodeList = secondGraphList;
		} else {
			int firstSubTreeWeight = sumSubTreeWeights(firstGraphList);
			int secondSubTreeWeight = sumSubTreeWeights(secondGraphList);
			if (firstSubTreeWeight < secondSubTreeWeight) {
				resultNodeList = secondGraphList;
			}
		}
		return resultNodeList;
	}

	public static Node getBetterSubGraph(Node firstNode, Node secondNode) {		
		Node resultNode = firstNode;
		int firstNumberOfNodes = firstNode.getNumberOfNodes();
		int secondNumberOfNodes = secondNode.getNumberOfNodes();
		if((firstNumberOfNodes<=0 && secondNumberOfNodes>0) ){
			resultNode = secondNode;
		}else if((secondNumberOfNodes<=0 && firstNumberOfNodes>0) ){
			resultNode = secondNode;
			
		}else if(firstNode.getNumberOfNodes()>secondNode.getNumberOfNodes()){
			resultNode = secondNode;
		}else if(firstNode.getNumberOfNodes()==secondNode.getNumberOfNodes()){
			double firstMax = getMaximumSubtreeWeight(firstNode);
			double secondMax = getMaximumSubtreeWeight(secondNode);
			if(firstMax<secondMax){
				resultNode = secondNode;
			}else if(secondMax==firstMax){
				if (firstNode.getSubtreeWeight() < secondNode.getSubtreeWeight()) {
					resultNode = secondNode;
				}		
			}
			
			
		}
		return resultNode;
	}
	
	public static double getMaximumSubtreeWeight(Node node){
		double maxSoFar = 0.0;
		for(Node curChild: node.getChildrenWithKids()){
			if(maxSoFar<curChild.getSubtreeWeight()) maxSoFar = curChild.getSubtreeWeight();
		}
		return maxSoFar;
		
	}
	
	
	public static List<Node> getBetterGraphGroupingThreaded(List<Future<List<Node>>> listOfGraphs) {
		List<Node> resultGraphList = new ArrayList<Node> ();

		for (int i = 0; i < listOfGraphs.size(); i++) {
			Future<List<Node>> curGraphListObject = listOfGraphs.get(i);
			List<Node> curGraphList;
			try {
				curGraphList = curGraphListObject.get();				
				resultGraphList = getBetterGraphGrouping(resultGraphList, curGraphList);				
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return resultGraphList;
	}

	public static List<Node> getBetterGraphGrouping(List<Node> firstGraphList, List<Node> secondGraphList) {
		if(firstGraphList.isEmpty()){
			if(secondGraphList.isEmpty()){
				return null;
			}else{
				return secondGraphList;	
			}			
		}else if(secondGraphList.isEmpty()){
			return firstGraphList;
		}
		
		List<Node> resultList = new ArrayList<>();
		int firstNumberOfNodes = 0;
		int firstSubtreeWeight = 0;
		int secondNumberOfNodes = 0;
		int secondSubtreeWeight = 0;
		
		for(Node curGraph: firstGraphList){
			firstNumberOfNodes += curGraph.getNumberOfNodes();
			firstSubtreeWeight += curGraph.getSubtreeWeight();
		}
		
		for(Node curGraph: secondGraphList){
			secondNumberOfNodes += curGraph.getNumberOfNodes();
			secondSubtreeWeight += curGraph.getSubtreeWeight();
		}
		
		if(firstNumberOfNodes < secondNumberOfNodes){
			resultList =  firstGraphList;
		}else if(firstNumberOfNodes > secondNumberOfNodes ){
			resultList =  secondGraphList;
		}else{
			if(firstSubtreeWeight < secondSubtreeWeight){
				resultList =  secondGraphList;
			}else{
				resultList =  firstGraphList;
			}
		}
		
		return resultList;
	}
}
