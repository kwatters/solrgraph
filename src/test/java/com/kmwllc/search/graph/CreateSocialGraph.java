package com.kmwllc.search.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

public class CreateSocialGraph extends AbstractGraphBuilder {

	public CreateSocialGraph() {
		// Default Constructor.
	}

	public void BuildGraphA() throws SolrServerException, IOException {
		// now that we have a solr server, lets create and add some docs.
		// a random number of edges up to 5 ?
		// avg number of edges is 1/2 of this.
		ArrayList<String> friendIDs = new ArrayList<String>();
		friendIDs.add("4");
		friendIDs.add("5");
		
		SolrInputDocument doc = CreatePerson("3",  "Charlie Cat", friendIDs);
		feed(doc);

		ArrayList<String> friendID2s = new ArrayList<String>();
		friendID2s.add("4");
		SolrInputDocument doc2 = CreatePerson("6",  "KMW Tech", friendID2s);
		feed(doc2);
		
		flush();
	}

	public void BuildGraphB() throws SolrServerException, IOException {
		// now that we have a solr server, lets create and add some docs.
		// a random number of edges up to 5 ?
		// avg number of edges is 1/2 of this.

		ArrayList<String> friendIDs = new ArrayList<String>();
		friendIDs.add("6");
		SolrInputDocument doc2 = CreatePerson("4", "Kevin", friendIDs);
		feed(doc2);
		SolrInputDocument doc3 = CreatePerson("5", "Lauren", null);
		feed(doc3);
		
		flush();
	}
	
	private SolrInputDocument CreatePerson(String id, String name, List<String> friendIDs) throws SolrServerException, IOException {
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "person_" + id);
		doc.setField("name", name);
		doc.addField("node_id", id);
		if (friendIDs != null) {
			for (String edge : friendIDs) {
				doc.addField("edge_id", edge);
			}
		}
		return doc;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	public static void main(String[] args) throws SolrServerException, IOException {
		// TODO Auto-generated method stub

		CreateSocialGraph sg = new CreateSocialGraph();
		sg.BuildGraph();
	}

	@Override
	public void BuildGraph() throws SolrServerException, IOException {
		// TODO Auto-generated method stub
		
		setSolrUrl("http://localhost:8983/solr");
		BuildGraphA();
		
		setSolrUrl("http://localhost:9983/solr");
		reconnect();
		BuildGraphB();
	}

}

