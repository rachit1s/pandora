package corrGeneric.report;

import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class Dear implements IReportParamPlugin {

	private static final String DEAR_DEFAULT = "Dear Sirs,";

	public String getName() {
		return "Dear Sir/Madam for the logger.";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
			
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		return getDear(co);
	}
	
	public static String getDear( CorrObject co )
	{
		String dear = "Dear " ;
		String assFieldName = co.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName).getBaFieldName();
		String rec = co.getAsString(assFieldName);
		if( null == rec )
			return DEAR_DEFAULT;
			
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
				return DEAR_DEFAULT;
			
			String sex = user.getSex();
			if( sex.equalsIgnoreCase("M"))
				dear += "Sir, " ;
			else if( sex.equalsIgnoreCase("F"))
				dear += "Madam, " ;
			else return DEAR_DEFAULT ;
		}
		else
		{
			return DEAR_DEFAULT;
		}
		
		return dear ;
	}

}
