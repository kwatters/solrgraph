package com.kmwllc.search.solr.client;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.BeforeClass;
import org.junit.Test;



import com.kmwllc.search.solr.client.AndExpression;
import com.kmwllc.search.solr.client.OrExpression;
import com.kmwllc.search.solr.client.PhraseTerm;
import com.kmwllc.search.solr.client.Term;
import com.kmwllc.search.solr.parser.KMWQueryParser;
import com.thoughtworks.xstream.XStream;

public class TermQueryTest extends SolrTestCaseJ4 {

	@BeforeClass
	public static void beforeTests() throws Exception {
		//initCore("solrconfig-graph.xml","schema-graph.xml");
		initCore("solr/graph/solrconfig-graph.xml","solr/graph/schema-graph.xml");
	}

	@Test
	public void testTerm() throws Exception {

		// 1 -> 2 -> 3 -> ( 4 5 )
		// 7 -> 1
		// 8 -> ( 1 2 )

		assertU(adoc("id", "doc_1", "title", "foo bar", "text", "this is some test"));
		assertU(adoc("id", "doc_2", "title", "foo baz", "text", "this is some text"));		
		assertU(adoc("id", "doc_3", "title", "Foo baz bar", "text", "this is some tezz"));

		assertU(commit());

		XStream xstream = KMWQueryParser.initXStream();

		// test a single term
		AndExpression e = new AndExpression();
		e.add(new Term("title", "Foo"));
		String kmwXMLQuery = xstream.toXML(e);
		SolrQueryRequest qReq = createRequest(kmwXMLQuery);
		assertQ(qReq,"//*[@numFound='3']");

		e = new AndExpression();
		e.add(new Term("title", "bAr "));
		kmwXMLQuery = xstream.toXML(e);
		qReq = createRequest(kmwXMLQuery);
		assertQ(qReq,"//*[@numFound='2']");


		//try a phrase term
		e = new AndExpression();
		PhraseTerm pt = new PhraseTerm("title", "foo bar");
		e.add(pt);
		kmwXMLQuery = xstream.toXML(e);

		qReq = createRequest(kmwXMLQuery);
		assertQ(qReq,"//*[@numFound='1']");
		
		
		
		 // lets make a more interesting expression
		//  AND(title:foo, OR(text:a, text:b))
		AndExpression topAnd = new AndExpression();
		topAnd.add(new Term("title", "foo"));
		OrExpression textOr = new OrExpression();
		textOr.add(new Term("text", "test"));
		textOr.add(new Term("text", "text"));
	    topAnd.add(textOr);
	    kmwXMLQuery = xstream.toXML(topAnd);
		qReq = createRequest(kmwXMLQuery);
		assertQ(qReq,"//*[@numFound='2']");	    
	}

	private SolrQueryRequest createRequest(String query) {
		SolrQueryRequest qr = req(query);
		// TODO: really, parameterized as Object?
		NamedList<Object> par = qr.getParams().toNamedList();
		par.add("defType", "graph");
		par.add("debug", "true");
		par.add("rows", "10");
		par.add("fl", "id");
		par.remove("qt");
		par.add("qt", "/select");
		SolrParams newp = SolrParams.toSolrParams(par);
		qr.setParams(newp);
		return qr;
	}

}
