package corrGeneric.com.tbitsGlobal.client.utils;

import java.util.ArrayList;


public class ClientUtility 
{
	public static ArrayList<String> splitToArrayList(String str )
	{
		return splitToArrayList(str,",");
	}
	
	public static ArrayList<String> splitToArrayList(String str, String separator)
	{
		if( null == str )
			return null;
		
		ArrayList<String> strings = new ArrayList<String>();
		String [] vars = str.split(separator);
		for( String var : vars )
		{
			var = var.trim();
			if( var.equals("") )
				continue;
			else
				strings.add(var);
		}
		
		return strings;
	}
}
