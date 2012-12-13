

package essardcn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.RuleResult;



public class ParentRequestUpdatingRule implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("essardcn");
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
			
			if( Source == TBitsConstants.SOURCE_CMDLINE )
		{
			return new RuleResult(true,"Rule not applicable for cmd-request.", true);
		}
	
		
		if (isAddRequest)	
		{
			return new RuleResult(true,"Rule not applicable for add request.",true);
		}
		
		try {
		String  is_Dcn = oldRequest.get("isdsn");
		if(is_Dcn.equalsIgnoreCase("true"))
		{
			int reqId = oldRequest.getRequestId();
			
			//getting parent request when we update a child request.
			int parentRequestID =  currentRequest.getParentRequestId();
			
	/*		if(parentRequestID != 0 )
			{
				
					Request pReq = Request.lookupBySystemIdAndRequestId(ba.getSystemId(),parentRequestID );
					parentRequestID = pReq.getParentRequestId();
					while(parentRequestID !=0)
					{
						Request paReq = Request.lookupBySystemIdAndRequestId(ba.getSystemId(),parentRequestID );
						parentRequestID = paReq.getParentRequestId();
					}
					 ArrayList<Request> pChildRequest = getChildRequest(ba.getSystemId(),parentRequestID,connection);
					 ArrayList<Request> reqTotalUpdate = getGrandChildRequest(pChildRequest,ba.getSystemId(),connection);
					 reqTotalUpdate.addAll(pChildRequest);
					 updateAllRequests(reqTotalUpdate,connection,isAddRequest,ba, currentRequest);
					
				
			}else{*/
				//getting child requests when we update a parent request..
		    ArrayList<Request> childRequest = getChildRequest(ba.getSystemId(),reqId,connection);
		    ArrayList<Request> reqToUpdate = getGrandChildRequest(childRequest,ba.getSystemId(),connection);
		    reqToUpdate.addAll(childRequest);
		    updateAllRequests(reqToUpdate,connection,isAddRequest,ba, currentRequest);
		  //  }
		   
		    
		}
		
		} catch (Exception e) {
			LOG.info("Exception while executing the rule............");
			e.printStackTrace();
		}
		return new RuleResult(true ,"Rule executed successfuly.................",true);
		
	}

	private void updateAllRequests(ArrayList<Request> reqTotalUpdate,
			Connection connection, boolean isAddRequest, BusinessArea ba, Request currentRequest) {
		
	for(Request req : reqTotalUpdate)
	{
		Hashtable<String,String> params = new Hashtable<String,String>();
		//int uId = req.getUserId();
		User su = null;
		try {
			su = User.lookupByUserLogin("root");
		} catch (DatabaseException e) {
		
			e.printStackTrace();
		}
	
		params.put(Field.USER, su.getUserLogin());
		params.put(Field.DESCRIPTION, "Update this request " + (isAddRequest == true ? "" : "in response to action on request :" + ba.getSystemPrefix()+"#"+currentRequest.getRequestId() +"#"+currentRequest.getMaxActionId()) );
	
		
		params.put(Field.BUSINESS_AREA, req.getSystemId()+"");
		params.put(Field.REQUEST, req.getRequestId()+"");
		params.put("isdsn", "false");
		
		
		UpdateRequest up = new UpdateRequest();
		up.setSource(TBitsConstants.SOURCE_CMDLINE);
		TBitsResourceManager trm = new TBitsResourceManager();
		try {
			Request requ = up.updateRequest(connection,trm, params);
		} catch (TBitsException e) {
			LOG.info("Exception while updating............");
			e.printStackTrace();
		} catch (APIException e) {
			LOG.info("Exception while updating............");
			e.printStackTrace();
		}

	

		
	}
		
	}

	private ArrayList<Request> getGrandChildRequest(
			ArrayList<Request> childRequest, int systemId, Connection connection) {
		ArrayList<Request> reqToUpdate = new ArrayList<Request>();
		 for(Request cReq : childRequest)
			{
		    	 ArrayList<Request> grandChildRequests= getChildRequest(systemId,cReq.getRequestId(),connection);
		    	 reqToUpdate.addAll(grandChildRequests);
			}
		 return reqToUpdate;
	}

	private ArrayList<Request> getChildRequest(int sysId ,int reqId, Connection connection) {
		Request request;
		ArrayList<Request> childReq = new ArrayList<Request>();
		connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select * from requests where sys_id = ? and parent_request_id = ?");
			ps.setInt(1, sysId);
			ps.setInt(2, reqId);
			ResultSet rs;
			rs = ps.executeQuery();

			if (rs != null) {
				while (rs.next() != false) {
					try {
						request = Request.createFromResultSet(rs);
						childReq.add(request);
					} catch (DatabaseException e) {
						LOG.info("Exception while contacting the database....");
						e.printStackTrace();
					}

				}
				if (rs != null)
					rs.close();
			}
			if (ps != null)
				ps.close();
		} catch (SQLException e) {

			e.printStackTrace();
			LOG.info("Exception while contacting the database....");
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				LOG.info("Exception while closing the connection");
			}
		}
		return childReq;
	}


	public double getSequence() {
	
		return 0;
	}


	public String getName() {
		
		return "ParentRequestUpdatingRule";
	}

}
