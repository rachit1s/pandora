package corrGeneric.com.tbitsGlobal.server.interfaces;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public interface IReportParamPlugin 
{
	public static final String CONNECTION = "Connection";
	public static final String CORROBJECT = "CorrObject";
	public static final String REPORTNAMEENTRY = "ReportNameEntry";
	public static final String REPORTPARAMENTRY = "ReportParamEntry";
	String getReportParam(Hashtable<String,Object> params) throws CorrException;
	String getName();
}
