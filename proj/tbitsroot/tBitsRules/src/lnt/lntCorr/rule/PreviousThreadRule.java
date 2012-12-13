package lntCorr.rule;

import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_NoPdforCorrNumber;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorrespondenceFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.StatusClosed;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.StatusFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.SuperUser;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;

public class PreviousThreadRule implements IPostRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
		{
			return new RuleResult(true,"Rule not applicable for requests from email.", true);
		}
		try
		{
		
		String appBAs = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
		
		if( null == appBAs )
		{
			LOG.info("Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
			return new RuleResult(true,"Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.",true);
		}
		
		ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);
		
		if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
			return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
		
		String fieldName = "previous_thread" ;
		Field field = null;
		try {
			field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		if( null == field )
		{
			return new RuleResult(true,"field " + fieldName + " not found for ba : " + ba.getSystemPrefix());
		}

		Integer value = (Integer) currentRequest.getObject(field);
		if( null == value || 0 == value )
		{
			return new RuleResult(true,"No value in " + fieldName,true);
		}
		
		Hashtable<String, FieldNameEntry> fieldMap = null;
			fieldMap = FieldNameManager.lookupFieldNameMap(ba.getSystemPrefix());
		
		if( null == fieldMap )
		{
			LOG.info("No fields mapping found for ba : " + ba.getSystemPrefix());
			return new RuleResult(false,"No fields mapping found for ba : " + ba.getSystemPrefix(),true);
		}
		
		FieldNameEntry genCorrFne = fieldMap.get(GenerateCorrespondenceFieldName);
		if( null == genCorrFne || null == genCorrFne.getBaFieldName())
		{
			LOG.info("No mapping found for field : " + GenerateCorrespondenceFieldName + " for ba : " + ba.getSystemPrefix());
//			return new RuleResult(false,"No mapping found for field : " + GenerateCorrespondenceFieldName + " for ba : " + ba.getSystemPrefix(),true);
		}
		
		Hashtable<String, ProtocolOptionEntry> diOptions = ProtocolOptionsManager.lookupAllProtocolEntry(ba.getSystemPrefix());
		ProtocolOptionEntry superUser = diOptions.get(SuperUser);
		User su = null;
		if( null != superUser && null != superUser.getValue())
		{
			String suLogin = superUser.getValue();
			try {
				su = User.lookupByUserLogin(suLogin);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		
		if( null == su )
		{
			LOG.info("No protocol option with name " + SuperUser + " configured for ba : " + ba.getSystemPrefix() + ". So going for the standard superuser 'root'");
			try {
				su = User.lookupByUserLogin("root");
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		
		if( null == su )
		{
			LOG.info("Cannot find any super user for the ba " + ba.getSystemPrefix() + ". So system cannot update request for this ba.");
			return new RuleResult(false,"Cannot find any super user for the ba " + ba.getSystemPrefix() + ". So system cannot update request for this ba.", true);
		}
		
//		FieldNameEntry satusFieldfne = fieldMap.get(StatusFieldName);
//		if( null == satusFieldfne || null == satusFieldfne.getBaFieldName() )
//		{
//			return new RuleResult(false,StatusFieldName + " field not configured for ba : " + ba.getSystemPrefix(),true );
//		}
		
		Hashtable<String,String> params = new Hashtable<String,String>();
		
		params.put(Field.USER, su.getUserLogin());
		if( null != genCorrFne )
			params.put(genCorrFne.getBaFieldName(), GenerateCorr_NoPdforCorrNumber);
		
		FieldNameEntry disProtFne = fieldMap.get(GenericParams.DisableProtocolFieldName);
		if( null == disProtFne || null == disProtFne.getBaFieldName())
		{
			params.put(disProtFne.getBaFieldName(), GenericParams.DisableProtocol_True);
		}
		params.put(Field.BUSINESS_AREA, ba.getSystemId()+"");
		params.put(Field.REQUEST, value+"");
		params.put(Field.PARENT_REQUEST_ID, currentRequest.getRequestId()+"");
		
		UpdateRequest up = new UpdateRequest();
		up.setSource(TBitsConstants.SOURCE_CMDLINE);
		TBitsResourceManager trm = new TBitsResourceManager();
		Request req = up.updateRequest(connection,trm, params);

		return new RuleResult(true,"request-added successfully in ba : " + ba.getSystemPrefix() , true);
		}
		catch(TBitsException te)
		{
			LOG.error(TBitsLogger.getStackTrace(te));
			return new RuleResult(false,te.getDescription(),false);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,e.getMessage(),false);
		} catch (APIException e) {
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,e.getMessage(),false);		}
	}

	public String getName() {
		return "If the field previous_thread is present then an update will be done on the request in this field of the same to make its parent as this request";
	}

	public double getSequence() {
		return 0;
	}

}
