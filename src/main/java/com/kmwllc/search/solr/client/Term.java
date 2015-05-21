package com.kmwllc.search.solr.client;

public class Term {

	protected static final String SEP = ":";
	protected static final String EOLN = "";
	
	private String field;
	private String term;
	private float boost;
	
	public Term(String field, String term) {
		super();
		this.field = field;
		this.term = term;
		this.boost = -1;
	}
	
	public Term(String field, String term, float boost) {
		super();
		this.field = field;
		this.term = term;
		this.boost = boost;
	}
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}

	@Override
	public String toString() {
		// return "TERM(" + field + SEP + term + ")\n";
		// TODO: add the boost value if non default.

		// we need to escape the term for the sep
		String escaped = term.replaceAll(SEP, "\\" + SEP);

    if ( (escaped.contains(" ") || escaped.contains(",")) && !escaped.startsWith("\"") && !escaped.endsWith("\"") ) {
      escaped = escaped.replace("\"", "\\\"");
      escaped = "\"" + escaped + "\"";
    }

		return "" + field + SEP + escaped + EOLN;
	}

	public float getBoost() {
		return boost;
	}

	public void setBoost(float boost) {
		this.boost = boost;
	}

}
