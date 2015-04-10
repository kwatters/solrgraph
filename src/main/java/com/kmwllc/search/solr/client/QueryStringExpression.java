package com.kmwllc.search.solr.client;

/**
 * This expression will take a query string and interpret it using 
 * the standard lucene parser
 * 
 * @author kwatters
 *
 */
public class QueryStringExpression extends Expression {

	private final String queryString;
	private final String parser = "lucene";

	public QueryStringExpression(String queryString) {
		// TODO: this should probably be the default query operator
		// for the system.. maybe not..
		super(Operator.AND);
		// TODO Auto-generated constructor stub
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getParser() {
		return parser;
	}

}
