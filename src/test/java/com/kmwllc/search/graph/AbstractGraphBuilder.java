package com.kmwllc.search.graph;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 * 
 * @author kwatters
 *
 */
public abstract class AbstractGraphBuilder {
	private String solrUrl = "http://localhost:8983/solr";
	private ArrayList<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>();
	private SolrServer solrServer = new HttpSolrServer( solrUrl );
	private int batchSize = 1000;

	public abstract void BuildGraph() throws SolrServerException, IOException;

	public void reconnect() {
		solrServer = new HttpSolrServer( solrUrl );
	}
	
	protected void feed(SolrInputDocument doc) throws SolrServerException, IOException {
		//
		solrDocs.add(doc);
		if (solrDocs.size() >= batchSize ) {
			solrServer.add(solrDocs);
			solrDocs.clear();
			System.out.println("committed batch.");
		}

	}

	protected void flush() throws SolrServerException, IOException {
		//
		if (solrDocs.size() > 0) {
			solrServer.add(solrDocs);
			// This is rather thread unsafe.
			solrDocs.clear();     
		} 
		// This is rather thread unsafe.
		solrServer.commit();

		System.out.println("Flushed..");

	}

	public String getSolrUrl() {
		return solrUrl;
	}

	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}
}
