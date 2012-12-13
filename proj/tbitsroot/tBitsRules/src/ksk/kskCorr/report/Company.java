package kskCorr.report;

import java.util.Hashtable;

import kskCorr.others.KskConst;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Company implements IReportParamPlugin {

	public String getName() 
	{
		return "Company";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		FieldNameEntry assFieldEntry = coob.getFieldNameMap().get(GenericParams.RecepientUserTypeFieldName);
		if( null == assFieldEntry )
			throw new CorrException(GenericParams.RecepientUserTypeFieldName + " not configured.");
		
		String assName =  coob.getAsString(assFieldEntry.getBaFieldName());
		if(null == assName )
			throw new CorrException("Null value in field : " + Utility.fdn(coob.getBa(), assFieldEntry.getBaFieldName()));
		
		String[] rec = assName.split(",");
		User recUser = null;
		try {
			recUser = User.lookupByUserLogin(rec[0]);
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String corrProt = coob.getAsString(KskConst.CorrProtocolFieldName);
		Type corrProtType = null;
		try {
			corrProtType = Type.lookupBySystemIdAndFieldNameAndTypeName(coob.getBa().getSystemId(), KskConst.CorrProtocolFieldName, corrProt);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getCompany(coob.getUserMapUsers().get(0),recUser,corrProtType) ;
	}

	public static String getCompany(User logger, User assignee, Type corrProt )
	{		
		String fullFirm = logger.getFullFirmName();//logInfo.get(UserInfoManager.FULL_FIRM_NAME) ;
		if( assignee.getLocation() != null && assignee.getLocation().trim().equalsIgnoreCase(KskConst.SEPCO_FIRM_NAME) && logger.getLocation() != null && logger.getLocation().trim().equalsIgnoreCase(KskConst.WPCL_FIRM_NAME) )
		{
			fullFirm = KskConst.WPCL_FULL_FIRM_NAME ;
		}
		return  "for " + fullFirm ;		
	}
}
