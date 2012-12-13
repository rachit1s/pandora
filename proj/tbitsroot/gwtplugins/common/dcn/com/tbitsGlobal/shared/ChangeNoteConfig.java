/**
 * 
 */
package dcn.com.tbitsGlobal.shared;

import java.io.Serializable;

/**
 * @author Lokesh
 *
 */
public class ChangeNoteConfig implements Serializable {
	
	private String srcSysPrefix;
	private String baType;
	private String targetSysPrefix;
	private String caption;
	private String templateName;
	private int srcAttachmentFieldId;
	private int targetAttachmentFieldId;
	private int changeNoteId;
	private String updateSysPrefix;
	
	private ChangeNoteConfig(){}
	
	public ChangeNoteConfig(int changeNoteId, String srcSysPrefix, String baType, String targetSysPrefix, String caption, 
			String templateName, int srcAttachmentFieldId, int targetAttachmentFieldId,String updateSysPrefix ){
		this.setChangeNoteId(changeNoteId);
		this.srcSysPrefix = srcSysPrefix;
		this.baType = baType;
		this.targetSysPrefix = targetSysPrefix;
		this.caption = caption;
		this.templateName = templateName;
		this.srcAttachmentFieldId = srcAttachmentFieldId;
		this.targetAttachmentFieldId = targetAttachmentFieldId;
		this.updateSysPrefix = updateSysPrefix;
	}

	public void setChangeNoteId(int changeNoteId) {
		this.changeNoteId = changeNoteId;
	}

	public int getChangeNoteId() {
		return changeNoteId;
	}
	
	public void setSrcSysPrefix(String srcSysPrefix) {
		this.srcSysPrefix = srcSysPrefix;
	}

	public String getSrcSysPrefix() {
		return srcSysPrefix;
	}

	public void setBaType(String baType) {
		this.baType = baType;
	}

	public String getBaType() {
		return baType;
	}

	public void setTargetSysPrefix(String targetSysPrefix) {
		this.targetSysPrefix = targetSysPrefix;
	}

	public String getTargetSysPrefix() {
		return targetSysPrefix;
	}
	
	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}
		
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateName() {
		return templateName;
	}
	
	public void setSrcAttachmentFieldId(int srcAttachmentFieldId) {
		this.srcAttachmentFieldId = srcAttachmentFieldId;
	}

	public int getSrcAttachmentFieldId() {
		return srcAttachmentFieldId;
	}

	public void setTargetAttachmentFieldId(int targetAttachmentFieldId) {
		this.targetAttachmentFieldId = targetAttachmentFieldId;
	}

	public int getTargetAttachmentFieldId() {
		return targetAttachmentFieldId;
	}
	 
	// accessor method of updateSysPrefix
	
	public String getUpdateSysPrefix()
	{
		return updateSysPrefix;
	}

	public void setUpdateSysPrefix(String updateSysPrefix)
	{
		this.updateSysPrefix = updateSysPrefix;
	}
	
	public String toString(){
		return "[change_note_id=" + this.changeNoteId + ", src_sys_prefix=" + this.srcSysPrefix + ", ba_type=" + this.baType 
			+ ", target_sys_prefix=" + this.targetSysPrefix + ", caption=" + this.caption  
			+ ", template_name=" + this.templateName + ", src_attachment_field_id=" 
			+ this.srcAttachmentFieldId + ", target_attachment_field_id="
			+ this.targetAttachmentFieldId + ", update_SysPrefix=" + this.updateSysPrefix +"]";
	}	
}
