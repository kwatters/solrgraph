package com.kmwllc.search.solr.client;

import org.junit.Assert;
import org.junit.Test;

import com.kmwllc.search.solr.client.Expression;
import com.kmwllc.search.solr.client.GraphExpression;
import com.kmwllc.search.solr.client.Operator;
import com.kmwllc.search.solr.client.Term;
import com.kmwllc.search.solr.parser.KMWQueryParser;
import com.thoughtworks.xstream.XStream;

public class ExpressionToXMLTest extends Assert {

	@Test
	public void serializeQuery() {
	
		
		Expression startNodes = new Expression(Operator.AND);
		startNodes.add(new Term("title", "foo"));
		startNodes.add(new Term("table", "t1"));		
		GraphExpression g = new GraphExpression(startNodes, "node_id", "edge_id", -1, null, true, false);
		XStream xstream = KMWQueryParser.initXStream();
		String graphXML = xstream.toXML(g);
		System.out.println(graphXML);
		
	}
}
