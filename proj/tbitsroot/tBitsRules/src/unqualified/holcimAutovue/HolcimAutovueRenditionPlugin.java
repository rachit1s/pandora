package holcimAutovue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IAutovueOnRendition;
import transbit.tbits.api.UpdateRequest;
import transbit.tbits.autovue.connector.ActionContext;
import transbit.tbits.autovue.connector.DocInfo;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.APIException;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;


public class HolcimAutovueRenditionPlugin implements IAutovueOnRendition{

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
	
	public static TBitsFileInfo getAttachmentInfoFromDocId(DMSQuery query) throws VuelinkException
    {
		String docId = null;
		final Property[] props = query.getDMSArgsProperties();
		for( Property prop : props )
		{
			if( prop.getName().equals(Property.CSI_BaseDocID ) )
			{ 
				docId = prop.getValue();
			}
		}

		if( !docId.startsWith(DocInfo.ATTACHMENTS))
    		throw new VuelinkException(DMSDefs.DMS_ERROR_CODE_ERROR,"This is not a attachment : " + docId);
    	
    	String[] parts = docId.split("/");
    	return DocInfo.getAttachmentInfo(parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
    
	private String saveFileAndUpdate(DMSQuery query, InputStream fIn) throws Exception 
	{
		File tmpFile = File.createTempFile("rendition", ".pdf");
		tmpFile.deleteOnExit();
		
		OutputStream out = new FileOutputStream(tmpFile);
		saveStream(fIn, out);

		System.out.println("Your file has been saved at : " + tmpFile.getAbsolutePath());
		// upload the file and update the request
		String url = query.getOriginalURL();
		String[] parts = url.split("/");
		BusinessArea ba = BusinessArea.lookupBySystemPrefix(parts[1]);
		Field fromField = Field.lookupBySystemIdAndFieldId(ba.getSystemId(),Integer.parseInt(parts[4]));
		String holcimFieldName = "VendorSubmissionFile";
		Field holcimField = null;
		if( null != holcimFieldName )
			holcimField = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), holcimFieldName);
		
		if( null == holcimField )
		{
			System.out.println("Field to upload not found. Uploading in the same field.");
			holcimField = fromField;
		}
		
		String docId = query.getDocID();
		TBitsFileInfo fileInfo = getAttachmentInfoFromDocId(query);
		
		Uploader uploader = new Uploader(0,0,ba.getSystemPrefix());
		String name = "decision_"+fileInfo.getFileName() ;
		
		if( !name.endsWith(".pdf"))
			name = name +".pdf";
		
		AttachmentInfo ai = uploader.copyIntoRepository(tmpFile,name);
		
	
		Collection<AttachmentInfo> cai = new ArrayList<AttachmentInfo>();
		cai.add(ai);
		String attInfo = AttachmentInfo.toJson(cai);
		
		Hashtable<String,String> params = new Hashtable<String,String>();
		params.put(Field.BUSINESS_AREA, ba.getSystemPrefix());
		params.put(Field.REQUEST, parts[2]);
		params.put(holcimField.getName(),attInfo);
		params.put(Field.USER, "root");
		
		UpdateRequest ur = new UpdateRequest();
		Request req = null;
		try {
			req = ur.updateRequest(params);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		return DocInfo.ATTACHMENTS + "/" + ba.getSystemPrefix() + "/" + req.getRequestId() + "/" +
				req.getMaxActionId() + "/" + holcimField.getFieldId() + "/" + 1 ;  
	}
	public Object execute(ActionContext context, DMSSession session,
			DMSQuery query, DMSArgument[] args, InputStream in) throws Exception {
		return saveFileAndUpdate(query, in);
	}
	public String getDescription() {
		return "Saves the rendition file uploads it in tbits and updates the request from which rendition has been received.";
	}
	public String getName() {
		return "AppendRenditionFileAsAttachments";
	}
	public double getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

}
