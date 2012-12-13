package lntCorr.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import lntCorr.others.GenCorrNoHelper;
import lntCorr.others.LnTConst;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

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

public class ValidateCorrNo implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		String appBAs = PropertiesHandler.getProperty(LnTConst.CorrBaList);
		if( null == appBAs )
		{
			LOG.info("Property not found : " + LnTConst.CorrBaList + " in tbits_properties.");
			return new RuleResult(true,"Property not found : " + LnTConst.CorrBaList + " in tbits_properties.",true);
		}
		
		ArrayList<String> baList = Utility.splitToArrayList(appBAs);
		
		if( !baList.contains(ba.getSystemPrefix()))
			return new RuleResult(true,"Rule not applicable for this ba.", true);
		
		try
		{
			FieldNameEntry genFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
			FieldNameEntry corrNoFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
			
			if( null == genFne || null == corrNoFne )
			{
				LOG.info("Either the GenerateCorrespondence field or CorrespondenceNumber field was not configured.");
				return new RuleResult(true,"Either the GenerateCorrespondence field or CorrespondenceNumber field was not configured. So aborting the rule.", true);
			}
			
			// if generate corr. is No Number No pdf then check for the validation of the number by not allowing the future number.
			Field genField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), genFne.getBaFieldName());
			Field corrField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), corrNoFne.getBaFieldName());
			
			if( null == genField || null == corrField )
			{
				LOG.info("Either the GenerateCorrespondence field or CorrespondenceNumber field was not configured properly.");
				return new RuleResult(true,"Either the GenerateCorrespondence field or CorrespondenceNumber field was not configured properly. So aborting the rule.", true);
			}
			
			Type genCorr = (Type) currentRequest.getObject(genField);
			String corrNo = (String) currentRequest.getObject(corrField); 
			
			if( null == genCorr || !genCorr.getName().equals(GenericParams.GenerateCorr_NoPdforCorrNumber) || null == corrNo || corrNo.trim().equals(""))
			{
				LOG.info("Generate was : " + genCorr.getDisplayName() + " and corrNo = " + corrNo + ". So aborting the rule.");
				return new RuleResult(true,"Generate was : " + genCorr.getDisplayName() + " and corrNo = " + corrNo + ". So aborting the rule.",true);
			}
			
			int lastInd = corrNo.lastIndexOf('-');
			if( lastInd == -1 || ( lastInd == corrNo.length() -1 ) ) // number does not contain '-' OR '-' is the last letter of the number ex : 'corrNo-'
			{
				return new RuleResult(true,"The corrNo is not in format of prefix-runningno. So aborting the rule.", true);
			}
			
			String prefix = corrNo.substring(0, lastInd);
			String runningNumber = corrNo.substring(lastInd+1);
			
			// running number should be integer.
			int runningNo = 0;
			try
			{
				runningNo = Integer.parseInt(runningNumber);
			}
			catch(NumberFormatException nfe)
			{
				LOG.error("The running number : " + runningNumber + " of the corrNo. " + corrNo + " was not integer. So aborting the rule.");
				return new RuleResult(true,"The running number : " + runningNumber + " of the corrNo. " + corrNo + " was not integer. So aborting the rule.",true);
			}
			
//			// no extra sanitization.
//			String sql = " select * from max_ids where name = '" + prefix + "'" ;
//			PreparedStatement ps = connection.prepareStatement(sql);
//			try
//			{
//				ResultSet rs = ps.executeQuery() ;
//				if( null != rs )
//				{
//					if( rs.next() )
//					{
						String maxIdName = GenCorrNoHelper.getMaxIdNameForPrefix(ba.getSystemPrefix(), corrNo);
						if( maxIdName != null )
						{
							int maxNumber = GenCorrNoHelper.getMaxCorrNo(connection, maxIdName);
							if( runningNo > maxNumber )
							{
								LOG.info("The maximum running number for the correspondence prefix = '" + prefix + "' can be set to : " + maxNumber);
								return new RuleResult(false,"The maximum running number for the correspondence prefix = '" + prefix + "' can be set to : " + maxNumber, true);
							}
							else
							{
								LOG.info("The running number of : " + runningNo + " was less than or equal to : " + maxNumber + " for the prefix : " + prefix + " and is allowed.");
							}
						}
						else
						{
							LOG.info("System could not find the maxIdName corresponding to the corrNo : " + corrNo + ". So the number is allowed in its present form");
						}
//					}
//				}
//			}
//			finally
//			{
//				if( null != ps )
//					ps.close();
//			}
			
			return new RuleResult(true,"The rule executed successfully.", true);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,"Exception occured : " + e.getMessage(),false);
		}
	}

	public String getName() 
	{
		return "checks if the provided corr. no. for No number and no pdf if matches any pattern in max_ids it should not allow any future numbers.";
	}

	public double getSequence() 
	{
		return 7;
	}

}
