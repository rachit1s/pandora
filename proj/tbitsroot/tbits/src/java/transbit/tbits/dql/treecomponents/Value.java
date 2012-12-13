package transbit.tbits.dql.treecomponents;

import java.util.ArrayList;
import java.util.List;


/**
 * This class encapsulates VALUE of the DQL grammar.
 * A VALUE is the value specified for a given field in the query.
 * It consists of an operator describing its relation with previous value in the constraint
 * and a PARAMETER of the value.
 * 
 * @author Karan Gupta
 *
 */
public class Value implements DqlConstants {

	//====================================================================================

	private Operator op;
	private List<Parameter> params = new ArrayList<Parameter>();;
	
	//====================================================================================

	// Getter and setter functions
	
	public void setOperator(Operator op){
		this.op = op;
	}
	
	public Operator getOperator(){
		return op;
	}
	
	public void setParams(List<Parameter> params){
		this.params = params;
	}
	
	public void addParam(Parameter p){
		this.params.add(p);
	}
	
	public List<Parameter> getParams(){
		return params;
	}
	
	//====================================================================================
	
	public void setNegation(boolean negate){
		if(negate){
			if(op != null){
				if(op.equals(Operator.OR))
					op = Operator.AND;
				else if(op.equals(Operator.AND))
					op = Operator.OR;
			}
			for(Parameter parameter : params){
				parameter.setNegation(true);
			}
		}
	}

}
