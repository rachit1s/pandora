package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;

import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

public class CorrTypeAsEmailInRequestFromEmail implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr.rule");
	
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if( Source != TBitsConstants.SOURCE_EMAIL )
			{
				LOG.info("Rule only valid for Request logged from email. So skiping the rule.");
				return new RuleResult(true,"Rule only valid for Request logged from email. So skiping the rule.",true);
			}
			
			String validBAs = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			
			if( null == validBAs )
			{
				LOG.info("Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
				return new RuleResult(true,"No corr. BAs configured so skipping the rule",true);
			}
			
			ArrayList<String> validBAList = Utility.splitToArrayList(validBAs, ","); 
			
			if( null == validBAList )
			{
				LOG.info("Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
				return new RuleResult(true,"No corr. BAs configured so skipping the rule",true);
			}
			
			if( !validBAList.contains(ba.getSystemPrefix()))
			{
				LOG.info("Rule not applicable for this ba.");
				return new RuleResult(true,"Rule not applicable for this ba.",true);
			}
			
			String fieldName = "CorrespondenceCategory";
			String emailTypeName = "Email" ;
			
			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
			
			if( null == field )
			{
				return new RuleResult(true,"Field not found with name : " + fieldName + ". So skipping the rule." );
			}
			
			Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(), field.getName(), emailTypeName);
			if( null == type )
			{
				return new RuleResult(true,"Type not found with name : " + emailTypeName + " in field with name : " + fieldName + ". So skipping the rule.", true);
			}
			
			currentRequest.setObject(field, type);
			
			return new RuleResult(true,"Rule excuted successfully.",true);
		}
		catch(Exception te)
		{
			te.printStackTrace();
			LOG.error(te);
			return new RuleResult(false,"Exception occured while running the rule : " + this.getClass().getName() + ". Error Msg : " + te.getMessage(), false );
		}

	}

	public String getName() 
	{
		return "sets the CorrespondenceCategory to Email when logged from email in corr bas.";
	}

	public double getSequence() {
		return 0;
	}

}
