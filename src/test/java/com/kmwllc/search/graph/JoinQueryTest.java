package com.kmwllc.search.graph;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class JoinQueryTest extends SolrTestCaseJ4 {
  
  @BeforeClass
  public static void beforeTests() throws Exception {
    //initCore("solrconfig-graph.xml","schema-graph.xml");
	  initCore("solrconfig.xml","schema.xml", "solr", "graph");
  }
  
  @Test
  public void testGraph() throws Exception {
    
    // 1 -> 2 -> 3 -> ( 4 5 )
    assertU(adoc("id", "1", "node_id", "1", "edge_id", "2", "text", "foo"));
    assertU(adoc("id", "2", "node_id", "2", "edge_id", "3", "text", "foo"));
    assertU(adoc("id", "3", "node_id", "3", "edge_id", "4", "edge_id", "5", "text", "foo"));
    assertU(adoc("id", "4", "node_id", "4", "text", "foo"));
    assertU(adoc("id", "5", "node_id", "5"));
    assertU(commit());
    // Now we have created a simple graph 
    
    // a join query to traverse the query
    String queryStr = "{!join from=edge_id to=node_id}node_id:3";
    SolrQueryRequest qr = req(queryStr);
    
    SolrParams params = qr.getParams();
    NamedList<Object> par = params.toNamedList();
    // par.add("q.type", "*");
    // defType specifies the parser to use.
    // par.add("defType", "graph");
    par.add("debugQuery", "true");
    par.add("hits", "10");

    SolrParams newp = SolrParams.toSolrParams(par);
    qr.setParams(newp);
    
    String responseXML = h.query(qr);
    System.out.println(responseXML);
    
    
  }
}
