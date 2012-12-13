package testCorr.report;

import java.sql.Connection;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.domain.ReportNameEntry;
import corrGeneric.com.tbitsGlobal.server.domain.ReportParamEntry;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportJavaObject;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class ReportJavaObjectImpl implements IReportJavaObject 
{
	String x ;

	/** this is the method that can be called from report
	 * 
	 * @return
	 */
	public String getText()
	{
		return x; 
	}

	public void initialize(Hashtable<String, Object> params) 
	{
		Connection con = (Connection) params.get(CONNECTION);
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		ReportNameEntry rne = (ReportNameEntry) params.get(REPORTNAMEENTRY);
		ReportParamEntry rpe = (ReportParamEntry) params.get(REPORTPARAMENTRY);
		
		x = "connection = " + con + "\ncoob = " + coob + "\nrne = " + rne + "\nrpe = " + rpe;
	}
}
