package transbit.tbits.dql.treecomponents;

import java.util.ArrayList;

/**
 * This class deifines EXPRESSION of the DQL grammar. An EXPRESSION is the basic
 * building block of a DQL query.<br>
 * An EXPRESSION contains a list of CONSTRAINTS and a list of children EXPRESSIONs.<br>
 * 
 * @author Karan Gupta
 *
 */
public class Expression implements DqlConstants{

	//====================================================================================

	private Operator op;
	private boolean negate;
	private ArrayList<Constraint> constraints;
	private ArrayList<Expression> children;
	
	//====================================================================================

	// Getter and setter functions
	
	public Object getOperator() {
		return op;
	}
	
	public void setOperator(Operator op){
		this.op = op;
	}

	public void addChild(Operator op, Expression parsedExpression) {
		if(children == null)
			children = new ArrayList<Expression>();
		parsedExpression.setOperator(op);
		children.add(parsedExpression);
	}
	
	public int getChildListSize() {
		if(children == null)
			return 0;
		return children.size();
	}
	
	public ArrayList<Expression> getChildren(){
		if(hasChildren())
			return children;
		else
			return new ArrayList<Expression>();
	}
	
	public boolean hasChildren(){
		if(children == null || children.size() <= 0)
			return false;
		return true;
	}
	
	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}
	
	public void setConstraints(ArrayList<Constraint> constraints){
		this.constraints = constraints;
	}

	public void addConstraint(Operator op, Constraint c){
		if(constraints == null)
			constraints = new ArrayList<Constraint>();
		c.setOperator(op);
		constraints.add(c);
	}
	
	public int getConstraintListSize(){
		if(constraints == null)
			return 0;
		return constraints.size();
	}

	public void setNegation(boolean negate) {
		this.negate = negate;
//		if(negate){
//			if(constraints != null)
//				for(Constraint c : constraints){
//					c.setNegation(true);
//				}
//			if(children != null)
//				for(Expression e : children){
//				e.setNegation(true);
//			}
//		}
	}
	
	public boolean isNegate(){
		return negate;
	}
	
	//====================================================================================

}
