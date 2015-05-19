package com.kmwllc.search.graph;

import java.util.Random;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.junit.BeforeClass;
import org.junit.Test;

import com.kmwllc.search.solr.client.AndExpression;
import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.Term;
import com.kmwllc.search.solr.parser.KMWQueryParser;
import com.thoughtworks.xstream.XStream;

/*
 * Licensed to KMW Technology LLC - All rights reserved.
 */

public class LargeGraphQueryTest extends SolrTestCaseJ4 {
  
  public static void beforeTests() throws Exception {
    //initCore("solrconfig-graph.xml","schema-graph.xml");
	  initCore("solrconfig.xml","schema.xml", "solr", "graph");
  }
  

  // TODO: re-enable with annotation
  public void loadGraph() throws Exception {
    
    // 1 -> 2 -> 3 -> ( 4 5 )
    // 7 -> 1
    // 8 -> ( 1 2 )
    
    createGraph();
    
    
//    SolrQueryRequest qr = createRequestWithTFilter("id:doc_1", "text:foo11");
//    assertQ(qr,"//*[@numFound='1']");

    Expression e = new AndExpression();
	e.add(new Term("id", "doc_id"));
	XStream xstream = KMWQueryParser.initXStream();
	String kmwXMLQuery = xstream.toXML(e);
	
	SolrQueryRequest qr = createRequest(kmwXMLQuery);
//    SolrQueryRequest qr = createRequestWithTFilter("id:doc_1", "title:b");
    
    SolrQueryResponse resp = h.queryAndResponse(null, qr);

    
    System.out.println("RESP: " + resp.toString());
    // assertQ(qr,"//*[@numFound='2']");
    

  }

  private void createGraph() {
    
    Random generator = new Random();
    int maxEdges = 4;
    int maxNodes = 10000;
    for (int i  = 0 ; i < maxNodes ; i++) {
      assertU(adoc(createDoc(generator, maxNodes, maxEdges, i)));
    }
    assertU(commit());
  }
  
  private SolrInputDocument createDoc(Random generator, int max, int maxEdges, int nodeId) {
    String id = "doc_" + nodeId;
    SolrInputDocument doc = new SolrInputDocument();
    doc.setField("id", id);
    // lets set the node id to the doc id
    // Does this need to be cast to a string?
    doc.setField("node_id", nodeId);
    // a random number from 1 to 100 for the edge
    // lets create 2 edges for each one.
    /// create a random number of edges.
    int numEdgesToGenerate = generator.nextInt(maxEdges);
    for (int nE = 0; nE < numEdgesToGenerate; nE++) {
      doc.addField("edge_id", generator.nextInt(max));
    }
    String tableValue = "document";
    // String tableValue = "person";
    doc.setField("table", tableValue);
    doc.setField("text", generateRandomText(generator));
    doc.setField("title", generateRandomText(generator));
    // doc.setField("date", new Date());
    return doc;
  }
  
  private  String generateRandomText(Random generator) {
    String[] bagOfWords = new String[]{"a","b", "c", "d", "e", "f"};
    StringBuilder textBuilder = new StringBuilder();
    String word = bagOfWords[generator.nextInt(bagOfWords.length)]; 
    textBuilder.append(word);
    textBuilder.append(" ");
    return textBuilder.toString().trim();
  }


  private SolrQueryRequest createRequest(String query) {
    SolrQueryRequest qr = req(query);
    NamedList<Object> par = qr.getParams().toNamedList();
    par.add("defType", "kmw");
    par.add("debug", "true");
    par.add("rows", "10");
    par.add("fl", "id,node_id,edge_id");
    par.remove("qt");
    par.add("qt", "/select");
    // par.add("")
    SolrParams newp = SolrParams.toSolrParams(par);
    qr.setParams(newp);
    return qr;
  }
  
  private SolrQueryRequest createRequestWithTFilter(String query, String traversalFilter) {
    SolrQueryRequest qr = req(query);
    NamedList<Object> par = qr.getParams().toNamedList();
    par.add("traversalFilter", traversalFilter);
    par.add("defType", "kmw");
    
    par.add("debug", "true");
    par.add("rows", "10");
    par.add("fl", "id,node_id,edge_id");
    par.remove("qt");
    par.add("qt", "/select");
    // par.add("")
    SolrParams newp = SolrParams.toSolrParams(par);
    qr.setParams(newp);
    return qr;
  }
  
  
}
