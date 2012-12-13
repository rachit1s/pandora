package transbit.tbits.common;

import java.io.Serializable;

public class ActionFileInfo implements Cloneable, Serializable {
	public ActionFileInfo(String name, String fileAction, int fieldId,
			int requestFileId) {
		super();
		this.name = name;
		this.fileAction = fileAction;
		this.fieldId = fieldId;
		this.requestFileId = requestFileId;
		this.fileId = null ;
	}
	
	public ActionFileInfo(int sysId, int reqId, int actionId, String name, String fileAction, int fieldId,
			Integer fileId, int requestFileId, String location, int size) 
	{
		this.systemId = sysId ;
		this.requestId = reqId ;
		this.actionId = actionId;
		this.name = name;
		this.fileAction = fileAction;
		this.fieldId = fieldId;
		this.requestFileId = requestFileId;
		this.fileId = fileId ;
		this.location = location ;
		this.size = size;
	}
	
	public ActionFileInfo(int sysId, int reqId, int actionId, String name, String fileAction, int fieldId,
			Integer fileId, int requestFileId, String location, int size, int priority) 
	{
		this.systemId = sysId ;
		this.requestId = reqId ;
		this.actionId = actionId;
		this.name = name;
		this.fileAction = fileAction;
		this.fieldId = fieldId;
		this.requestFileId = requestFileId;
		this.fileId = fileId ;
		this.location = location ;
		this.size = size;
		this.priority = priority ;
	}
	public ActionFileInfo(String name, String fileAction, int fieldId,
			int requestFileId, int size) {
		super();
		this.name = name;
		this.fileAction = fileAction;
		this.fieldId = fieldId;
		this.requestFileId = requestFileId;
		this.size = size;
		this.fileId = null ;
	}

	public ActionFileInfo( String name, String fileAction, int fieldId, int requestFileId, int size , Integer fileId )
	{
		this.name = name;
		this.fileAction = fileAction;
		this.fieldId = fieldId;
		this.requestFileId = requestFileId;
		this.size = size;
		this.fileId = fileId ;
	}
	
	public ActionFileInfo(int sys_id, int request_id, int action_id, String name, String file_action, 
			int field_id, int file_id, int request_file_id, String location, int size, String hash, int security_code) {

		this.systemId = sys_id ;
		this.requestId = request_id ;
		this.actionId = action_id;
		this.name = name;
		this.fileAction = file_action;
		this.fieldId = field_id;
		this.requestFileId = request_file_id;
		this.fileId = file_id ;
		this.location = location ;
		this.size = size;
		this.hash = hash;
		this.securityCode = security_code;
	}

	private String name;
	private String fileAction;
	private int fieldId;
	private int requestFileId;
	private int size;
	private Integer fileId ;
	private String location = null ;
	private double priority = 0 ; 
	private int systemId ;
	private int requestId ;
	private int actionId ;
	private String hash;
	private int securityCode;
	private boolean isAnonymousDownload = false;
	
	// whether the file finally goes as attachment into the e-mail.
	private boolean isAttached = false;

	public boolean isAttached() {
		return isAttached;
	}

	public void setAttached(boolean isAttached) {
		this.isAttached = isAttached;
	}

	public int getSystemId()
	{
		return systemId ;
	}
	public int getRequestId()
	{
		return requestId;
	}
	public int getActionId()
	{
		return actionId;
	}
	public String getName() {
		return name;
	}

	public double getPriority()
	{
		return priority ;
	}
	
	public void setPriority(double p)
	{
		priority = p ;
	}
	
	public void setLocation(String loc)
	{
		location = loc;
	}
	
	public String getLocation()
	{
		return location ;
	}
	
	public Integer getFileId()
	{
		return fileId ;
	}

	public String getFileAction() {
		return fileAction;
	}

	public int getFieldId() {
		return fieldId;
	}

	public int getRequestFileId() {
		return requestFileId;
	}

	public int getSize() {
		return size;
	}
	
	public ActionFileInfo clone() throws CloneNotSupportedException
	{
		return (ActionFileInfo) super.clone() ;
	}
	
	public String toString()
	{
		return "{Name:" + name + ",fileAction:" + fileAction + ",fieldId:" +fieldId + 
				",requestFileId:" + requestFileId + ",size:" + size + ",location:" + location + 
				",priority:" + priority + ",hash:" + hash + ",securityCode:" + securityCode + "}";
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public void setSecurityCode(int securityCode) {
		this.securityCode = securityCode;
	}

	public int getSecurityCode() {
		return securityCode;
	}

	public void setAnonymousDownload(boolean isAnonymousDownload) {
		this.isAnonymousDownload = isAnonymousDownload;
	}

	public boolean isAnonymousDownload() {
		return isAnonymousDownload;
	}	
}
