package kskCorr.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

import transbit.tbits.api.AttachmentInfo;
import corrGeneric.com.tbitsGlobal.server.interfaces.IReportParamPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class Attachments implements IReportParamPlugin {

	public static String getAttachList( Collection<AttachmentInfo> cur, Collection<AttachmentInfo> prev )
	{
		String retStr = "" ;
		if( null == cur )
			return retStr ;
		
		for( AttachmentInfo currInfo : cur )
    	{
    		boolean incl = true ;
    		if( null != prev )
	    		for( AttachmentInfo prevInfo : prev )
	    		{
	    			if( prevInfo.repoFileId == currInfo.repoFileId
	    				&& 
	    				prevInfo.requestFileId == prevInfo.requestFileId )
	    			{
	    				incl = false ;
	    				break ;
	    			}
	    		}
    		
    		if( incl )
    			retStr += currInfo.name + "<br />" ;
    	}
		
		return retStr ;
	}
	
	public String getName() {
		return "The diff of previous and current attachments";
	}

	public String getReportParam(Hashtable<String, Object> params)
			throws CorrException
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		
		FieldNameEntry attFieldEntry = coob.getFieldNameMap().get(GenericParams.OtherAttachmentFieldName);
		if( null == attFieldEntry )
			throw new CorrException(GenericParams.OtherAttachmentFieldName + " was not configured.");
		
		String otherAttFieldName = attFieldEntry.getBaFieldName();
		Collection<AttachmentInfo> currAtts = null;
		
		if( coob.getSource() == CorrObject.SourcePreview )
		{
			POJOAttachment pa = (POJOAttachment) coob.getTtrd().getAsPOJO(otherAttFieldName);
			if( null != pa )
			{
				List<FileClient> ca = pa.getValue();
				currAtts = toAttaInfo( ca );
			}
		}
		else
		{
			currAtts = (Collection<AttachmentInfo>) coob.getRequest().getObject(otherAttFieldName);
		}
		
		Collection<AttachmentInfo> prevAtts = null;
		if( coob.getType() == CorrObject.TypeUpdateRequest)
			prevAtts = (Collection<AttachmentInfo>) coob.getPrevRequest().getObject(otherAttFieldName);
		
		return getAttachList(currAtts, prevAtts);
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
