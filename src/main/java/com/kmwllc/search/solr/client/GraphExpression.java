package com.kmwllc.search.solr.client;

public class GraphExpression extends Expression {

	private Expression startNodes = null;
	private String nodeField = "node_field";
	private String edgeField = "edge_field";
	private int maxDepth = -1;
	private Expression traversalFilter = null;
	private boolean returnSeedNodes = true;
	private boolean leafOnly = false;
	
	public GraphExpression(Expression startNodes, String nodeField, String edgeField, 
			int maxDepth, Expression traversalFilter, boolean returnSeeds,
			boolean leafOnly) {
		super(Operator.GRAPH);
		this.startNodes = startNodes;
		this.nodeField = nodeField;
		this.edgeField = edgeField;
		this.maxDepth = maxDepth;
		this.traversalFilter = traversalFilter;
		this.returnSeedNodes = returnSeeds;
		this.leafOnly = leafOnly;
	}

	public String getNodeField() {
		return nodeField;
	}

	public void setNodeField(String nodeField) {
		this.nodeField = nodeField;
	}

	public String getEdgeField() {
		return edgeField;
	}

	public void setEdgeField(String edgeField) {
		this.edgeField = edgeField;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public Expression getTraversalFilter() {
		return traversalFilter;
	}

	public void setTraversalFilter(Expression traversalFilter) {
		this.traversalFilter = traversalFilter;
	}

	public boolean isReturnSeeds() {
		return returnSeedNodes;
	}

	public void setReturnSeeds(boolean returnSeeds) {
		this.returnSeedNodes = returnSeeds;
	}

	public boolean isLeafOnly() {
		return leafOnly;
	}

	public void setLeafOnly(boolean leafOnly) {
		this.leafOnly = leafOnly;
	}

	@Override
	public String toString() {
		// TODO: make a good version of this that can do a pretty print.
		// as well as a no white space version
		String graphString = "GRAPH(";
		graphString += startNodes +",";
		// TODO: remove the defaults from the to string ?
		graphString += "nodeField=" + nodeField + ", edgeField=" + edgeField
				+ ", maxDepth=" + maxDepth + ", traversalFilter="
				+ traversalFilter + ", returnSeeds=" + returnSeedNodes
				+ ", leafOnly=" + leafOnly;
		graphString += ")";
		return graphString;
	}

	public Expression getStartNodes() {
		return startNodes;
	}

	public void setStartNodes(Expression startNodes) {
		this.startNodes = startNodes;
	}

}
