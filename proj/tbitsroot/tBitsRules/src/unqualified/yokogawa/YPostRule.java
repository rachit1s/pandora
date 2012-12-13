package yokogawa;

import static yokogawa.YConst.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class YPostRule implements IPostRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		// ba 
		if( ba == null || ba.getSystemPrefix() == null || !ba.getSystemPrefix().equalsIgnoreCase(privBA) )
			return new RuleResult(true,"Not valid for this ba.", true);
		
		
		if( same(oldRequest,currentRequest) )
		{
			return new RuleResult(true,"The considered values are not changed.",true);
		}
		
		BusinessArea pba = null ;
		try {
			pba = BusinessArea.lookupBySystemPrefix(pubBA);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		if( null == pba )
			return new RuleResult(false,"Cannot find the businessArea with sysprefix : " + pubBA, false);

		String sql = " select * from related_requests where primary_sys_id = " + pba.getSystemId() + " and related_sys_id = " + ba.getSystemId() + " and related_request_id = " + currentRequest.getRequestId() ;

		System.out.println("sql : " + sql);
		int relatedRequestId = 0 ;
		Connection con = null ;
		try
		{
			con = DataSourcePool.getConnection() ;
			if( con != null )
			{
				PreparedStatement ps = con.prepareStatement(sql);
				System.out.println("executing query.");
				ResultSet rs = ps.executeQuery() ;
				System.out.println("query executed.");
				if( rs != null )
				{
					while( rs.next() )
					{
						relatedRequestId = rs.getInt("primary_request_id");
						break ;
					}
				}
				
				rs.close();
				ps.close();
			}			
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
		}
		finally
		{
			if( con != null )
			{
				try {
					con.close();
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
		}
		
		Hashtable<String,String> reqParams = new Hashtable<String,String>() ;
		
		reqParams.put(Field.BUSINESS_AREA, pba.getSystemId()+"");
		reqParams.put(Field.USER, root);
		
		String catId = currentRequest.get(Field.CATEGORY);
		if( null != catId )
			reqParams.put(Field.CATEGORY,catId);
		
		String reqTId = currentRequest.get(Field.REQUEST_TYPE);
		if( null != reqTId)
			reqParams.put(Field.REQUEST_TYPE, reqTId);
		
		String statId = currentRequest.get(Field.STATUS);
		if( null != statId )
			reqParams.put(Field.STATUS, statId);
		
		String sevId = currentRequest.get(Field.SEVERITY);
		if( null != sevId )
			reqParams.put(Field.SEVERITY, sevId);
		
		String ftn = currentRequest.get(FieldTagNo);
		if( null != ftn )
			reqParams.put(FieldTagNo, ftn);			
	
		String relatedRequest = ba.getSystemPrefix() + "#" + currentRequest.getRequestId() ;
		reqParams.put(Field.RELATED_REQUESTS, relatedRequest);
		
		Collection<AttachmentInfo> atts = currentRequest.getAttachments() ;
		String attJson = AttachmentInfo.toJson(atts);
		reqParams.put(Field.ATTACHMENTS, attJson);
		
		String desc = "This action was automatically added by system for an add / update on : " + ba.getSystemPrefix() + "#" + currentRequest.getRequestId() + ( currentRequest.getMaxActionId() == 0 ? "" : "#" + currentRequest.getMaxActionId() ) ; 
		reqParams.put(Field.DESCRIPTION, desc);
		
		Request req = null ;
		try
		{
			if( relatedRequestId == 0 )
			{
				AddRequest addRequest = new AddRequest() ;
				
				addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
				
				System.out.println("addnew new request with paramters : " + reqParams);
				req = addRequest.addRequest(reqParams);
			}
			else
			{
				reqParams.put(Field.REQUEST, relatedRequestId+"");
				UpdateRequest updateRequest = new UpdateRequest() ;
				
				updateRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
				
				System.out.println("updating request with params : " + reqParams);
				req = updateRequest.updateRequest(reqParams);
			}
		}
		catch(TBitsException te)
		{
			te.printStackTrace() ;
			return new RuleResult(false, "Exception : " + te.getDescription() , false );
		}
		catch(Exception te)
		{
			te.printStackTrace() ;
			return new RuleResult(false, "Exception : " + te.getMessage() , false );
		} catch (APIException e) 
		{			
			e.printStackTrace();
			return new RuleResult(false, "Exception : " + e.getMessage() , false );
		}
		if( null != req )
			return new RuleResult(true, "Request added successFully : " + pba.getSystemPrefix() + "#" + req.getRequestId() , true );
		else
			return new RuleResult( false ,"Request Addition failed.", false);
	}

	public String getName() 
	{
		return "Add / Update request in the public ba.";
	}

	public double getSequence() {
		return 0;
	}

}
