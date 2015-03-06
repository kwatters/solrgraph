package com.kmwllc.search.graph;

import org.apache.lucene.search.Query;

public class FrontierQuery {

	private final Query first;
	private final Integer frontierSize;
	
	public FrontierQuery(Query first, Integer frontierSize) {
		super();
		this.first = first;
		this.frontierSize = frontierSize;
	}
	public Query getFirst() {
		return first;
	}
	public Integer getFrontierSize() {
		return frontierSize;
	}
	
}
