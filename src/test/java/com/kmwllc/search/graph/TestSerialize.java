package com.kmwllc.search.graph;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class TestSerialize {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// XStream xstream = new XStream();
		// XStream xstream = new XStream(new DomDriver());
		XStream xstream = new XStream(new StaxDriver());
		TermQuery tq = new TermQuery(new Term("foo", "bar"));
		GraphQuery gq = new GraphQuery(tq, "edgeid", "nodeid", null);
		
		
		GraphQuery gq2 = new GraphQuery(gq, "edgeid2", "nodeid2", null);
		
		xstream.alias("graphQuery", GraphQuery.class);
		xstream.alias("termQuery", TermQuery.class);
		
		String xml = xstream.toXML(gq2);
		// xml = xml.replaceAll(">", ">\n");
		System.out.println(xml);
		
		gq = (GraphQuery) xstream.fromXML(xml);
		
		System.out.println(gq.toString());
	}
	

}
