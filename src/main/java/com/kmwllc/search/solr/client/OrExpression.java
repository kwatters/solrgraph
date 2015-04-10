package com.kmwllc.search.solr.client;

public class OrExpression extends Expression {

	int minShouldMatch = 1;
	
	public OrExpression() {
		super(Operator.OR);
		// TODO Auto-generated constructor stub
	}

	public int getMinShouldMatch() {
		return minShouldMatch;
	}

	public void setMinShouldMatch(int minShouldMatch) {
		this.minShouldMatch = minShouldMatch;
	}

}
