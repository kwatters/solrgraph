package com.kmwllc.search.solr.client;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SyntaxError;
import org.junit.Assert;
import org.junit.Test;

import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.GraphExpression;
import com.kmwllc.search.solr.client.Operator;
import com.kmwllc.search.solr.client.QueryUtils;
import com.kmwllc.search.solr.client.Term;

public class GraphExpressionTest extends Assert {

	@Test
	public void testGraphExpression() throws SyntaxError {
		Expression startNodes = new Expression(Operator.AND);
		startNodes.add(new Term("text", "bar"));
		GraphExpression g = new GraphExpression(startNodes, "node_id", "edge_id", -1, null, false, false);
		System.out.println(g);
		String expected ="GRAPH(AND(text:bar),nodeField=node_id, edgeField=edge_id, maxDepth=-1, traversalFilter=null, returnSeeds=false, leafOnly=false)";
		System.out.println(expected);
		System.out.println(g.toString());
		assertEquals(expected, g.toString());

		
		g.setMaxDepth(2);
		//SolrQueryRequest req = null;
		//String lucGraph = QueryUtils.toLuceneQuery(g, req).toString();		
		// System.out.println(lucGraph);

	}

}
