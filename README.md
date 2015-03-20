# solrgraph
Graph query support for Lucene and Solr.  Traverse records that contain a field that links to other records to resolve the final result set.

This currently builds on Solr 4.10.3.

For more information regarding support and integration contact Kevin Watters - kwatters@kmwllc.com 

The Graph Query is a lucene level query operator that will allow the traversal of documents that contain a field which represents a node ID and a field that contains a list of values that represent the Edge IDs.

GraphQuery contains the following options:

q - the initial start query that identifies the universe of documents to start traversal from.

fromField - the field name that contains the node id

toField - the name of the field that contains the edge id(s).

tarversalFilter - this is an additional query that can be supplied to limit the scope of graph traversal to just the edges that satisify the traversalFilter query.

returnStartNodes - boolean to determine if the documents that matched the original "q" should be returned as part of the graph.

onlyLeafNodes - boolean that filters the graph query to only return documents/nodes that have no edges.

maxDepth - integer specifying how deep the breadth first search should go.

Example, given 3 documents 
Doc1.
title: "foo"
node_id:1
edge_id:2

Doc2 
title: "bar"
node_id:2
edge_id:3

Doc3 
title: "baz"
node_id:3


A graph query could start with a query of   title:foo
title:foo    this will find Doc1.
Doc1 has node id "1" , and an edge that points to "2".  The graph will traverse into "Doc2" because it matches node_id:2. 
It will then take the value of "edge_id:3" and match Doc3 becasue it has "node_id:3" 

The full result set will include doc1, doc2 and doc3.




