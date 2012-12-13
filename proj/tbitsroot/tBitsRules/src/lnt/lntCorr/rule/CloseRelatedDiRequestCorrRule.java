package lntCorr.rule;

import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.DisableProtocolFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.DisableProtocol_True;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_NoPdforCorrNumber;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorrespondenceFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.ProtMappedDIBA;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.StatusClosed;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.StatusFieldName;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.SuperUser;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import lntCorr.others.LnTConst;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IPostRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.TBitsResourceManager;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.User;
import transbit.tbits.exception.APIException;
import transbit.tbits.exception.TBitsException;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.ProtocolOptionsManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;

public class CloseRelatedDiRequestCorrRule implements IPostRule
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
		{
			return new RuleResult(true,"Rule not applicable for email-request.", true);
		}
		try
		{
//			PropertyEntry appBAs = null;
//			try 
//			{
//				appBAs = PropertyManager.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
//			} catch (TBitsException e1) {
//				e1.printStackTrace();
//			}
//			if( null == appBAs || null == appBAs.getValue() )
//				return new RuleResult(true, "No BA configured for Correspondence.", true );
			
			String appBAs = PropertiesHandler.getProperty(LnTConst.CorrBaList);
			if( null == appBAs )
			{
				LOG.info("Property not found : " + LnTConst.CorrBaList + " in tbits_properties.");
				return new RuleResult(true,"Property not found : " + LnTConst.CorrBaList + " in tbits_properties.",true);
			}
			
			ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);
			
			if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
			
			ProtocolOptionEntry corr_option = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), ProtMappedDIBA);
			if( null == corr_option || corr_option.getValue() == null )
			{
				return new RuleResult(true, "No protocol option mapping found for option name : " + ProtMappedDIBA + " for the ba : " + ba.getSystemPrefix() , true);
			}
			
			String diSysprefix = corr_option.getValue();
	
			BusinessArea diba = BusinessArea.lookupBySystemPrefix(diSysprefix);
			if( null == diba )
			{
				return new RuleResult(false, "Ba not found with sysPrefix : " + diSysprefix,false);
			}
			
			String currRelReqs = currentRequest.getRelatedRequests();
			String prevRelReqs = null;
			if( null != oldRequest )
				prevRelReqs = oldRequest.getRelatedRequests();
			
			RequestDataType rdt = LnTConst.getDiffRelReq( currRelReqs , prevRelReqs);
			if( null == rdt )
			{
				LOG.info("No request to update for : " + ba.getSystemPrefix() + currentRequest.getRequestId() + " : subject : " + currentRequest.getSubject() );
				return new RuleResult(true, "", true);
			}
			
			Hashtable<String, FieldNameEntry> fieldMap = FieldNameManager.lookupFieldNameMap(diba.getSystemPrefix());
			if( null == fieldMap )
			{
				LOG.info("No fields mapping found for ba : " + diba.getSystemPrefix());
				return new RuleResult(false,"No fields mapping found for ba : " + diba.getSystemPrefix(),true);
			}
			
			FieldNameEntry genCorrFne = fieldMap.get(GenerateCorrespondenceFieldName);
			if( null == genCorrFne || null == genCorrFne.getBaFieldName())
			{
				LOG.info("No mapping found for field : " + GenerateCorrespondenceFieldName + " for ba : " + diba.getSystemPrefix());
				return new RuleResult(false,"No mapping found for field : " + GenerateCorrespondenceFieldName + " for ba : " + diba.getSystemPrefix(),true);
			}
			
			FieldNameEntry disProt = fieldMap.get(DisableProtocolFieldName);
			
			Hashtable<String, ProtocolOptionEntry> diOptions = ProtocolOptionsManager.lookupAllProtocolEntry(diba.getSystemPrefix());
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
				LOG.info("No protocol option with name " + SuperUser + " configured for ba : " + diba.getSystemPrefix() + ". So going for the standard superuser 'root'");
				try {
					su = User.lookupByUserLogin("root");
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
			
			if( null == su )
			{
				LOG.info("Cannot find any super user for the ba " + diba.getSystemPrefix() + ". So system cannot update request for this ba.");
				return new RuleResult(true,"Cannot find any super user for the ba " + diba.getSystemPrefix() + ". So system cannot update request for this ba.", false);
			}
			
			FieldNameEntry satusFieldfne = fieldMap.get(StatusFieldName);
			if( null == satusFieldfne || null == satusFieldfne.getBaFieldName() )
			{
				return new RuleResult(true,StatusFieldName + " field not configured for ba : " + diba.getSystemPrefix(),false);
			}
			
			Request diRequest = Request.lookupBySystemIdAndRequestId(connection, diba.getSystemId(), rdt.getRequestId());
			if( null == diRequest )
			{
				return new RuleResult(true,"Cannot find request to update the status of : " + diba.getSystemPrefix() + "#" + rdt.getRequestId());
			}
			
			String currStatus = diRequest.get(satusFieldfne.getBaFieldName());
			if( null != currStatus )
			{
				if( currStatus.equals(StatusClosed) )
					return new RuleResult(true,"The corresponding di request is already closed so not closing it", true);
			}
			
			Hashtable<String,String> params = new Hashtable<String,String>();
			
			params.put(Field.USER, su.getUserLogin());
			params.put(Field.DESCRIPTION, LnTConst.DI_UPDATE_DESCRIPTION + (isAddRequest == true ? "" : "in response to action on request :" + ba.getSystemPrefix()+"#"+currentRequest.getRequestId() +"#"+currentRequest.getMaxActionId()) );
			params.put(genCorrFne.getBaFieldName(), GenerateCorr_NoPdforCorrNumber);
			if( null != disProt && null != disProt.getBaFieldName())
			{
				params.put(disProt.getBaFieldName(),DisableProtocol_True);
			}
			params.put(Field.BUSINESS_AREA, diba.getSystemId()+"");
			params.put(Field.REQUEST, rdt.getRequestId()+"");
			params.put(satusFieldfne.getBaFieldName(), StatusClosed);
			
			UpdateRequest up = new UpdateRequest();
			up.setSource(TBitsConstants.SOURCE_CMDLINE);
			TBitsResourceManager trm = new TBitsResourceManager();
			Request req = up.updateRequest(connection,trm, params);
	
			return new RuleResult(true,"request-added successfully in ba : " + diba.getSystemPrefix() , true);
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
			return new RuleResult(false,e.getMessage(),false);
		}
	}

	public String getName() {
		return "Checks if their is any new linked DI and updates it to and marks its status as closed.";
	}

	public double getSequence() {
		return 7;
	}

}
