package com.kmwllc.search.solr.client;

import java.util.ArrayList;

/**
 * An Advanced query language parser... This should be re-written.
 * @author kwatters
 *
 */
public class AdvQueryParser {

	public static ArrayList<String> smartSplit(String argString) {
		return smartSplit(argString, ',');
	}
	
	public static ArrayList<String> smartSplit(String argString, char sep) {
		ArrayList<String> parts = new ArrayList<String>();
		// non escaped commas 
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		int level = 0;
		for (int i=0; i < argString.length(); i++) {
			char current = argString.charAt(i);
			if (current == '\\') {
				escaped = true;
				continue;
			} else if (current == '(') {
				level++;
			} else if (current == ')') {
				--level;
			} else {
				if (escaped) {
					sb.append(current);
					escaped = false;
					continue;
				}
			}
			if (!escaped && level == 0) {
				if (current == sep) {
					// this is the break
					parts.add(sb.toString().trim());
					sb = new StringBuilder();
					continue;
				}
			}
			sb.append(current);
		}
		parts.add(sb.toString().trim());
		return parts;
	}
	
	public static Expression parse(String queryString) {
		Expression e = null;
		
		int len = queryString.length();
		
		int optOffset = queryString.indexOf("(");
		String op = queryString.substring(0, optOffset);
		if (op.equals("AND")) {
			e = new AndExpression();
		} else if (op.equals("OR")) {
			e = new OrExpression();
		} else if (op.equals("NOT")) {
			e = new NotExpression();
// TODO:
//		} else if (op.equals("JOIN")) {
//			e = new JoinExpression();
//		} else if (op.equals("GRAPH")) {
//			e = new GraphExpression();
		} else {
			System.out.println("Unknown Op :" + op);
			return null;
		}
		
		String args = queryString.substring(optOffset+1, len-1);
		ArrayList<String> parts = AdvQueryParser.smartSplit(args);
		
		System.out.println(op);
		System.out.println(args);
		for (String p : parts) {
			System.out.println(">" + p + "<");
			if (isOperator(p)) {
				Expression subE = parse(p);
				e.add(subE);
			} else if (isTerm(p)) {
				Term t = parseTerm(p);
				e.add(t);
			} else {
				// this is an option.
				ArrayList<String> optParts = AdvQueryParser.smartSplit(p, '=');
				String option = optParts.get(0);
				String val = optParts.get(1);
				if (e instanceof OrExpression) {
					// the option can be one of the following
					// minShouldMatch
					if (option.equalsIgnoreCase("minShouldMatch")) {
						int minShouldMatch = Integer.valueOf(val);
						((OrExpression)e).setMinShouldMatch(minShouldMatch);
					}
				} else if (e instanceof AndExpression) {
					// no params for an and expression
					System.out.println("Unknown param for an And expression.");
				} else if (e instanceof NotExpression) {
					System.out.println("Unknown param for a NOT expression.");					
				} else if (e instanceof JoinExpression) {
					if (option.equals("on")) {	
						ArrayList<String> keys = AdvQueryParser.smartSplit(val, '=');
						if (keys.size() == 1) {
							((JoinExpression)e).setPrimaryKey(keys.get(0));
							((JoinExpression)e).setPrimaryKey(keys.get(0));
						} else {
							((JoinExpression)e).setPrimaryKey(keys.get(0));
							((JoinExpression)e).setPrimaryKey(keys.get(1));							
						}
					} else {
						System.out.println("Unknown param for a JOIN expression.");											
					}
				} else if (e instanceof GraphExpression) {
					System.out.println("Unknown param for a GRAPH expression.");					
				}

			}
		}
		return e;
	}
	
	private static Term parseTerm(String p) {
		// TODO Auto-generated method stub
		
		int start = p.indexOf("(");
		String payload = p.substring(start+1, p.length()-1);
		
		ArrayList<String> parts = AdvQueryParser.smartSplit(payload, ':');
		if (parts.size() == 3) {
			Term t = new Term(parts.get(0), parts.get(1));
			// third part is a boost
			int boost = Integer.valueOf(parts.get(3).split("=")[1]);
			t.setBoost(boost);
			return t;
		} else if (parts.size() == 2) {
			Term t = new Term(parts.get(0), parts.get(1));
			return t;
		} else if (parts.size() == 1) {
			Term t = new Term(null, parts.get(0));			
			return t;
		}
		return null;
	}

	private static boolean isTerm(String p) {
		// TODO Auto-generated method stub
		if (p.startsWith("TERM(")) {
			return true;
		}
		
		return false;
	}

	private static boolean isOperator(String p) {
		// TODO Auto-generated method stub
		if (p.startsWith("AND(")) {
			return true;
		}
		if (p.startsWith("OR(")) {
			return true;
		}
		if (p.startsWith("NOT(")) {
			return true;
		}
		if (p.startsWith("GRAPH(")) {
			return true;
		}
		if (p.startsWith("JOIN(")) {
			return true;
		}
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// String testStr = "AND(TERM(\"title\",\"a\"),TERM(\"b\"),TERM(\"c\"))";
		// String testStr = "AND(OR(title:foo, text:bar)\\, date:2012)";
		String testStr = "AND(TERM(title:foo\\,bar), OR(TERM(a:b), TERM(c:d)), TERM(text:bar), TERM(date:2012))";

//		String tstArgStr = "title:foo\\,bar, text:bar, date:2012";
//		for (String p : AdvQueryParser.smartSplit(tstArgStr)) {
//			System.out.println(">" + p + "<");
//		}
		Expression e = AdvQueryParser.parse(testStr);
		System.out.println("EXPRESISON : " + e.toString());
	}

}
