package kskCorr.report;

import java.util.Hashtable;

import kskCorr.others.GenReportHelper;
import kskCorr.others.KskConst;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class CC implements IReportParamPlugin 
{
	public String getName() {
		return "CC";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		String subList = coob.getAsString(KskConst.SubscriberFieldName);
		
		return getCCs(subList);
	}

	/**
	 * takes the input list of cc as userLogins and generates html code of their complete name
	 * @param ccs
	 * @return
	 */
	public static String getCCs( String ccs ) 
	{	
		if( null == ccs || ccs.trim().equals("") )
			return "" ;
		
		String ccList = "<br>" ;
		String[] ccArray = ccs.split(",") ;
		for( int i = 0 ; i < ccArray.length ; i++ )
		{
			String ccName = ccArray[i] ;
			if( null == ccName || ccName.trim().equals("") ) 
				continue ;
			ccList += GenReportHelper.getNameDesignation(ccName) + "<br>";
		}
		
		return ccList ;
	}
}
