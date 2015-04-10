package com.kmwllc.search.graph;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;


/**
  * Licensed to KMW Technology - All rights reserved.
  */
public class QueryTest {
  
  /*
   * @param args - 
   * @throws SolrServerException  -
   */
  public static void main(String[] args) {
    String solrUrl = "http://localhost:8983/solr";
    SolrServer solrServer = new HttpSolrServer( solrUrl );
    int numQueries = 1000;
    long totalTime = 0;
    long maxTime = 0;
    long maxCount = 0;
    long largestResultSet = 0;
    for (int id = 0 ; id < numQueries; id++) {
      ModifiableSolrParams params = new ModifiableSolrParams();
      String queryString = "id:doc_"+id;
      params.add("q", queryString);
      params.add("defType", "graph");
      params.add("rows", "0");
      QueryResponse qresp;
      try {
        qresp = solrServer.query(params);
      } catch (SolrServerException e) {
//        throw new RuntimeException();
        System.out.println(e.getLocalizedMessage());
        e.printStackTrace();
        break;
      }
      long numFound = qresp.getResults().getNumFound();
      int qTime = qresp.getQTime();
      System.out.println("Query " + queryString + " Found " + numFound + " in " + qTime + "ms.");
      totalTime += qTime;
      if (qTime > maxTime) {
        maxTime = qTime;
        maxCount = numFound;
      }
      if (numFound > largestResultSet) {
        largestResultSet = numFound;
      }
    }
    long avgTime = totalTime / numQueries;
    System.out.println("Total Time : " + totalTime + " AVG: " + avgTime);
    System.out.println("Max Time : " + maxTime + " Num: " + maxCount);
    System.out.println("Largest Result Set : " + largestResultSet);
  }
  
}
