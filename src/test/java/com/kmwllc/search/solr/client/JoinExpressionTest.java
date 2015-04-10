package com.kmwllc.search.solr.client;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SyntaxError;
import org.junit.Assert;
import org.junit.Test;

import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.JoinExpression;
import com.kmwllc.search.solr.client.Operator;
import com.kmwllc.search.solr.client.QueryUtils;
import com.kmwllc.search.solr.client.Term;

public class JoinExpressionTest extends Assert {

	
	@Test
	public void testJoinExpression() throws SyntaxError {
		Expression leftQuery = new Expression(Operator.AND);
		leftQuery.add(new Term("text", "foo"));
		leftQuery.add(new Term("table", "t1"));

		Expression rightQuery = new Expression(Operator.AND);
		rightQuery.add(new Term("text", "bar"));
		rightQuery.add(new Term("table", "t2"));

		JoinExpression jq = new JoinExpression(leftQuery, rightQuery, "node_id", "edge_id");
		System.out.println(jq);
	
		String expected = "JOIN(AND(text:foo,table:t1), INNER(AND(text:bar,table:t2), on=\"node_id=edge_id\"))";

		assertEquals(expected, jq.toString());		
		IndexSchema schema = null;
		SolrQueryRequest req = null;
		String lucJoin = QueryUtils.toLuceneQuery(jq, req).toString();		
		System.out.println(lucJoin);
		
	}

}
