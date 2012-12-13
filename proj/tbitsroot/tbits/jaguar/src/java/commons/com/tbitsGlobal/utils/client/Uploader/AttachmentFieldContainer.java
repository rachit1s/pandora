package commons.com.tbitsGlobal.utils.client.Uploader;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.GlobalConstants;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsInfo;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.Uploader.gears.TbitsGearsUploader;
import commons.com.tbitsGlobal.utils.client.Uploader.gears.UploadProgessAttachmentGridGears;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.grids.AutoHeightGridContainer;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

/**
 * Attachment fields container contains the complete uploader widget. 
 * Apart from the UI buttons such as "Add", "Edit file", "Download as zip", "paste", it has the instances of file uploaders (addUploader, editUploader).
 * It also has the Grid for displaying the attachments.
 * 
 * It operates in two modes: 1. for viewing attachments (in view request) and 2. for uploading (in the update/add request)
 *  
 * @author sandeepgiri
 *
 */
public class AttachmentFieldContainer extends FieldSet{
	private IUploaderProgressCount addUploader;
	private IUploaderProgressCount editUploader;
	private AttachmentGrid uploadProgressGrid;
	
	private String sysPrefix;
	private int requestId;
	
	private String heading;
	
	public AttachmentFieldContainer(Mode mode, String sysPrefix, TbitsTreeRequestData model, final BAFieldAttachment field) {
		super();
		
		this.heading = field.getDisplayName();
		
		this.setHeading(heading);
		this.setCollapsible(true);
		
		this.sysPrefix = sysPrefix;
		this.requestId = 0;
		
		int actionId = 0;
		
		if(model != null)
		{
			requestId = model.getRequestId();
			actionId = model.getMaxActionId();
		}
		
		LayoutContainer uploaderContainer = new LayoutContainer(new ColumnLayout());
		
		try{
			uploadProgressGrid = new UploadProgessAttachmentGridGears(mode);
			if(mode == Mode.EDIT){
				addUploader = new TbitsGearsUploader(sysPrefix, requestId, (UploadProgessAttachmentGridGears) uploadProgressGrid);
				editUploader = new TbitsGearsUploader(sysPrefix, requestId, (UploadProgessAttachmentGridGears) uploadProgressGrid);
			
				Button addButton = new Button("Add Files...", new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent ce) {
						((TbitsGearsUploader) addUploader).openFiles();
					}});
				Button editButton = new Button("Edit Files...", new SelectionListener<ButtonEvent>(){
					public void componentSelected(ButtonEvent ce) {
						((TbitsGearsUploader) editUploader).openFiles();
					}});
				
				uploaderContainer.add(addButton, new ColumnData(75));
				uploaderContainer.add(editButton, new ColumnData(75));
			}
		}catch(Exception e){
			uploadProgressGrid = new UploadProgressAttachmentGrid(mode);
			if(model != null){
			POJOAttachment pojo = (POJOAttachment)model.getAsPOJO(field.getName());
			if(pojo != null)
			uploadProgressGrid.preloadAttachments(pojo.getValue());
			}
			if(mode == Mode.EDIT){
				addUploader = new TbitsMultiUploader(sysPrefix, requestId, actionId, new TbitsAddUploadStatus((IUploadProgress) uploadProgressGrid), TbitsMultiUploader.ADD_FILES);
				uploaderContainer.add((TbitsMultiUploader)addUploader, new ColumnData(75));
				
				editUploader = new TbitsMultiUploader(sysPrefix, requestId, actionId, 
						new TbitsEditUploadStatus((IUploadProgress) uploadProgressGrid, uploadProgressGrid), TbitsMultiUploader.EDIT_FILES);
				uploaderContainer.add((TbitsMultiUploader)editUploader, new ColumnData(75));
			}
		}
		
		AutoHeightGridContainer container = new AutoHeightGridContainer(uploadProgressGrid,false);
		this.add(container);
		
		if(mode == Mode.EDIT){
			uploaderContainer.add(new Button("Paste Files", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					if(GlobalConstants.filesClipboard != null && GlobalConstants.filesClipboard.size() > 0){
						for(AttachmentInfoClient attachment : GlobalConstants.filesClipboard){
							FileClient file = new FileClient(attachment);
							uploadProgressGrid.addAttachment(file, FileClient.STATUS_UPLOADED);
						}
					}else{
						TbitsInfo.info("No Files in clipboard to be pasted");
					}
				}}), new ColumnData(75));
			
			this.add(uploaderContainer);
		}else{
			ButtonBar bar = new ButtonBar();
			bar.add(new Button("Copy Selected", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					List<FileClient> files = uploadProgressGrid.getSelectionModel().getSelectedItems();
					if(files != null){
						GlobalConstants.filesClipboard = new ArrayList<AttachmentInfoClient>();
						GlobalConstants.filesClipboard.addAll(files);
					}
					TbitsInfo.info(((GlobalConstants.filesClipboard != null && GlobalConstants.filesClipboard.size() > 0) ?  
							GlobalConstants.filesClipboard.size() + "" : "No" )
								+ " files copied to the clipboard");
				}}));
			bar.add(new Button("Download Selected as ZIP", new SelectionListener<ButtonEvent>(){
				@Override
				public void componentSelected(ButtonEvent ce) {
					List<FileClient> files = uploadProgressGrid.getSelectionModel().getSelectedItems();
					if(files == null || files.size() == 0)
						files = uploadProgressGrid.getStore().getModels();
					
					if(files == null || files.size() == 0){
						TbitsInfo.info("No Files available to download");
						return;
					}
					
					String requestFileIdStr = "";
					for(FileClient file : files){
						requestFileIdStr = requestFileIdStr + "-" + file.getRequestFileId();
					}
					String url = ClientUtils.getUrlToFilefromBase("/read-attachment/"
							+ AttachmentFieldContainer.this.sysPrefix + "?request_id=" + requestId + "&request_file_id_str="
							+ requestFileIdStr + "&field_id=" + field.getFieldId()) + "&saveAs=true&format=zip";
					
					ClientUtils.showPreview(url);
				}}));
			this.add(bar);
		}
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
	
	public void setFiles(List<FileClient> atts){
		uploadProgressGrid.getStore().removeAll();
		if(atts != null)
			uploadProgressGrid.preloadAttachments(atts);
		
		this.setHeading(heading + " (" + uploadProgressGrid.getStore().getCount() + ")");
	}
	
}
