package com.kmwllc.search.solr.client;

public class RangeTerm extends Term {

	private String lowerTerm;
	private String upperTerm;
	// TODO: add more types , such as date/int/float/long/double
	public RangeTerm(String field, String lowerTerm, String upperTerm) {
		// super(field, term);
		// TODO Auto-generated constructor stub
		// TODO: propigate a boost here?
		// TODO: override the getTerm method.
		super(field, null, -1);
		this.lowerTerm = lowerTerm;
		this.upperTerm = upperTerm;
	}
	
	public RangeTerm(String field, String lowerTerm, String upperTerm, float boost) {
		// super(field, term);
		// TODO Auto-generated constructor stub
		// TODO: propigate a boost here?
		super(field, null, boost);
		this.lowerTerm = lowerTerm;
		this.upperTerm = upperTerm;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String val = getField() + Term.SEP + "RANGE(" + lowerTerm + " TO " + upperTerm + ")";
		return val;
	}

}
