package kskCorr.report;

import java.util.Hashtable;

import kskCorr.others.KskConst;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class To implements IReportParamPlugin
{

	public String getName() {
		return "Returns the value for To field.";
	}

	public static String getTo( User ass,User log, Type corrProt ) 
	{
		String fullFirm = ass.getFullFirmName();//assInfo.get(UserInfoManager.FULL_FIRM_NAME) ;
		if( log.getLocation() != null && log.getLocation().trim().equalsIgnoreCase(KskConst.SEPCO_FIRM_NAME) && ass.getLocation() != null && ass.getLocation().trim().equalsIgnoreCase(KskConst.WPCL_FIRM_NAME) )
		{
			fullFirm = KskConst.WPCL_FULL_FIRM_NAME ;
		}
		String to = "" ;
		String add = ass.getFirmAddress();
		add = add.replace("\n", "<br>") ;
		
		to += fullFirm + "<br>" + add ;
		return to ;
	}
	
	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		String assFieldName = coob.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName).getBaFieldName();
		String rec = coob.getAsString(assFieldName);
		
		User log = coob.getUserMapUsers().get(0);
		
		String [] recs = rec.split(",");
		if( null != recs )
		{
			User user = null;
			try {
				user = User.lookupByUserLogin(recs[0]);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			if( null == user )
				return "";
			
			String corrProt = coob.getAsString(KskConst.CorrProtocolFieldName);
			
			if( null == corrProt )
				throw new CorrException("Null value not allowed in field : " + Utility.fdn(coob.getBa(),KskConst.CorrProtocolFieldName));

			Type corrProtType = null;
			try {
				corrProtType = Type.lookupBySystemIdAndFieldNameAndTypeName(coob.getBa().getSystemId(), KskConst.CorrProtocolFieldName, corrProt);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			
			if( null == corrProtType )
				throw new CorrException("Cannot find type: " + corrProt + " in field " + Utility.fdn(coob.getBa(),KskConst.CorrProtocolFieldName));
			
			return getTo(user, log, corrProtType);
		}
		else
		{
			throw new CorrException("Null value not allowed in field : " + Utility.fdn(coob.getBa(),assFieldName));
		}
	}
	
}
