package com.kmwllc.search.solr.client;

public class PhraseTerm extends Term {

	// TODO: consider removing this class and explicity handle
	// this if the term tokenizes to more than one token.
	public PhraseTerm(String field, String term) {
		super(field, term);
		// TODO Auto-generated constructor stub
	}
	
	public PhraseTerm(String field, String term, float boost) {
		super(field, term, boost);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		// return "TERM(" + field + SEP + term + ")\n";
		// TODO: add the boost value if non default.
		String escapedTerm = getTerm().replaceAll("\"", "\\\"");
		
		return "" + getField() + Term.SEP + "\"" + escapedTerm +  "\"" + Term.EOLN;
	}

}
