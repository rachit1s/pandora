package transbit.tbits.admin.common;

import java.util.Hashtable;

import javax.servlet.http.HttpServlet;

public class Helper {
	
public static HttpServlet  resolveChildServlet(String pathInfo, Class<? extends HttpServlet> parentClass, String parentURL)
{
	/*
	 * /abc.xyz.admin -> xyz -> XYZ.class
	 */
	//String parentURL = ".admin";
	
	pathInfo = pathInfo.substring(1);
	String parts[] = pathInfo.split("\\.");
	if(parts.length > 1)
	{
		String nextUrlPart = parts[parts.length - 2];
		int idxSlash = nextUrlPart.lastIndexOf('/');
		if(idxSlash > -1)
		{
			nextUrlPart = nextUrlPart.substring(idxSlash + 1);
		}
		Hashtable<String, Class<? extends HttpServlet>> tuples =  
			URLRegistry.getInstance().getMappingTuple(parentClass);
		
		if((tuples != null) && (tuples.size() != 0))
		{
			Class childClass = tuples.get(nextUrlPart);
			HttpServlet childServlet;
			try {
				childServlet = (HttpServlet) childClass.newInstance();
				return childServlet;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}
	return null;
}}
