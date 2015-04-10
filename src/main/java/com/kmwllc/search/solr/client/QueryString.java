package com.kmwllc.search.solr.client;

public class QueryString extends Term {
	private String query;
	private String parser = "lucene";
	
	public QueryString(String query) {
		super(null, null, -1);
		this.query = query;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parser == null) ? 0 : parser.hashCode());
		result = prime * result + ((query == null) ? 0 : query.hashCode());
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
		QueryString other = (QueryString) obj;
		if (parser == null) {
			if (other.parser != null)
				return false;
		} else if (!parser.equals(other.parser))
			return false;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		return true;
	}
	
}
