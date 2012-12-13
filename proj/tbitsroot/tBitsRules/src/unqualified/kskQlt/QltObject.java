package kskQlt;

import java.util.ArrayList;
import java.util.Collection;

import org.jfree.util.Log;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class QltObject 
{
	public ArrayList<User> loggers = null;
	public ArrayList<User> assignees = null; 
	public ArrayList<User> subscribers = null; 
	public ArrayList<User> ccs = null;
	public User loginUser = null ;
	
	public Boolean genInspNo = null ;
	public Boolean genMdccNo = null ;
	public String inspNo = null ;
	public String lloydsNo = null ;
	public String mdccNo = null ;
	public Type unitNumber = null ;
	public Type subAreaCode = null ;
	public Type decision = null ;
	public Type flowStatus = null ;
	public Type pdfi = null ;
	
	Collection<AttachmentInfo> mdccFile = null ;
	Collection<AttachmentInfo> inspCallFile = null ;
	Collection<AttachmentInfo> lloydsDecFile = null ;
	Collection<AttachmentInfo> otherAttach = null ;
	
	public QltObject(Request req) throws TBitsException
	{
		try 
		{
			loginUser = User.lookupByUserId(req.getUserId()) ;
			loggers = getUsersFromRequestUsers(req.getLoggers());
			assignees = getUsersFromRequestUsers(req.getAssignees());
			subscribers = getUsersFromRequestUsers(req.getSubscribers());
			ccs = getUsersFromRequestUsers(req.getCcs());
			
			genInspNo = req.getExBoolean(QltConstants.QLT_GEN_INSP_NO_FIELD_NAME);
			genMdccNo = req.getExBoolean(QltConstants.QLT_GEN_MDCC_NO_FIELD_NAME);
			
			inspNo = req.get(QltConstants.QLT_INSP_NO_FIELD_NAME);
			lloydsNo = req.get(QltConstants.QLT_LLOYDS_DOC_NO_FIELD_NAME);
			mdccNo = req.get(QltConstants.QLT_MDCC_NO_FIELD_NAME) ;
			
			String un = req.get(QltConstants.QLT_UNIT_NO_FIELD_NAME);			
			unitNumber = Type.lookupAllBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QltConstants.QLT_UNIT_NO_FIELD_NAME, un);
			
			String sac = req.get(QltConstants.QLT_SUB_AREA_CODE_FIELD_NAME);
			subAreaCode = Type.lookupAllBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QltConstants.QLT_SUB_AREA_CODE_FIELD_NAME, sac);
	
			String dec = req.get(QltConstants.QLT_DECISION_FIELD_NAME );
			decision= Type.lookupAllBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QltConstants.QLT_DECISION_FIELD_NAME, dec);
			
			String fs = req.get(QltConstants.QLT_FLOW_STATUS_FIELD_NAME);
			flowStatus = Type.lookupAllBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QltConstants.QLT_FLOW_STATUS_FIELD_NAME, fs);
			
			String p = req.get(QltConstants.QLT_PDFI_FIELD_NAME) ;
			pdfi = Type.lookupAllBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QltConstants.QLT_PDFI_FIELD_NAME, p);
			
			String mf = req.get(QltConstants.QLT_MDCC_FILE_FIELD_NAME);
			if( mf != null )
				mdccFile = AttachmentInfo.fromJson(mf);
			
			String icf = req.get( QltConstants.QLT_INSP_FILE_FIELD_NAME ) ;
			if( icf != null )
				inspCallFile = AttachmentInfo.fromJson(icf);
			
			String ld = req.get(QltConstants.QLT_LLOYDS_FILE_FIELD_NAME);
			if( ld != null )
				lloydsDecFile = AttachmentInfo.fromJson(ld);
			
			String oa = req.get(QltConstants.QLT_OTHER_ATT_FIELD_NAME);
			if( oa != null )
				otherAttach = AttachmentInfo.fromJson(oa);
			
		}catch (Exception e) 
		{		
			e.printStackTrace();
			throw new TBitsException("Invalid state of the request.");
		}	
	}
	
	private ArrayList<User> getUsersFromRequestUsers(
			Collection<RequestUser> rus) throws DatabaseException 
	{
		ArrayList<User> users = new ArrayList<User>();
		if( rus == null )
			return users ;
		
		for( RequestUser ru : rus )
		{
			users.add(ru.getUser());
		}
		
		return users ;
	}

	public void validate() throws TBitsException
	{
		if( null == pdfi || null == flowStatus || null == decision || unitNumber == null || subAreaCode == null )
		{
			Log.info("Illegal state of the request.");
			throw new TBitsException("Illegal request object" );
		}

		genInspNo = ( null == genInspNo ? false : genInspNo );
		genMdccNo = ( null == genMdccNo ? false : genMdccNo );
		
		inspNo = ( null == inspNo ? "" : inspNo ) ;
		mdccNo = ( null == mdccNo ? "" : mdccNo ) ;
		lloydsNo = ( null == lloydsNo ? "" : lloydsNo ) ;
		
		lloydsDecFile = ( null == lloydsDecFile ? new ArrayList<AttachmentInfo>() : lloydsDecFile ) ;
		inspCallFile = ( null == inspCallFile ? new ArrayList<AttachmentInfo>() : inspCallFile ) ;
		otherAttach = ( null == otherAttach ? new ArrayList<AttachmentInfo>() : otherAttach ) ; 
	}	
}
