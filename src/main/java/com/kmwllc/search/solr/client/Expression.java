package com.kmwllc.search.solr.client;

import java.util.ArrayList;

public class Expression {

	private ArrayList<Expression> expressions;
	
	// TODO: an expression should have at minimum 1 term or another expression?
	private ArrayList<Term> terms;
	
	// TODO: get rid of the op
	// create subclasses for and / or / not / range / join / graph expressions
	// potentially we want to treat polygon and distance as operators?
	// Other spatial stuff?
	private Operator op;
	
	public Expression(Operator op) {
		super();
		this.op = op;
		expressions = new ArrayList<Expression>();
		terms = new ArrayList<Term>();
	}
	
	public void add(Expression e) {
		expressions.add(e);
	}
	
	public void add(Term t) {
		terms.add(t);
	}
	
	public ArrayList<Expression> getExpressions() {
		return expressions;
	}
	public void setExpressions(ArrayList<Expression> expressions) {
		this.expressions = expressions;
	}
	public ArrayList<Term> getTerms() {
		return terms;
	}
	public void setTerms(ArrayList<Term> terms) {
		this.terms = terms;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(op.toString());
		sb.append("(");
		
		int i = terms.size();
		for (Term t : terms) {
			i--;
			sb.append(t.toString());
			if (i >0) {
				sb.append(",");
			}
		}
		int j = expressions.size();
		if (terms.size() > 0 && expressions.size() > 0) {
			sb.append(",");
		}
		
		for (Expression e : expressions) {
			j--;
			sb.append(e.toString());
			if (j>0) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expressions == null) ? 0 : expressions.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((terms == null) ? 0 : terms.hashCode());
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
		Expression other = (Expression) obj;
		if (expressions == null) {
			if (other.expressions != null)
				return false;
		} else if (!expressions.equals(other.expressions))
			return false;
		if (op != other.op)
			return false;
		if (terms == null) {
			if (other.terms != null)
				return false;
		} else if (!terms.equals(other.terms))
			return false;
		return true;
	}

}
