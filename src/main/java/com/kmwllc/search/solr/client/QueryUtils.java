package com.kmwllc.search.solr.client;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;
//import com.kmwllc.solr.graph.GraphQuery;
//import com.kmwllc.solr.graph.distributed.DistributedGraphQuery;


import com.kmwllc.search.graph.GraphQuery;

public class QueryUtils {

	public static Query toLuceneQuery(Expression e, SolrQueryRequest req) throws SyntaxError {
		IndexSchema schema = req.getSchema();

		if (e == null) {
			return null;
		}
		if (e instanceof GraphExpression) {
			// if this is a graph lets return the graphQuery object
			GraphExpression g = (GraphExpression)e;
			Query startQuery = QueryUtils.toLuceneQuery(g.getStartNodes(), req);
			Query traversalFilter = QueryUtils.toLuceneQuery(g.getTraversalFilter(), req);
			//DistributedGraphQuery gq = new DistributedGraphQuery(startQuery, g.getNodeField(), g.getEdgeField(), traversalFilter);
			GraphQuery gq = new GraphQuery(startQuery, g.getNodeField(), g.getEdgeField(), traversalFilter);
			gq.setReturnStartNodes(g.isReturnSeeds());
			gq.setOnlyLeafNodes(g.isLeafOnly());
			gq.setMaxDepth(g.getMaxDepth());		
			return gq;
		} else if (e instanceof JoinExpression) {
			JoinExpression j = (JoinExpression)e;
			Query leftQuery = QueryUtils.toLuceneQuery(j.getStartNodes(), req);
			Query rightQuery = QueryUtils.toLuceneQuery(j.getTraversalFilter(), req);
			//DistributedGraphQuery gq = new DistributedGraphQuery(leftQuery, j.getPrimaryKey(), j.getForeignKey(), rightQuery);
			GraphQuery gq = new GraphQuery(leftQuery, j.getPrimaryKey(), j.getForeignKey(), rightQuery);
			gq.setReturnStartNodes(false);
			gq.setOnlyLeafNodes(false);
			return gq;			
		} else if (e instanceof QueryStringExpression) {
			// TODO: handle this!
			String qs = ((QueryStringExpression) e).getQueryString();
			String queryParser = ((QueryStringExpression) e).getParser();

			QParser parser = QParser.getParser(qs, queryParser, req);
			Query query = parser.getQuery();
			return query;

		} else {
			BooleanQuery bq = new BooleanQuery();
			Operator op = e.getOp();
			Occur occ = null;
			if (op.equals(Operator.AND)) {
				occ = Occur.MUST;
			} else if (op.equals(Operator.OR)) {
				// TODO: expose this as a paramter for an OR expression
				OrExpression or = (OrExpression)e;
				bq.setMinimumNumberShouldMatch(or.getMinShouldMatch());
				occ = Occur.SHOULD;
			} else if (op.equals(Operator.NOT)) {
				bq.add(new MatchAllDocsQuery(), Occur.MUST);
				occ = Occur.MUST_NOT;
			}

			// add all the terms first
			for (Term t : e.getTerms()) {
				if (t instanceof RangeTerm) {
					RangeTerm rt = (RangeTerm)t;
					FieldType fieldType = schema.getFieldType(t.getField());
					QParser parser = QParser.getParser(null, null, req);  // TODO: not sure what this should be
					Query rq = fieldType.getRangeQuery(parser, schema.getField(t.getField()),
						rt.getLowerTerm(), rt.getUpperTerm(), rt.isLowerInclusive(), rt.isUpperInclusive());
					BooleanClause bc = new BooleanClause(rq, occ);
					bq.add(bc);
				} else if (t instanceof PhraseTerm) {
					if ("*".equals(t.getField())){
						t.setField(getAsteriskField(schema));
					}
					PhraseTerm pt = (PhraseTerm)t;
					// TODO: what do i need to do here??
					PhraseQuery pq = new PhraseQuery();
					// tokenize the terms to create the query objs
					ArrayList<String> tokens = tokenizeTerm(schema, t);
					for (String token : tokens) {
						pq.add(new org.apache.lucene.index.Term(pt.getField(), token));
					}
					if (pt.getBoost() != -1) {
						pq.setBoost(pt.getBoost());
					}
					BooleanClause bc = new BooleanClause(pq, occ);
					bq.add(bc);							
				} else if ("*".equals(t.getField()) && "*".equals(t.getTerm())){
					MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
					//TODO:: does boosting a MatchAllDocsQuery even make sense?
					if (t.getBoost() != -1) {
						allDocsQuery.setBoost(t.getBoost());
					}
					BooleanClause bc = new BooleanClause(allDocsQuery, occ);
					bq.add(bc);
				} else {
					if ("*".equals(t.getField())){
						t.setField(getAsteriskField(schema));
					}
					ArrayList<String> tokens = tokenizeTerm(schema, t);
					// this should only be a single token..
					if (tokens.size() > 1) { 
						System.out.println("Warning term query generated more than 1 term, ignoring additional terms");						
					}
					String token = tokens.get(0);
					Query q;
					if (token.contains("?") || token.contains("*")) {
						q = new WildcardQuery(new org.apache.lucene.index.Term(t.getField(), token));
					} else {
						q = new TermQuery(new org.apache.lucene.index.Term(t.getField(), token));
					}
					if (t.getBoost() != -1) {
						q.setBoost(t.getBoost());
					}
					BooleanClause bc = new BooleanClause(q, occ);
					bq.add(bc);
				}
			}

			for (Expression subE : e.getExpressions()) {
				// TODO: I think should should be the operator of the parent expression?
				// op = subE.getOp();
				if (op.equals(Operator.AND)) {
					occ = Occur.MUST;
				} else if (op.equals(Operator.OR)) {
					occ = Occur.SHOULD;
				} else if (op.equals(Operator.NOT)) {
					occ = Occur.MUST_NOT;
				}
				// TODO: how to handle join + graph operators?
				bq.add(QueryUtils.toLuceneQuery(subE, req), occ);
			}

			return bq;
		}

	}

	private static String getAsteriskField(IndexSchema schema) {
		//TODO:: getDefaultSearchFieldName is returning null. Hardcoding to "content" field for now.
		//t.setField(schema.getDefaultSearchFieldName());
		return "content";
	}

	private static ArrayList<String> tokenizeTerm(IndexSchema schema, Term t) {
		ArrayList<String> tokens = new ArrayList<String>();
		FieldType fieldType = schema.getFieldType(t.getField());
		Analyzer fieldAnalyzer = fieldType.getQueryAnalyzer();

		// If this field is a numeric type just return the value , don't bother tokenizing
		if (fieldType.getNumericType() != null) {
			// this is a string.. just return
			ArrayList<String> numericTokens = new ArrayList<String>();
			numericTokens.add(t.getTerm());
			return numericTokens;
		}

		Reader r = new StringReader(t.getTerm());

		try {
			TokenStream ts = fieldAnalyzer.tokenStream(t.getField(), r);
			// OffsetAttribute offsetAttribute = ts.addAttribute(OffsetAttribute.class);
			CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
			ts.reset();

			while (ts.incrementToken()) {
				// Gotta get the tokens from this token stream.
				String token = charTermAttribute.toString();
				tokens.add(token);
				// TODO: do we care about other info about our tokens?
				// System.out.println(ts);
				// TODO: do we care about the start/stop offset of the tokens?
				// int start = offsetAttribute.startOffset();
				// int stop = offsetAttribute.stopOffset();
				// TODO: do we care about other attributes?
			}
			ts.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Exception tokenizing the query string!");
			return null;
		}
		return tokens;
	}
}
