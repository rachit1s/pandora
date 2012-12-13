package lntCorr.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import lntCorr.others.LnTConst;

import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;

public class AppendPreviousCorrNos implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
		{
			return new RuleResult(true,"Rule not applicable for adding request from Email.", true);
		}
		
		if( isAddRequest )
		{
			return new RuleResult(true,"Rule not applicable for add-request.", true); // to avoid the fail of sql request.
		}
//		
//		PropertyEntry appBAs = null;
//		try {
//			appBAs = PropertyManager.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
//		} catch (CorrException e1) {
//			e1.printStackTrace();
//		}
//		
//		if( null == appBAs || null == appBAs.getValue() )
//			return new RuleResult(true, "No BA configured for Correspondence.", true );
		
		String appBAs = PropertiesHandler.getProperty(LnTConst.CorrBaList) ; //"kdi_di,kdi_corr,Malwa_DI,Malwa_Corr,RJP_DI,RJP_Corr,APPDCL_DI,APPDCL_Corr,JPN_DI,JPN_Corr,VPGL2_DI,VPGL2_Corr";
		
		if( null == appBAs )
		{
			LOG.info("No property found with name : " + LnTConst.CorrBaList + " in tbits_properties.");
			return new RuleResult(true,"No property found with name : " + LnTConst.CorrBaList + " in tbits_properties.", true );
		}
		
		ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);
		
		if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
			return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
		
		try 
		{
			FieldNameEntry genCorrFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
			
			if( genCorrFne == null || genCorrFne.getBaFieldName() == null )
			{
				LOG.info("No Mapping found for " + GenericParams.GenerateCorrespondenceFieldName + " in the ba : " + ba.getSystemPrefix() + ". Hence not considering it as a part of corr. protocol.");
				return new RuleResult(true,"Rule executed successfully for request : " + currentRequest, true);			
			}
			else
			{
				String value = currentRequest.get(genCorrFne.getBaFieldName());
//				if ( value != null && !value.equals( GenericParams.GenerateCorr_NoPdforCorrNumber ) )
//				{
					FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
					if( null == corrNofne || null == corrNofne.getBaFieldName() )
					{
						LOG.info("Corr. No. or Corr. File was not configured hence not generating no. and file.");
						return new RuleResult(true,"Corr. No. or Corr. File was not configured hence not generating no. and file.", true);
					}
					
					Field corrNoField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), corrNofne.getBaFieldName());
					if( null == corrNoField )
						return new RuleResult(true,"Corr No. Field configured to " + corrNofne.getBaFieldName() + " not found. So aborting the rule.", false);
						
					String prevCorrNoFieldName = LnTConst.PreviousCorrNo;
					Field prevCoField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), prevCorrNoFieldName);
					
					if( null != prevCoField )
					{
						String corrNoSql = " select * from actions_ex where sys_id = " + ba.getSystemId() + " and request_id = " + currentRequest.getRequestId() + " and field_id = " + corrNoField.getFieldId() + " order by action_id ";
						PreparedStatement ps = connection.prepareStatement(corrNoSql);
						ResultSet rs = ps.executeQuery() ;
						
						String cnList = "" ;
						boolean first = true ;
						if( null != rs )
						{
							while( rs.next() )
							{
								String cn = rs.getString("varchar_value");
								if( cn != null )
								{
									cn = cn.trim() ;
									if( !cn.equals(""))
									{
										if( first )
										{
											first = false ;
											cnList += cn ;
										}
										else
										{
											cnList += ", " + cn;
										}
									}
								}
							}
						}
						
						currentRequest.setObject(prevCoField, cnList);
					}
//				}
			}
			
			return new RuleResult(true,"Rule executed successfully.", true);
		}
		catch(Exception te)
		{
			LOG.error(TBitsLogger.getStackTrace(te));
			return new RuleResult(false,te.getMessage(),false);
		}
	}

	public String getName() {
		return "Appends the previous corr no. to a varchar field." ;
	}

	public double getSequence() {
		return 10;
	}

}
