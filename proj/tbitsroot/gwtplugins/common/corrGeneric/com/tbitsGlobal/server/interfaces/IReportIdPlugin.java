package corrGeneric.com.tbitsGlobal.server.interfaces;

import java.util.Hashtable;

public interface IReportIdPlugin 
{
	public static final String CORROBJECT = "CorrObject";
	public static final String CONNECTION = "Connection";
	public static final String REPORTENTRY = "ReportEntry";
	
	Integer getReportId(Hashtable<String,Object> params);
	String getName();
	double getOrder();	
}
