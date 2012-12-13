package lntCorr.rule;

import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.DisableProtocolFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.DisableProtocol_True;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_NoPdforCorrNumber;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorrespondenceFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerationAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.LoggerFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalf1_Other;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalf2_Other;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.OnBehalf3_Other;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientAgencyFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.RecepientUserTypeFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMap1_Other;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMap2_Other;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMap3_Other;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType1;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType2;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.UserMapType3;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import lntCorr.others.LnTConst;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.OnBehalfManager;
import corrGeneric.com.tbitsGlobal.server.managers.UserMapManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.OnBehalfEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.UserMapEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

/**
 * 
 * @author nitiraj
 *
 */
public class CorrEmailPreRule implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");

	private OnBehalfEntry getUniqueProtocol(ArrayList<OnBehalfEntry> validProtocol,BusinessArea ba, Request currentRequest,Hashtable<String, FieldNameEntry> fieldMap, User user) throws DatabaseException 
	{
		String obt1 = null, default1 = null;
		String obt2 = null, default2 = null;
		String obt3 = null, default3 = null;
		
		{
			FieldNameEntry fieldEntry = fieldMap.get(OnBehalfType3);
			if( null != fieldEntry )
			{
				Type defaultType = Type.getDefaultTypeBySystemIdAndFieldName(ba.getSystemId(), fieldEntry.getBaFieldName());
				if( null != defaultType)
				{
					default3 = defaultType.getName();
				}
			}
		}
		{
			FieldNameEntry fieldEntry = fieldMap.get(OnBehalfType2);
			if( null != fieldEntry )
			{
				Type defaultType = Type.getDefaultTypeBySystemIdAndFieldName(ba.getSystemId(), fieldEntry.getBaFieldName());
				if( null != defaultType)
				{
					default2 = defaultType.getName();
				}
			}
		}
		
		{
			FieldNameEntry fieldEntry = fieldMap.get(OnBehalfType1);
			if( null != fieldEntry )
			{
				Type defaultType = Type.getDefaultTypeBySystemIdAndFieldName(ba.getSystemId(), fieldEntry.getBaFieldName());
				if( null != defaultType)
				{
					default1 = defaultType.getName();
				}
			}
		}
		if( validProtocol == null || validProtocol.size() == 0 )
			return new OnBehalfEntry(-1, ba.getSystemPrefix(), user.getUserLogin(), default1,default2, default3,user.getUserLogin());
		// just return the first 
		if( validProtocol.size() == 1 )
			return validProtocol.get(0);
		
		// check onbehalf type1 configured for the Protocol Field
		OnBehalfEntry ob1 = validProtocol.get(0);
		obt1 = ob1.getType1();
		obt2 = ob1.getType2();
		obt3 = ob1.getType3();
		
		// check if all the protocol fields are same ? 
		for( OnBehalfEntry ob : validProtocol )
		{
			if( (null == ob.getType1() && null != ob1.getType1() ) || (null != ob.getType1() && null == ob1.getType1() ) || (null != ob.getType1() && null != ob1.getType1() && !ob.getType1().equals(ob1.getType1())))
			{
				obt1 = null;
			}
			
			if( (null == ob.getType2() && null != ob1.getType2() ) || (null != ob.getType2() && null == ob1.getType2() ) || null != ob.getType2() && null != ob1.getType2() && !ob.getType2().equals(ob1.getType2()))
			{
				obt2 = null;
			}
			
			if( (null == ob.getType3() && null != ob1.getType3() ) || (null != ob.getType3() && null == ob1.getType3() ) || (null != ob.getType3() && null != ob1.getType3() && !ob.getType3().equals(ob1.getType3())))
			{
				obt3 = null;
			}
		}

		if( obt1 == null )
		{
			return new OnBehalfEntry(-1,ba.getSystemPrefix(), user.getUserLogin(), default1,default2, default3, user.getUserLogin());
		}
		else
		if ( obt2 == null )
		{
			return new OnBehalfEntry(0, ba.getSystemPrefix(), user.getUserLogin(), obt1,default2, default3, user.getUserLogin());
		}
		else
		if( obt3 == null )
		{
			return new OnBehalfEntry(0, ba.getSystemPrefix(), user.getUserLogin(), obt1,obt2, default3, user.getUserLogin());
		}
		else
		{
			return new OnBehalfEntry(0, ba.getSystemPrefix(), user.getUserLogin(), obt1,obt2, obt3, user.getUserLogin());
		}
	}

//	private OnBehalfEntry findOnBehalfEntry(String obt1, String obt2, String obt3, ArrayList<OnBehalfEntry> validProtocol)
//	{
//		// find this entry
//		for( OnBehalfEntry ob : validProtocol )
//		{
//			if( (null == obt1 && null == ob.getType1()) || (null != ob.getType1() && null != obt1 && ob.getType1().equals(obt1)))
//			{
//				if( (null == obt2 && null == ob.getType2()) || (null != ob.getType2() && null != obt2 && ob.getType2().equals(obt2)))
//				{
//					if( (null == obt3 && null == ob.getType3()) || (null != ob.getType3() && null != obt3 && ob.getType3().equals(obt3)))
//					{
//						return ob;
//					}
//				}
//			}
//		}		
//		
//		return null;
//	}
//	private boolean setThisProtocol(BusinessArea ba ,Request currentRequest, OnBehalfEntry validProtocol,
//			Hashtable<String, FieldNameEntry> fieldMap, User loginUser) throws DatabaseException, CorrException 
//	{
//		if( null != fieldMap.get(GenericParams.OnBehalfType1) && validProtocol.getType1() != null )
//		{
//			String fieldName1 = fieldMap.get(GenericParams.OnBehalfType1).getBaFieldName();
//			Type type1 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName1, validProtocol.getType1());
//
//			if( null != type1 )
//				currentRequest.setObject(fieldName1, type1);
//		}
//		
//		if( null != fieldMap.get(GenericParams.OnBehalfType2) && validProtocol.getType2() != null )
//		{
//			String fieldName2 = fieldMap.get(GenericParams.OnBehalfType2).getBaFieldName();
//			Type type2 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName2, validProtocol.getType2());
//
//			if( null != type2 )
//				currentRequest.setObject(fieldName2, type2);
//		}
//		
//		if( null != fieldMap.get(GenericParams.OnBehalfType3) && validProtocol.getType3() != null )
//		{
//			String fieldName3 = fieldMap.get(GenericParams.OnBehalfType3).getBaFieldName();
//			Type type3 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName3, validProtocol.getType3());
//
//			if( null != type3 )
//				currentRequest.setObject(fieldName3, type3);
//		}
//		
//		String loggerLogin = validProtocol.getOnBehalfUser();
//		User loggerUser = User.lookupByUserLogin(loggerLogin);
//		
//		ArrayList<UserMapEntry> userMap = UserMapManager.lookupUserMap(ba.getSystemPrefix(), loggerUser.getUserLogin());
//		
//		if( null != userMap && userMap.size() > 0 )
//		{
//			boolean foundDiff = false;
//			UserMapEntry ob = userMap.get(0);
//			for( int i = 1 ; i < userMap.size() ; i++ )
//			{
//				boolean wasSame = false;
//				UserMapEntry ume = userMap.get(i);
//				
//				if( (ume.getType1() == null && ob.getType1() == null) || ( null != ume.getType1() && null != ob.getType1() && ume.getType1().equals(ob.getType1()) ) )
//				{
//					if( (ume.getType2() == null && ob.getType2() == null) || ( null != ume.getType2() && null != ob.getType2() && ume.getType2().equals(ob.getType2()) ) )
//					{
//						if( (ume.getType3() == null && ob.getType3() == null) || ( null != ume.getType3() && null != ob.getType3() && ume.getType3().equals(ob.getType3()) ) )
//						{
//							wasSame = true;
//						}
//					}
//				}
//			
//				if( wasSame == false )
//				{
//					foundDiff = true ;
//					break;
//				}
//			}
//		
//			
//			
//			if( foundDiff == true )
//			{
//				// find the current state of usermap_type1 2 and 3 and remove from user map which are not equal to these
//				String t1 = null;
//				String t2 = null ;
//				String t3 = null ;
//				FieldNameEntry t1fne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), UserMapType1);
//				FieldNameEntry t2fne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), UserMapType2);
//				FieldNameEntry t3fne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), UserMapType3);
//				
//				if( null != t1fne )
//					t1 = currentRequest.get(t1fne.getBaFieldName());
//				
//				if( null != t2fne )
//					t2 = currentRequest.get(t2fne.getBaFieldName());
//				
//				if( null != t3fne )
//					t3 = currentRequest.get(t3fne.getBaFieldName());
//				
//				for( Iterator<UserMapEntry> iter = userMap.iterator() ; iter.hasNext() ;)
//				{
//					UserMapEntry ume = iter.next();
//					if( 
//							//  negate equality to get inequality
//						!(
//								// check equality
//						   ( ( t1 == null && ume.getType1() == null ) || ( t1 != null && ume.getType1() != null && t1.equals(ume.getType1()) ) )
//						&& ( ( t2 == null && ume.getType2() == null ) || ( t2 != null && ume.getType2() != null && t2.equals(ume.getType2()) ) )
//						&& ( ( t3 == null && ume.getType3() == null ) || ( t3 != null && ume.getType3() != null && t3.equals(ume.getType3()) ) )
//						)
//					  ) 
//					  {
//						 // if given user-map does not contain values as per the values in the request then remove it
//						iter.remove();
//					  }
//				}
//			}
//			
//			Hashtable<String,ArrayList<RequestUser>> fieldValueMap = new Hashtable<String, ArrayList<RequestUser>>();
//			for( int i = 0 ; i < userMap.size() ; i++ )
//			{
//				UserMapEntry ume = userMap.get(i);
//				
//				String userLogin = ume.getUserLoginValue();
//				if( null == userLogin || userLogin.trim().equals("") )
//					continue;
//				
//				String fieldName = ume.getUserTypeFieldName();
//				Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
//				if( null == field )
//				{
//					LOG.error("Cannot find field with name : " + fieldName + ". Hence ignoring.");
//					continue ;
//				}
//	
//				User user = null ;
//				try
//				{
//					user = User.lookupByUserLogin(userLogin);
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//					LOG.info("Exception occurred while finding user with login : " + userLogin );
//					continue;
//				}
//				
//				if( null != user )
//				{
//					RequestUser ru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), user.getUserId(), 0, false, field.getFieldId());
//					ArrayList<RequestUser> rus = fieldValueMap.get(ume.getUserTypeFieldName());
//					if( null == rus )
//						rus = new ArrayList<RequestUser>();
//					
//					rus.add(ru);
//					fieldValueMap.put(ume.getUserTypeFieldName(), rus);
//				}
//				
//			}
//			
//			for( Enumeration<String> fieldNames = fieldValueMap.keys() ; fieldNames.hasMoreElements() ;)
//			{
//				String fieldName = fieldNames.nextElement() ;
//				ArrayList<RequestUser> users = fieldValueMap.get(fieldName);
//				if( null != users )
//				{
//					Collection<RequestUser> oldUsers = (Collection<RequestUser>) currentRequest.getObject(fieldName);
//					if( null != oldUsers )
//					{
//						for(Iterator<RequestUser> iter = users.iterator() ; iter.hasNext() ; )
//						{
//							RequestUser ru = iter.next() ;
//							for( Iterator<RequestUser> oiter = oldUsers.iterator() ; oiter.hasNext() ;)
//							{
//								RequestUser oru = oiter.next() ;
//								if( ru.getUserId() == oru.getUserId() && ru.getFieldId() == oru.getFieldId() )
//								{
//									iter.remove();
//									break;
//								}
//							}
//						}
//						
//						users.addAll(oldUsers);
//					}
//					
//					currentRequest.setObject(fieldName, users);
//				}
//			}
//			
//			if( userMap.size() > 0 )
//			{
//				UserMapEntry ume = userMap.get(0);
//				FieldNameEntry umtype1fne = fieldMap.get(GenericParams.UserMapType1);
//				if( null != umtype1fne )
//				{
//					String fn = umtype1fne.getBaFieldName();
//					Type type1 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fn, ume.getType1());
//					if( null != type1 )
//						currentRequest.setObject(fn, type1);
//				}
//				
//				FieldNameEntry umtype2fne = fieldMap.get(GenericParams.UserMapType2);
//				if( null != umtype2fne )
//				{
//					String fn = umtype2fne.getBaFieldName();
//					Type type2 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fn, ume.getType2());
//					if( null != type2 )
//						currentRequest.setObject(fn, type2);
//				}
//				
//				FieldNameEntry umtype3fne = fieldMap.get(GenericParams.UserMapType3);
//				if( null != umtype3fne )
//				{
//					String fn = umtype3fne.getBaFieldName();
//					Type type3 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fn, ume.getType3());
//					if( null != type3 )
//						currentRequest.setObject(fn, type3);
//				}
//			}
//		}
//		
//		// set the logger
//		FieldNameEntry loggerFne = fieldMap.get(LoggerFieldName);
//		if( null != loggerFne )
//		{
//			String fieldName = loggerFne.getBaFieldName();
//			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
//			if( null != field )
//			{
//				RequestUser ru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), loggerUser.getUserId(), 0, false, field.getFieldId());
//				ArrayList<RequestUser> rus = new ArrayList<RequestUser>();
//				rus.add(ru);
//				currentRequest.setObject(field, rus);
//				
//				FieldNameEntry genAgen = fieldMap.get(GenerationAgencyFieldName);
//				if( null != genAgen )
//				{
//					String agenName = loggerUser.getLocation();
//					if( null != agenName )
//					{
//						Type genType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), genAgen.getBaFieldName(), agenName);
//						if( null != genType )
//						{
//							currentRequest.setObject(genAgen.getBaFieldName(), genType);
//						}
//					}
//				}
//			}
//		}
//		
//		FieldNameEntry recAgen = fieldMap.get(RecepientAgencyFieldName);
//		FieldNameEntry recUserTypeFne = fieldMap.get(RecepientUserTypeFieldName);
//		if( null != recAgen && null != recUserTypeFne )
//		{
//			Collection<RequestUser> recUsers = (Collection<RequestUser>) currentRequest.getObject(recUserTypeFne.getBaFieldName());
//			if( null != recUsers && recUsers.size() != 0 )
//			{
//				ArrayList<RequestUser> arrayList = new ArrayList<RequestUser>(recUsers); 
//				Collections.sort(arrayList, new Comparator<RequestUser>() {
//
//					public int compare(RequestUser arg0, RequestUser arg1) 
//					{
//						if( arg0.getOrdering() < arg1.getOrdering() )
//							return -1 ;
//						if( arg0.getOrdering() > arg1.getOrdering() )
//							return 1 ;
//						return 0;
//					}
//				});
//				
//				RequestUser firstru = arrayList.get(0);
//				User firstUser = User.lookupByUserId(firstru.getUserId());
//				String agency = firstUser.getLocation();
//				if( null != agency )
//				{
//					Type recAgenType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), recAgen.getBaFieldName(), agency);
//					if( null != recAgenType )
//					{
//						currentRequest.setObject(recAgen.getBaFieldName(), recAgenType);
//					}
//				}
//			}
//		}
//		
//		return true;
//	}

//	private boole
	public String getName() {
		return "It refactors the requests received from email for corr-bas";
	}

	public double getSequence() {
		return 1;
	}

	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if( currentRequest.getAppendInterface() != TBitsConstants.SOURCE_EMAIL  )
				return new RuleResult(true, "Aborting. As the rule is only valid for request from email.", true);
	
			String appBAs = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			if( null == appBAs )
			{
				LOG.info("Property not found : " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
				return new RuleResult(true,"Property not found : " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.",true);
			}
			
			ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);
			
			if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);

			String header = currentRequest.getHeaderDescription();
			if( null == header )
				header = "";
			
			Hashtable<String, FieldNameEntry> fieldMap = FieldNameManager.lookupFieldNameMap(ba.getSystemPrefix());
			if( null == fieldMap )
			{
				header += "\nNo field-map was configured for ba : " +ba.getSystemPrefix() + ". The protocol might be out of sync for this Correspondence.\n";
				currentRequest.setHeaderDescription(header);
				return new RuleResult(true,"No field-map was configured for ba : " +ba.getSystemPrefix(), true);
			}
			
			FieldNameEntry genCorrFne = fieldMap.get(GenerateCorrespondenceFieldName);
			FieldNameEntry disableProtFne = fieldMap.get(DisableProtocolFieldName);
			
			if( null != genCorrFne )
			{
				try
				{
					String genCorrFieldName = genCorrFne.getBaFieldName();
					Type genCorrValue = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), genCorrFieldName, GenerateCorr_NoPdforCorrNumber);
				
					if( null != genCorrValue )
						currentRequest.setObject(genCorrFieldName, genCorrValue);
				}
				catch(Exception e)
				{
					LOG.error(TBitsLogger.getStackTrace(e));
				}
			}

			if( null != disableProtFne )
			{
				try
				{
					Field disableProtField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), disableProtFne.getBaFieldName());
					if( null != disableProtField )
					{
						Type t = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), disableProtField.getName(), DisableProtocol_True);
						if( null != t)
							currentRequest.setObject(disableProtField.getName(),t);
					}
				}
				catch(Exception e)
				{
					LOG.error(TBitsLogger.getStackTrace(e));
				}
			}
		
			int userId = currentRequest.getUserId() ;
			User loginUser = User.lookupByUserId(userId);
			
			if( user.getUserTypeId() == UserType.INTERNAL_USER && !ba.getSystemPrefix().equals(LnTConst.IOM_SYSPREFIX) )
				return new RuleResult(false,"You (" + loginUser.getDisplayName() + ") are an internal user hence not allowed to add/update request from email interface for Business Area with sys_prefix = " + ba.getSystemPrefix() + ". Hence this request will not be added into the tBits system.",true );
			

			if( isAddRequest == false )
			{
				// set the category to previous value
				// this is done to nullify the effect of category id being set by the
				// ba_mail_accounts tables category_id
				// and implicitly assumes carry-over of the category_id
				currentRequest.setCategoryId(oldRequest.getCategoryId());
				
				return handleUpdateRequest(connection, ba, oldRequest, currentRequest, Source, loginUser, isAddRequest, fieldMap);
			}
			else
			{
				if( ba.getSystemPrefix().equals(LnTConst.IOM_SYSPREFIX))
					return new RuleResult(true,"Doing nothing if add-request in IOM BA.",true);
				// set the category to previous value
				// this is done to nullify the effect of category id being set by the
				// ba_mail_accounts tables category_id
				currentRequest.setCategoryId(Type.getDefaultTypeBySystemIdAndFieldName(ba.getSystemId(), Field.CATEGORY));
				
				return handleAddRequest(connection,ba,oldRequest,currentRequest,Source,loginUser, isAddRequest,fieldMap);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String header = currentRequest.getHeaderDescription();
			if( header == null )
				header = "" ;
			header += "\nSome error occured while running the email prerule. Please see the logs." ;
			currentRequest.setHeaderDescription(header);
			
			return new RuleResult(true,"Following exception occured in rule : " + e.getMessage(),false);
		}
			
	}

	private RuleResult handleAddRequest(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int source,
			User loginUser, boolean isAddRequest,
			Hashtable<String, FieldNameEntry> fieldMap) throws CorrException, DatabaseException 
	{
		String header = currentRequest.getHeaderDescription();
		if( null == header )
			header = "";
	
		ArrayList<OnBehalfEntry> onbehalfs = OnBehalfManager.lookupOnBehalfList(ba.getSystemPrefix(), loginUser.getUserLogin());
	
		OnBehalfEntry uniqueProtocol = getUniqueProtocol(onbehalfs, ba, currentRequest, fieldMap, loginUser);
		setTheGivenProtocolAndTypes(uniqueProtocol, ba, header, currentRequest, fieldMap);
		if( uniqueProtocol.getId() == -1 )
		{
			setUserTypesEmpty(header, ba, currentRequest);
		}
		else
		{
			setUserTypes(ba, currentRequest);
		}
		setGenerationAgency(ba, currentRequest, fieldMap);
		setRecepientAgency(ba, currentRequest, fieldMap);
		setOriginatorAgency(ba, currentRequest, fieldMap);
		
		return new RuleResult(true,"Rule executed successfully.", true);
	}

	private void setLocationType(String typeFieldName, String typeUserFieldName,BusinessArea ba, Request currentRequest, Hashtable<String, FieldNameEntry> fieldMap ) throws CorrException, DatabaseException
	{
		FieldNameEntry agenFieldEntry = fieldMap.get(typeFieldName);
		FieldNameEntry userFieldEntry = fieldMap.get(typeUserFieldName);
		if( null != agenFieldEntry && null != userFieldEntry)
		{
			// assuming only one logger.
			Collection<RequestUser> userList = (Collection<RequestUser>) currentRequest.getObject(userFieldEntry.getBaFieldName());
			if( null != userList && userList.size() > 0 )
			{
				ArrayList<RequestUser> arrayList = new ArrayList<RequestUser>(userList); 
				Collections.sort(arrayList, new Comparator<RequestUser>() {

					public int compare(RequestUser arg0, RequestUser arg1) 
					{
						if( arg0.getOrdering() < arg1.getOrdering() )
							return -1 ;
						if( arg0.getOrdering() > arg1.getOrdering() )
							return 1 ;
						return 0;
					}
				});
				
				RequestUser requestUser = arrayList.get(0);
				User user = User.lookupByUserId(requestUser.getUserId());
				if( null != user )
				{
					String agenName = user.getLocation();
					if( null != agenName )
					{
						Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), agenFieldEntry.getBaFieldName(), agenName);
						if( null != type )
						{
							currentRequest.setObject(agenFieldEntry.getBaFieldName(), type);
						}
					}
				}
			}
		}
	}
	
	private void setGenerationAgency(BusinessArea ba, Request currentRequest, Hashtable<String, FieldNameEntry> fieldMap ) throws CorrException, DatabaseException
	{
		setLocationType(GenerationAgencyFieldName,LoggerFieldName,ba,currentRequest,fieldMap);
	}
	
	private void setRecepientAgency(BusinessArea ba, Request currentRequest, Hashtable<String, FieldNameEntry> fieldMap ) throws CorrException, DatabaseException
	{
		setLocationType(RecepientAgencyFieldName,RecepientUserTypeFieldName,ba,currentRequest,fieldMap);
	}
	
	private void setOriginatorAgency(BusinessArea ba, Request currentRequest, Hashtable<String, FieldNameEntry> fieldMap ) throws CorrException, DatabaseException
	{
		setLocationType(GenericParams.OriginatorFieldName,LoggerFieldName,ba,currentRequest,fieldMap);
	}
	
	private RuleResult handleUpdateRequest(Connection connection,
			BusinessArea ba, Request oldRequest, Request currentRequest,
			int source, User loginUser, boolean isAddRequest,
			Hashtable<String, FieldNameEntry> fieldMap) throws CorrException, DatabaseException 
	{
		Collection<RequestUser> rLoggers = new ArrayList<RequestUser>();
		Collection<RequestUser> rAssignees = new ArrayList<RequestUser>();
		Collection<RequestUser> rSubscribers = new ArrayList<RequestUser>();

		//check if current logger is same as the previous logger
		ArrayList<User> loggers = getUserFromRequsers((Collection<RequestUser>) oldRequest.getObject(Field.LOGGER));
		ArrayList<User> assignees = getUserFromRequsers((Collection<RequestUser>)oldRequest.getObject(Field.ASSIGNEE));
		ArrayList<User> subs = getUserFromRequsers((Collection<RequestUser>)oldRequest.getObject(Field.SUBSCRIBER));
		
		ArrayList<User> allUsers = new ArrayList<User>();
		
		if( null != loggers )
		{
			allUsers.addAll(loggers);
		}
		if( null != assignees )
		{
			allUsers.addAll(assignees);
		}
		
		if( null != subs )
		{
			allUsers.addAll(subs);
		}
		
		if( !allUsers.contains(loginUser) )
		{
			return new RuleResult(false, "You(" + loginUser.getUserLogin() + ") are not allowed to update this request from email interface as you are not in the list of Loggers or Assignees or Subscribers for this request.", true);
		}
		
		if( null != loggers && !loggers.contains(loginUser))
		{
			ArrayList<User> nlogs = new ArrayList<User>();
			nlogs.add(loginUser);
			rLoggers.addAll(getRequestUsers(ba, currentRequest, Field.LOGGER, nlogs));
			
			rAssignees.addAll(getRequestUsers(ba, currentRequest, Field.ASSIGNEE,loggers));
			
			subs.addAll(assignees);
			
			subs.removeAll(nlogs);
			subs.removeAll(loggers);
			
			subs = getUnique(subs);
			
			rSubscribers.addAll(getRequestUsers(ba, currentRequest, Field.SUBSCRIBER, subs));
		}
		else
		{
			rAssignees.addAll(getRequestUsers(ba, currentRequest, Field.ASSIGNEE, assignees));
			if( loggers != null )
				rLoggers.addAll(getRequestUsers(ba, currentRequest, Field.LOGGER, loggers));
			else
			{
				ArrayList<User> nlogs = new ArrayList<User>();
				nlogs.add(loginUser);
				rLoggers.addAll(getRequestUsers(ba, currentRequest, Field.LOGGER, nlogs));
			}
			
			rSubscribers.addAll(getRequestUsers(ba, currentRequest, Field.SUBSCRIBER, subs));
		}
		
		currentRequest.setObject(Field.ASSIGNEE, rAssignees);
		currentRequest.setObject(Field.SUBSCRIBER, rSubscribers);
		currentRequest.setObject(Field.LOGGER, rLoggers);
		
		return new RuleResult(true,"update request handled properly : " ,true);
	}

	
	private ArrayList<User> getUnique(ArrayList<User> subs) 
	{
		ArrayList<User> nsubs = new ArrayList<User>();
		
		for( Iterator<User> iter = subs.iterator() ; iter.hasNext() ; )
		{
			User user = iter.next();
			boolean found = false;
			for( Iterator<User> niter = nsubs.iterator() ; niter.hasNext() ; )
			{
				User nuser = niter.next();
				if( nuser.equals(user) )
				{
					found = true;
					break;
				}
			}
			if( found == false )
				nsubs.add(user);
		}
		
		return nsubs;
	}

	private void setUserTypes(BusinessArea ba, Request currentRequest) throws CorrException, DatabaseException 
	{
		FieldNameEntry loggerFieldEntry = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), LoggerFieldName);
		Collection<RequestUser> loggers = (Collection<RequestUser>) currentRequest.getObject(loggerFieldEntry.getBaFieldName());
		ArrayList<User> logs = getUserFromRequsers(loggers);
		if( null == logs || logs.size() == 0 )
			return ;
		User loggerUser = logs.get(0);
		
		ArrayList<UserMapEntry> userMap = UserMapManager.lookupUserMap(ba.getSystemPrefix(), loggerUser.getUserLogin());
		
		if( null != userMap && userMap.size() > 0 )
		{
				// find the current state of usermap_type1 2 and 3 and remove from user map which are not equal to these
				String t1 = null;
				String t2 = null ;
				String t3 = null ;
				FieldNameEntry t1fne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), UserMapType1);
				FieldNameEntry t2fne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), UserMapType2);
				FieldNameEntry t3fne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), UserMapType3);
				
				if( null != t1fne )
					t1 = currentRequest.get(t1fne.getBaFieldName());
				
				if( null != t2fne )
					t2 = currentRequest.get(t2fne.getBaFieldName());
				
				if( null != t3fne )
					t3 = currentRequest.get(t3fne.getBaFieldName());
				
				for( Iterator<UserMapEntry> iter = userMap.iterator() ; iter.hasNext() ;)
				{
					UserMapEntry ume = iter.next();
					if( 
							//  negate equality to get inequality
						!(
								// check equality
						   ( ( t1 == null && ume.getType1() == null ) || ( t1 != null && ume.getType1() != null && t1.equals(ume.getType1()) ) )
						&& ( ( t2 == null && ume.getType2() == null ) || ( t2 != null && ume.getType2() != null && t2.equals(ume.getType2()) ) )
						&& ( ( t3 == null && ume.getType3() == null ) || ( t3 != null && ume.getType3() != null && t3.equals(ume.getType3()) ) )
						)
					  ) 
					  {
						 // if given user-map does not contain values as per the values in the request then remove it
						iter.remove();
					  }
				}
			
			Hashtable<String,ArrayList<RequestUser>> fieldValueMap = new Hashtable<String, ArrayList<RequestUser>>();
			for( int i = 0 ; i < userMap.size() ; i++ )
			{
				UserMapEntry ume = userMap.get(i);
				
				String userLogin = ume.getUserLoginValue();
				if( null == userLogin || userLogin.trim().equals("") )
					continue;
				
				String fieldName = ume.getUserTypeFieldName();
				Field field = null;
				try {
					field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
				} catch (DatabaseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if( null == field )
				{
					LOG.error("Cannot find field with name : " + fieldName + ". Hence ignoring.");
					continue ;
				}
	
				User user = null ;
				try
				{
					user = User.lookupByUserLogin(userLogin);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					LOG.info("Exception occurred while finding user with login : " + userLogin );
					continue;
				}
				
				if( null != user )
				{
					RequestUser ru = new RequestUser(ba.getSystemId(), currentRequest.getRequestId(), user.getUserId(), 0, false, field.getFieldId());
					ArrayList<RequestUser> rus = fieldValueMap.get(ume.getUserTypeFieldName());
					if( null == rus )
						rus = new ArrayList<RequestUser>();
					
					rus.add(ru);
					fieldValueMap.put(ume.getUserTypeFieldName(), rus);
				}
				
			}
			
			for( Enumeration<String> fieldNames = fieldValueMap.keys() ; fieldNames.hasMoreElements() ;)
			{
				String fieldName = fieldNames.nextElement() ;
				ArrayList<RequestUser> users = fieldValueMap.get(fieldName);
				if( null != users )
				{
					Collection<RequestUser> oldUsers = (Collection<RequestUser>) currentRequest.getObject(fieldName);
					if( null != oldUsers )
					{
						for(Iterator<RequestUser> iter = users.iterator() ; iter.hasNext() ; )
						{
							RequestUser ru = iter.next() ;
							for( Iterator<RequestUser> oiter = oldUsers.iterator() ; oiter.hasNext() ;)
							{
								RequestUser oru = oiter.next() ;
								if( ru.getUserId() == oru.getUserId() && ru.getFieldId() == oru.getFieldId() )
								{
									iter.remove();
									break;
								}
							}
						}
						
						users.addAll(oldUsers);
					}
					
					currentRequest.setObject(fieldName, users);
				}
			}
		}
	}

	private void setTheGivenProtocolAndTypes(OnBehalfEntry validProtocol, BusinessArea ba, String header,
			Request currentRequest, Hashtable<String, FieldNameEntry> fieldMap) throws DatabaseException 
	{
		if( null != fieldMap.get(GenericParams.OnBehalfType1) )
		{
			String fieldName1 = fieldMap.get(GenericParams.OnBehalfType1).getBaFieldName();
			if( validProtocol.getType1() != null)
			{
				Type type1 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName1, validProtocol.getType1());

				if( null != type1 )
					currentRequest.setObject(fieldName1, type1);
				else setOtherType(header, ba, currentRequest, fieldName1);
			}
			else setOtherType(header, ba, currentRequest, fieldName1);
		}
		
		if( null != fieldMap.get(GenericParams.OnBehalfType2) )
		{
			String fieldName2 = fieldMap.get(GenericParams.OnBehalfType2).getBaFieldName();
			if( validProtocol.getType2() != null )
			{
				Type type2 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName2, validProtocol.getType2());
	
				if( null != type2 )
					currentRequest.setObject(fieldName2, type2);
				else setOtherType(header, ba, currentRequest, fieldName2);
			}
			else setOtherType(header, ba, currentRequest, fieldName2);
		}
		
		if( null != fieldMap.get(GenericParams.OnBehalfType3) )
		{
			String fieldName3 = fieldMap.get(GenericParams.OnBehalfType3).getBaFieldName();
			if( validProtocol.getType3() != null )
			{
				Type type3 = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), fieldName3, validProtocol.getType3());
	
				if( null != type3 )
				{
					currentRequest.setObject(fieldName3, type3);
				}
				else setOtherType(header, ba, currentRequest, fieldName3);
			}
			else setOtherType(header, ba, currentRequest, fieldName3);
		}
		
		FieldNameEntry loggerFieldEntry = fieldMap.get(LoggerFieldName);
		if( null != loggerFieldEntry )
		{
			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), loggerFieldEntry.getBaFieldName());
			if( null != field )
			{
				User logger = User.lookupByUserLogin(validProtocol.getOnBehalfUser());
				if( null != logger )
				{
					ArrayList<User> userList = new ArrayList<User>();
					userList.add(logger);
					Collection<RequestUser> reqUserList = getRequestUsers(ba,currentRequest,field,userList);
					currentRequest.setObject(field, reqUserList);
				}
			}
		}
	}
	
	private Collection<RequestUser> getRequestUsers(BusinessArea ba, Request request, Field field, ArrayList<User> userList)
	{
		Collection<RequestUser> requser = new ArrayList<RequestUser>();
		for( int i = 0 ; i < userList.size() ; i++ )
		{
			RequestUser ru = new RequestUser(ba.getSystemId(), request.getRequestId(), userList.get(i).getUserId(),i + 1, false, field.getFieldId());
			requser.add(ru);
		}
		
		return requser;
	}
	
	private Collection<RequestUser> getRequestUsers(BusinessArea ba, Request request, String fieldName, ArrayList<User> userList) throws DatabaseException
	{
		Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
		
		return getRequestUsers(ba, request, field, userList);
	}
	
	private ArrayList<User> getUserFromRequsers(Collection<RequestUser> requsers) throws DatabaseException
	{
		if( null == requsers )
			return null; 
		ArrayList<RequestUser> requs = new ArrayList<RequestUser>();
		requs.addAll(requsers);
		Collections.sort(requs, new Comparator<RequestUser>() 
		{
			@Override
			public int compare(RequestUser arg0, RequestUser arg1) {
				return arg0.getOrdering() - arg1.getOrdering() ;
			}
		});
		
		ArrayList<User> users = new ArrayList<User>();
		for( RequestUser ru : requs )
		{
			User user = User.lookupByUserId(ru.getUserId());
			if( null != user && user.getIsActive() == true )
				users.add(user);
		}
		
		return users;
	}

	private void setUserTypesEmpty(String header, BusinessArea ba,
			Request currentRequest) 
	{
		currentRequest.setObject(Field.ASSIGNEE, new ArrayList<RequestUser>());
		currentRequest.setObject(Field.SUBSCRIBER, new ArrayList<RequestUser>());
	}

//	private void setOtherOnBehalfTypes(String header, BusinessArea ba,
//			Request currentRequest, Hashtable<String, FieldNameEntry> fieldMap) 
//	{
//		{
//			FieldNameEntry fne = fieldMap.get(GenericParams.OnBehalf1_Other);
//			if( null != fne && fne.getBaFieldName() != null )
//			{
//				setOtherType(header,ba,currentRequest,fne.getBaFieldName());
//			}
//		}
//		
//		{
//			FieldNameEntry fne = fieldMap.get(GenericParams.OnBehalf2_Other);
//			if( null != fne && fne.getBaFieldName() != null )
//			{
//				setOtherType(header,ba,currentRequest,fne.getBaFieldName());
//			}
//		}
//		
//		{
//			FieldNameEntry fne = fieldMap.get(GenericParams.OnBehalf3_Other);
//			if( null != fne && fne.getBaFieldName() != null )
//			{
//				setOtherType(header,ba,currentRequest,fne.getBaFieldName());
//			}
//		}
//	}
	
	private void setOtherType(String header, BusinessArea ba,
			Request currentRequest, String fieldName)
	{
		try {
			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(),fieldName );
			Type type = Type.getDefaultTypeBySystemIdAndFieldName(ba.getSystemId(), fieldName);
			if( null != type )
				currentRequest.setObject(field, type);
			else
				header += "Default type was not properly set for the field '" + field.getDisplayName() + "'" ;
		} catch (DatabaseException e) {
			e.printStackTrace();
			header += "OnBehalfType2 field not configured properly." ;
		}
	}

}
