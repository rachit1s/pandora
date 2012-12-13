package transbit.tbits.searcher;

import static transbit.tbits.Helper.TBitsConstants.PKG_SEARCH;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.UserType;
import transbit.tbits.dql.treecomponents.Constraint;
import transbit.tbits.dql.treecomponents.DqlConstants;
import transbit.tbits.dql.treecomponents.Parameter;
import transbit.tbits.dql.treecomponents.Value;
import transbit.tbits.dql.treecomponents.DqlConstants.ParamType;
import transbit.tbits.indexer.LuceneSearcher;

/**
 * The searcher is the class that searches the tables based on the given parameters The searcher class
 * needs to be extended as per the requirements of the searcher.<br>
 * The searcher supports two types :<br>
 * 	* <b>SIMPLE : </b>The searcher performs a basic search and the result is returned as a HashMap of
 * 				sys_id vs HashMap of relevant request_ids vs the requested columns<br>
 * 	* <b>REQUEST_OBJECT : </b>The searcher builds request objects ot of the relevant requests and
 * 						returns the result as a HashMap of sys_id vs HashMap of 
 * 						relevant request_ids vs their Request objects<br>
 * <br>
 * Further options for the searcher are :  <br>
 * 	* <b>isSearchAcrossBA : </b>The searcher searches across BAs and the result is not BA specific. Is false by default. Currently we do not support this feature<br>
 * 	* <b>isPagingEnabled : </b>The searcher searches for a specific page with a size and number. Is false by default.<br>
 * 	* <b>isQueryOnActions : </b>The searcher searches on the actions tables instead of the requests tables. The action_id is also returned. Is false by default.<br>
 * <br>
 * The REQUEST_OBJECT type is a CPU intensive process for the formation of the Request objects.
 * It is recommended that the REQUEST_OBJECT type be used only for paging enabled searches of small page sizes.
 * Multiple paged searches can be performed in case the intended number of requests is not fetched (as in case of filtering).
 * 
 * @author Karan Gupta
 *
 */
public abstract class Searcher {
	public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_SEARCH);
	// Hacks for tags search to be allowed in fields
	// Used in Searcher.getQuery() and SqlQueryGenerator.appendCondition()
	public static final String PRIVATE_TAGS_FIELD_FILTER = "__private_tags";
	public static final String PUBLIC_TAGS_FIELD_FILTER = "__public_tags";
	public static final String READ_UNREAD_FIELD_FILTER = "__is_read";
	
	//====================================================================================

	// Abstract methods
	
	protected abstract void search() throws Exception;
	
	protected abstract Object getResult();

	//====================================================================================

	// Search types
	public static final int SIMPLE = 0;
	public static final int REQUEST_OBJECT = 1;
	
	// Searcher specific parameters
	protected int type;
	protected int sysId;
	protected int userId;
	protected Hashtable<String, Field> fields;
	
	// Query related variables
	protected ExpressionTreeNode expTree;
	protected ExpressionTreeNode luceneExpTree;
	protected ArrayList<String> requestedColumns;
	protected HashMap<String, String> ordering;
	protected boolean checkPermissions;
	
	// This map defines the external join. The requests in this map are joined with the queries result
	// See the method formSqlQueries() for the implementation
	protected HashMap<Integer, ArrayList<Integer>> externalJoin;
	
	// The sqls for the search should remain private to restrict access to wrapper searchers.
	// The wrapper searchers can access the makeAndExecuteQuery() method and access the result from it.
	private String basicSql;
	
	// Searcher options
	protected boolean isSearchAcrossBA;
	protected boolean isQueryOnActions;
	protected boolean isPagingEnabled;
	
	// Paging related variables
	protected int pageSize;
	protected int pageNumber;

	//====================================================================================

	/**
	 * Constructor. Sets the default values of the fields.
	 * 
	 * @param sysId
	 */
	protected Searcher(int sysId) {
		this.sysId = sysId;
		this.userId = -1;
		try {
			fields = Field.getFieldsTableBySystemId(sysId);
		} 
		catch (DatabaseException e) {
			fields = new Hashtable<String, Field>();
			e.printStackTrace();
		}
		isSearchAcrossBA = false;
		isPagingEnabled = false;
		isQueryOnActions = false;
		requestedColumns = new ArrayList<String>();
		ordering = new HashMap<String, String>();
		checkPermissions = false;
	}
	
	//====================================================================================

	/**
	 * @return root of the expressions tree
	 */
	protected ExpressionTreeNode getExpTreeRoot(){
		return expTree;
	}

	//====================================================================================

	/**
	 * Enable page based search. The searcher only returns the valid result for 
	 * the page number and page size specified.<br>
	 * Paging is disabled by default.
	 * 
	 * @param pageNumber
	 * @param pageSize
	 */
	protected void enablePaging(int pageNumber, int pageSize){
		this.isPagingEnabled = true;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}

	/**
	 * Enable search across Business Areas. The result is not BA specific.
	 */
	// TODO we do not support search over multiple business areas currently
//	public void enableSearchAcrossBA(){
//		this.isSearchAcrossBA = true;
//	}
	
	/**
	 * Enable query on actions tables instead of requests tables.
	 */
	protected void enableQueryOnActions(){
		this.isQueryOnActions = true;
	}
	
	//====================================================================================

	/**
	 * Add specifications for the columns to be requested. The columns can be speciied or a simple searcher only.
	 * 
	 * @param rc
	 * @throws Exception 
	 */
	protected void setRequestedColumns(ArrayList<String> rc) throws Exception{
		if(type == SIMPLE){
			for(String requestedColumn : rc){
				if(!requestedColumns.contains(requestedColumn))
					requestedColumns.add(requestedColumn);
			}
		}
		else throw new Exception("Column selection can only be set for SIMPLE search.");
	}
	
	protected void setOrderBy(HashMap<String, String> ordering){
		this.ordering = ordering;
	}
	
	/**
	 * Sets the user id of the searcher for user related searching
	 * @param userId
	 */
	protected void setUserId(int userId){
		this.userId = userId;
		checkPermissions = true;
	}

	//====================================================================================

	/**
	 * Runs the lucene search on the luceneExpTree. The luceneExpTree must be set before this function is called.
	 * The lucene query is generated and the search results are returned.
	 * 
	 * @return List of request_ids of the searched requests
	 * @throws Exception 
	 */
	protected ArrayList<String> getLuceneSearchResults(int sysId) throws Exception {

		// Form the lucene query from the expressions
		String luceneQuery = getLuceneQueryFor(luceneExpTree);
		if(!isSearchAcrossBA)
			luceneQuery = "+sys_id:" + sysId + " +(" + luceneQuery + ")";
		
		// Search and return the results
		ArrayList<String> luceneResult = new ArrayList<String>();
		try {
			luceneResult = LuceneSearcher.search(BusinessArea.lookupBySystemId(sysId).getSystemPrefix(), luceneQuery, isQueryOnActions);
		} catch (DatabaseException e) {
			System.out.println("Unable to fetch sys_prefix for lucene search!");
			e.printStackTrace();
		}
		
		return luceneResult;
	}

	//====================================================================================

	/**
	 * Form the lucene query for the given expression
	 * 
	 * @param node
	 * @return
	 * @throws Exception 
	 */
	protected String getLuceneQueryFor(ExpressionTreeNode node) throws Exception {

		String expForLucene = "";
		
		ArrayList<Constraint> constraints = node.getExpression().getConstraints();
		if(constraints != null){
			for(Constraint c : constraints){
				
				if(!c.getField().equals(DqlConstants.ALL_LUCENE) && !c.getField().equals(DqlConstants.ALL_TEXT_LUCENE)){
					if(fields.get(c.getField()) == null){
						throw new Exception("Invalid field name for text search : " + c.getField());
					}
					else if(fields.get(c.getField()).getDataTypeId() != DataType.ATTACHMENTS 
							&& fields.get(c.getField()).getDataTypeId() != DataType.TEXT
							&& fields.get(c.getField()).getDataTypeId() != DataType.STRING 
							){
						throw new Exception("Only attachment type, text and string parameters supported in HAS TEXT.\n" +
											"Non-text-search field constraints should be put in the WHERE clause : " + c.getField());
					}
				}
				
				if(c.getOperator() != null)
					expForLucene += (c.getOperator().equals(DqlConstants.Operator.AND))?" AND":" OR";
				String values = "";
				for(Value v : c.getValues()){
					if(v.getOperator() != null && !values.equals(""))
						values += (v.getOperator().equals(DqlConstants.Operator.AND))?" AND":" OR";
					String parameters = "";
					for(Parameter parameter : v.getParams()){
						if(!parameters.equals(""))
							parameters += " OR";
						if(parameter.type.equals(ParamType.STRING))
							values += " \"" + parameter.param + "\"";
						else
							values += " " + parameter.param;
					}
					values += parameters;
				}
				expForLucene += " +" + c.getField() + ":(" + values + ")";
			}
		}
		
		for(ExpressionTreeNode child : node.getChildren()){
			if(!expForLucene.equals("")){
				expForLucene += (child.getExpression().getOperator() == null || child.getExpression().getOperator().equals(DqlConstants.Operator.AND))?" AND":" OR";
				expForLucene += (child.getExpression().isNegate())?" NOT":"";
				expForLucene += "(" + getLuceneQueryFor(child) + ")";
			}
			else{
				expForLucene = getLuceneQueryFor(child);
			}
		}
		
		return expForLucene;
	}
	
	//====================================================================================

	/**
	 * Form the sql queries for each table and the final sql query by joining the independent queries.
	 * The queries are formed for the given tree of expressions.
	 * 
	 * @return final sql query for the given expression tree
	 * @throws Exception
	 */
	protected ArrayList<String> formSqlQueries() throws Exception {
		
		ArrayList<String> sqls = new ArrayList<String>();
		
		String externalTableSql = null;
		if(externalJoin != null){
			if(externalTableSql == null)
				externalTableSql = "create table #externalJoin (sys_id int, request_id int)\n";
			for(int sid : externalJoin.keySet()){
				for(int rid : externalJoin.get(sid)){
					externalTableSql += "insert into #externalJoin values (" + sid + ", "+ rid +")\n";
				}
			}
		}
		
		// Form the final query from the indivisual sqls generated.
		if(basicSql == null){
			
			basicSql = 	"select distinct final.sys_id, final.request_id" +((isQueryOnActions)?", final.action_id":"")+ " \n" +
						"from (\n\t" + getQuery(expTree, 0) + "\n) final\n";
			
			// Join the permissions query
			if(checkPermissions){
				String permQuery = SqlQueryGenerator.getQueryForPermissions(sysId, userId);
				if(permQuery != null)
					basicSql += "join ("+permQuery+") perm on final.sys_id=perm.sys_id and final.request_id=perm.request_id \n";
			}
			
			if(externalTableSql != null){
				basicSql = basicSql + "join #externalJoin e on final.sys_id=e.sys_id and final.request_id=e.request_id \n" ;
			}
		}
		
		String sql = basicSql;
		
		// Add the custom columns in select
		if(type == SIMPLE && requestedColumns.size()>0){
			sql = SqlQueryGenerator.addQueriesForCustomRequestColumns(sql, requestedColumns, fields, ordering);
		}
		
		// Add paging and ordering
		// This needs to be called AFTER adding queries for custom request columns as the ordering column names are modified in it.
		sql = SqlQueryGenerator.addPagingAndOrderingTo(sql, isPagingEnabled, pageSize, pageNumber, ordering);	

		// If the search type is for Request objects, add the required joins
		String requestObjSql = "";
		if(type == REQUEST_OBJECT){
			
			// Put the relevant requests into #relevantReqs
			int splitIndex = sql.indexOf("from");
			sql = sql.substring(0, splitIndex) + "into #relevantReqs " + sql.substring(splitIndex);
			
			requestObjSql = SqlQueryGenerator.addQueriesForRequestObject();
		}
		
		if(externalTableSql != null)
			sql += "\n drop table #externalJoin \n";
		
		if(externalTableSql != null)
			sqls.add(externalTableSql);
		sqls.add(sql);
		if(!requestObjSql.equals(""))
			sqls.add(requestObjSql);
		
		return sqls;
	}

	//====================================================================================

	/**
	 * Execute the final query. Form the result based on the type of search requested.
	 * 
	 * @return result of the search
	 */
	protected List<SearchResult> executeQuery(ArrayList<String> sql){
		
		List<SearchResult> result = new ArrayList<SearchResult>();
		Connection conn = null;
		
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			Statement stmt = conn.createStatement();
			LOG.info("Execting SQL: " + sql);
			
			for(int i=0; i<sql.size(); i++)
			{
				stmt.execute(sql.get(i));
			}
			
			int count = 0;
			
			// Basic result
			ResultSet rs = stmt.getResultSet();
			if(rs == null){
				while(!stmt.getMoreResults());
				rs = stmt.getResultSet();
			}
			if(rs != null){
				while(rs.next()){
					
					SearchResult sr = new SearchResult(rs.getInt("sys_id"), rs.getInt("request_id"));
					
					// Add the requested type of result
					if(type == SIMPLE){
						if(isQueryOnActions)
							sr.addToResult("action_id", rs.getInt("action_id"));
						for(String rc : requestedColumns)
							sr.addToResult(rc, rs.getObject(rc));
					}
					else if(type == REQUEST_OBJECT)
						sr.addRequestObjectToResult(Request.createFromResultSet(rs));
					
					count++;
					
					result.add(sr);
				}
				
				// Populate the request objects using the remaining queries if required
				if(type == REQUEST_OBJECT){
					Map<Integer, Map<Integer, SearchResult>> mapping = new HashMap<Integer, Map<Integer,SearchResult>>();
					for(SearchResult sr : result){
						if(!mapping.containsKey(sr.getSysId()))
							mapping.put(sr.getSysId(), new HashMap<Integer, SearchResult>());
						mapping.get(sr.getSysId()).put(sr.getRequestId(), sr);
					}
					populateRequestObjects(mapping, stmt);
				}
			}
			
			System.out.println("Requests fetched : " + count);
			if(rs != null)
				rs.close();
			stmt.close();
			
			conn.commit();
		} 
		catch (Exception e) {
			LOG.error(e.getMessage(),e);
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		} 
		finally{
			if(conn != null)
				try {
					conn.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		return result;
	}

	//====================================================================================

	// Utility Functions

	/**
	 * Populate the Request objects or the REQUEST_OBJECT type search.
	 * 
	 * @param stmt
	 * @throws Exception 
	 */
	private void populateRequestObjects(Map<Integer, Map<Integer,SearchResult>> result, Statement stmt) throws Exception {
		
		// Result set for the request_users table
		while(!stmt.getMoreResults());
		ResultSet rs = stmt.getResultSet();
		if(rs != null){
			while(rs.next()){
				RequestUser reqUser = RequestUser.createFromResultSet(rs);
				Request currReq = result.get(reqUser.getSystemId()).get(reqUser.getRequestId()).getRequestObject();
                switch (reqUser.getUserTypeId()) {
                case UserType.LOGGER :		if(currReq.getLoggers() == null)
                								currReq.setLoggers(new ArrayList<RequestUser>());
                							currReq.getLoggers().add(reqUser);
                							break;

                case UserType.ASSIGNEE : 	if(currReq.getAssignees() == null)
												currReq.setAssignees(new ArrayList<RequestUser>());
                							currReq.getAssignees().add(reqUser);
                							break;

                case UserType.SUBSCRIBER : 	if(currReq.getSubscribers() == null)
												currReq.setSubscribers(new ArrayList<RequestUser>());
											currReq.getSubscribers().add(reqUser);
                							break;

                case UserType.TO : 			if(currReq.getTos() == null)
												currReq.setTos(new ArrayList<RequestUser>());
											currReq.getTos().add(reqUser);
                							break;

                case UserType.CC : 			if(currReq.getCcs() == null)
												currReq.setCcs(new ArrayList<RequestUser>());
											currReq.getCcs().add(reqUser);
                    						break;
                    						
                case UserType.USERTYPE: 	Field f = Field.lookupBySystemIdAndFieldId(reqUser.getSystemId(), reqUser.getFieldId());
                							if(currReq.getExUserType(f) == null)
												currReq.setExUserType(f, new ArrayList<RequestUser>());
											currReq.getExUserType(f).add(reqUser);
                }
			}
			
			// Result set for the requests_ex table
			while(!stmt.getMoreResults());
			rs = stmt.getResultSet();
			if(rs != null){
				while(rs.next()){
					RequestEx reqEx   = RequestEx.createFromResultSet(rs);
                    try{
                    	result.get(reqEx.getSystemId()).get(reqEx.getRequestId()).getRequestObject().addExtendedField(reqEx);
                    }
                    catch(IllegalArgumentException e){
                    	continue;
                    }
				}
				
				// Result set for the related_requests table
				while(!stmt.getMoreResults());
				rs = stmt.getResultSet();
				if(rs != null){
					HashMap<Integer, HashMap<Integer, ArrayList<RequestDataType>>> relatedReqs = new HashMap<Integer, HashMap<Integer,ArrayList<RequestDataType>>>();
					while(rs.next()){
						int sys_id = rs.getInt("primary_sys_id");
						int request_id = rs.getInt("primary_request_id");
						if(relatedReqs.get(sys_id) == null){
							relatedReqs.put(sys_id, new HashMap<Integer, ArrayList<RequestDataType>>());
						}
						if(relatedReqs.get(sys_id).get(request_id) == null){
							relatedReqs.get(sys_id).put(request_id, new ArrayList<RequestDataType>());
						}
						relatedReqs.get(sys_id).get(request_id).add(new RequestDataType(rs.getInt("related_sys_id"), rs.getInt("related_request_id"), rs.getInt("related_action_id")));
					}
					for(int sysId : relatedReqs.keySet()){
						for(int reqId : relatedReqs.get(sysId).keySet()){
							result.get(sysId).get(reqId).getRequestObject().setRelatedRequests(relatedReqs.get(sysId).get(reqId));
						}
					}
					
					// Result set for the childCount
					while(!stmt.getMoreResults());
					rs = stmt.getResultSet();
					if(rs != null){
						while(rs.next()){
							int sys_id = rs.getInt("sys_id");
							int parent_id = rs.getInt("parent_request_id");
							Request parentReq = result.get(sys_id).get(parent_id).getRequestObject();
							parentReq.setChildCount(parentReq.getChildCount() + 1);
						}
					}
				}
			}
		}
	}

	//====================================================================================

	/**
	 * Get the query defined by the expression e
	 * 
	 * @param e
	 * @param expressionCount
	 * @return
	 * @throws Exception
	 */
	private String getQuery(ExpressionTreeNode e, int expressionCount) throws Exception{
		
		// Initialise the query generator for the expression
		e.initialiseQueryGenerator((isSearchAcrossBA)?-1:sysId, userId, isQueryOnActions);
		
		// Verify the fields of the expressions constraints and append the condition for each
		ArrayList<Constraint> constraints = e.getExpression().getConstraints();
		if(constraints != null){
			for(Constraint c : constraints){
				Field f = fields.get(c.getField());
				if(f == null){
					// A hack to allow tags search in the fields
					if(!c.getField().equals(PUBLIC_TAGS_FIELD_FILTER) 
					&& !c.getField().equals(PRIVATE_TAGS_FIELD_FILTER) 
					&& !c.getField().equals(READ_UNREAD_FIELD_FILTER))
						throw new Exception("Invalid search field : " + c.getField());
				}
				e.appendCondition(f, c);
			}
		}
		
		// Find out if the expression's query starts with constraints or children expressions
		boolean startWithConstraint = true;
		if(e.getExpression().getConstraints() == null || e.getExpression().getConstraints().get(0).getOperator() != null)
			startWithConstraint = false;
		
		// Default query
		String query = e.getQuery();
		
		// Add children expressions
		for(ExpressionTreeNode child : e.getChildren()){
			expressionCount++;
			if(child.getExpression().getOperator() == null){
				if(child.getExpression().isNegate()){
					query = "(" + query + ")\n" +
							"except\n" +
							"(" + getQuery(child, expressionCount) + ") ";
				}
				else
					query = getQuery(child, expressionCount);
			}
			else if(child.getExpression().getOperator().equals(DqlConstants.Operator.AND)){
				if(child.getExpression().isNegate()){
					query = "(" + query + ")\n" +
							"except\n" +
							"(" + getQuery(child, expressionCount) + ") ";
				}
				else{
					query = "select distinct tbl"+expressionCount+".sys_id, tbl"+expressionCount+".request_id" +((isQueryOnActions)?", tbl"+expressionCount+".action_id":"")+ " \n" +
							"from (" + query + ") tbl"+(expressionCount-1)+ "\n" +
							"join (" + getQuery(child, expressionCount) + ") tbl"+expressionCount+ "\n" +
							"on tbl"+expressionCount+".sys_id=tbl"+(expressionCount-1)+".sys_id " +
							"and tbl"+expressionCount+".request_id=tbl"+(expressionCount-1)+".request_id ";
				}
			}
			else if(child.getExpression().getOperator().equals(DqlConstants.Operator.OR)){
				query += 	"\nunion\n" +	getQuery(child, expressionCount);
			}
		}
		
		// Append constraints if the chilren came before the constraints
		if(!startWithConstraint && e.getExpression().getConstraints()!= null){
			if(e.getExpression().getConstraints().get(0).getOperator().equals(DqlConstants.Operator.AND)){
				query = 	"select tble.* from (\n" + query + ") tble\n"+ 
							"join (\n" + e.getQuery() + ") tblc\n"+
							"on tble.sys_id=tblc.sys_id and tble.request_id=tblc.request_id ";
			}
			else if(e.getExpression().getConstraints().get(0).getOperator().equals(DqlConstants.Operator.OR)){
				query = 	"select * from \n" + query + "\nunion\n" +	e.getQuery();
			}
		}
		
		return query;
	}
	
	//====================================================================================

}
