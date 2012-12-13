package transbit.tbits.dql.treecomponents;

import java.util.ArrayList;

public class ParseResult {

	private ArrayList<String> reqCols;
	private ArrayList<String> BAs;
	private Expression constraintRoot;
	private Expression textRoot;
	private ArrayList<Ordering> ordering;
	private Limits limits;
	
	public ParseResult() {

		reqCols = new ArrayList<String>();
		BAs = new ArrayList<String>();
		constraintRoot = new Expression();
		ordering = new ArrayList<Ordering>();
		limits = new Limits(-1, -1);
	}

	public void setReqCols(ArrayList<String> reqCols) {
		this.reqCols = reqCols;
	}

	public ArrayList<String> getReqCols() {
		return reqCols;
	}

	public void setBAs(ArrayList<String> bAs) {
		BAs = bAs;
	}

	public ArrayList<String> getBAs() {
		return BAs;
	}

	public void setConstraintRoot(Expression root) {
		this.constraintRoot = root;
	}

	public Expression getConstraintRoot() {
		return constraintRoot;
	}

	public void setOrdering(ArrayList<Ordering> ordering) {
		this.ordering = ordering;
	}

	public ArrayList<Ordering> getOrdering() {
		return ordering;
	}

	public void setLimits(int pageNum, int pageSize) {
		this.limits.pageNumber = pageNum;
		this.limits.pageSize = pageSize;
	}

	public Limits getLimits() {
		return limits;
	}

	public void setTextRoot(Expression textRoot) {
		this.textRoot = textRoot;
	}

	public Expression getTextRoot() {
		return textRoot;
	}
	
}
