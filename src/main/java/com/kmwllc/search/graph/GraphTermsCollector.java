package com.kmwllc.search.graph;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefHash;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.search.BitDocSet;
import org.apache.solr.search.DocSet;

/**
  * Licensed to KMW Technology
  * A graph hit collector.
  */

public class GraphTermsCollector extends Collector {
  
  private String field;
  private BytesRefHash collectorTerms;
  private SortedSetDocValues docTermOrds;
  
  private Bits currentResult;
  private DocSet leafNodes;
  
  // TODO: could i create a more effecient way of caching
  // doc id to compiled autn for it's edge?
  // private GraphCache graphEdgeCache;

  // TODO: add caching to the collector :) 
  // improve the lookup of field values.
  
  // TODO: more effecient storage of the term ords
  // private HashSet<Integer> termOrds = new HashSet<Integer>();
  
  int numHits=0;
  FixedBitSet bits;
  final int maxDoc;
  int base;
  int baseInParent;
  
  GraphTermsCollector(String field,int maxDoc, Bits currentResult, DocSet leafNodes) {
    this.field = field;
    this.maxDoc = maxDoc;
    this.collectorTerms =  new BytesRefHash();
    this.currentResult = currentResult;
    this.leafNodes = leafNodes;
    //this.base = base;
    // TODO: consider creating an edge cache for the compiled autn
    // for the query out of a given doc.
    // this.graphEdgeCache = GraphCache.getInstance();
	if (bits==null) {
      bits = new FixedBitSet(maxDoc);
    }	
  }
  
  @Override
  public void setScorer(Scorer scorer) throws IOException {}
  
  public void collect(int doc) throws IOException {    
    //System.out.println("COLLECTING " + doc + " BASE " + base);
    doc += base;
    if (currentResult.get(doc)) {
      // cycle detected
      // already been here.
      // System.out.println("CYCLE DETECTED.");
      // TODO: when we track the links into a document
      // we would still need to track that before we return
      // we actually don't really know why we entered this node, 
      // tracking the path of traversal is postentially very expense.
      return;
    }
    // collect the docs
    addDocToResult(doc);
    // collect the terms for the edge ids
    // Optimization to not traverse the edge id's for a document
    // that is known to not have any edges.
    if (!leafNodes.exists(doc)) {
      // If this document is not a leaf node we might need
      // to traverse its edges.
      addEdgeIdsToResult(doc-base);
    } 
    //    else {
    //      System.out.println("Leaf node...");
    //    }
    
    // TODO: add some metadata to what the links into this doc were, 
    // so we could trace back in the result set.
    
  }

  private void addEdgeIdsToResult(int doc) throws IOException {
	// set the doc to pull the edges ids for.
	docTermOrds.setDocument(doc);
	// TODO: why is this final?
	BytesRef scratch = new BytesRef();
	long ord;
    while ((ord = docTermOrds.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
       scratch = docTermOrds.lookupOrd(ord);
       // add the edge id to the collector terms.
       // TODO: how do we handle non-string type fields?
       // do i need to worry about that here?
       collectorTerms.add(scratch);
    }
  }

  private void addDocToResult(int docWithBase) {
    // System.out.println("Adding internal doc id :" + docWithBase);
	// TODO: what happened to fastSet(id) ?
    bits.set(docWithBase);
    numHits++;
  }
  
  public DocSet getDocSet() {
	  
    if (bits == null) {
      bits = new FixedBitSet(maxDoc);
    }
    // System.out.println("BIT SET POSITION :" + pos);
    return new BitDocSet(bits,numHits);
  }
  
  @Override
  public void setNextReader(AtomicReaderContext context) throws IOException {
	docTermOrds = FieldCache.DEFAULT.getDocTermOrds(context.reader(), field);
    base = context.docBase;
    baseInParent = context.docBaseInParent;
    
	// System.out.println("#################### NEW READER CONTEXT : " + context.docBase + " IN PARENT: " + context.docBaseInParent);
    //docTermOrds = FieldCache.DEFAULT.getDocTermOrds(context.reader(), field);
    //docTermsEnum = docTermOrds.getOrdTermsEnum(context.reader());
    // update the doc base when we're in a new context?
    // reuse = null; // LUCENE-3377 needs to be fixed first then this statement can be removed...
  }
  
  @Override
  public boolean acceptsDocsOutOfOrder() {
    return true;
  }
  
  public BytesRefHash getCollectorTerms() {
    return collectorTerms;
  }
  
}
