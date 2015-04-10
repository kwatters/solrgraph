package com.kmwllc.search.solr.parser;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;
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

public class KMWQueryParser extends QParser {

	public KMWQueryParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		super(qstr, localParams, params, req);
	}

	@Override
	public Query parse() throws SyntaxError {
		// if the original query string can deserialize 
		// via xstream
		// lets deserialize the query here.
		try {
			// TODO: initialize this as a singleton and re-use it
			XStream xstream = initXStream();
			Expression e = (Expression)xstream.fromXML(qstr);
			IndexSchema schema = req.getSchema();
			// we can pass the index schema in so we know 
			// how to tokenize phrases and terms & stuff
			// schema.getField("foo").getType().getAnalyzer();
			
			Query lucQuery = QueryUtils.toLuceneQuery(e, req);			
			System.out.println("######################################################");
			System.out.println(lucQuery.toString());
			System.out.println("######################################################");			
			return lucQuery;			
		} catch (Exception e) {
			System.out.println("XStream serialization failed, trying manual parsing.");
			e.printStackTrace();			
		}
		// TODO: the parser used should probably be configurable
//		Query startNodesQuery = null;
//		QParser parser = QParser.getParser(qstr, queryParser, req);
//		startNodesQuery = parser.getQuery();
//		System.out.println("Query Parser : " + parser.getClass());
//
//		// parse the traversal filter if it exists.
//		String tf = req.getParams().get("traversalFilter");
//		QParser tParser = QParser.getParser(tf, "lucene", req);
//		Query traversalFilter = tParser.getQuery();
//
//		// from field 
//		String fromField = req.getParams().get("nodeField", "node_id");
//		String toField = req.getParams().get("edgeField", "edge_id");
//		// Graph query construction
//		int maxDepth = Integer.valueOf(req.getParams().get("maxDepth", "-1"));
//
//		GraphQuery gq = new GraphQuery(startNodesQuery, fromField, toField, traversalFilter);
//
//		boolean returnStartNodes = req.getParams().getBool("noSeeds", true);
//		gq.setReturnStartNodes(returnStartNodes);
//
//		boolean leafOnly = req.getParams().getBool("leafOnly", false);
//		gq.setOnlyLeafNodes(leafOnly);
//		gq.setMaxDepth(maxDepth);
//
//		return gq;
		// TODO: this is bad if we make it here.
		return null;
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
