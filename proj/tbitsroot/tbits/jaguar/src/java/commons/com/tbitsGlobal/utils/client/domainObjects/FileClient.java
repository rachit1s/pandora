package commons.com.tbitsGlobal.utils.client.domainObjects;

import com.google.gwt.core.client.GWT;
import commons.com.tbitsGlobal.utils.client.ClientUtils;

public class FileClient extends AttachmentInfoClient{
	public static String SYS_PREFIX			= "sys_prefix";
	public static String REQUEST_ID			= "request_id";
	public static String FIELD_ID 			= "field_id";
	public static String STATUS 			= "status";

	public static String STATUS_UPLOADING 	= "Cancel";
	public static String STATUS_UPLOADED	= "Remove";
	public static String STATUS_ERROR 		= "Try Again";
	public static String STATUS_CANCELLED 	= "Cancelled";
	
	private String error = "";
	
	public FileClient() {
		super();
		
		this.setSysPrefix("");
		this.setRequestId(0);
		this.setFieldId(0);
	}
	
	public FileClient(AttachmentInfoClient attachment){
		this();
		
		for(String property : attachment.getPropertyNames()){
			this.set(property, attachment.get(property));
		}
		
		this.setSysPrefix("");
		this.setRequestId(0);
		this.setFieldId(0);
	}
	
	public String getSysPrefix() {
		return (String) this.get(SYS_PREFIX);
	}

	public void setSysPrefix(String sysPrefix) {
		this.set(SYS_PREFIX, sysPrefix);
	}

	public void setRequestId(int requestId) {
		this.set(REQUEST_ID, requestId);
	}

	public int getRequestId() {
		return (Integer) this.get(REQUEST_ID);
	}

	public void setFieldId(int fieldId) {
		this.set(FIELD_ID, fieldId);
	}

	public int getFieldId() {
		return (Integer) this.get(FIELD_ID);
	}

	public void setStatus(String value) {
		this.set(STATUS, value);
	}

	public void setUploaded() {
		this.setStatus(STATUS_UPLOADED);
	}

	public void setUploading() {
		this.setStatus(STATUS_UPLOADING);
	}

	public void setFailed() {
		this.setStatus(STATUS_ERROR);
	}
	
	public void setCancelled() {
		this.setStatus(STATUS_CANCELLED);
	}

	public String getStatus() {
		return (String) this.get(STATUS);
	}
	
	/**
	 * Can be called on the client side only
	 */
	@Override
	public String toString() {
//		if (this.getRepoFileId() != 0) {
//			return GlobalConstants.CONTEXT_PATH + "/read-attachment/" + "?filerepoid=" + this.getRepoFileId();
//		}
		String sysPrefix = this.getSysPrefix();
		if(GWT.isClient() && sysPrefix == null)
			sysPrefix = ClientUtils.getSysPrefix();
		return ClientUtils.getUrlToFilefromBase("/read-attachment/"
				+ sysPrefix + "?request_id=" + this.getRequestId() + "&request_file_id="
				+ this.getRequestFileId() + "&field_id=" + this.getFieldId());
	}
	
	public String getAnchor(){
		if(this.getRequestId() == 0 || this.getRequestFileId() == 0 || this.getFieldId() == 0)
			return this.getFileName();
		return "<a target='_blank' href='" + this.toString() + "'>" + this.getFileName() + "</a>";
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}
}
