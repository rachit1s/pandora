package nccCorres;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import static nccCorres.CorresConstants.*;
import static nccCorres.GenCorresHelper.*;

public class CorresObject implements Serializable
{
	public User loginUser = null ;
	public User logger0User = null ;
	public User ass0User = null ;
		
	public Type corrType = null ;
	public Type originator = null ;
	public Type recepient = null ;
	public Type protApp = null ;
	public Type generate = null ;
	public Type contractReference = null ;
	public Type pack = null ;
	public Type genAtt = null ;
	public Type wbsAtt = null ;
	public Type discipline = null ;
	
	public Type location = null ;
	
	public ArrayList<User> loggers = null ;
	public ArrayList<User> subscribers = null ;
	public ArrayList<User> assignees = null ;
	public ArrayList<User> ccs = null ;
	public String corrNo = null ;
	public String description = null ;
	public String subject = null ;
	public String linkedRequests = null; // Billing
	public String tempFieldBilling = null; // Billing
	
	public BusinessArea ba = null ;
	
	public Collection<AttachmentInfo> otherAttach = null ;
	public Collection<AttachmentInfo> corrAttach = null ;
	
	public CorresObject( HttpServletRequest hr ) throws TBitsException
	{		
		// ba
		String sys_id = hr.getParameter("systemId") ;
		if( null == sys_id )
			throw new TBitsException("Illegal BusinessArea (systemId cannot be null)");
		int sysId = 0 ;
		
		try
		{
			sysId = Integer.parseInt(sys_id) ;
		}
		catch(NumberFormatException nfe)
		{
			throw new TBitsException("Illegal BusinessArea : sys_id = " + sys_id );
		}
		
		try {
			ba = BusinessArea.lookupBySystemId(sysId) ;
		} catch (DatabaseException e1) {			
			e1.printStackTrace();
			throw new TBitsException("Cannot access BusinessArea with sys_Id :" + sysId ) ;
		}
		
		if(null == ba || null == ba.getSystemPrefix() || !ba.getSystemPrefix().equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX))
			throw new TBitsException("Illegal BusinessArea with sys_id : " + sys_id ) ;
		
		// users
		try {
			loginUser = WebUtil.validateUser(hr);
			if( null == loginUser )
				throw new TBitsException("Illegal Login User.") ;
		} catch (DatabaseException e) {			
			throw new TBitsException("Cannot verify Login User.") ;
		}
		
		String loggerList = hr.getParameter(CorresConstants.CORR_LOGGER_FIELD_NAME) ;
		if( null != loggerList && !loggerList.trim().equals(""))	
		{
			loggers = APIUtil.toUserList(loggerList, false);
			
			if( loggers != null && loggers.size() > 0 )
				logger0User = loggers.get(0) ;
		}
			
		String assList = hr.getParameter(CorresConstants.CORR_ASSIGNEE_FIELD_NAME) ;
		if(null != assList && !assList.trim().equals(""))
		{
			assignees = APIUtil.toUserList(assList, false);
			
			if( assignees != null && assignees.size() > 0 )
				ass0User = assignees.get(0); 
		}
		
		String subList =  hr.getParameter(CorresConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
		if( null != subList )
		{
			subscribers = APIUtil.toUserList(subList,false) ;
		}
		
		String ccList = hr.getParameter(CorresConstants.CORR_CC_FIELD_NAME);
		if( null != ccList )
		{
			ccs = APIUtil.toUserList(ccList, false);
		}
		
		// types		
		String corrTypeName = hr.getParameter(CorresConstants.CORR_CORR_TYPE_FIELD_NAME) ;		
		if( null != corrTypeName )
		{
			try {
				corrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_CORR_TYPE_FIELD_NAME, corrTypeName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Corr Type with name : " + corrTypeName ) ;
			}			
		}
		
		String locationName = hr.getParameter(CorresConstants.CORR_LOC_FIELD_NAME) ;		
		if( null != locationName )
		{
			try {
				location = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_LOC_FIELD_NAME, locationName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Location with name : " + locationName ) ;
			}			
		}
		
		String orgName = hr.getParameter(CorresConstants.CORR_ORIGINATOR_FIELD_NAME);
		if( null != orgName )
		{
			try {
				originator = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_ORIGINATOR_FIELD_NAME, orgName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Originator with name : " + orgName ) ;
			}	
		}
	
		String recName = hr.getParameter(CorresConstants.CORR_RECEPIENT_FIELD_NAME);
		if( null != recName )
		{
			try {
				recepient = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_RECEPIENT_FIELD_NAME, recName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Recepient with name : " + recName ) ;
			}	
		}
		
		String protName = hr.getParameter(CorresConstants.CORR_PROTOCOL_APPLICABLE_FIELD_NAME);
		if( null != protName )
		{
			try {
				protApp = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_PROTOCOL_APPLICABLE_FIELD_NAME, protName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Protocol Applicable with name : " + protName ) ;
			}	
		}
		
		String contRefName = hr.getParameter(CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME);
		if( null != contRefName )
		{
			try {
				contractReference = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME, contRefName);
			} catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access ContractReference with name : " + contRefName ) ;
			}	
		}
		
		String genName = hr.getParameter(CorresConstants.CORR_GENERATE_FIELD_NAME);
		if( null != genName )
		{
			try
			{
				generate = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_GENERATE_FIELD_NAME, genName);
			}catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Generate with name : " + genName ) ;
			}
		}
		
		String packName = hr.getParameter(CorresConstants.CORR_PACKAGE_FIELD_NAME);
		if( null != packName )
		{
			try
			{
				pack = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_PACKAGE_FIELD_NAME, packName);
			}catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Package with name : " + packName ) ;
			}
		}
		
		String genAttName = hr.getParameter(CorresConstants.CORR_GENATT_FIELD_NAME);
		if( null != genAttName )
		{
			try
			{
				genAtt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_GENATT_FIELD_NAME, genAttName);
			}catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access " + cfdn(CORR_GENATT_FIELD_NAME) +" with name : " + genAttName ) ;
			}
		}
		
		String wbsAttName = hr.getParameter(CorresConstants.CORR_WBSATT_FIELD_NAME);
		if( null != wbsAttName )
		{
			try
			{
				wbsAtt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_WBSATT_FIELD_NAME, wbsAttName);
			}catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access " + cfdn(CORR_WBSATT_FIELD_NAME) + " with name : " + wbsAttName ) ;
			}
		}
		
		String discName = hr.getParameter(CorresConstants.CORR_DISCIPLINE_FIELD_NAME);
		if( null != discName )
		{
			try
			{
				discipline = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_DISCIPLINE_FIELD_NAME, discName);
			}catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access " + cfdn(CORR_DISCIPLINE_FIELD_NAME) + " with name : " + discName ) ;
			}
		}
		
		// strings
		description = hr.getParameter(CorresConstants.CORR_DESCRIPTION_FIELD_NAME);
		try {
			description = WebUtil.prepareValidHtml(description);
		} catch (IOException e) {
			LOG.info("Exception occurred while preparing valid html for description.");
			e.printStackTrace();
		} 
		subject = hr.getParameter(CorresConstants.CORR_SUBJECT_FIELD_NAME);
		corrNo = hr.getParameter(CorresConstants.CORR_CORRESPONDENCE_NUMBER_FIELD);
		linkedRequests = hr.getParameter(CorresConstants.CORR_LINKED_REQUESTS_FIELD_NAME); // billing
		tempFieldBilling = hr.getParameter(CorresConstants.CORR_TEMP_FIELD_BILLING); //billing
		
		// attachments 
		String attachments = hr.getParameter(Field.ATTACHMENTS) ;
		if( null == attachments )
			attachments = "[]" ;
//			LOG.info("The attachments are: " + attachments);
    	JsonParser jp = new JsonParser();
    	JsonObject mainObj = jp.parse(attachments).getAsJsonObject();
    	Set<Entry<String, JsonElement>> mainNode = mainObj.entrySet();
    	Iterator<Entry<String, JsonElement>> iter = mainNode.iterator();
    	while(iter.hasNext())
    	{
    		Entry<String, JsonElement> element = iter.next();
    		JsonElement filesElement = element.getValue().getAsJsonObject().get("files");
    		Collection<AttachmentInfo> attachInfos = AttachmentInfo.fromJson(filesElement.toString());
    		if( element.getKey().equals(CorresConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME))
    		{
    			otherAttach = attachInfos ; 
    		}        		
    		else if( element.getKey().equals(CorresConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME))
    		{
    			corrAttach = attachInfos ; 
    		}        		
    	}	
	}
	
	public CorresObject( Request req ) throws TBitsException
	{
		// ba
		String sys_id = req.get(Field.BUSINESS_AREA) ;
		if( null == sys_id )
			throw new TBitsException("Illegal BusinessArea (sys_id cannot be null)");
		int sysId = 0 ;
		
		try
		{
			sysId = Integer.parseInt(sys_id) ;
		}
		catch(NumberFormatException nfe)
		{
			throw new TBitsException("Illegal BusinessArea : sys_id = " + sys_id );
		}
		
		try {
			ba = BusinessArea.lookupBySystemId(sysId) ;
		} catch (DatabaseException e1) {			
			e1.printStackTrace();
			throw new TBitsException("Cannot access BusinessArea with sys_Id :" + sysId ) ;
		}
		
		if(null == ba || null == ba.getSystemPrefix() || !ba.getSystemPrefix().equalsIgnoreCase(CorresConstants.CORR_SYSPREFIX))
			throw new TBitsException("Illegal BusinessArea with sys_id : " + sys_id ) ;
		
		try {
			loginUser = User.lookupByUserId(req.getUserId()) ;
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if( null == loginUser )
			throw new TBitsException("Illegal Login User.") ;
		
		String loggerList = req.get(CorresConstants.CORR_LOGGER_FIELD_NAME) ;
		if( null != loggerList && !loggerList.trim().equals(""))	
		{
			loggers = APIUtil.toUserList(loggerList, false);
			
			if( loggers != null && loggers.size() > 0 )
				logger0User = loggers.get(0) ;
		}
			
		String assList = req.get(CorresConstants.CORR_ASSIGNEE_FIELD_NAME) ;
		if(null != assList && !assList.trim().equals(""))
		{
			assignees = APIUtil.toUserList(assList, false);
			
			if( assignees != null && assignees.size() > 0 )
				ass0User = assignees.get(0); 
		}
		
		String subList =  req.get(CorresConstants.CORR_SUBSCRIBER_FIELD_NAME) ;
		if( null != subList )
		{
			subscribers = APIUtil.toUserList(subList,false) ;
		}
		
		String ccList = req.get(CorresConstants.CORR_CC_FIELD_NAME);
		if( null != ccList )
			ccs = APIUtil.toUserList(ccList, false);
		
		// types	
		try
		{
			String corrTypeName = req.get(CorresConstants.CORR_CORR_TYPE_FIELD_NAME) ;		
			if( null != corrTypeName )
			{
				try {
					corrType = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_CORR_TYPE_FIELD_NAME, corrTypeName);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access Corr Type with name : " + corrTypeName ) ;
				}			
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage()	);
		}
		
		try
		{
			String orgName = req.get(CorresConstants.CORR_ORIGINATOR_FIELD_NAME);
			if( null != orgName )
			{
				try {
					originator = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_ORIGINATOR_FIELD_NAME, orgName);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access Originator with name : " + orgName ) ;
				}	
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
	
		try
		{
			String recName = req.get(CorresConstants.CORR_RECEPIENT_FIELD_NAME);
			if( null != recName )
			{
				try {
					recepient = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_RECEPIENT_FIELD_NAME, recName);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access Recepient with name : " + recName ) ;
				}	
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		try
		{
			String protName = req.get(CorresConstants.CORR_PROTOCOL_APPLICABLE_FIELD_NAME);
			if( null != protName )
			{
				try {
					protApp = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_PROTOCOL_APPLICABLE_FIELD_NAME, protName);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access Protocol Applicable with name : " + protName ) ;
				}	
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		try
		{
			String contRefName = req.get(CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME);
			if( null != contRefName )
			{
				try {
					contractReference = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_CONTRACT_REFERENCE_FIELD_NAME, contRefName);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access ContractReference with name : " + contRefName ) ;
				}	
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		try
		{
			String locationName = req.get(CorresConstants.CORR_LOC_FIELD_NAME) ;		
			if( null != locationName )
			{
				try {
					location = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_LOC_FIELD_NAME, locationName);
				} catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access Location with name : " + locationName ) ;
				}			
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		String genName = req.get(CorresConstants.CORR_GENERATE_FIELD_NAME);
		if( null != genName )
		{
			try
			{
				generate = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_GENERATE_FIELD_NAME, genName);
			}catch (DatabaseException e) {
				e.printStackTrace();
				throw new TBitsException("Cannot access Generate with name : " + genName ) ;
			}
		}
		
		try
		{
			String packName = req.get(CorresConstants.CORR_PACKAGE_FIELD_NAME);
			if( null != packName )
			{
				try
				{
					pack = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_PACKAGE_FIELD_NAME, packName);
				}catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access Package with name : " + packName ) ;
				}
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		try
		{
			String genAttName = req.get(CorresConstants.CORR_GENATT_FIELD_NAME);
			if( null != genAttName )
			{
				try
				{
					genAtt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_GENATT_FIELD_NAME, genAttName);
				}catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access " + cfdn(CORR_GENATT_FIELD_NAME) +" with name : " + genAttName ) ;
				}
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		try
		{
			String wbsAttName = req.get(CorresConstants.CORR_WBSATT_FIELD_NAME);
			if( null != wbsAttName )
			{
				try
				{
					wbsAtt = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_WBSATT_FIELD_NAME, wbsAttName);
				}catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access " + cfdn(CORR_WBSATT_FIELD_NAME) + " with name : " + wbsAttName ) ;
				}
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e);
		}
		
		try
		{
			String discName = req.get(CorresConstants.CORR_DISCIPLINE_FIELD_NAME);
			if( null != discName )
			{
				try
				{
					discipline = Type.lookupAllBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), CorresConstants.CORR_DISCIPLINE_FIELD_NAME, discName);
				}catch (DatabaseException e) {
					e.printStackTrace();
					throw new TBitsException("Cannot access " + cfdn(CORR_DISCIPLINE_FIELD_NAME) + " with name : " + discName ) ;
				}
			}
		}
		catch(IllegalArgumentException e)
		{
			LOG.info("Suppressed Exception : " + e.getMessage());
		}
		
		
		// strings
		description = req.get(CorresConstants.CORR_DESCRIPTION_FIELD_NAME);
		subject = req.get(CorresConstants.CORR_SUBJECT_FIELD_NAME);
		corrNo = req.get(CorresConstants.CORR_CORRESPONDENCE_NUMBER_FIELD);
		linkedRequests = req.get(CorresConstants.CORR_LINKED_REQUESTS_FIELD_NAME); // billing
		tempFieldBilling = req.get(CorresConstants.CORR_TEMP_FIELD_BILLING); //billing
		
		String cai = req.get(CorresConstants.CORR_CORRESPONDANCE_FILE_FIELD_NAME);
		if( cai != null )
			corrAttach = AttachmentInfo.fromJson(cai);
		
		String oai = req.get(CorresConstants.CORR_OTHER_ATTACHMENTS_FIELD_NAME) ;
		if( oai != null )
			otherAttach = AttachmentInfo.fromJson(oai);
		
	}
	
	public static void validate( boolean isAddRequest, CorresObject currCo ,CorresObject oldCo ) throws TBitsException
	{
		if( null == currCo.loginUser || null == currCo.loginUser.getFirmCode() || currCo.loginUser.getFirmCode().trim().equals(""))
			throw new TBitsException("Illegal Login User.") ;
		
		if( null == currCo.logger0User || null == currCo.logger0User.getFirmCode() || currCo.logger0User.getFirmCode().trim().equals("") )
			throw new TBitsException("Illegal " + cfdn(CORR_LOGGER_FIELD_NAME)) ;
		
		if( null == currCo.ass0User || null == currCo.ass0User.getFirmCode() || currCo.ass0User.getFirmCode().trim().equals(""))
			throw new TBitsException("Illegal " + cfdn(CORR_ASSIGNEE_FIELD_NAME)) ;
		
		if(null == currCo.corrType )
			throw new TBitsException("Illegal " + cfdn(CORR_CORR_TYPE_FIELD_NAME)) ;
		
		if(null == currCo.originator )
			throw new TBitsException("Illegal " + cfdn(CORR_ORIGINATOR_FIELD_NAME));
		
		if(null == currCo.recepient )
			throw new TBitsException("Illegal " + cfdn(CORR_RECEPIENT_FIELD_NAME));
		
		if( null == currCo.protApp )
			throw new TBitsException("Illegal " + cfdn(CORR_PROTOCOL_APPLICABLE_FIELD_NAME));
		
		if( null == currCo.contractReference )
			throw new TBitsException("Illegal " + cfdn(CORR_CONTRACT_REFERENCE_FIELD_NAME));
		
		if( null == currCo.generate )
			throw new TBitsException("Illegal value in " + cfdn(CORR_GENERATE_FIELD_NAME));	
		
		// protocol constraints
//		checkProtocolConstraints( caller,  currCo,  oldCo ); 
	}

	public static void checkProtocolConstraints(boolean isAddRequest, CorresObject currCo, CorresObject oldCo) throws TBitsException 
	{
		if( null == currCo.loggers || currCo.loggers.size() > 1 )
			throw new TBitsException("Exactly one " + cfdn(CORR_LOGGER_FIELD_NAME) + " is allowed.");
		
		if( !currCo.logger0User.getFirmCode().equals(currCo.originator.getName()) )
			throw new TBitsException( cfdn(CORR_ORIGINATOR_FIELD_NAME) + " was not set properly." );
		
		if( !currCo.ass0User.getFirmCode().equals(currCo.recepient.getName()))
			throw new TBitsException( cfdn(CORR_RECEPIENT_FIELD_NAME) + " was not set properly." );
				
		if( currCo.corrType.getName().equals(CORR_CORR_TYPE_NONE))
			throw new TBitsException("You must select one of the valid values in " + cfdn(CORR_CORR_TYPE_FIELD_NAME));		
	}
	
	public static void agencySpecificConstraints(boolean isAddRequest, CorresObject currCo, CorresObject oldCo) throws TBitsException
	{
		// for all agencies. The login_user is only allowed to log on behalf of himself.
		// unless there is a mapping of user_id,logger_id in the corres_map table of db.
		
		if( ! currCo.loginUser.equals(currCo.logger0User) )
		{
			ArrayList<User> al = CorresMapManager.getCorresAllowedLogger(currCo.loginUser);
			if( null == al || al.size() == 0 )
				throw new TBitsException("You (" + currCo.loginUser.getUserLogin() + ")" + " are only allowed to log on behalf of your self.") ;
			
			boolean found = false ;
			for( User lg : al )
			{
				if( lg.equals(currCo.logger0User))
				{
					found = true ;
					break;
				}
			}
			
			if( found == false )
				throw new TBitsException("You (" + currCo.loginUser.getUserLogin() + ") are not allowed to log on behalf of (" + currCo.logger0User.getUserLogin() + ")");
		}
		
		if(isAddRequest == false  && oldCo.corrType.getName().equals(CORR_CORR_TYPE_ION) && !currCo.corrType.getName().equals(CORR_CORR_TYPE_ION))
		{
			throw new TBitsException("It is not allowed to change the " + cfdn(CORR_CORR_TYPE_FIELD_NAME) + " to other than " + ctdn(CORR_CORR_TYPE_FIELD_NAME,CORR_CORR_TYPE_ION) + " once it is set.");			
		}
		
		if( currCo.corrType.getName().equals(CORR_CORR_TYPE_ION))
		{			
			if( isAddRequest == false && !oldCo.corrType.getName().equals(CORR_CORR_TYPE_ION) )
			{
				throw new TBitsException("You are not allowed to change the " + cfdn(CORR_CORR_TYPE_FIELD_NAME) + " to " + ctdn(CORR_CORR_TYPE_FIELD_NAME,CORR_CORR_TYPE_ION) + " in update request.");
			}
			
			if( !CorresConstants.isValidIONUser(currCo.loginUser) )
			{			
				throw new TBitsException("You are not allowed to create a correspondence with " + cfdn(CORR_CORR_TYPE_FIELD_NAME) + " set to " + ctdn(CORR_CORR_TYPE_FIELD_NAME,CORR_CORR_TYPE_ION)) ;
			}
			
			// validate all users must be of NCCB / NCCP
			ArrayList<User> illegalUsers = new ArrayList<User>() ;
			getNonNCCUsers(currCo.assignees, illegalUsers);
			getNonNCCUsers(currCo.subscribers,illegalUsers);
			getNonNCCUsers(currCo.ccs,illegalUsers);
			getNonNCCUsers(currCo.loggers,illegalUsers);
			
			if( illegalUsers.size() != 0 )
			{
				throw new TBitsException("Following users are not " + VALID_ION_AGENCY + " members and are not allowed when " + cfdn(CORR_CORR_TYPE_FIELD_NAME) + " is set to " + ctdn(CORR_CORR_TYPE_FIELD_NAME,CORR_CORR_TYPE_ION) + "<br />" + getUserList(illegalUsers) );
			}			
		}
		// KNPL specific fields check
		if(currCo.logger0User.getFirmCode().equals(CORR_ORIG_KNPL))
		{
			if( currCo.pack == null || currCo.pack.getName().equals(CORR_PACK_NONE) )
				throw new TBitsException("You must fill the " + cfdn(CORR_PACKAGE_FIELD_NAME) + " field.");
		}
		
		if( currCo.logger0User.getFirmCode().equals(CORR_ORIG_KNPL) || currCo.logger0User.getFirmCode().equals(CORR_ORIG_NCCB) || currCo.logger0User.getFirmCode().equals(CORR_ORIG_NCCP))
		{
			if( currCo.genAtt == null || currCo.genAtt.getName().equals(CORR_GENATT_NONE))
				throw new TBitsException("You must fill one of the valid values in " + cfdn(CORR_GENATT_FIELD_NAME));
			
			if( currCo.wbsAtt == null || currCo.wbsAtt.getName().equals(CORR_WBSATT_NONE) )
				throw new TBitsException("You must fill one of the valid values in " + cfdn(CORR_WBSATT_FIELD_NAME));				
		}
		
		if( currCo.logger0User.getFirmCode().equals(CORR_ORIG_DCPL))
		{			
			if( currCo.discipline == null || currCo.discipline.getName().equals(CORR_DISC_NONE))
				throw new TBitsException("You must fill one of the valid values in " + cfdn(CORR_DISCIPLINE_FIELD_NAME));
		}
		
		if( currCo.logger0User.getFirmCode().equals(CORR_ORIG_DESEIN))
		{
			if( currCo.location == null )
				throw new TBitsException("You must fill on of the valid values in " + cfdn(CORR_LOC_FIELD_NAME));
		}
	}

	public static void getNonNCCUsers(ArrayList<User> users, ArrayList<User> illegalUsers) 
	{
		for( User user : users )
		{
			if( !CorresConstants.isValidIONUser(user) )
			{
				illegalUsers.add(user);
			}
		}
	}
}
