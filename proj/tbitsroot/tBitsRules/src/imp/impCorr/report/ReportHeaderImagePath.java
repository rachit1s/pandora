package impCorr.report;
import java.io.File;
import java.util.Hashtable;

import transbit.tbits.common.Configuration;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;


public class ReportHeaderImagePath implements IReportParamPlugin {

	public String getName() 
	{
		return "return the header for the report";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		Type agency = coob.getGenerationAgency();
		if( null == agency )
			throw new CorrException("Generation Agency was null.");
		
		String agencyName = agency.getName();
		PropertyEntry imageDir = PropertyManager.lookupProperty(GenericParams.PropReportImageDir);
		File imageFolder = null;
		if( null != imageDir )
		{
			if( null != imageDir.getValue() )
			{
				File f = new File(imageDir.getValue());
				if( f.exists() == true )
					imageFolder = f;
			}
		}

		if( null == imageFolder)
			imageFolder = Configuration.findPath("tbitsreports");
		
		File imageFile = new File(imageFolder.getAbsolutePath() + File.separator + coob.getBa().getSystemPrefix() + "_" + agencyName+"_header.gif");
		if( imageFile != null && imageFile.exists() )
			return imageFile.getAbsolutePath();
		else return "";
	}

}
