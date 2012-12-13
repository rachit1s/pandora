package lntCorr.rule;

import java.sql.Connection;
import java.util.ArrayList;

import lntCorr.others.LnTConst;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
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

public class OnlyNumberRule implements IRule {

	public static final TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if( Source == TBitsConstants.SOURCE_EMAIL )
			{
				return new RuleResult(true,"Rule not applicable for source = email.",true);
			}
			
			String validBAs = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			
			if( null == validBAs )
			{
				LOG.info("Property not found : " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties");
				return new RuleResult(true,"Property not found : " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties",true); 
			}
			
			ArrayList<String> bas = Utility.splitToArrayList(validBAs, ",");
	
			if( null == ba || null == ba.getSystemPrefix() || !bas.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Rule not applicable for this ba.",true);
			
			String corrTypeFn = "CorrespondenceCategory";
			
			String emailTn = "Email";
			
			FieldNameEntry genCorrFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(),GenericParams.GenerateCorrespondenceFieldName);
			
			if( null == genCorrFne || genCorrFne.getBaFieldName() == null )
			{
				LOG.info("Cannot find the mapping for " + GenericParams.GenerateCorrespondenceFieldName + " for ba : " + ba.getSystemPrefix());
				return new RuleResult(true,"Cannot find the mapping for " + GenericParams.GenerateCorrespondenceFieldName + " for ba : " + ba.getSystemPrefix(),true);
			}
			
			String genCorrFn = genCorrFne.getBaFieldName();
			
			String bothNoAndPdfTn = GenericParams.GenerateCorr_BothNumberAndPdf;
			
			Field corrCatField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), corrTypeFn);
			Type corrCatEmailType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(),corrTypeFn,emailTn);
			
			Field generateField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), genCorrFn);
			Type bothNoAndPdfType = Type.lookupBySystemIdAndFieldNameAndTypeName(ba.getSystemId(),genCorrFn,bothNoAndPdfTn);
			
			if( null == corrCatField || null == corrCatEmailType || null == generateField || null == bothNoAndPdfType )
			{
				return new RuleResult(true,"either the corr. Type of email type or generate Corr. Field or onlyNumber type was null. So skipping the rule.",false);
			}
			
			String ctValue = currentRequest.get(corrCatField.getName());
			String onValue = currentRequest.get(generateField.getName());
			
			if( ctValue.equals(corrCatEmailType.getName()) && onValue.equals(bothNoAndPdfType.getName()))
			{
				return new RuleResult(false,"You cannot select " + bothNoAndPdfType.getDisplayName() + " in " + generateField.getDisplayName() + " when selecting " + corrCatEmailType.getDisplayName() + " in " + corrCatField.getDisplayName());
			}
		}
		catch(Exception e)
		{
			LOG.info(TBitsLogger.getStackTrace(e));
			LOG.info("An exception occurred while checking the email should only be allowed with OnlyNumber in generate.");
		}
		return new RuleResult(true,"",true);
	}

	public String getName() {
		return "Does not allows CorrespondenceCategory as Email & GenerateCorrespondence as BothNumberAndPdf";
	}

	public double getSequence() 
	{
		return 0;
	}

}
