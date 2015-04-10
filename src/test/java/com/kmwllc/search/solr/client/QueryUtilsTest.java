package com.kmwllc.search.solr.client;

import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.SyntaxError;
import org.junit.Assert;
import org.junit.Test;

import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.Operator;
import com.kmwllc.search.solr.client.QueryUtils;
import com.kmwllc.search.solr.client.Term;

public class QueryUtilsTest extends Assert {

	@Test
	public void testToLuceneQuery() throws SyntaxError {
		// IndexSchema schema = null;
		// TODO: fix this
		SolrQueryRequest req = null;
		Expression e = new Expression(Operator.AND);
		e.add(new Term("text", "foo"));
		e.add(new Term("text", "bar"));
		
		String lQ = QueryUtils.toLuceneQuery(e, req).toString();		
		System.out.println(lQ);
		assertEquals("+text:foo +text:bar", lQ);
		
		Expression eOr = new Expression(Operator.OR);
		eOr.add(new Term("title", "a"));
		eOr.add(new Term("title", "b"));
		
		String lorQ = QueryUtils.toLuceneQuery(eOr, req).toString();		
		System.out.println(lorQ);
		// Default operator is OR?
		assertEquals("title:a title:b", lorQ);

		e.add(eOr);
		
		lQ = QueryUtils.toLuceneQuery(e, req).toString();		
		System.out.println(lQ);
		// should there be a + infront of the ( ?
		assertEquals("+text:foo +text:bar (title:a title:b)", lQ);
		
		
	}
}
