package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.IUploadStatus;

import com.google.gwt.user.client.Window;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public class TbitsEditUploadStatus extends TbitsUploadStatus{
	private AttachmentGrid grid;
	public TbitsEditUploadStatus(IUploadProgress progress, AttachmentGrid grid) {
		super(progress);
		
		this.grid = grid;
	}

	@Override
	protected FileClient getAttachment() {
		FileClient att = grid.getSelectionModel().getSelectedItem();
		if(att == null)
			Window.alert("No file has been selected to edit");
		assert att != null : "No file has been selected to edit";
		
		if(att.getStatus().equals(FileClient.STATUS_UPLOADING)){
			Window.alert("File is already being edited");
		}
		assert !att.getStatus().equals(FileClient.STATUS_UPLOADING) : "File is already being edited";
		
		return att;
	}

	@Override
	public IUploadStatus newInstance() {
		return new TbitsEditUploadStatus(this.progress, this.grid);
	}

}