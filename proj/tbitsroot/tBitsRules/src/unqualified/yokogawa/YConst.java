package yokogawa;

import static yokogawa.YConst.svgParams;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import transbit.tbits.domain.Request;

public class YConst 
{
	public static String FieldTagNo = "FieldTagNo";
	public static String CableNo1 = "CableNo1";
	public static String CableNo2 = "CableNo2";
	public static String JobNo1 = "JobNo1";
	public static String PairNo1 = "PairNo1";
	public static String TBLoc1 = "TBLoc1";
	public static String Relay_Board_Loc = "Relay_Board_Loc";
	public static String DCSTag = "DCSTag";
	public static String SysLoc = "SysLoc";
	public static String NIU_No = "NIU_No";
	public static String NodeLoc = "NodeLoc";
	public static String NestLoc = "NestLoc";
	public static String Slot = "Slot";
	public static String CHNo = "CHNo";
	public static String NestModel = "NestModel";
	public static String DocNo = "DocNo";
	
	public static String root = "root";
	
	public static String privBA = "MRPCLPrivate";
	public static String pubBA = "Public";
	
	public static String svgFileName = "overlay.svg" ;
	
	public static String[] svgParams = { 
			FieldTagNo,
			CableNo1,
			CableNo2,
			JobNo1,
			PairNo1,
			TBLoc1,
			Relay_Board_Loc,
			DCSTag,
			SysLoc,
			NIU_No,
			NodeLoc,
			NestLoc,
			Slot,
			CHNo,
			NestModel,
			DocNo } ;
	
	public static Hashtable<String,String> getSVGMap(Request request)
	{
		Hashtable<String,String> map = new Hashtable<String,String>() ;
		for( String key : svgParams )
		{
			String value = (String) request.get(key);
			if( value == null )
				value = "" ;
			
			map.put(key, value);			
		}
		
		return map;
	}
	

	public static boolean same(Request oldRequest, Request currentRequest) 
	{
		if( null == oldRequest ) 
			return false ;
		
		for( String key : svgParams )
		{
			String oldValue = oldRequest.get(key);
			String newValue = currentRequest.get(key);
			
			if( oldValue == null && newValue == null )
				continue ;
			if( oldValue == null && newValue != null )
				return false ;
			if( oldValue != null && newValue == null )
				return false ;
			
			if(oldValue.trim().equals(newValue.trim()))
				continue ;
			else
				return false ;
		}
		
		return true ;
	}
}
