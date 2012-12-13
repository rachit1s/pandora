package lntCorr.report;

import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
public class IOMKindAtt implements IReportParamPlugin {

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException {
CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		String assFieldName = co.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName).getBaFieldName();
		String rec = co.getAsString(assFieldName);
		
		if( null == rec )
			return "" ;
		
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
			
			String salutation = "";
			String sex = user.getSex() ;
			if(sex.equalsIgnoreCase("M"))
				salutation += "Mr. " ;
			else if( sex.equalsIgnoreCase("F"))
				salutation += "Ms. " ;
			
			String designation = user.getDesignation();
			
			 String retValue = salutation + user.getDisplayName() ;
			 
			 if( null != designation && !designation.trim().equals(""))
				 retValue += "," + designation ;
			 
			 return retValue ;
		}
		
		return "";
	}

}
