package lntCorr.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class CheckManuallyAddedCorrNo implements IRule {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntCorr");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		try
		{
			if(Source == TBitsConstants.SOURCE_EMAIL )
				return new RuleResult(true, "Rule Not applicable for request from email.",true);
	
			String appBAs = PropertiesHandler.getProperty(LnTConst.CorrBaList);
			if( null == appBAs )
			{
				LOG.info("Property not found : property_name = " + LnTConst.CorrBaList);
				return new RuleResult(true,"Property not found : property_name = " + LnTConst.CorrBaList);
			}
			
			ArrayList<String> validBAs = Utility.splitToArrayList(appBAs);
			
			if( null == ba || null == ba.getSystemPrefix() || !validBAs.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Rule not applicable for this ba.", true);
	
			FieldNameEntry genCorrfne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
			FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
			FieldNameEntry genAgefne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.GenerationAgencyFieldName);
			
			if( null == genCorrfne || null == corrNofne || null == genAgefne)
				return new RuleResult(true, "Either Generate or CorrNo or GenerationAgency field not configured. So aborting the rule.", true);
			
			Field genCorrField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), genCorrfne.getBaFieldName());
			Field corrNoField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), corrNofne.getBaFieldName());
			Field genAgeField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), genAgefne.getBaFieldName());
			
			if( null == genCorrField || null == corrNoField || null == genAgeField )
				return new RuleResult(true,"Either Generate or CorrNo or GenerationAgency field not found. So aborting the rule.", true);
			
//			String genCorrValue = currentRequest.get(genCorrfne.getBaFieldName());
			String corrNo = currentRequest.get(corrNofne.getBaFieldName());
			Type genAgeValue = (Type) currentRequest.getObject(genAgeField.getName());
			
//			if( null == genCorrValue || !genCorrValue.equals(GenericParams.GenerateCorr_NoPdforCorrNumber) || null == corrNo || corrNo.trim().equals("") || genAgeValue == null)
//				return new RuleResult(true,"Either the Generate or the CorrNo or the Generation Agency field was not set in the request. Or Generate was not NoPDFNoCorrNo.",true);

			if( corrNo == null || corrNo.trim().equals(""))
				return new RuleResult( true, "The corrNo was null or empty. So Aborting the plugin.", true );

			corrNo = corrNo.trim();
			
			// check (corr. no. + generation agency) uniqueness 
			String corrNoSql = "select * from actions_ex where sys_id = " + ba.getSystemId() + " and field_id = " + corrNoField.getFieldId() + " and varchar_value = '" + corrNo + "'" ;
//			System.out.println("Nitiraj : CorrNoSql : " + corrNoSql);
			PreparedStatement ps = connection.prepareStatement(corrNoSql);
			ResultSet rs = ps.executeQuery();
			
			if( rs != null )
			{
				ArrayList<RequestDataType> reqs = new ArrayList<RequestDataType>();
				while( rs.next() )
				{
					int sysId = rs.getInt("sys_id");
					int reqId = rs.getInt("request_id");
					int actionId = rs.getInt("action_id");
					RequestDataType rdt = new RequestDataType( sysId,reqId,actionId );
					reqs.add(rdt);
				}
				
				rs.close();
				ps.close();
				
				if( reqs.size() != 0 )
				{
					String agencySql = "select * from actions_ex where sys_id = " + ba.getSystemId() + " and request_id in (" + ( commaSeparatedRequests(reqs) ) + ") " +
							" and field_id = " + genAgeField.getFieldId() + " and type_value = " + genAgeValue.getTypeId() ;
					
					System.out.println("Nitiraj : agencySql = " + agencySql);
					
					PreparedStatement ps2 = connection.prepareStatement(agencySql);
					ResultSet rs2 = ps2.executeQuery();
					
					if( rs2 != null )
					{
						ArrayList<RequestDataType> ereqs = new ArrayList<RequestDataType>();
						
						while( rs2.next() )
						{
							int sysId = rs2.getInt( "sys_id");
							int reqId = rs2.getInt("request_id");
							int actionId = rs2.getInt("action_id");
							
							RequestDataType rdt = new RequestDataType(sysId, reqId, actionId);
							ereqs.add(rdt);
						}
						
						rs2.close();
						ps2.close();
						
						if( ereqs.size() != 0 )
						{
							String msg = " The " + corrNoField.getDisplayName() + " = " + corrNo + " for " + genAgeField.getDisplayName() + " = " + genAgeValue.getDisplayName() + " is not unique.\n" +
									"And is already present in following request : " + commaSeparatedRequests(reqs) + " of BusinessArea : " + ba.getSystemPrefix() ;
							
							return new RuleResult(false,msg,true);
						}
					}
				}
			}
			
			return new RuleResult(true,"Rule executed successfully.", true);
		}
		catch(Exception e)
		{
			LOG.info(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,e.getMessage(),false);
		}
	}

	private String commaSeparatedRequests(ArrayList<RequestDataType> reqs) 
	{
		if( null == reqs )
			return "" ;
		
		boolean first = true ;
		String reqList = "" ;
		for( RequestDataType rdt : reqs )
		{
			if( first )
			{
				reqList += rdt.getRequestId() +"";
				first = false;
			}
			else
			{
				reqList += ", " + rdt.getRequestId() ;
			}				
		}
		
		return reqList;
	}

	public String getName() {
		return "If the Generate is " + GenericParams.GenerateCorr_NoPdforCorrNumber + " and Corr. No. is mentioned then check the corr. no.'s uniqueness.";
	}

	public double getSequence() {
		return 10;
	}

}
