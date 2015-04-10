package com.kmwllc.search.solr.client;

import org.junit.Assert;
import org.junit.Test;

import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.Operator;
import com.kmwllc.search.solr.client.Term;

public class ExpressionTest extends Assert {

	@Test
	public void testExpression() {
		Expression e = new Expression(Operator.AND);
		Term t1 = new Term("text", "foo");
		Term t2 = new Term("text", "bar");

		e.add(t1);		
 		e.add(t2);
 		System.out.println(e.toString());
 		
 		assertEquals("AND(text:foo,text:bar)", e.toString());

 		Expression or = new Expression(Operator.OR);
 		or.add(new Term("title", "a"));
 		or.add(new Term("title", "b"));

 		assertEquals("OR(title:a,title:b)", or.toString());
 		e.add(or);
 		assertEquals("AND(text:foo,text:bar,OR(title:a,title:b))", e.toString());
 		
 		
		
	}

}
