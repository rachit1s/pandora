package transbit.tbits.searcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import transbit.tbits.dql.treecomponents.Expression;

public class SearcherTester {
	
	public static Date startTime;

	//====================================================================================

	public static void main(String arg[]){
		
		//--------------------------- Command line testing tool --------------------------
		
//		String cont = "y";
//		while(cont.equals("y") || cont.equals("Y")){
//			try {
//				cont = commandLineTester();
//			}
//			catch (IOException e) {
//				e.printStackTrace();
//				return;
//			}
//		}
//		return;
		
    	//------------------------ Multi threaded performance test -----------------------

		try {
			User.lookupByUserLogin("root");
		}
		catch (DatabaseException e) {
			e.printStackTrace();
		}
		System.out.println("DB initialized.");
		
		SearcherTester.startTime = new Date();
    	
    	for(int i=0; i<1; i++){
    		new XXXThread("thread"+i);
    	}
    	
    	//---------------------------------------------------------------------------------

		//------------------------ Verify dql against sql ---------------------------------

//    	String dql = "SELECT sys_id, request_id " +
//					 "WHERE status_id:\"-\"";
//    	String sql = "select * from requests r " +
//    				 "join types t on t.sys_id=r.sys_id and t.name='-' and r.status_id=t.type_id " +
//    				 "join fields f on f.sys_id=r.sys_id and r.sys_id=77 and t.field_id=f.field_id and f.name='status_id'";
//    	
//    	System.out.println("Verify dql against sql" + validateDqlSearcherResult(77, dql, sql));
		
    	//---------------------------------------------------------------------------------
		
    	//------------------- Verify parser constructed tree structure---------------------
    	
//    	// Define your tree here
//		/*
//		 * sms_id:>0 																| c1
//		 * AND NOT (status_id:(\"CU\" OR \"Desein\") AND assignee_ids:\"root\") 	| e1 -> c11, c12
//		 * OR NOT ((status_id:\"LTHO\") AND category_id:\"LTHOLTSL\") 				| e2 -> e21 -> c211, c21
//		 * AND due_datetime:<today 													| c2
//		 * OR NOT attachments:NULL 													| c3
//		 * OR attachments:(<>NULL AND \"Recovery plan.pdf\")						| c4
//		 */
//		Expression root = new Expression();
//		for(int i=0; i<4; i++){
//			Constraint c = new Constraint("");
//			root.addConstraint(null, c);
//		}
//		
//		Expression e1 = new Expression();
//		for(int i=0; i<2; i++){
//			Constraint c = new Constraint("");
//			e1.addConstraint(null, c);
//		}
//		root.addChild(null, e1);
//		
//		Expression e2 = new Expression();
//		Expression e21 = new Expression();
//		Constraint c211 = new Constraint("");
//		e21.addConstraint(null, c211);
//		e2.addChild(null, e21);
//		Constraint c21 = new Constraint("");
//		e2.addConstraint(null, c21);
//		root.addChild(null, e2);
//		
//		String dql = "sms_id:>0 AND NOT (status_id:(\"CU\" OR \"Desein\") AND assignee_ids:\"root\") OR NOT ((status_id:\"LTHO\") AND category_id:\"LTHOLTSL\") AND due_datetime:<today OR NOT attachments:NULL OR attachments:(<>NULL AND \"Recovery plan.pdf\")";
//		Searcher searcher = new DqlSearcher(77, 1, dql);
//		System.out.println("Validation : " + validateTreeStructure(searcher, root));
    	
    	//---------------------------------------------------------------------------------

	}

	private static String commandLineTester() throws IOException {
		
		//  open up standard input
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	    int sysId = 0;
	    try {
	    	System.out.println("Enter the sysId : ");
	    	sysId = Integer.parseInt(br.readLine());
	    } catch (IOException ioe) {
	        System.out.println("IO error trying to read sysId!");
	        System.exit(1);
	    }
	    
	    int userId = 0;
	    try {
	    	System.out.println("Enter the userId : ");
	    	userId = Integer.parseInt(br.readLine());
	    } catch (IOException ioe) {
	        System.out.println("IO error trying to read userId!");
	        System.exit(1);
	    }
	    
	    String dql = "";
	    try {
	    	System.out.println("Enter the dql : ");
	    	dql = br.readLine();
	    } catch (IOException ioe) {
	        System.out.println("IO error trying to read dql!");
	        System.exit(1);
	    }
	    
	    SearcherTester.startTime = new Date();
	    Map<Integer, Map<Integer, SearchResult>> result = null;
	    try{
	    	Searcher searcher = new DqlSearcher(sysId, userId, dql);
			searcher.search();
			result = (Map<Integer, Map<Integer, SearchResult>>) searcher.getResult();
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
		}
	    
	    if(result != null){
	    	System.out.println("Time to search = " + (new Date().getTime() - SearcherTester.startTime.getTime()));
		    System.out.println("===========================RESULT==============================");
		    boolean flag = true;
	    	String requestedCols = "sys_id\trequest_id";
	    	String resultStr = "";
	    	for(int s : result.keySet()){
	    		for(int r : result.get(s).keySet()){
	    			resultStr += ("\n"+s+"\t"+r);
	    			for(String colName : result.get(s).get(r).getResult().keySet()){
	    				if(flag)
	    					requestedCols += "\t"+colName;
	    				resultStr += "\t"+result.get(s).get(r).getResult().get(colName).toString();
	    			}
	    			flag = false;
	    		}
	    	}
	    	System.out.println(requestedCols);
	    	System.out.println("---------------------------------------------------------------");
	    	System.out.println(resultStr);
	    	System.out.println("===============================================================");
	    }
	    System.out.println("\n\n Press [y] to search again.");
		return br.readLine();
	}

	//====================================================================================

	/**
	 * Compares search results without user permission based filtering
	 * <br><br>
	 * <b>Sample usage:</b><br>
	 * String dql = "SELECT sys_id, request_id " + <br>
	 *				 "WHERE status_id:\"-\""; <br>
     *	String sql = "select * from requests r " + <br>
     *				 "join types t on t.sys_id=r.sys_id and t.name='-' and r.status_id=t.type_id " + <br>
     *				 "join fields f on f.sys_id=r.sys_id and r.sys_id=77 and t.field_id=f.field_id and f.name='status_id'"; <br>
     *	System.out.println(validateDqlSearcherResult(77, dql, sql)); <br>
	 * <br>
	 * 
	 * @param sysId
	 * @param dql
	 * @param sql
	 * @return
	 */
	public static boolean validateDqlSearcherResult(int sysId, String dql, String sql){
		
		// Get the search result for the given dql
		DqlSearcher dSearcher = new DqlSearcher(sysId, -1, dql);
		try {
			dSearcher.search();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		Map<Integer, Map<Integer, SearchResult>> dResult = dSearcher.getResult();
		
		// Get the result for the given sql
		Connection conn = null;
		HashMap<Integer, ArrayList<Integer>> sResult = new HashMap<Integer, ArrayList<Integer>>();
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();
			
			while(rs.next()){
				int s = rs.getInt("sys_id");
				int r = rs.getInt("request_id");
				if(!sResult.containsKey(s))
					sResult.put(s, new ArrayList<Integer>());
				sResult.get(s).add(r);
			}
			
			conn.commit();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		finally{
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		// Compare the two results
		if(dResult.keySet().size() != sResult.keySet().size())
			return false;
		
		for(int dsys : dResult.keySet()){
			if(!sResult.containsKey(dsys))
				return false;
			
			if(dResult.get(dsys).keySet().size() != sResult.get(dsys).size())
				return false;
			
			for(int dreq : dResult.get(dsys).keySet()){
				if(!sResult.get(dsys).contains(dreq))
					return false;
			}
		}
		
		return true;
	}
	
	//====================================================================================

	/**
	 * Compares search results with user permission based filtering. Request ids are provided explicitly.
	 * 
	 * @param sysId
	 * @param dql
	 * @param sql
	 * @return
	 */
	public static boolean validateDqlSearcherResult(int sysId, int userId, String dql, ArrayList<Integer> requests){
		
		// Get the search result for the given dql
		DqlSearcher dSearcher = new DqlSearcher(sysId, userId, dql);
		try {
			dSearcher.search();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		Map<Integer, Map<Integer, SearchResult>> dResult = dSearcher.getResult();
		
		// Compare the two results
		if(dResult.get(sysId).keySet().size() != requests.size())
			return false;
		
		for(int dreq : dResult.get(sysId).keySet()){
			if(!requests.contains(dreq))
				return false;
		}
		
		return true;
	}
	
	//====================================================================================

	/**
	 * Validates the parser built tree structure. <br>
	 * <br>
	 * <b>Sample Usage : </b><br>
	 * 
	 * Expression root = new Expression(); <br>
	 *	for(int i=0; i<4; i++){ <br>
	 *		Constraint c = new Constraint(""); <br>
	 *		root.addConstraint(null, c); <br>
	 *	} <br>
	 *	<br>
	 *	Expression e1 = new Expression(); <br>
	 *	for(int i=0; i<2; i++){ <br>
	 *		Constraint c = new Constraint(""); <br>
	 *		e1.addConstraint(null, c); <br>
	 *	} <br>
	 *	root.addChild(null, e1); <br>
	 *	<br>
	 *	Expression e2 = new Expression(); <br>
	 *	Expression e21 = new Expression(); <br>
	 *	Constraint c211 = new Constraint(""); <br>
	 *	e21.addConstraint(null, c211); <br>
	 *	e2.addChild(null, e21); <br>
	 *	Constraint c21 = new Constraint(""); <br>
	 *	e2.addConstraint(null, c21); <br>
	 *	root.addChild(null, e2); <br>
	 *	<br>
	 *	String dql = "sms_id:>0 AND NOT (status_id:(\"CU\" OR \"Desein\") AND assignee_ids:\"root\") OR 
	 *	NOT ((status_id:\"LTHO\") AND category_id:\"LTHOLTSL\") AND due_datetime:<today OR 
	 *	NOT attachments:NULL OR attachments:(<>NULL AND \"Recovery plan.pdf\")"; <br>
	 *	Searcher searcher = new DqlSearcher(77, 1, dql); <br>
	 *	System.out.println("Validation : " + validateTreeStructure(searcher, root)); <br>
	 *	<br>
	 *	@param searcher
	 *	@param root
	 */
	public static boolean validateTreeStructure(Searcher searcher, Expression root){
		
		ExpressionTreeNode rootTree = ExpressionTreeNode.makeTreeFrom(root);
		
		try {
			ExpressionTreeNode parseTree = searcher.getExpTreeRoot();
			// Compare parseTree to root
			return validateNodes(parseTree, rootTree);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//====================================================================================

	public static boolean validateNodes(ExpressionTreeNode ex1, ExpressionTreeNode ex2){

		if(ex1.getExpression().getConstraintListSize() != ex2.getExpression().getConstraintListSize())
			return false;
		if(ex1.getExpression().getChildListSize() != ex2.getExpression().getChildListSize())
			return false;
		if(ex1.getChildListSize()>0){
			for(int i=0; i<ex1.getChildren().size(); i++){
				if(!validateNodes(ex1.getChildren().get(i), ex2.getChildren().get(i)))
						return false;
			}
		}
		
		return true;
	}
	
	//====================================================================================

}

//====================================================================================

class XXXThread extends Thread {
	
	XXXThread() {
	}
	
	XXXThread(String threadName) {
		super(threadName); // Initialize thread.
		System.out.println(this);
		start();
	}
	
	public void run(){
		try {
			
			/*
			 * "SELECT sys_id, request_id " +
			 * "WHERE status_id:\"-\" AND lastupdated_datetime:<today OR sms_id:0 AND NOT (assignee_ids:(\"root\" OR \"hitendra_pooniwala\")) " +
			 *				"OR notify_loggers:<>0 " +
			 *		"HAS TEXT attachments:deva.doc " +
			 *		"LIMIT 1,50" +
			 *		"";
			 */
			
//			String dql = "SELECT sys_id, request_id " +
//						"WHERE " +
////						"HAS TEXT all:(some OR thing)" +
//						"ORDER BY request_id ASC";
			
//			String dql = "SELECT sys_id, request_id WHERE " +
//						"SendSMS:(no OR IN {true,yes}) OR " +
//						"(status_id:IN {\"Active\", \"Pending\"}) AND " +
//						"(assignee_ids:('sandeep.g OR IN {\"sand'e'ep,g\",\"utka%rsh.d\",\"sour'abh.a\",\"%nitiraj.r\",\"karan.g\",manoj.s}))";
			
//			String dql = "SELECT sys_id, request_id FOR root WHERE NOT(request_id:IN{1,2,3,4,5,6,7,8,9} AND NOT(request_id:IN{1,2,3}))";
			
//			String dql = 	"SELECT sys_id, request_id WHERE logged_datetime:(<today AND >=today-200d)";
			
//			String dql = "SELECT sys_id, request_id WHERE v_ESD:(>20/6/2011 AND <21/6/2011)";
			
//			String dql = "SELECT sys_id, request_id WHERE (request_id:1 OR request_id:3) OR (request_id:2) OR (SendSMS:(no OR false) AND order:0)";
			
//			String dql = "SELECT x, \"y\\\"\" FROM \\\"sa, \"sa sa sa\", \" sa \\\"sa sa sa AND WHERE \" WHERE requ\\est_id:1 OR request_id:\"\\ 2\"";
			
//			String dql = "SELECT sys_id, request_id WHERE related_requests:(IN {1,2,\"luceneTest#1\"}) HAS TEXT  ORDER BY request_id DESC";
			
//			String dql = "SELECT sys_id, request_id WHERE user_id:\"arnab.basu\"";

//			String dql = "SELECT sys_id, request_id WHERE subject:\"INSTALLATION\"";
			
//			String dql = "SELECT sys_id, request_id WHERE status_id:IN {\"Active\", \"Pending\"} AND NOT (attachments:NULL)";
			
			String dql = "SELECT sys_id, request_id WHERE exFieldInt:<=100 LIMIT 2,50";
			
			Searcher searcher = new DqlSearcher(183, 1, dql);
//			searcher.enablePaging("request_id", 50, 1);
//			searcher.enableQueryOnActions();
			searcher.search();
			searcher.getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(this + " -> Time till now = " + (new Date().getTime() - SearcherTester.startTime.getTime()));
		return;
	}
}

//====================================================================================

class XXThread extends Thread {
	
	XXThread() {
	}
	
	XXThread(String threadName) {
		super(threadName); // Initialize thread.
		System.out.println(this);
		start();
	}
	
	public void run(){
		try {
			Searcher searcher = new RequestHierarchySearcher(77, 1);
			searcher.search();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(this + " -> Time till now = " + (new Date().getTime() - SearcherTester.startTime.getTime()));
		return;
	}
}

//====================================================================================

