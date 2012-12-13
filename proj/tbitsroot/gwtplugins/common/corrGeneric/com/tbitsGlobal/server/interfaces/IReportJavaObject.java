package corrGeneric.com.tbitsGlobal.server.interfaces;

import java.io.Serializable;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public interface IReportJavaObject extends Serializable 
{
	public static String CORROBJECT = "CorrObject";
	public static String CONNECTION = "Connection";
	public static String REPORTNAMEENTRY = "ReportNameEntry";
	public static final String REPORTPARAMENTRY = "ReportParamEntry";
	
	public void initialize(Hashtable<String,Object> params);
}
