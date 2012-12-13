package nccCorr.report;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.api.AttachmentInfo;

import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class Attachments implements IReportParamPlugin {

	public String getName() {
		return "attach";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException 
	{
		Connection con = (Connection) params.get(IReportParamPlugin.CONNECTION);
		CorrObject co = (CorrObject) params.get(IReportParamPlugin.CORROBJECT);
		
		return getAttachList(co, null);
	}
	
	public static String getAttachList( CorrObject co, CorrObject prevCo ) throws CorrException 
	{   
		String fileNames = "" ;
//		FieldNameEntry oattEntry = co.getFieldNameMap().get(GenericParams.OtherAttachmentFieldName);
//		if( null == oattEntry || null == oattEntry.getBaFieldName() )
//			throw new CorrException("The field : " + GenericParams.OtherAttachmentFieldName + " is not configured for the ba : " + co.getBa().getSystemPrefix());
//		
		String otherAttFieldName = "attachments";//oattEntry.getBaFieldName();
		Collection<AttachmentInfo> infos = null;
		
		if( co.getSource() == CorrObject.SourcePreview )
		{
			POJOAttachment pa = (POJOAttachment) co.getTtrd().getAsPOJO(otherAttFieldName);
			if( null != pa )
			{
				List<FileClient> ca = pa.getValue();
				infos = toAttaInfo( ca );
			}
		}
		else
		{
			infos = (Collection<AttachmentInfo>) co.getRequest().getObject(otherAttFieldName);
		}
		
		if( null == infos )
			return fileNames ;
      
        	for( AttachmentInfo nai : infos )
        	{  
        		fileNames += nai.name + "<br />" ;
        	}
		return fileNames;		
	}

	private static Collection<AttachmentInfo> toAttaInfo(
			Collection<FileClient> ca) 
	{
		if( null == ca )
			return null ;
		
		ArrayList<AttachmentInfo> aai = new ArrayList<AttachmentInfo>(ca.size());
		
		for( AttachmentInfoClient aif : ca )
		{
			AttachmentInfo ai = new AttachmentInfo(aif.getFileName(), aif.getRepoFileId(), aif.getRequestFileId(), aif.getSize());
			aai.add(ai);
		}
		
		return aai;
	}

}
