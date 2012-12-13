package corrGeneric.report;

import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class To implements IReportParamPlugin {

	public String getName() {
		return "get to";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		return getTo(co);
	}
	
	public static String getTo( CorrObject co) 
	{
		String assFieldName = co.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName).getBaFieldName();
		String rec = co.getAsString(assFieldName);
		
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
//			String fullFirm = user.getFullFirmName() ;		
			String to = "" ;
			String add = user.getFirmAddress();
			add = add.replace("\n", "<br>") ;
			
			to +=  add ;
			return to ;
		}
		else
		{
			return "" ;
		}
	}

}
