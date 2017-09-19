package org.gesis.wts.amur.graph.objects;

import java.util.ArrayList;
import java.util.List;

import org.gesis.wts.amur.graph.tools.TheTool;

public class Node implements Comparable<Node> {

	private String type;
	int weight;
	private double subtreeWeight;
	private int numberOfNodes;
	private List<Node> children;
	// TODO: find more stupid naming for the two lists
	private List<Node> childrenWithKids;
	private List<Node> childlessChildren;

	public Node() {
		this.children = new ArrayList<Node>();
		this.childrenWithKids = new ArrayList<Node>();
		this.childlessChildren = new ArrayList<Node>();
		this.weight = 1;
		this.type = "init";
	}

	public Node(String type, int weight) {
		this.children = new ArrayList<Node>();
		this.childrenWithKids = new ArrayList<Node>();
		this.childlessChildren = new ArrayList<Node>();
		this.weight = weight;
		this.type = type;
	}

	public Node(List<Node> newList) {
		this.setChildren(newList);
	}
	
	public Node(Node copyNode) {
		this.children = new ArrayList<Node>(copyNode.getChildren());		
		this.childrenWithKids = new ArrayList<Node>(copyNode.getChildrenWithKids());
		this.childlessChildren = new ArrayList<Node>(copyNode.getChildlessChildren());
		this.weight = copyNode.getWeight();
		this.type = copyNode.getType();
		this.subtreeWeight = copyNode.subtreeWeight;
		this.numberOfNodes = copyNode.numberOfNodes;
	}

	public Node(Node copyNode, boolean deepCopy) {
		if(deepCopy){
			this.children = Node.getCopies(copyNode.getChildren());		
			this.childrenWithKids = Node.getCopies(copyNode.getChildrenWithKids());
			this.childlessChildren = Node.getCopies(copyNode.getChildlessChildren());
			this.weight = copyNode.getWeight();
			this.type = copyNode.getType();
			this.subtreeWeight = copyNode.subtreeWeight;
			this.numberOfNodes = copyNode.numberOfNodes;
		}	
		
	}
	
	public static List<Node> getCopies(List<Node> nodeList){
		List<Node> copyNodeList = new ArrayList<Node>();
		for(Node curNode: nodeList){
			copyNodeList.add(new Node(curNode, true));
		}
		return copyNodeList;
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public double getSubtreeWeight() {
		return subtreeWeight;
	}

	public void setSubtreeWeight(double subtreeWeight) {
		this.subtreeWeight = subtreeWeight;
	}

	public List<Node> getChildrenWithKids() {
		return childrenWithKids;
	}

	public void setChildrenWithKids(List<Node> childrenWithKids) {
		this.childrenWithKids = childrenWithKids;
	}

	public List<Node> getChildlessChildren() {
		return childlessChildren;
	}

	public void setChildlessChildren(List<Node> childlessChildren) {
		this.childlessChildren = childlessChildren;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public boolean hasChildrenWithKids() {
		return !this.childrenWithKids.isEmpty();
	}

	public boolean hasChildlessChildren() {
		return !this.childlessChildren.isEmpty();
	}

	public void addWeight(int weight) {
		this.weight += weight;

	}

	public boolean addChildWithKids(Node newChildWithKids) {
		return this.childrenWithKids.add(newChildWithKids);
	}

	public boolean removeChildWithKids(Node child) {
		return this.childrenWithKids.remove(child);
	}

	public boolean addChildrenWithKids(List<Node> newChildrenWithKids) {
		return this.childrenWithKids.addAll(newChildrenWithKids);
	}

	public boolean addChildlessChildren(List<Node> newChildlessChildren) {
		return this.childlessChildren.addAll(newChildlessChildren);
	}
	
	public boolean addChildlessChild(Node newChildlessChildren) {
		return this.childlessChildren.add(newChildlessChildren);
	}

	@Override
	public int compareTo(Node node) {
		int subTreeComparison = Double.compare(this.subtreeWeight, node.getSubtreeWeight());
		if (subTreeComparison == 0) {			
			return Integer.compare(this.getWeight(), node.getWeight());
		} else {
			return subTreeComparison;
		}
	}

	public void init() {
		this.initSubTreeWeight();
		this.initNumberOfNodes();
	}

	public void update() {
		this.updateSubTreeWeight();
		this.updateNumberOfNodes();
	}

	public void updateSubTreeWeight() {
		double newSubtreeWeight = 2;
		//
		for (Node curChild : this.childrenWithKids) {
			newSubtreeWeight += curChild.weight * curChild.getSubtreeWeight();
		}
		for (Node curChild : this.childlessChildren) {
			newSubtreeWeight += curChild.weight * curChild.getSubtreeWeight();
		}
		
		this.subtreeWeight = Math.log(this.weight * newSubtreeWeight)/Math.log(2);
	}

	public double initSubTreeWeight() {
		double newSubtreeWeight = 2;

		for (Node curChild : this.children) {
			if (curChild.hasChildren()) {
				this.childrenWithKids.add(curChild);
			} else {
				this.childlessChildren.add(curChild);
			}
			newSubtreeWeight += curChild.weight * curChild.initSubTreeWeight();
		}
		this.children.clear();

		TheTool.sort(this);

		this.subtreeWeight = Math.log(this.weight * newSubtreeWeight)/Math.log(2);
		return newSubtreeWeight;
	}

	public void updateNumberOfNodes() {
		int newNumberOfNodes = 1;
		newNumberOfNodes += childlessChildren.size();
		for (Node curChild : this.childrenWithKids) {
			newNumberOfNodes += curChild.getNumberOfNodes();
		}
		this.numberOfNodes = newNumberOfNodes;
	}

	public int initNumberOfNodes() {
		int newNumberOfNodes = 1;
		newNumberOfNodes += childlessChildren.size();
		for (Node curChild : childrenWithKids) {
			newNumberOfNodes += curChild.initNumberOfNodes();
		}
		for (Node curChild : childlessChildren) {
			curChild.initNumberOfNodes();
		}
		this.numberOfNodes = newNumberOfNodes;
		return newNumberOfNodes;
	}

	public int getNumberOfNodes() {
		return this.numberOfNodes;
	}

	

}
