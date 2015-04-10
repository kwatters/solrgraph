package com.kmwllc.search.solr.client;

import org.junit.Assert;
import org.junit.Test;

import com.kmwllc.search.solr.client.Term;

public class TermTest extends Assert {

	@Test
	public void testTerm() {		
		Term t1 = new Term("text", "term");
		Term t2 = new Term("text", "term");
		Term t3 = new Term("text", "term3");
		assertEquals(t1, t2);
		assertNotSame(t1, t3);
		assertEquals("text:term", t1.toString());
		assertEquals(t1.hashCode(), t2.hashCode());
		assertNotSame(t1.hashCode(), t3.hashCode());
		
	}
	
}
