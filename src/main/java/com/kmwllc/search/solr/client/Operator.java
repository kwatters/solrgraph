package com.kmwllc.search.solr.client;

/**
 * Core expressions in the KMW Solr Query language
 * AND   - all terms and sub expressions must match 
 * OR    - 1 or more terms may match (OR expressions can specify 
 *         the minimum number of terms that can match
 * NOT   - non of the terms or subexpression should match
 * GRAPH - initial startNode query is used and then the values in 
 *         the edge id are matched against the values in the node id field, this is done
 *         recursively up to the maxDepth for the query (if maxDepth = -1, it will traverse the full graph.
 * JOIN  - Similar to a graph query, except the graph traversal is only 1 level deep.
 * 
 * @author kwatters
 *
 */
public enum Operator {
	AND,
	OR,
	NOT,
	GRAPH,
	JOIN
}
