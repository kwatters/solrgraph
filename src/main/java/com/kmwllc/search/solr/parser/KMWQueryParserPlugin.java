package com.kmwllc.search.solr.parser;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
  * Licensed to KMW Technology.
  * http://www.kmwllc.com/
  * Query parser plugin for solr to wrap the graph query parser.
  * 
  */
public class KMWQueryParserPlugin extends QParserPlugin {

	public static String NAME = "graph";

	private NamedList args = null;

	public void init(NamedList args) {
		this.args = args;
	}

	@Override
	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new KMWQueryParser(qstr, localParams, params, req);
	}

}
