package zipuploader.com.tbitsGlobal.client;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentGrid;
import commons.com.tbitsGlobal.utils.client.Uploader.IUploadContainerCallback;
import commons.com.tbitsGlobal.utils.client.Uploader.IUploadProgress;
import commons.com.tbitsGlobal.utils.client.Uploader.IUploaderProgressCount;
import commons.com.tbitsGlobal.utils.client.Uploader.TbitsAddUploadStatus;
import commons.com.tbitsGlobal.utils.client.Uploader.TbitsMultiUploader;
import commons.com.tbitsGlobal.utils.client.Uploader.UploadProgressAttachmentGrid;
import commons.com.tbitsGlobal.utils.client.grids.AutoHeightGridContainer;

public class HolcimAttachmentFieldContainer extends FieldSet {
	private IUploaderProgressCount addUploader;
	private IUploaderProgressCount editUploader;
	private AttachmentGrid uploadProgressGrid;
	

	
	private String heading;
	
	public HolcimAttachmentFieldContainer(HolcimWizard parentWizard) {
		super();
		
		
		
		LayoutContainer uploaderContainer = new LayoutContainer(new ColumnLayout());
		
//		Log.info("Inside Grid");
	
			uploadProgressGrid = new UploadProgressAttachmentGrid(Mode.EDIT);
			
			
			addUploader = new HolcimMultiUploader("", 0, 0, new TbitsAddUploadStatus((IUploadProgress) uploadProgressGrid), TbitsMultiUploader.ADD_FILES,parentWizard);
			uploaderContainer.add((HolcimMultiUploader)addUploader, new ColumnData(75));
				
		
		AutoHeightGridContainer container = new AutoHeightGridContainer(uploadProgressGrid,false);
		this.add(container);
		this.add(uploaderContainer);
		
		
		
	}
	
	public int getInProgressUploads(){
		return this.addUploader.getInProgressUploads() +
			this.editUploader.getInProgressUploads();
	}
	
	public int getQueuedUploads(){
		return this.addUploader.getQueuedUploads() + 
			this.editUploader.getQueuedUploads();
	}
	
	public void setCallback(IUploadContainerCallback callback) {
		this.uploadProgressGrid.setCallback(callback);
	}

	public IUploadContainerCallback getCallback() {
		return this.uploadProgressGrid.getCallback();
	}

	public AttachmentGrid getUploadProgressGrid() {
		return uploadProgressGrid;
	}
	
	
	

}
