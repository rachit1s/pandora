package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import lntCorr.others.LnTConst;
//import lntCorr.others.LnTManager;

import org.pdfbox.encryption.ARCFour;

import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class ReadAccess implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			
			String appBAs = PropertiesHandler.getProperty(LnTConst.CorrBaList);
			
			if( null == appBAs )
			{
				LOG.info("Property not found : property_name = " + LnTConst.CorrBaList + " in tbits_properties.");
				return new RuleResult(true,"Property not found : property_name = " + LnTConst.CorrBaList + " in tbits_properties.",true);
			}
			
			ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);
			
			if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
			
			String readAccessFN = "ReadAccess";
			String loggerFN = Field.LOGGER;
			String assigneeFN = Field.ASSIGNEE;
			String forInfoFN = "internal_cc" ;
			String subscriberFN = Field.SUBSCRIBER;
			
	//		ArrayList<RequestUser> rus = new ArrayList<RequestUser>();
			
			HashSet<User> raUsers = new HashSet<User>();
			
			Field field = null;
			try {
				field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), readAccessFN);
			} catch (DatabaseException e) {
				LOG.error(TBitsLogger.getStackTrace(e));
			}
			
			if( null != field )
			{
				String typeFieldName = "severity_id" ;
				String typeField = currentRequest.get(typeFieldName);
				if( null != typeField )
				{
					if( !typeField.equals("CM") && !typeField.equals("Contracts"))
					{
						String mailingList = null;
						ProtocolOptionEntry poe = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), LnTConst.ReadAccessMailList);
						if( null != poe )
							mailingList = poe.getValue();
						
						if( null != mailingList )
						{
							User readUser = null;
							try 
							{
								readUser = User.lookupByUserLogin(mailingList);
							} catch (DatabaseException e) {
								
								e.printStackTrace();
								return new RuleResult(false,"Cannot find user with login " + mailingList, false );
							}
							if( null != readUser )
							{
								raUsers.add(readUser);
								LOG.info("User with login : " + mailingList + " added successfuly.");
							}
						}
					}
				}
				
				if( isAddRequest == false ) // add diff of users from various field into ReadAccess
				{
					ArrayList<String> fieldNames = new ArrayList<String>();
					fieldNames.add(loggerFN);
					fieldNames.add(assigneeFN);
					fieldNames.add(forInfoFN);
					fieldNames.add(subscriberFN);
					
					for( String fieldName : fieldNames )
					{
						Collection<User> removedUsers = getRemovedUsers(currentRequest,oldRequest,fieldName);
						if( null != removedUsers )
							raUsers.addAll(removedUsers);
					}
				}
				
				// now add them in ReadAccessField ( also include any user already in ReadAccess)
				Collection<RequestUser> currRAUser = (Collection<RequestUser>) currentRequest.getObject(field);
				if( null != currRAUser )
				{
					Collection<User> currUsers = Utility.getUsersFromRequestUser(currRAUser);
					if( null != currUsers )
						raUsers.addAll(currUsers);
				}
				
				ArrayList<RequestUser> raRUs = new ArrayList<RequestUser>();
				
				int order = 1 ;
				for( User u : raUsers )
				{
					if(u.getIsActive() == false )
						continue ;
					RequestUser ru = new RequestUser(currentRequest.getSystemId(), currentRequest.getRequestId(), u.getUserId(), order++, false, field.getFieldId());
					raRUs.add(ru);
				}
				
				currentRequest.setObject(field, raRUs);
			}
			else
			{
				return new RuleResult(false,"Cannot find field with name " + readAccessFN, false );
			}
			return new RuleResult(true,"Rule executed successfully.",true);
		}
		catch(Exception te)
		{
			te.printStackTrace();
			LOG.error(TBitsLogger.getStackTrace(te));
			return new RuleResult(false,"Exception occured while Running the ReadAccess Rule : Error Message : " + te.getMessage(), false);
		}
	}

	private Collection<User> getRemovedUsers(Request currentRequest,
			Request oldRequest, String fieldName) throws DatabaseException 
	{
		Field field = null ;

		field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
		
		Collection<User> removedUsers = null; 
		if( field != null )
		{
			Collection<RequestUser> arus = (Collection<RequestUser>) currentRequest.getObject(fieldName);
			Collection<RequestUser> brus = (Collection<RequestUser>) oldRequest.getObject(fieldName);
			
			Collection<User> a = Utility.getUsersFromRequestUser(arus);
			Collection<User> b = Utility.getUsersFromRequestUser(brus);
		
			removedUsers = Utility.getExtraInB(a, b);
		}
		
		return removedUsers;
	}

	public String getName() 
	{
		return "Add mailing list and removed users of other fields to the read-access field";
	}

	public double getSequence() {
		return 9;
	}

}
