package commons.com.tbitsGlobal.utils.client.fieldconfigs;

import java.util.List;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentFieldContainer;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

/**
 * 
 * @author sourabh
 * 
 * Field Config for Attachment Type Fields
 */
public class AttachmentFieldConfig extends BaseFieldConfig<List<FileClient>, AttachmentFieldContainer>{
	
	public AttachmentFieldConfig(Mode mode, String sysPrefix, TbitsTreeRequestData model, BAFieldAttachment baField) {
		super(baField);
		
		this.field = new AttachmentFieldContainer(mode, sysPrefix, model, baField);
	}

	@SuppressWarnings("unchecked")
	public POJOAttachment getPOJO() {
		List<FileClient> files = field.getUploadProgressGrid().getFiles();
		if(files == null)
			return null;
		return new POJOAttachment(files);
	}

	public List<FileClient> getValue() {
		List<FileClient> files = field.getUploadProgressGrid().getFiles();
		return files;
	}

	public <T extends POJO<List<FileClient>>> void setPOJO(T pojo) {
		List<FileClient> attachments = pojo.getValue();
		field.setFiles(attachments);
	}

	public void setValue(List<FileClient> value) {
		field.setFiles(value);
	}

	public void clear() {
		
	}
}
