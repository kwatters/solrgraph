package com.kmwllc.search.solr.client;

import org.apache.tools.ant.taskdefs.condition.Equals;

/**
 * Created by dmeehl
 * Created on 5/18/15
 */
public class EqualsTerm extends Term {

  public EqualsTerm(String field, String term) {
    this(field, term, -1);
  }

  public EqualsTerm(String field, String term, float boost) {
    super(field, term, boost);
    //EqualsTerm.SOLN = "EQUALS(";
  }

  //TODO:: Override toString with proper output
  // currently Term just outputs "" + field + SEP + term + EOLN

  @Override
  public String toString() {
    // we need to escape the term for the sep
    String escaped = getTerm().replaceAll(SEP, "\\" + SEP);

    if ( (escaped.contains(" ") || escaped.contains(",")) && !escaped.startsWith("\"") && !escaped.endsWith("\"") ) {
      escaped = escaped.replace("\"", "\\\"");
      escaped = "\"" + escaped + "\"";
    }

    String boostTerm = "";
    if (getBoost() >= 0) {
      boostTerm = "," + getBoost();
    }
    return "" + getField() + SEP + "EQUALS(" + escaped + boostTerm + ")" + EOLN;
  }

  public static void main(String[] args){
    EqualsTerm e = new EqualsTerm("aaa", "bbb", 1);
    System.out.println(e);

    Term t = new Term("aaa", "bbb", 1);
    System.out.println(t);

    RangeTerm r = new RangeTerm("aaa", "1234", "4567", 1);
    System.out.println(r);
    r = new RangeTerm("aaa", null, "4567", 1);
    System.out.println(r);
    r = new RangeTerm("aaa", "1234", null, 1);
    System.out.println(r);

    r = new RangeTerm("aaa", "1234", "4567", false, true);
    System.out.println(r);
    r = new RangeTerm("aaa", null, "4567", true, false);
    System.out.println(r);
    r = new RangeTerm("aaa", "1234", null, false, false);
    System.out.println(r);
  }
}
