package transbit.tbits.searcher;

import java.util.ArrayList;

import transbit.tbits.domain.Field;
import transbit.tbits.dql.treecomponents.Constraint;
import transbit.tbits.dql.treecomponents.Expression;
import transbit.tbits.dql.treecomponents.DqlConstants.Operator;

public class ExpressionTreeNode {

	//====================================================================================

	private Expression exp;
	private ArrayList<ExpressionTreeNode> children;
	
	private SqlQueryGenerator generator;
	private String defaultQuery;
	private String sql;
	private boolean isQueryFormed = false;
	
	//====================================================================================

	/**
	 * Constructor
	 * 
	 * @param exRoot
	 */
	public ExpressionTreeNode(Expression exRoot) {

		exp = exRoot;
	}

	//====================================================================================

	// Getter and setter methods
	
	public Expression getExpression(){
		return exp;
	}
	
	public void setChildren(ArrayList<ExpressionTreeNode> children) {
		this.children = children;
	}

	public boolean hasChildren(){
		if(children == null || children.size() <= 0)
			return false;
		return true;
	}
	
	public ArrayList<ExpressionTreeNode> getChildren() {
		if(hasChildren())
			return children;
		else
			return new ArrayList<ExpressionTreeNode>();
	}
	
	public void addChild(ExpressionTreeNode child) {
		if(children == null)
			children = new ArrayList<ExpressionTreeNode>();
		children.add(child);
	}
	
	public int getChildListSize() {
		if(children == null)
			return 0;
		return children.size();
	}

	//====================================================================================

	/**
	 * Initialise the query generator
	 * @param sysId
	 * @param isQueryOnActions
	 * @param requestedColumns 
	 */
	public void initialiseQueryGenerator(int sysId, int userId, boolean isQueryOnActions){
		generator = new SqlQueryGenerator(sysId, userId, (isQueryOnActions)?SqlQueryGenerator.QUERY_ACTIONS:SqlQueryGenerator.QUERY_REQUESTS);
		isQueryFormed = false;
		sql = "select distinct basic.sys_id, basic.request_id" + ((isQueryOnActions)?", basic.action_id ":" ");
		defaultQuery = generator.getDefaultQueryForBasic(sysId);
	}

	//====================================================================================

	/**
	 * Append the condition for the given constraint into the query generator.
	 * 
	 * @param f
	 * @param c
	 * @throws Exception
	 */
	public void appendCondition(Field f, Constraint c) throws Exception {
		
		if(generator == null)
			throw new Exception("Query generator not initialised. Call initialiseQueryGenerator() first.");
		generator.appendCondition(f, c);
	}

	//====================================================================================

	/**
	 * Form and return the SQL query represented by this Expression.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getQuery() throws Exception {

		if(isQueryFormed)
			return sql;
		
		if(generator == null)
			throw new Exception("Query generator not initialised. Call initialiseQueryGenerator() first.");
		
		sql += generator.getQuery();
		
		if(exp.isNegate() && (exp.getOperator() != null && exp.getOperator().equals(Operator.OR))){
			sql = 	"(" + defaultQuery + ")\n" +
					"except \n" + 
					"(" + sql + ")";
		}
		
		isQueryFormed = true;
		
		return sql;
	}

	//====================================================================================

	/**
	 * Static function to make an expression tree node out of an expression
	 * 
	 * @param exRoot
	 * @return the root of the expression tree formed from the given expression
	 */
	public static ExpressionTreeNode makeTreeFrom(Expression exRoot) {

		if(exRoot == null)
			return null;
		
		ExpressionTreeNode root = new ExpressionTreeNode(exRoot);
		
		if(exRoot.hasChildren()){
			for(Expression exp : exRoot.getChildren()){
				root.addChild(makeTreeFrom(exp));
			}
		}
		
		return root;
	}
	
	//====================================================================================

}
