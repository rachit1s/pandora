package nccCorr.report;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Hashtable;

import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Subscriber implements IReportParamPlugin {

	public String getName() {
		return "Subscribers";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		return getCCs(co);
	}
	
	public static String getCCs( CorrObject co ) throws CorrException 
	{
		
		String logins = null ;
		logins = co.getAsString(Field.SUBSCRIBER);
		HashSet<User> users = new HashSet<User>() ;
		if( logins != null )
			try {
				users.addAll(Utility.toUsers(logins));
			} catch (CorrException e) {
				e.printStackTrace();
			}
		
		 
		String subscriberList = "" ;	

		for( User user : users )
		{
			if(null != user.getDisplayName() && !user.getDisplayName().trim().equals("-"))
				subscriberList += getNameDesignation(user) + ", " + user.getFullFirmName() + "<br />" ;
		}
		
		return subscriberList ;
	}
	
	public static String getNameDesignation( User user ) 
	{				
		String name = getName(user) ;
		
		String designation = user.getDesignation() ;
		String sex = user.getSex() ;
		
		String nd = "" ;
		if( null != sex && !sex.trim().equals(""))
		{ 
			if( sex.equalsIgnoreCase("M"))
				nd = "Mr. " ; 
			else if( sex.equalsIgnoreCase("F") )
				nd = "Ms. " ;
		}
	
		nd += name ;
		if( null != designation && !designation.trim().equals(""))
			nd +=  ",  " + designation ;
		
		return nd ;
	}
	
	public static String getName( User user)
	{
		String name = user.getDisplayName() ;
		if( null == name || name.trim().equals(""))
		{
			name = "" ;
			String first_name = user.getFirstName() ;
			String last_name = user.getLastName() ;
			if( null != first_name )
				name = first_name ;
			if( null != last_name )
				name += " " + last_name ;
			
			if( name.trim().equals(""))
				name = user.getUserLogin() ;
		}
		
		return name ;
	}

}
