package com.kmwllc.search.solr.client;

public class RangeTerm extends Term {

	private String lowerTerm;
	private String upperTerm;

  // set default inclusivity
  private boolean lowerInclusive = true;
  private boolean upperInclusive = true;

  // TODO: override the getTerm method.
	// TODO: add more types , such as date/int/float/long/double

  public RangeTerm(String field, String lowerTerm, String upperTerm, float boost) {
    super(field, null, boost);
    this.lowerTerm = lowerTerm;
    this.upperTerm = upperTerm;
  }

  public RangeTerm(String field, String lowerTerm, String upperTerm, boolean lowerInclusive, boolean upperInclusive, float boost) {
    this(field, lowerTerm, upperTerm, boost);
    this.lowerInclusive = lowerInclusive;
    this.upperInclusive = upperInclusive;
	}

  public RangeTerm(String field, String lowerTerm, String upperTerm) {
    this(field, lowerTerm, upperTerm, -1);
  }

  public RangeTerm(String field, String lowerTerm, String upperTerm, boolean lowerInclusive, boolean upperInclusive) {
    this(field, lowerTerm, upperTerm);
    this.lowerInclusive = lowerInclusive;
    this.upperInclusive = upperInclusive;
  }

	@Override
	public String toString() {
		String val = getField() + Term.SEP + "RANGE(" + (lowerInclusive?">=":">") + lowerTerm + "," + (upperInclusive?"<=":"<") + upperTerm + "," + getBoost() + ")";
		return val;
	}

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    
    RangeTerm other = (RangeTerm)obj;

    if (lowerInclusive != other.lowerInclusive) {
      return false;
    }
    if (upperInclusive != other.upperInclusive) {
      return false;
    }

    if (lowerTerm == null) {
      if (other.lowerTerm != null)
        return false;
    } else if (!lowerTerm.equals(other.lowerTerm)) {
      return false;
    }

    if (upperTerm == null) {
      if (other.upperTerm != null)
        return false;
    } else if (!upperTerm.equals(other.upperTerm)) {
      return false;
    }

    return true;
  }
}
