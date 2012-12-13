package transbit.tbits.dql.treecomponents;

import java.util.ArrayList;

/**
 * This class encapsulates CONSTRAINT of the DQL grammar. Every CONSTRAINT is of the following format : <br>
 * 		fieldName:VALUE(s)<br>
 * The class contains an operator that describes the constraints relation to previous
 * constraint in the EXPRESSION, the field name and a list of VALUE of this constraint.
 * 
 * @author Karan Gupta
 *
 */
public class Constraint implements DqlConstants{
	
	//====================================================================================

	private Operator op;
	private String field;
	private ArrayList<Value> values;
	
	//====================================================================================

	/**
	 * Constructor
	 * @param field
	 */
	public Constraint(String fieldName) {
		this.field = fieldName;
		values = new ArrayList<Value>();
	}

	//====================================================================================

	// Getter and setter functions
	
	public void setOperator(Operator op) {
		this.op = op;
	}
	
	public void setValues(ArrayList<Value> values){
		this.values = values;
	}
	
	public void addValue(Value v){
		values.add(v);
	}

	public String getField() {
		return field;
	}
	
	public ArrayList<Value> getValues(){
		return values;
	}

	public Operator getOperator() {
		return op;
	}

	//====================================================================================

	/**
	 * Negate this constraint. This is used when a NOT is encountered before the constraint.
	 * 
	 * @param negate
	 */
	public void setNegation(boolean negate) {
		if(negate){
			// Invert the operator
			if(op != null){
				if(op.equals(Operator.OR))
					op = Operator.AND;
				else if(op.equals(Operator.AND))
					op = Operator.OR;
			}
			for(Value val : values){
				val.setNegation(true);
			}
		}
	}
	
	//====================================================================================

}
