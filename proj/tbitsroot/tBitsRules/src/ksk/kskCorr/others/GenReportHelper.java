package kskCorr.others;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;

public class GenReportHelper 
{

	/**
	 * takes the login-name of the user and generates the html code for his/her complete name and designation
	 * @param loginname
	 * @return
	 */
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
	
	public static String getNameDesignation( String loginname ) 
	{		
		if( null == loginname || loginname.trim().equals(""))
			return "" ;
		User user = null ;
		try {
			user = User.lookupAllByUserLogin(loginname);
		} catch (DatabaseException e) {			
			e.printStackTrace();
			return "" ;
		}
		if( null == user ) 
			return "" ;
			
		String name = getName(user) ;
		String designation = user.getDesignation(); //user_info.get(UserInfoManager.DESIGNATION) ;
		String sex = user.getSex(); //user_info.get(UserInfoManager.SEX) ;

		String nd = "" ;
		if( null != sex && !sex.trim().equals(""))
		{ 
			nd = sex.trim().equalsIgnoreCase("M") ? "Mr. " : "Ms. " ;
		}
		
		nd += name + ",  " + designation ;
		
		return nd ;
	}
}
