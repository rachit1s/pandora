package lntCorr.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import lntCorr.others.LnTConst;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestDataType;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class SameProtocolInLatestDIAndCorr implements IRule {

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
			String appBAs = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
			
			if( null == appBAs )
			{
				LOG.info("Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.");
				return new RuleResult(true,"Property not found : property_name = " + GenericParams.CommaSeparatedListOfApplicableBa + " in tbits_properties.",true);
			}
						
			ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBAs);
			
			if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
				return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
			
//			ProtocolOptionEntry corr_option = ProtocolOptionsManager.lookupProtocolEntry(ba.getSystemPrefix(), ProtMappedDIBA);
//			if( null == corr_option || corr_option.getValue() == null )
//			{
//				return new RuleResult(true, "No protocol option mapping found for option name : " + ProtMappedDIBA + " for the ba : " + ba.getSystemPrefix() , true);
//			}
//			
//			String diSysprefix = corr_option.getValue();
//	
//			BusinessArea diba = BusinessArea.lookupBySystemPrefix(diSysprefix);
//			if( null == diba )
//			{
//				return new RuleResult(false, "Ba not found with sysPrefix : " + diSysprefix,false);
//			}
			
			String currRelReqs = currentRequest.getRelatedRequests();
			String prevRelReqs = null;
			if( null != oldRequest )
				prevRelReqs = oldRequest.getRelatedRequests();
			
			RequestDataType rdt = LnTConst.getDiffRelReq( currRelReqs , prevRelReqs);
			if( null == rdt )
			{
				LOG.info("No request to check for : " + ba.getSystemPrefix() + "#" + currentRequest.getRequestId() + " : subject : " + currentRequest.getSubject() );
				return new RuleResult(true, "No request to check against.", true);
			}
			
			BusinessArea otba = BusinessArea.lookupBySystemId(rdt.getSysId());
			if( null == otba )
			{
				return new RuleResult(false, "Ba not found with sysId : " + rdt.getSysId() + " which was found in the related Requests.",false);
			}
			
//			Hashtable<String, FieldNameEntry> fieldMap = FieldNameManager.lookupFieldNameMap(diba.getSystemPrefix());
//			if( null == fieldMap )
//			{
//				LOG.info("No fields mapping found for ba : " + diba.getSystemPrefix());
//				return new RuleResult(false,"No fields mapping found for ba : " + diba.getSystemPrefix(),true);
//			}
			
//			Request relRequest = Request.lookupBySystemIdAndRequestId(connection, rdt.getSysId(), rdt.getRequestId());
//
//			if( null == relRequest )
//			{
//				LOG.info("Cannot find the related request " + otba.getSystemPrefix() + "#" + rdt.getRequestId());
//				return new RuleResult(true,"Cannot find the related request " + otba.getSystemPrefix() + "#" + rdt.getRequestId(), true);
//			}

			Integer otProtocolId = getProtocolForAction(connection, rdt, LnTConst.ProtocolFieldName);
			String myProtocol = currentRequest.get(LnTConst.ProtocolFieldName);
			if( null != otProtocolId && null != myProtocol )
			{
				Type otProtocolType = Type.lookupBySystemIdAndFieldNameAndTypeId(rdt.getSysId(), LnTConst.ProtocolFieldName, otProtocolId);
				if( null != otProtocolType )
				{
					String otProtocol = otProtocolType.getName();
					if( !otProtocol.equals(myProtocol) )
					{
						return new RuleResult(false,"The " + Utility.fdn(ba,LnTConst.ProtocolFieldName) + " was changed from " + otProtocolType.getDisplayName()+ " to " + Utility.tdn(ba, LnTConst.ProtocolFieldName, myProtocol) + " which is not allowed.", true );
					}
				}
			}

			return new RuleResult(true,"Rule executed successfully.", true);
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
			return new RuleResult(false,e.getMessage(),false);
		}
	}

	private Integer getProtocolForAction(Connection connection,
			RequestDataType rdt, String protocolfieldname) throws SQLException 
	{
		Integer protocol = null;
		String sql = " select " + protocolfieldname + " from actions where sys_id = " + rdt.getSysId() + " and request_id = " + rdt.getRequestId() + " and action_id = " + rdt.getActionid() ;
		
		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		if( null != rs && rs.next())
		{
			protocol = rs.getInt(protocolfieldname);
			rs.close();
		}
		ps.close();
		
		return protocol;
	}

	public String getName() {
		return "Checks if the Corr's " + LnTConst.ProtocolFieldName + " is same as the latest DI " + LnTConst.ProtocolFieldName + " and vice versa";
	}

	public double getSequence() {
		return 4;
	}

}
