package transbit.tbits.autovue.connector;

/**
 * Services that a document object must implement
 * 
 * @author Cimmetry Systems Inc.
 * @version 19.1
 * @since	19.1
 */
import java.io.File;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.DocID;
import com.cimmetry.vuelink.defs.VuelinkException;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class DocInfo{
	private File file; 
    private DocID docId ;
    private boolean isAttachment = true ;
    private TBitsFileInfo fileInfo = null;
    
    public static final String MARKUPS = "markups";
    public static final String ATTACHMENTS = "attachments"; //PropertiesHandler.getProperty("transbit.tbits.attachmentdir");
//    public static final String MARKUPDIR = "build";
    
    public static String getMarkupFolder()
    {
    	String markupFolderProp = PropertiesHandler.getProperty(TBitsPropEnum.KEY_MARKUPDIR);
    	String markupPath = null;
    	
    	if(!(null == markupFolderProp || markupFolderProp.trim().equals("")))
    			markupPath = Configuration.findAbsolutePath(markupFolderProp);
    	
    	if( null == markupPath || markupPath.trim().equals(""))
    		markupPath = Configuration.getAppHome().getAbsolutePath() + File.separator + MARKUPS;
    	
    	return markupPath ;
    }

    public static String getAttachmentsFolder()
    {
//    	String attachmentProp = PropertiesHandler.getProperty(TBitsPropEnum.KEY_ATTACHMENTDIR);
//    	String attachmentPath = null;
//    	if( null != attachmentProp && !attachmentProp.trim().equals("") )
    	String	attachmentPath =  APIUtil.getAttachmentLocation();//Configuration.findAbsolutePath(attachmentProp);
    	
    	if( null == attachmentPath || attachmentPath.trim().equals("" ))
    		attachmentPath = Configuration.getAppHome().getAbsolutePath() + File.separator + ATTACHMENTS;
    	
    	return attachmentPath ;
    }
    public static TBitsFileInfo getAttachmentInfo( String sysPrefix, String requestId, String actionId, String fieldId, String requestFileId) throws VuelinkException
    {
    	try
    	{
    		BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
    		int reqId  = Integer.parseInt(requestId);
    		int actId = Integer.parseInt(actionId);
    		int fId = Integer.parseInt(fieldId);
    		int reqFileId = Integer.parseInt(requestFileId);
    		
    		TBitsFileInfo fileInfo = Uploader.getFileInfo(ba.getSystemId(), reqId, actId, reqFileId, fId);
    		
    		return fileInfo;
    	}
    	
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new VuelinkException(DMSDefs.DMS_ERROR_CODE_ERROR,"Cannot Find Attachment");
    	}
    }
    
    public static TBitsFileInfo getAttachmentInfoFromDocId(String docId) throws VuelinkException
    {
    	if( !docId.startsWith(ATTACHMENTS))
    		throw new VuelinkException(DMSDefs.DMS_ERROR_CODE_ERROR,"This is not a attachment : " + docId);
    	
    	String[] parts = docId.split("/");
    	return getAttachmentInfo(parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
    
    public DocInfo(String docId) throws VuelinkException
    {
    	this.docId = new TbitsDocID(docId);
    	if( docId.startsWith(ATTACHMENTS) )
    	{
    		// this is attachment so search with tbits
    		String[] parts = docId.split("/");
    		isAttachment = true;
    		fileInfo = getAttachmentInfo(parts[1], parts[2], parts[3], parts[4], parts[5]);
    		file = new File(getAttachmentsFolder() + "/" + fileInfo.getFileLocation());
    		
    	}
    	else if( docId.startsWith(MARKUPS)) // this is a markup, it starts with "markups/"
    	{
    		String path = getMarkupFolder()  + docId.replaceFirst(MARKUPS, "") ;
    		file = new File(path);
    		isAttachment = false ;
    	}
    	else throw new VuelinkException(DMSDefs.DMS_ERROR_CODE_ERROR,"Illegal format of DocID : it does not starts with attachments or markups.");
    }

    public DocInfo(DocID docId) throws VuelinkException
    {
    	this(docId.DocID2String());
    }
    
	public File getFile() { 
		return file;
	}

	public DocID getDocID() {
		return docId;
	}
	
	public String getName() 
	{
		if( isAttachment )
		{
			return fileInfo.getFileName();
		}
		else
			return file.getName();
	}
	
	public boolean isAttachment()
	{
		return isAttachment;
	}
}
