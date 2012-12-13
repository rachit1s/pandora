package corrGeneric.report;

import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class KindAtt implements IReportParamPlugin {

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
//		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
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
			String designation = "" ;
			if( user.getDesignation() != null && !user.getDesignation().trim().equals(""))
			{
				designation = ", " + user.getDesignation().trim() ;
			}
			return salutation + user.getDisplayName() + designation ;
		}
		
		return "";
	}
	
//	public static String getName( User user)
//	{
//		String name = user.getDisplayName() ;
//		if( null == name || name.trim().equals(""))
//		{
//			name = "" ;
//			String first_name = user.getFirstName() ;
//			String last_name = user.getLastName() ;
//			if( null != first_name )
//				name = first_name ;
//			if( null != last_name )
//				name += " " + last_name ;
//			
//			if( name.trim().equals(""))
//				name = user.getUserLogin() ;
//		}
//		
//		return name ;
//	}
//	
//	public static String getKindAttn(CorrObject co)
//	{
//		String assFieldName = co.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName).getBaFieldName();
//		String rec = co.getAsString(assFieldName);
//		
//		String [] recs = rec.split(",");
//		if( null != recs )
//		{
//			User user = null;
//			try {
//				user = User.lookupByUserLogin(recs[0]);
//			} catch (DatabaseException e) {
//				e.printStackTrace();
//			}
//			if( null == user )
//				return "";
//			String name = getName(user) ;
//			String designation = user.getDesignation() ;
//			String kind_att = "" ;
//			String sex = user.getSex() ;
//			if(sex.equalsIgnoreCase("M"))
//				kind_att += "Mr. " ;
//			else if( sex.equalsIgnoreCase("F"))
//				kind_att += "Ms. " ;
//					
//			kind_att += name ;
//			if( null != designation && !designation.trim().equals(""))
//				kind_att += ", " + designation ;
//			
//			return kind_att ;
//		}
//		
//		return "";
//	}


}
