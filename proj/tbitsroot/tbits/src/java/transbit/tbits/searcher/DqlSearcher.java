package transbit.tbits.searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.User;
import transbit.tbits.dql.antlr.DQLLexer;
import transbit.tbits.dql.antlr.DQLParser;
import transbit.tbits.dql.treecomponents.DqlConstants;
import transbit.tbits.dql.treecomponents.Ordering;
import transbit.tbits.dql.treecomponents.ParseResult;

/**
 * This class searches based on a DQL provided. The search parameters are formed by parsing the DQL.
 * The results are filtered as per the permissions of the user requesting the search.
 * 
 * @author Karan Gupta
 *
 */
public class DqlSearcher extends Searcher{

	//====================================================================================

	public static final TBitsLogger LOG = TBitsLogger.getLogger("transbit.tbits.searcher");
	
	private String dql;
	private List<SearchResult> result;
	private boolean searchLucene;
	
	//====================================================================================

	/**
	 * Constructor
	 * 
	 * @param sysId
	 * @param userId
	 * @param dql
	 */
	public DqlSearcher(int sysId, int userId, String dql){
		super(sysId);
		this.sysId = sysId;
		this.dql = dql;
		this.searchLucene = false;
		setUserId(userId);
	}
	
	/**
	 * This method can be used for finding the results by internal programs like plugins or Jobs.
	 * It does not takes the user id so it also does not checks for the permissions.
	 * Also note that it does not search in lucene so text search or search inside files will not happen.
	 * @param sysId
	 * @param dql
	 */
	public DqlSearcher(int sysId,String dql){
		super(sysId);
		this.sysId = sysId;
		this.dql = dql;
		this.searchLucene = false;
//		setUserId(userId);
		checkPermissions = false ;
	}
	
	//====================================================================================

	/**
	 * @return the filtered result for the given user
	 */
	public Map<Integer, Map<Integer, SearchResult>> getResult(){
		Map<Integer, Map<Integer, SearchResult>> mapping = new HashMap<Integer, Map<Integer,SearchResult>>();
		for(SearchResult sr : result){
			if(!mapping.containsKey(sr.getSysId()))
				mapping.put(sr.getSysId(), new HashMap<Integer, SearchResult>());
			mapping.get(sr.getSysId()).put(sr.getRequestId(), sr);
		}
		return mapping;
	}
	
	public List<SearchResult> getOrderedResult(){
		return result;
	}
	
	//====================================================================================

	/**
	 * Initiate the search. The result can be fetched using getResult()
	 */
	public void search() throws Exception {
		
		LOG.info("Searching for DQL -> "+dql);
		System.out.println("Searching for DQL -> "+dql);
		
		parseDqlQueryAndSetParameters();
		
		if(searchLucene){
			ArrayList<String> luceneResults = getLuceneSearchResults(sysId);
			for(String rid : luceneResults){
				if(externalJoin == null)
					externalJoin = new HashMap<Integer, ArrayList<Integer>>();
				if(!externalJoin.containsKey(sysId))
					externalJoin.put(sysId, new ArrayList<Integer>());
				try{
					externalJoin.get(sysId).add(Integer.parseInt(rid));
				}
				catch (NumberFormatException e){
					continue;
				}
			}
		}
		
		result = makeAndExecuteQuery();
	}

	//====================================================================================

	/**
	 * Parses the dql and sets the parameters for the searcher.
	 * 
	 * @throws Exception
	 */
	private void parseDqlQueryAndSetParameters() throws Exception {
		
		CharStream stream = new ANTLRStringStream(dql);
		DQLLexer lexer = new DQLLexer(stream);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		DQLParser parser = new DQLParser(tokenStream);
		ParseResult pr = parser.query().result;
		// Report any errors that the parser might have encountered
		parser.errReporter.reportErrors();
		
		// Get the searcher type specified and the requested columns for SIMPLE type
		if(pr.getReqCols().size()>0){
			if(pr.getReqCols().get(0).equals(DqlConstants.REQUEST_COL)){
				type = REQUEST_OBJECT;
			}
			else{
				type = SIMPLE;
				requestedColumns.addAll(pr.getReqCols());
			}
		}
		else{
			type = SIMPLE;
		}
		
		expTree = ExpressionTreeNode.makeTreeFrom(pr.getConstraintRoot());
		
		luceneExpTree = ExpressionTreeNode.makeTreeFrom(pr.getTextRoot());
		if(luceneExpTree != null)
			searchLucene = true;
		
		// Get the list of BAs on which the search is requested
		// TODO we currently do not support multiple business areas
//		if(pr.getBAs().size()>0){
//			if(pr.getBAs().get(0).equals("*")){
//				isSearchAcrossBA = true;
//			}
//			else{
//				Constraint c = new Constraint(Field.BUSINESS_AREA);
//				for(String ba : pr.getBAs()){
//					Value v = new Value();
//					Parameter p = new Parameter();
//					p.comp = DqlConstants.Comparator.E;
//					p.type = DqlConstants.ParamType.STRING;
//					p.param = ba;
//					v.setOperator((c.getValues().size()==0)?null:DqlConstants.Operator.OR);
//					v.setParam(p);
//					c.addValue(v);
//				}
//				expTree.addConstraint((expTree.getConstraintListSize()==0)?null:DqlConstants.Operator.AND, c);
//			}
//		}
		
		// Get the ordering constraints
		ArrayList<Ordering> orders = pr.getOrdering();
		if(orders.size()>0){
			for(Ordering order : orders){
				if(!fields.containsKey(order.orderCol))
					throw new Exception("Invalid column name for ordering! No such field exists : " + order.orderCol);
				if(!requestedColumns.contains(order.orderCol))
					throw new Exception("Invalid column name for ordering! Only valid field names that have been requested in SELECT can be ordered upon.");
				if(order.order == null)
					order.order = DqlConstants.Order.ASC;
				ordering.put(order.orderCol, ((order.order.equals(DqlConstants.Order.ASC)?"ASC":"DESC")));
			}
		}
		
		if(pr.getLimits().pageNumber > 0 && pr.getLimits().pageSize > 0)
			enablePaging(pr.getLimits().pageNumber, pr.getLimits().pageSize);
	}
	
	//====================================================================================

	/**
	 * This method forms the sql queries and executes them.
	 * The result is returned as a HashMap
	 *
	 * @return result
	 * @throws Exception
	 */
	protected List<SearchResult> makeAndExecuteQuery() throws Exception{
		
		ArrayList<String> sqls = formSqlQueries();
		
		System.out.println("*************************************** SQL ********************************************");
		System.out.println(sqls);
		System.out.println("*************************************** END ********************************************");
		
		return executeQuery(sqls);
	}
	
	//====================================================================================

}
