package siemensCorr.report;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import transbit.tbits.domain.Field;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CC implements IReportParamPlugin {

	public String getName() {
		return "CC";
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
//		if( co.getBa().getSystemPrefix().equals("kdi_di") || co.getBa().getSystemPrefix().equals("JPN_DI") || co.getBa().getSystemPrefix().equals("Malwa_DI") ||co.getBa().getSystemPrefix().equals("VPGL2_DI" ) ||co.getBa().getSystemPrefix().equals("RJP_DI" ) ||co.getBa().getSystemPrefix().equals("APPDCL_DI" ))
//			logins = co.getAsString("CorrCC");
//		else
//			logins = co.getAsString(Field.SUBSCRIBER);
		
		FieldNameEntry ccEntry = co.getFieldNameMap().get(GenericParams.CcUserTypeFieldName);
		if( null == ccEntry || null == ccEntry.getBaFieldName())
			throw new CorrException("The field " + GenericParams.CcUserTypeFieldName + " was not properly configured for ba : " + co.getBa().getSystemPrefix());
		
		String ccfieldName = ccEntry.getBaFieldName();
		logins = co.getAsString(ccfieldName);
//		HashSet<User> users = new HashSet<User>() ;
//		if( logins != null )
//			try {
//				users.addAll(Utility.toUsers(logins));
//			} catch (CorrException e) {
//				e.printStackTrace();
//			}
		ArrayList<User> users = Utility.toUsers(logins);
		 
		String ccList = "" ;	

		for( User user : users )
		{
			if(null != user.getDisplayName() && !user.getDisplayName().trim().equals("-"))
				ccList += getNameDesignation(user) + ", " + user.getFullFirmName() + "<br />" ;
			
			String agencies = "LTSL,MPPGCL" ;
			ArrayList<String> agencyList = Utility.splitToArrayList(agencies, ",");
			
			String sysPrefixes = "Malwa_DI,Malwa_Corr";
			ArrayList<String> sysPrefixList = Utility.splitToArrayList(sysPrefixes,",");
			
			Type recAgency = co.getRecepientAgency();
			
			if( sysPrefixList.contains(co.getBa().getSystemPrefix()))
			{
				if( null != recAgency )
				{
					if( agencyList.contains(recAgency.getName()) )
					{
						if( null != user.getFirmAddress() && !user.getFirmAddress().equals(""))
							ccList += user.getFirmAddress().replaceAll("\n", "<br />") + "<br /><br />" ;
					}
				}
			}
			
			
		}
		
		return ccList ;
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
