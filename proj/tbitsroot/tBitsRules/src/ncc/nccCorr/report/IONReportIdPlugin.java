package nccCorr.report;

import java.sql.Connection;
import java.util.Hashtable;

import nccCorr.others.CorresConstants;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportIdPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;

public class IONReportIdPlugin implements IReportIdPlugin {

	@Override
	public String getName() {
		return "returns report id = 8 for type = ION";
	}

	@Override
	public double getOrder() {
		return 0;
	}

	@Override
	public Integer getReportId(Hashtable<String, Object> params) 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		Connection con = (Connection) params.get(CONNECTION);

		if( !coob.getBa().getSystemPrefix().equals(CorresConstants.CORR_SYSPREFIX) )
			return null;
		
		String corrType = coob.getAsString(CorresConstants.CORR_CORR_TYPE_FIELD_NAME);
		if( null != corrType && corrType.equals(CorresConstants.CORR_CORR_TYPE_ION))
		{
			return 8 ; 
		}
		
		return null;
	}

}
