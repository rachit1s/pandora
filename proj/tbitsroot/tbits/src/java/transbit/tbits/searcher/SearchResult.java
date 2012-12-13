package transbit.tbits.searcher;

import java.util.Date;
import java.util.HashMap;

import transbit.tbits.domain.Request;
import transbit.tbits.dql.treecomponents.DqlConstants;

/**
 * This class defines the format for the searcher result. It contains a hashmap of
 * the column name and the object fetched by the searcher corresponding to that 
 * column name. There are get unctions provided for each data type that is fetched.
 * 
 * @author Karan Gupta
 *
 */
public class SearchResult {
	
	//====================================================================================

	private int sys_id;
	private int request_id;
	
	// Nitiraj onbehalf of karan : it returns only the request ex values only. and not request_users etc in the object 
	private HashMap<String, Object> result;

	//====================================================================================

	/**
	 * Constructor
	 */
	public SearchResult(int sysId, int requestId){
		this.sys_id = sysId;
		this.request_id = requestId;
		result = new HashMap<String, Object>();
	}
	
	public int getSysId(){
		return this.sys_id;
	}
	
	public int getRequestId(){
		return this.request_id;
	}
	
	//====================================================================================

	// Functions to add to the result
	
	/**
	 * Add the given object to the result
	 * @param colName
	 * @param obj
	 */
	public void addToResult(String colName, Object obj){
		result.put(colName, obj);
	}
	
	/**
	 * Add the given Request object to the result
	 * @param reqObj
	 */
	public void addRequestObjectToResult(Request reqObj) {
		result.put(DqlConstants.REQUEST_COL, reqObj);
	}
	
	//====================================================================================

	// Functions to retrieve from the result
	
	/**
	 * Get the Integer mapped to the specified column name
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	public int getInteger(String colName) throws Exception{
		if(!result.containsKey(colName))
			throw new Exception("Column name not found!");
		if(!(result.get(colName) instanceof Integer))
			throw new Exception("Type mismatch!");
		
		return (Integer)result.get(colName);
	}
	
	/**
	 * Get the Float mapped to the specified column name
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	public float getFloat(String colName) throws Exception{
		if(!result.containsKey(colName))
			throw new Exception("Column name not found!");
		if(!(result.get(colName) instanceof Float))
			throw new Exception("Type mismatch!");
		
		return (Float)result.get(colName);
	}
	
	/**
	 * Get the String mapped to the specified column name
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	public String getString(String colName) throws Exception{
		if(!result.containsKey(colName))
			throw new Exception("Column name not found!");
		if(!(result.get(colName) instanceof String))
			throw new Exception("Type mismatch!");
		
		return (String)result.get(colName);
	}
	
	/**
	 * Get the Date mapped to the specified column name
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	public Date getDate(String colName) throws Exception{
		if(!result.containsKey(colName))
			throw new Exception("Column name not found!");
		if(!(result.get(colName) instanceof Date))
			throw new Exception("Type mismatch!");
		
		return (Date)result.get(colName);
	}
	
	/**
	 * Get the Request object in the result
	 * @return
	 * @throws Exception
	 */
	public Request getRequestObject() throws Exception{
		String colName = DqlConstants.REQUEST_COL;
		if(!result.containsKey(colName))
			throw new Exception("Column name not found!");
		if(!(result.get(colName) instanceof Request))
			throw new Exception("Type mismatch!");
		
		return (Request)result.get(colName);
	}

	//====================================================================================
	
	public HashMap<String, Object> getResult(){
		return this.result;
	}

}
