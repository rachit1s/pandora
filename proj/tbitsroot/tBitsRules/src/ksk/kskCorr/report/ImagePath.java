package kskCorr.report;

import java.io.File;
import java.util.Hashtable;

import transbit.tbits.common.Configuration;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class ImagePath implements IReportParamPlugin {

	public String getName() {
		return "The path to the signature file of the logger.";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		String imageName = coob.getUserMapUsers().get(0).getUserLogin() + ".gif" ;
		System.out.println( "imagename = " + imageName ) ;
		File imageFile = Configuration.findPath("tbitsreports/" + imageName);			
		if( imageFile != null )
		{
			return imageFile.getAbsolutePath() ;
		}	
		else
			return "";
	}

}
