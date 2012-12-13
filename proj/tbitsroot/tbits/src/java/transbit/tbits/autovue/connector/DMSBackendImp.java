package transbit.tbits.autovue.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

import com.cimmetry.vuelink.authentication.AuthorizationException;
import com.cimmetry.vuelink.backend.DMSBackend;
import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.DocID;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.session.DMSBackendSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class DMSBackendImp implements DMSBackend {

	final String USER = "user";
	//Contains all session info
	private Hashtable<String,Object> data = new Hashtable<String, Object>();
    
    private String m_id = null;

    public DMSBackendImp()
    {
    	// do nothing.
    }
//    public DMSBackendImp(final Hashtable<String,Object> info, final String id){
//    	data = info;
//        m_id = id;
//    }

	public String getID() {
		return m_id;
	}

	public Object getAttribute(String attribName) {
		return data.get(attribName);
	}

	public void setAttribute(String attribName, Object attrib) {
		data.put(attribName, attrib);
	}

	public void removeAttribute(String attribName) {
		data.remove(attribName);
	}

	@Override
	public DMSBackendSession connect(Hashtable<String, Object> arg0)
			throws AuthorizationException 
	{
		data = arg0;
		String user = (String) arg0.get(USER);
		return DMSBackendSessionImp.newInstance(user);
	}
	
	public Vector<DocInfo> dmsListMarkups(DMSBackendSession session, DocID docID) throws VuelinkException {
		Vector<DocInfo> docInfos = new Vector<DocInfo>();
		if( !docID.DocID2String().startsWith(DocInfo.ATTACHMENTS) )
		{
			return docInfos;
		}
		else 
		{
			String relPath = docID.DocID2String().replace(DocInfo.ATTACHMENTS,"");
			String folderPath = DocInfo.getMarkupFolder() + relPath ;
			File markupFolder = new File(folderPath);
			if( markupFolder.exists() == false || markupFolder.isDirectory() == false )
				return docInfos;
				
			String markups[] = markupFolder.list();
			for( String markup : markups )
			{
				DocInfo di = new DocInfo( DocInfo.MARKUPS + relPath + "/" + markup );
				docInfos.add(di);
			}
			
			return docInfos;
		}
	}
//    /*
//     * Returns document (folder or file) attributes
//     * @see com.cimmetry.vuelink.filesys.backend.FilesysDMSBackend#getAttributes(com.cimmetry.vuelink.session.DMSBackendSession, com.cimmetry.vuelink.core.DocID)
//     */
//    public DMSProperty getAttributes(DMSBackendSession session, DocID docID) {
//    	Vector<DMSProperty> result = new Vector<DMSProperty>();
//    	try{
//    		Hashtable<String,String> attrs = m_filesysInfo.getAttributes(fsDocID);
//    		Enumeration<String> keys = attrs.keys();
//    		while (keys.hasMoreElements()) {
//    			String key = keys.nextElement();
//    			String value = attrs.get(key);
//    			if (value != null && value.split(";").length > 1) {
//    				result.add(new DMSProperty(key,value.split(";"))); //multi value
//    				
//    			}else {
//    				result.add(new DMSProperty(key,value));   //single value
//    			}
//    		}
//    		
//    	}catch(Exception e){
////    		m_logger.error(DMSDefs.DMS_ERROR_CODE_ERROR , e);
//    		e.printStackTrace();
//    	}
//    	DMSProperty[] answer = new DMSProperty[0];
//    	answer = result.toArray(answer);
//		return new DMSProperty(DMSProperty.CSI_ListAllProperties,answer);
//    }

	public DocID saveMarkup(DMSBackendSession beSession, DocID baseID,
			DocID saveID, String docName, InputStream fIn) throws VuelinkException 
	{
		try
		{
			String markupFolder = null ;
			if( baseID != null ) 
				markupFolder  = baseID.DocID2String().replaceFirst(DocInfo.ATTACHMENTS,"");
			
			if( saveID == null ) // create new markup with name docName
			{
				saveID = new TbitsDocID(DocInfo.MARKUPS + markupFolder + "/" + docName );
			}
			else // save into existing markupID
			{
			}
			
			if( null != markupFolder )
			{
				File mrkFolder = new File(DocInfo.getMarkupFolder() + "/" + markupFolder);
				if( mrkFolder.exists() == false )
					mrkFolder.mkdirs();
			}
			
			File mrkFile = new File( DocInfo.getMarkupFolder() + saveID.DocID2String().replaceFirst(DocInfo.MARKUPS, "") );
			if( mrkFile.exists() == false )
				mrkFile.createNewFile();
			
			OutputStream out = new FileOutputStream(mrkFile);
			
			saveStream(fIn, out);
			
			return saveID;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new VuelinkException(DMSDefs.DMS_ERROR_CODE_ERROR, "Cannot save markup file.", e);
		}
	}
	
	private void saveStream(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] b = new byte[128*1024];
			int read;
			while ((read = in.read(b)) > 0) {
				out.write(b, 0, read);
			}

		} finally {
			out.close();
		}
	}
}
