package kskCorr.report;

import java.util.Hashtable;

import kskCorr.others.GenReportHelper;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class KindAtt implements IReportParamPlugin {

	public String getName() {
		return "KindAtt";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject)params.get(CORROBJECT);
		FieldNameEntry recEntry = coob.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName);
	
		if( null == recEntry )
			throw new CorrException(GenericParams.RecepientUserTypeFieldName + " not configured.");
		
		String asses = coob.getAsString(recEntry.getBaFieldName());
		if( null == asses )
			throw new CorrException("Null value not allowed in field " + Utility.fdn(coob.getBa(), recEntry.getBaFieldName()));
		
		String [] recs = asses.split(",");
		User user = null;
		try {
			user = User.lookupByUserLogin(recs[0]);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( null == user )
			throw new CorrException("Illegal value in field : " + Utility.fdn(coob.getBa(), recEntry.getBaFieldName()) + ". It should contain the recepient user." );
	
		return getKindAtt(user);
	}
	
	public static String getKindAtt( User user )
	{
		String name = GenReportHelper.getName(user) ;
		String designation = user.getDesignation(); //info.get(UserInfoManager.DESIGNATION) ;
		String kind_att = "" ;
		String sex =  user.getSex();//info.get(UserInfoManager.SEX ) ;
		if(sex.equalsIgnoreCase("M"))
			kind_att += "Mr. " ;
		else if( sex.equalsIgnoreCase("F"))
			kind_att += "Ms. " ;
				
		kind_att += name ;
		kind_att += ", " + designation ;
		
		return kind_att ;
	}

}
