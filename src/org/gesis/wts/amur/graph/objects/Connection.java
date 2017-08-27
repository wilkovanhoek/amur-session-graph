package org.gesis.wts.amur.graph.objects;

public class Connection {
	String firstNode;
	String secondNode;
	int weight;
	
	public Connection(String firstNode, String secondNode, int weight) {
		super();
		this.firstNode = firstNode;
		this.secondNode = secondNode;
		this.weight = weight;
	}
	
	public String getFirstNode() {
		return firstNode;
	}
	public void setFirstNode(String firstNode) {
		this.firstNode = firstNode;
	}
	public String getSecondNode() {
		return secondNode;
	}
	public void setSecondNode(String secondNode) {
		this.secondNode = secondNode;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	

}