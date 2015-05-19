package com.kmwllc.search.solr.parser;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

import com.kmwllc.search.graph.GraphQuery;
import com.kmwllc.search.solr.client.AndExpression;
import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.GraphExpression;
import com.kmwllc.search.solr.client.JoinExpression;
import com.kmwllc.search.solr.client.NotExpression;
import com.kmwllc.search.solr.client.OrExpression;
import com.kmwllc.search.solr.client.QueryUtils;
import com.kmwllc.search.solr.client.Term;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Licensed KMW Technology LLC - All rights reserved.
 * 
 * Solr query parser that will handle parsing graph query requests.
 * it uses xstream to deserialize the query string into a graph query
 * if possible.
 */

public class GraphQueryParser extends QParser {

	private SolrParams solrParams;

	public GraphQueryParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		super(qstr, localParams, params, req);
	}

	@Override
	public Query parse() throws SyntaxError {
		SolrParams localParams = getLocalParams();
		SolrParams params = getParams();
		solrParams = SolrParams.wrapDefaults(localParams, params);
		
		QParser baseParser = subQuery(localParams.get("q"), null);
        Query startNodesQuery = baseParser.getQuery();	
        
        String fromField = localParams.get("fromField", "node_id");
		String toField = localParams.get("toField", "edge_ids");
		
		QParser traversalBaseParser = subQuery(localParams.get("q"), null);		
        Query traversalFilter = traversalBaseParser.getQuery();	
        
		boolean onlyLeafNodes = Boolean.valueOf(localParams.get("onlyLeafNodes", "false"));
		boolean returnStartNodes = Boolean.valueOf(localParams.get("returnStartNodes", "false"));;
		int maxDepth = Integer.valueOf(localParams.get("maxDepth", "-1"));
		
		GraphQuery gq = new GraphQuery(startNodesQuery, fromField, toField, traversalFilter);
		gq.setMaxDepth(maxDepth);
		gq.setOnlyLeafNodes(onlyLeafNodes);
		gq.setReturnStartNodes(returnStartNodes);
		return gq;
	}

	// TODO: replace xstream with other serialization library
	public static XStream initXStream() {
		XStream xstream = new XStream(new StaxDriver());
		xstream.alias("graphQuery", GraphQuery.class);
		xstream.alias("termQuery", TermQuery.class);
		xstream.alias("GRAPH", GraphExpression.class);
		xstream.alias("JOIN", JoinExpression.class);
		xstream.alias("AND", AndExpression.class);
		xstream.alias("OR", OrExpression.class);
		xstream.alias("NOT", NotExpression.class);
		xstream.alias("TERM", Term.class);		
		return xstream;
	}
}
