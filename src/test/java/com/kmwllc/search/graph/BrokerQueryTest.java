package com.kmwllc.search.graph;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.DirectXmlRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;

public class BrokerQueryTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int level = 1;
		int shardId = 1;
		String shardHostPort = "localhost:8983";
		String solrUrl = "http://" + shardHostPort + "/solr"; 
		SolrServer solrServer = new HttpSolrServer( solrUrl );
		
	    ModifiableSolrParams params = new ModifiableSolrParams();
	    String queryId = "123";
	    // params.add("q", queryString);
	    // params.add("qt", "frontier");
	    params.add("level", Integer.toString(level));
	    params.add("shardId", Integer.toString(shardId));
	    params.add("queryId", queryId);
		QueryResponse qresp;
	    try {
	    	DirectXmlRequest sreq = new DirectXmlRequest("frontier", "<test/>");
	    	sreq.setPath("/frontier");
	    	sreq.setParams(params);
	    	NamedList<Object> res = solrServer.request(sreq);
	    	Object o = res.get("part_1");
	    	Query q = (Query)o;
	        System.out.println(q);
	    } catch (SolrServerException e) {
//	        throw new RuntimeException();
	        System.out.println(e.getLocalizedMessage());
	        e.printStackTrace();
	    }
	    
	    
	}

}
