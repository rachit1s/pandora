package transbit.tbits.TVN;

import transbit.tbits.api.AttachmentInfo;

public class FileAction {
	public FileAction(AttachmentInfo attachmentInfo, String fileAction,
			int fieldId) {
		super();
		this.attachmentInfo = attachmentInfo;
		FileAction = fileAction;
		this.fieldId = fieldId;
	}
	private AttachmentInfo attachmentInfo;
	private String FileAction;
	private int fieldId;
	public void setAttachmentInfo(AttachmentInfo attachmentInfo) {
		this.attachmentInfo = attachmentInfo;
	}
	public AttachmentInfo getAttachmentInfo() {
		return attachmentInfo;
	}
	public void setFileAction(String fileAction) {
		FileAction = fileAction;
	}
	public String getFileAction() {
		return FileAction;
	}
	public void setFieldId(int fieldId) {
		this.fieldId = fieldId;
	}
	public int getFieldId() {
		return fieldId;
	}
}
