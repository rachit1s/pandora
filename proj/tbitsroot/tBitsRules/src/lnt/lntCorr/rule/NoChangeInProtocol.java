package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;

import lntCorr.others.LnTConst;

import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class NoChangeInProtocol implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if( isAddRequest == true )
				return new RuleResult(true,"Rule not applicable for add-request.", true);
			
			String validBAs = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			
			if( null == validBAs )
			{
				LOG.info("Property not found : " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties");
				return new RuleResult(true,"Property not found : " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties",true); 
			}
			
			ArrayList<String> baList = Utility.splitToArrayList(validBAs, ",");

			if(!baList.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Rule not applicable for this ba.", true);
			
			String protocolFieldName =  LnTConst.ProtocolFieldName;
			Field protField = null;
			try {
				protField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), protocolFieldName);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			if( null == protField )
				return new RuleResult(true,"Cannot find field with name : " + protocolFieldName + ". So aborting the rule.", true);
			
			Type currProt = (Type) currentRequest.getObject(protocolFieldName);
			Type oldProt = (Type) oldRequest.getObject(protocolFieldName);
			if( null == currProt )
				return new RuleResult(false,"Null value not allowed in field " + protField.getDisplayName() );
			
			if( null != oldProt && !oldProt.getName().equals(LnTConst.ProtocolFieldNameOthers) && !currProt.getName().equals(oldProt.getName()))
				return new RuleResult(false, "Once set the field " + protField.getDisplayName() + " cannot be changed. Please revert it back to " + oldProt.getDisplayName());
			
			return new RuleResult(true,"Rule executed successfully.", true);
		}
		catch(Exception e)
		{
			return new RuleResult(false,"Exception occured : " + e.getMessage(), false);
		}
	}

	public String getName() {
		return "Protocol does not changes ";
	}

	public double getSequence() {
		return 6;
	}

}
