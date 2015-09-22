package com.kmwllc.search.solr.client;

public class JoinExpression extends GraphExpression {

	private Expression leftQuery;
	private Expression rightQuery;
	private String primaryKey;
	private String foreignKey;
	
	public JoinExpression(Expression leftQuery, Expression rightQuery, String primaryKey, String foreignKey) {
		super(leftQuery, primaryKey, foreignKey, 2, rightQuery, true, false);
		this.leftQuery = leftQuery;
		this.rightQuery = rightQuery;
		this.primaryKey = primaryKey;
		this.foreignKey = foreignKey;
	}

	@Override
	public String toString() {
		String joinStr = "JOIN(";
		joinStr += leftQuery + ", INNER(" + rightQuery + ", on=\"" + primaryKey + "=" + foreignKey +"\")";		
		joinStr += ")";
		
		return joinStr;
	}

	public Expression getLeftQuery() {
		return leftQuery;
	}

	public void setLeftQuery(Expression leftQuery) {
		this.leftQuery = leftQuery;
	}

	public Expression getRightQuery() {
		return rightQuery;
	}

	public void setRightQuery(Expression rightQuery) {
		this.rightQuery = rightQuery;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(String foreignKey) {
		this.foreignKey = foreignKey;
	}
	

}
