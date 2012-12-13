package transbit.tbits.searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import transbit.tbits.domain.Request;

/**
 * This searcher constructs and returns the Request object hierarchy for a specified request.
 * The request object hierarchy has the specified request as the root and its children underneath.
 * This searcher does not use constraints or constraints list to form the sql.
 * The sql is hardcoded inside this searcher.
 * 
 * @author Karan Gupta
 *
 */
public class RequestHierarchySearcher extends Searcher{

	//====================================================================================

	private int reqId;
	private RequestHierarchyNode treeResult;
	private HashMap<Integer, ArrayList<Request>> childMap;
	
	//====================================================================================

	/**
	 * Constructor
	 * 
	 * @param sysId
	 * @param reqId
	 */
	protected RequestHierarchySearcher(int sysId, int reqId) {
		super(sysId);
		this.reqId = reqId;
		type = REQUEST_OBJECT;
	}

	//====================================================================================

	/**
	 * @return the root for the tree hierarchy
	 */
	protected RequestHierarchyNode getResult() {
		return treeResult;
	}

	//====================================================================================

	/**
	 * The main search method.
	 */
	protected void search() throws Exception {

		ArrayList<String> sqls = new ArrayList<String>();
		
		sqls.add("create table #relevantReqs (sys_id int, request_id int)\n" );
		sqls.add(getBasicQuery());
		sqls.add(SqlQueryGenerator.addQueriesForRequestObject());
		sqls.add("drop table #relevantReqs\n" );
		
		List<SearchResult> result = executeQuery(sqls);
		Map<Integer, Map<Integer, SearchResult>> mapping = new HashMap<Integer, Map<Integer,SearchResult>>();
		for(SearchResult sr : result){
			if(!mapping.containsKey(sr.getSysId()))
				mapping.put(sr.getSysId(), new HashMap<Integer, SearchResult>());
			mapping.get(sr.getSysId()).put(sr.getRequestId(), sr);
		}
		
		formTreeFrom(mapping);
	}

	//====================================================================================

	/**
	 * Forms the hierarchy tree from the search results.
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void formTreeFrom(Map<Integer, Map<Integer, SearchResult>> result) throws Exception {

		childMap = new HashMap<Integer, ArrayList<Request>>();
		for(int sys_id : result.keySet()){
			for(int req_id : result.get(sys_id).keySet()){
				Request req = result.get(sys_id).get(req_id).getRequestObject();
				int pid = req.getParentRequestId();
				if(!childMap.containsKey(pid))
					childMap.put(pid, new ArrayList<Request>());
				childMap.get(pid).add(req);
			}
		}
		
		treeResult = new RequestHierarchyNode(result.get(sysId).get(reqId).getRequestObject());
		populateChildren(treeResult);
	}

	//====================================================================================

	/**
	 * Populates the children of the given node from the childmap.
	 * 
	 * @param currentNode
	 */
	private void populateChildren(RequestHierarchyNode currentNode) {

		if(childMap.get(currentNode.getRequestObject().getRequestId()) == null)
			return;
		
		for(Request req : childMap.get(currentNode.getRequestObject().getRequestId())){
			RequestHierarchyNode rNode = new RequestHierarchyNode(req);
			populateChildren(rNode);
			currentNode.addChild(rNode);
		}
	}

	//====================================================================================

	/**
	 * This query puts all the relevant requests into the temporary table #relevantReqs.
	 * The relevant requests contains the specified request_id and the request_ids of all the
	 * requests that lie under it in the hierarchy.
	 * 
	 * @return the basic query for request hierarchy search.
	 */
	private String getBasicQuery(){
		
		return	"declare @rc int\n" +
						
				"create table #current (sys_id int, request_id int)\n" +
				
				"insert into #current (sys_id, request_id) \n" +
				"values (" + sysId + ", " + reqId + ") \n" +
		
				"set @rc=1 \n" +
		
				"while(@rc<>0) \n" +
				"begin \n" +
		
				"insert into #relevantReqs (sys_id, request_id) \n" +
				"select * from (select * from #current) as tbl \n" +
		
				"select * into #temp from #current \n" +
		
				"delete from #current \n" +
		
				"insert into #current (sys_id, request_id) \n" +
				"select r.sys_id, r.request_id \n" + 
				"from requests r join #temp t \n" +
				"on r.sys_id=t.sys_id and t.request_id=r.parent_request_id \n" +
		
				"set @rc = @@rowcount \n" + 
				"drop table #temp \n" +
		
				"end \n" +
				
				"drop table #current";
	}

}

//====================================================================================
//====================================================================================
//====================================================================================


/**
 * The node for the request hierarchy tree.
 * 
 * @author Karan Gupta
 *
 */
class RequestHierarchyNode{

	//====================================================================================

	private Request requestObject;
	private ArrayList<RequestHierarchyNode> children;

	//====================================================================================

	/**
	 * Constructor
	 * 
	 * @param req
	 */
	public RequestHierarchyNode(Request req) {
		requestObject = req;
		children = new ArrayList<RequestHierarchyNode>();
	}

	//====================================================================================

	// Getter and setter methods
	
	public Request getRequestObject() {
		return requestObject;
	}

	public void setChildren(ArrayList<RequestHierarchyNode> children) {
		this.children = children;
	}
	
	public void addChild(RequestHierarchyNode child){
		this.children.add(child);
	}

	public ArrayList<RequestHierarchyNode> getChildren() {
		return children;
	} 
	
	//====================================================================================

}
