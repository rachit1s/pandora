package transbit.tbits.searcher;

import transbit.tbits.dql.treecomponents.Constraint;
import transbit.tbits.dql.treecomponents.DqlConstants;
import transbit.tbits.dql.treecomponents.DqlConstants.Operator;


/**
 * This class encapsulates the sql conditions that need to be appended to the final query of the searcher.
 * The AND'ed and OR'ed queries are stored seperately.
 * 
 * @author Karan Gupta
 *
 */
public class SqlConditionEncapsulator {
	
	//====================================================================================

	public int andConstraints;
	private String andSql;
	public int orConstraints;
	private String orSql;
	private String defaultQuery;

	//====================================================================================

	/**
	 * Constructor
	 * 
	 * @param defaultQuery
	 */
	public SqlConditionEncapsulator(String defaultQuery) {
		this.defaultQuery = defaultQuery;
		andConstraints = 0;
		orConstraints = 0;
		andSql = "";
		orSql = "";
	}
	
	public String getAndSql(){
		if(andConstraints == 0)
			return defaultQuery;
		if(defaultQuery.indexOf(" where ", 0) < 0)
			return defaultQuery + " where (" + this.andSql + ")";
		else
			return defaultQuery + " and (" + this.andSql + ")";
	}
	
	public String getOrSql(){
		if(orConstraints == 0)
			return defaultQuery;
		if(defaultQuery.indexOf(" where ", 0) < 0)
			return defaultQuery + " where (" + this.orSql + ")";
		else
			return defaultQuery + " and (" + this.orSql + ")";
	}
	
	//====================================================================================

	/**
	 * Increment the constraint type ( AND / OR )
	 * @param c
	 */
	public void addConstraint(Constraint c) {
		
		if(c.getOperator() == null || c.getOperator().equals(DqlConstants.Operator.AND))
			andConstraints++;
		else if(c.getOperator().equals(DqlConstants.Operator.OR))
			orConstraints++;
	}

	//====================================================================================

	/**
	 * Append the conditions to the relevant type of query ( AND / OR )
	 * @param conditions
	 * @param operator
	 */
	public void appendCondition(String conditions, Operator operator) {
		
		if(operator == null || operator.equals(DqlConstants.Operator.AND)){
			if(!andSql.equals(""))
				andSql += " and (" + conditions + ")";
			else
				andSql = "(" + conditions + ")";
		}
		else if(operator.equals(DqlConstants.Operator.OR)){
			if(!orSql.equals(""))
				orSql += " or (" + conditions + ")";
			else
				orSql = "(" + conditions + ")";
		}
	}
	
	//====================================================================================

}
