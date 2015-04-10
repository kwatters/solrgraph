package com.kmwllc.search.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
  * Licensed to the KMW Technology , all rights reserved.
  * 
  */

public class CreateGraph {
  
  private Random generator = new Random();
  private String solrUrl = "http://localhost:8983/solr";
  private String solrUrl2 = "http://localhost:9983/solr";
  private ArrayList<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>();
  private int batchSize = 5000;
  // 
  private int batches = 0;
  private int numNodes = 100000;
  private int maxEdges = 10;
  private int startId = 0;
  private SolrServer solrServer = new HttpSolrServer( solrUrl );
  private String[] bagOfWords = new String[]{"a","b", "c", "d", "e", "f"};
  
  public CreateGraph() {
    // Default Constructor.
  }
  
  public void BuildGraph() throws SolrServerException, IOException {
    // now that we have a solr server, lets create and add some docs.
    // a random number of edges up to 5 ?
    // avg number of edges is 1/2 of this.
    
    for (int i = startId; i < numNodes+startId; i++) {
      SolrInputDocument doc = createDoc(generator, numNodes, maxEdges, i);
      feed(doc);
    }
    flush();
  }
  
  private void feed(SolrInputDocument doc) throws SolrServerException, IOException {
    //
    solrDocs.add(doc);
    if (solrDocs.size() >= batchSize ) {
      solrServer.add(solrDocs);
      solrDocs.clear();
      System.out.println("committed batch.");
      batches++;
      if (batches % 2 == 0) {
    	  solrServer = new HttpSolrServer( solrUrl );
      } else {
    	  solrServer = new HttpSolrServer( solrUrl2 );
      }
    }

  }
  
  private void flush() throws SolrServerException, IOException {
    //
    if (solrDocs.size() > 0) {
      solrServer.add(solrDocs);
      // This is rather thread unsafe.
      solrDocs.clear();     
    } 
    // This is rather thread unsafe.
    
	  solrServer = new HttpSolrServer( solrUrl );
	  solrServer.commit();
	  solrServer = new HttpSolrServer( solrUrl2 );
	  solrServer.commit();
    

    System.out.println("Flushed..");

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
    doc.setField("text", generateRandomText());
    doc.setField("title", generateRandomText());
    doc.setField("date", new Date());
    return doc;
  }

  private  String generateRandomText() {
    StringBuilder textBuilder = new StringBuilder();
    String word = bagOfWords[generator.nextInt(bagOfWords.length)]; 
    textBuilder.append(word);
    textBuilder.append(" ");
    return textBuilder.toString().trim();
  }
  
  /**
   * @param args - none
   * @throws IOException - from solr 
   * @throws SolrServerException - from solr
   */
  public static void main(String[] args) throws SolrServerException, IOException {
    CreateGraph cg = new CreateGraph();
    System.out.println("Starting...");
    cg.BuildGraph();
    System.out.println("Finished.");
  }
  
}
