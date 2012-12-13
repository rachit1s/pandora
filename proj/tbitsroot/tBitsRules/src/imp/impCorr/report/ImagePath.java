package impCorr.report;

import java.io.File;
import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.common.Configuration;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.PropertyManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.PropertyEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class ImagePath implements IReportParamPlugin {

	public String getName() {
		// TODO Auto-generated method stub
		return "Signature File";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		String imageName = co.getUserMapUsers().get(0).getUserLogin() + ".gif" ;
		System.out.println( "imagename = " + imageName ) ;
		
		PropertyEntry sigImageDir = PropertyManager.lookupProperty(GenericParams.PropSignatureImageDir);
		File imageFolder = null;
		if( null != sigImageDir )
		{
			if( null != sigImageDir.getValue() )
			{
				File f = new File(sigImageDir.getValue());
				if( f.exists() == true )
					imageFolder = f;
			}
		}
		if(null == imageFolder)
			imageFolder = Configuration.findPath("tbitsreports");
		
		String imagePath = imageFolder.getAbsolutePath() + File.separator + imageName;
		File imageFile =  new File(imagePath);		
		if( imageFile != null )
		{
			String imageLocation = imageFile.getAbsolutePath() ;
			return imageLocation;
		}
		else return "";
	}

}
