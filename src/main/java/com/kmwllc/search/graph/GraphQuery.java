package com.kmwllc.search.graph;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;
import org.apache.lucene.util.FixedBitSet;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

/**
 * Licensed to the KMW Technology
 * http://www.kmwllc.com/
 * 
 * Author: kwatters
 * 
 */

public class GraphQuery extends Query {
  
  /** The inital node matching query */
  private Query q;
  /** the field with the node id */
  private String fromField;
  /** the field containing the edge ids */
  private String toField;
  /** A query to apply while traversing the graph to filter out edges */
  private Query traversalFilter;
  /** The max depth to traverse the graph, -1 means no limit. */
  private int maxDepth = -1;
  /** Use automaton compilation for graph query traversal (expert use only) */
  // TODO: fix autn support!
  private boolean useAutn = false;
  
  // True if we should only return items that are leaf nodes in the graph.
  // i.e. no edge ids.
  private boolean onlyLeafNodes = false;
  
  // True if the seed query for the graph should be excluded from the result set.
  private boolean returnStartNodes = true;
  
  // For printing debug info about the graph.
  private boolean debug = false;
  
  // The ability to turn this on / off for testing purposes 
  // would be good.
  // private boolean useEdgeCycleDetection = false;
  /**
   * Create a graph query 
   * @param q - the starting node query
   * @param fromField - the field containing the node id
   * @param toField - the field containing the edge ids
   */
  public GraphQuery(Query q, String fromField, String toField) {
    this.q = q;
    this.fromField = fromField;
    this.toField = toField;
    this.traversalFilter = null;
  }
  
  /**
   * Create a graph query with a traversal filter applied while traversing the frontier.
   * @param q - the starting node query
   * @param fromField - the field containing the node id
   * @param toField - the field containing the edge ids
   * @params traversalFilter - the filter to be applied on each iteration of the frontier.
   */
  public GraphQuery(Query q, String fromField, String toField, Query traversalFilter) {
	// System.out.println("CONSTRUTOR 2");
    this.q = q;
    this.fromField = fromField;
    this.toField = toField;
    this.traversalFilter = traversalFilter;
    if (q == null) {
    	System.out.println("Q Was null");
    }
    if (fromField == null) {
    	System.out.println("ff Was null");
    }
    if (toField == null) {
    	System.out.println("tof Was null");
    }
    if (traversalFilter == null) {
    	System.out.println("travf Was null");
    }
  }
  
  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    // TODO: what do we really need to pass here?
	// passing the searcher seems ok for now, but what about a shared request?
    Weight graphWeight = new GraphQueryWeight((SolrIndexSearcher)searcher);
    return graphWeight;
  }
  
  @Override
  public String toString(String field) {
	// TODO:make something more formal here.
	// possibly return the serialized xml for this?
	StringBuilder sb = new StringBuilder();
    sb.append("[[" + q.toString() + "]," + fromField + "=" + toField + "]");
    if (traversalFilter != null) {
      sb.append(" [TraversalFilter: " + traversalFilter.toString() + "]");
    }
    sb.append("[returnStartNodes=" + returnStartNodes + "]");
    sb.append("[onlyLeafNodes=" + onlyLeafNodes + "]");
    // TODO: need to add the boolean flag to the to string method.
    return sb.toString();
  }
  
  protected class GraphQueryWeight extends Weight {
    SolrIndexSearcher fromSearcher;
    private float queryNorm = 1.0F;
    private float queryWeight = 1.0F; 
    int frontierSize = 0;
    
    public int currentDepth = 0;
    
    private Filter filter;
    private ResponseBuilder rb;
    private DocSet resultSet;

    
    public GraphQueryWeight(SolrIndexSearcher searcher) {
      // Grab the searcher so we can run additional searches.
      this.fromSearcher = searcher;
    }
    
    @Override
    public Explanation explain(AtomicReaderContext context, int doc) throws IOException {
    	// TODO: figure out what this actually is!
    	final Scorer cs = scorer(context, context.reader().getLiveDocs());
        final boolean exists = (cs != null && cs.advance(doc) == doc);
        final ComplexExplanation result = new ComplexExplanation();
        if (exists) {
          result.setDescription(GraphQuery.this.toString() + ", product of:");
          result.setValue(queryWeight);
          result.setMatch(Boolean.TRUE);
          result.addDetail(new Explanation(getBoost(), "boost"));
          result.addDetail(new Explanation(queryNorm, "queryNorm"));
        } else {
          result.setDescription(GraphQuery.this.toString() + " doesn't match id " + doc);
          result.setValue(0);
          result.setMatch(Boolean.FALSE);
        }
        return result;
    }
    
    @Override
    public Query getQuery() {
      // TODO: not sure when this gets called.
      return null;
    }
    
    @Override
    public float getValueForNormalization() throws IOException {
      // No normalization 1.0
      return 1F;
    }
    
    @Override
    public void normalize(float norm, float topLevelBoost) {
      // TODO: normalize the weight?  do I care about this? likely not, but who knows.
      this.queryWeight = norm * topLevelBoost;
    }
    
    /**
     * This computes the matching doc set for a given graph query
     * 
     * @return DocSet representing the documents in the graph.
     * @throws IOException - if a sub search fails... maybe other cases too! :)
     */
    private DocSet getDocSet() throws IOException {
     
      DocSet fromSet = null;
      
      FixedBitSet seedResultBits = null;
      
      // Size that the bit set needs to be.
      int capacity = fromSearcher.getAtomicReader().maxDoc();
      // The bit set to contain the results that match the query.
      FixedBitSet resultBits = new FixedBitSet(capacity);
      
      // The measure of how deep in the graph we have gone.
      currentDepth = 1;
      // the edge id's that have been visited
      // HashSet<BytesRef> visitedNodes = new HashSet<BytesRef>();
      // the initial query for the frontier for the first query
      Query frontierQuery = q;
      
      // Find all documents in this graph that are leaf nodes to speed traversal
      // this is an optimization to avoid looking up terms for a document that
      // doesn't have any edge ids.
      BooleanQuery leafNodeQuery = new BooleanQuery();
      WildcardQuery edgeQuery =new WildcardQuery(new Term(toField, "*"));
      leafNodeQuery.add(edgeQuery, Occur.MUST_NOT);

      // TODO: cache the leaf node computation at a higher level and pass it in.
      long start = System.currentTimeMillis(); 
      // TODO: this is a good one for auto-warming!
      DocSet leafNodes = fromSearcher.getDocSet(leafNodeQuery);      
      long leafDelta = System.currentTimeMillis() - start;
      System.out.println("Leaf node query took " + leafDelta + " ms.");
      // Start the breadth first graph traversal.
      do {
        // Time each level traversal of the frontier
        long startFSearch = System.currentTimeMillis();

        // Create the graph result collector.
        // TODO: can i / should i create this outside of this loop? (for now use a local instance for each iteration?)
        GraphTermsCollector graphResultCollector = new GraphTermsCollector(toField,capacity, resultBits, leafNodes);
        // traverse the next level!
        fromSearcher.search(frontierQuery, graphResultCollector);
        // how long it took to run the search
        long deltaFSearch = System.currentTimeMillis() - startFSearch;        
        // All edge ids on the frontier.
        BytesRefHash collectorTerms = graphResultCollector.getCollectorTerms();
        // TODO: log message not stdout!
        if (collectorTerms != null) {
          System.out.println("Frontier query took " + deltaFSearch + " ms. FrontierSize: " + collectorTerms.size() + " Depth " + currentDepth);
        } else {
          System.out.println("Collector terms was null.");
        }
        // how big is our frontier?
        frontierSize = collectorTerms.size();

        // The resulting doc set from the frontier.
        fromSet = graphResultCollector.getDocSet();
        
        if (seedResultBits == null) {
          // grab a copy of the see bits   
          // TODO: fix this type cast!
          seedResultBits = ((BitDocSet)fromSet).getBits().clone();
        }
        
        // TODO: Maybe it's an optimization to exit on an empty result set here?

        // Debug if needed
        // printDocIDs(fromSet);

        long startCompile = System.currentTimeMillis();
        // TODO: log message vs system.out
        System.out.println("FRONTIER SIZE BEFORE: " + frontierSize);
        Integer fs = new Integer(frontierSize);
        FrontierQuery fq = buildFrontierQuery(collectorTerms, fs);
        if (fq == null) {
        	// eek.. TODO: clean this up.
        	fq = new FrontierQuery(null, 0);
        }
        frontierQuery = fq.getFirst();
        frontierSize = fq.getFrontierSize();
        // TODO: log message not stdout
        System.out.println("FRONTIER SIZE AFTER: " + frontierSize);
        long compileTime = System.currentTimeMillis() - startCompile;
        // TODO: log message not stdout
        System.out.println("Frontier Size : " + frontierSize + " Depth : " + currentDepth + " Compile Time : " + compileTime );
        if (debug) {
          // TODO : log message not stdout
          if (frontierQuery != null) {
            System.out.println("Frontier Query : " + frontierQuery.toString());
          } else {
            System.out.println("Frontier query was null.");
          }
        }
        // Add our frontier docs to the main graph results.
        
        // resultBits.union(((BitDocSet)fromSet).getBits());
        // TODO: validate that OR is what UNION was
        resultBits.or(((BitDocSet)fromSet).getBits());
        
        // Increment how far we have gone in the frontier.
        currentDepth++;
        // Break out if we have reached our max depth
        if (currentDepth > maxDepth && maxDepth != -1) {
          break;
        }

      } while (frontierSize > 0);
      

      if (!returnStartNodes) {
          resultBits.andNot(seedResultBits);
      }
      BitDocSet resultSet = new BitDocSet(resultBits);

      // If we only want to return leaf nodes do that here.
      if (onlyLeafNodes) {
    	 return resultSet.intersection(leafNodes);
      } else {
          // currentDepth -= 1;
	      // System.out.println("Traversed " + numFound + " docs.");
	      // System.out.println("Traversal Depth " + currentDepth + " levels.");
	      // System.out.println("Result Bits Has " + resultBits.size() + " bits set.");
	      // create a doc set off the bits that we found.
    	  return resultSet;
      }
    }
    
    @SuppressWarnings("unused")
    private void printDocIDs(DocSet fromSet) throws IOException {
      // Only used in debugging.
      System.out.println("---------------------------");
      DocIterator iter = fromSet.iterator();
      while (iter.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
    	Integer docId = iter.next();
        Document d = fromSearcher.doc(docId);
        String id = d.getValues("id")[0];
        // TODO :log message  (this is too verbose?)
        System.out.println("INTERNAL ID : " + docId + " DOC : " + id);
      }
      System.out.println("---------------------------");
    }
    
    /** Build an automaton to represent the frontier query */
//    private Automaton buildAutomaton(BytesRefHash termBytesHash) {
//      // TODO: perhaps we could improve this by 
//      // using a sorted list to insert them into..
//      // maybe a tree set or something?
//      final TreeSet<BytesRef> terms = new TreeSet<BytesRef>();
//      for (int i = 0 ; i < termBytesHash.size(); i++) {
//        BytesRef ref = new BytesRef();
//        termBytesHash.get(i, ref);
//        terms.add(ref);
//      }
//      // TODO: This builder isn't public/visible!!! WTF.. it works though :)
//      // I Believe the set needs to be sorted to call this autn builder.
//
//      // 
//      // TODO: THIS IS BAD!
//      // Gotta build this and add it to the class path! for the time being , 
//      // we can't use this one until we update the build.
//      // useAutn must be false.
//      // DaciukMihovAutomatonBuilder builder = new DaciukMihovAutomatonBuilder();
//      // final Automaton a =  DaciukMihovAutomatonBuilder.build(terms);
//      // DaciukMihovAutomatonBuilder.build(terms);
//      Automaton a = AutomatonBuilder.build(terms);      
//      return a;    
//    }
    
    /**
     * This method returns a query based on a frontier set of edge ids
     * 
     * @param collectorTerms - the terms that represent the edge ids for the current frontier.
     * @param frontierSize 
     * @return a query that represents the next hops in the graph. 
     */
    public FrontierQuery buildFrontierQuery(BytesRefHash collectorTerms, Integer frontierSize) {
      // TODO: pick a non-public interface.. and still support the distributed graph stuff.
      if (collectorTerms == null || collectorTerms.size() == 0) {
        // return null if there are no terms (edges) to traverse.
        return null;
      } else {
        // Create a query
        BooleanQuery frontierQuery = new BooleanQuery();
        // Optionally use the automaton based query
        // TODO: see if we should dynamically select this based
        // Based on the frontier size.
        if (useAutn) {
        	// TODO: re-enable the autn compilation of graph frontier query
//          // build an automaton based query for the frontier.
//          Automaton autn = buildAutomaton(collectorTerms);
//          AutomatonQuery autnQuery = new AutomatonQuery(new Term(fromField), autn);
//          BooleanClause autnClause = new BooleanClause(autnQuery, Occur.MUST);
//          frontierQuery.add(autnClause);
        } else {
          // Iterate and build a booleanQuery with many clauses for the query.
          BooleanQuery edgeQuery = new BooleanQuery(true);
          for (int i = 0 ; i < collectorTerms.size(); i++) {
            BytesRef ref = new BytesRef();
            collectorTerms.get(i, ref);
            // TODO: Do I need turn it into a string? it would be nice to use the bytesref instead.
            // In previous testing, if i didn't resolve it to a utf8 string, it didn't work..
            // edgeQuery.add(new TermQuery(new Term(fromField, ref)), Occur.SHOULD);
            edgeQuery.add(new TermQuery(new Term(fromField, ref.utf8ToString())), Occur.SHOULD);
          }
          BooleanClause edgeClause = new BooleanClause(edgeQuery, Occur.MUST);
          frontierQuery.add(edgeClause);
        }
        
        // If there is a filter to be used while crawling the graph, add that.
        if (traversalFilter != null) {
          frontierQuery.add(traversalFilter, Occur.MUST);
        } 
        // return the new query. 
        FrontierQuery fq = new FrontierQuery(frontierQuery, frontierSize);
        return fq;
      }
    }

	@Override
	public Scorer scorer(AtomicReaderContext context, Bits acceptDocs) throws IOException {
		
		 if (filter == null) {
             boolean debug = rb != null && rb.isDebug();
             long start = System.currentTimeMillis();
             resultSet = getDocSet();
             long delta = System.currentTimeMillis()-start;
             System.out.println("Graph Traverse took : " + delta + " ms.");
             filter = resultSet.getTopFilter();
     }
     // TODO: understand this comment.
     // Although this set only includes live docs, other filters can be pushed down to queries.
     DocIdSet readerSet = filter.getDocIdSet(context, acceptDocs);
     // create a scrorer on the result set, if results from right query are empty, use empty iterator.
     return new GraphScorer(this, readerSet == null ? DocIdSetIterator.empty() : readerSet.iterator(), getBoost());
	}
  }
  
  private class GraphScorer extends Scorer {
    
    final DocIdSetIterator iter;
    final float score;
    
    public GraphScorer(Weight w) throws IOException {
      super(w);
      // TODO: what should these get initialized to?
      // TODO: a proper constructor.. I think I need the doc id set iterator..
      iter = DocIdSet.EMPTY.iterator();
      score = 0;
    }
    
    public GraphScorer(Weight w, DocIdSetIterator iter, float score) throws IOException {
      super(w);
      this.score = score;
      this.iter = iter==null ? DocIdSet.EMPTY.iterator() : iter;
    }
    
    @Override
    public float score() throws IOException {
      // System.out.println("SCORE: " + score);
      // we have the curDocId ... maybe i can have an array that contains the link-in count.
      return score;
    }
    
    @Override
    public int nextDoc() throws IOException {
      return iter.nextDoc();
    }
    
    @Override
    public int docID() {
      return iter.docID();
    }
    
    @Override
    public int advance(int target) throws IOException {
      return iter.advance(target);
    }
    
    @Override
    public int freq() throws IOException {
      // TODO: what does this really do?
      return 1;
    }

	@Override
	public long cost() {
		// TODO Auto-generated method stub
		return 0;
	}
  }

  public Query getTraversalFilter() {
    return traversalFilter;
  }

  public void setTraversalFilter(Query traversalFilter) {
    this.traversalFilter = traversalFilter;
  }

  public Query getQ() {
    return q;
  }

  public void setQ(Query q) {
    this.q = q;
  }

  public String getFromField() {
    return fromField;
  }

  public void setFromField(String fromField) {
    this.fromField = fromField;
  }

  public String getToField() {
    return toField;
  }

  public void setToField(String toField) {
    this.toField = toField;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public boolean isUseAutn() {
    return useAutn;
  }

  public void setUseAutn(boolean useAutn) {
    this.useAutn = useAutn;
  }

public boolean isOnlyLeafNodes() {
	return onlyLeafNodes;
}

public void setOnlyLeafNodes(boolean onlyLeafNodes) {
	this.onlyLeafNodes = onlyLeafNodes;
}

public boolean isReturnStartNodes() {
	return returnStartNodes;
}

public void setReturnStartNodes(boolean returnStartNodes) {
	this.returnStartNodes = returnStartNodes;
}

@Override
public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (debug ? 1231 : 1237);
	result = prime * result + ((fromField == null) ? 0 : fromField.hashCode());
	result = prime * result + maxDepth;
	result = prime * result + (onlyLeafNodes ? 1231 : 1237);
	result = prime * result + ((q == null) ? 0 : q.hashCode());
	result = prime * result + (returnStartNodes ? 1231 : 1237);
	result = prime * result + ((toField == null) ? 0 : toField.hashCode());
	result = prime * result + ((traversalFilter == null) ? 0 : traversalFilter.hashCode());
	result = prime * result + (useAutn ? 1231 : 1237);
	return result;
}

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (!super.equals(obj))
		return false;
	if (getClass() != obj.getClass())
		return false;
	GraphQuery other = (GraphQuery) obj;
	if (debug != other.debug)
		return false;
	if (fromField == null) {
		if (other.fromField != null)
			return false;
	} else if (!fromField.equals(other.fromField))
		return false;
	if (maxDepth != other.maxDepth)
		return false;
	if (onlyLeafNodes != other.onlyLeafNodes)
		return false;
	if (q == null) {
		if (other.q != null)
			return false;
	} else if (!q.equals(other.q))
		return false;
	if (returnStartNodes != other.returnStartNodes)
		return false;
	if (toField == null) {
		if (other.toField != null)
			return false;
	} else if (!toField.equals(other.toField))
		return false;
	if (traversalFilter == null) {
		if (other.traversalFilter != null)
			return false;
	} else if (!traversalFilter.equals(other.traversalFilter))
		return false;
	if (useAutn != other.useAutn)
		return false;
	return true;
}
  
}
