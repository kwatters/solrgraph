package com.kmwllc.search.solr.parser;

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;
import com.kmwllc.search.graph.GraphQuery;

/**
 * Licensed KMW Technology LLC - All rights reserved.
 * 
 * Solr query parser that will handle parsing graph query requests.
 * it uses xstream to deserialize the query string into a graph query
 * if possible.
 */

public class GraphQueryParser extends QParser {

	public GraphQueryParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		super(qstr, localParams, params, req);
	}

	@Override
	public Query parse() throws SyntaxError {
		SolrParams localParams = getLocalParams();
		SolrParams params = getParams();
		SolrParams solrParams = SolrParams.wrapDefaults(localParams, params);
		QParser baseParser = subQuery(solrParams.get(QueryParsing.V), null);
        Query startNodesQuery = baseParser.getQuery();	
        
        String fromField = localParams.get("fromField", "node_id");
		String toField = localParams.get("toField", "edge_ids");
		
		QParser traversalBaseParser = subQuery(localParams.get("traversalFilter"), null);		
        Query traversalFilter = traversalBaseParser.getQuery();	
        
		boolean onlyLeafNodes = Boolean.valueOf(localParams.get("onlyLeafNodes", "false"));
		boolean returnStartNodes = Boolean.valueOf(localParams.get("returnStartNodes", "true"));;
		int maxDepth = Integer.valueOf(localParams.get("maxDepth", "-1"));
		
		GraphQuery gq = new GraphQuery(startNodesQuery, fromField, toField, traversalFilter);
		gq.setMaxDepth(maxDepth);
		gq.setOnlyLeafNodes(onlyLeafNodes);
		gq.setReturnStartNodes(returnStartNodes);
		return gq;
	}

}
