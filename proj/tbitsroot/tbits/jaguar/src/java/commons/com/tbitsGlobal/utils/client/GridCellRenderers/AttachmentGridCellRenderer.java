package commons.com.tbitsGlobal.utils.client.GridCellRenderers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import commons.com.tbitsGlobal.utils.client.ClickableLink;
import commons.com.tbitsGlobal.utils.client.ClientUtils;
import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.ClickableLink.ClickableLinkListener;
import commons.com.tbitsGlobal.utils.client.Uploader.IUploadContainerCallback;
import commons.com.tbitsGlobal.utils.client.Uploader.UploadWindow;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldAttachment;
import commons.com.tbitsGlobal.utils.client.domainObjects.AttachmentInfoClient;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.pojo.POJO;
import commons.com.tbitsGlobal.utils.client.pojo.POJOAttachment;

/**
 * 
 * @author sourabh
 * 
 * Cell renderer for attachment fields
 */
@SuppressWarnings("unchecked")
public class AttachmentGridCellRenderer extends AbstractFieldCellRenderer<TbitsTreeRequestData, BAFieldAttachment> {
	private HashMap<ClickableLink, UploadWindow> windowMap = new HashMap<ClickableLink, UploadWindow>();
	private HashMap<TbitsTreeRequestData, ClickableLink> keyMap = new HashMap<TbitsTreeRequestData, ClickableLink>();
	
	public AttachmentGridCellRenderer(BAFieldAttachment field){
		super(Mode.VIEW, field);
	}
	
	public AttachmentGridCellRenderer(BAFieldAttachment field, Mode mode){
		super(mode, field);
	}
	
	public String render(final TbitsTreeRequestData model, final String property,
			ColumnData config, final int rowIndex, int colIndex,
			ListStore<TbitsTreeRequestData> store,
			final Grid<TbitsTreeRequestData> grid) {
		String response = "";
		if(model != null){
			POJO obj = model.getAsPOJO(property);
			if(obj == null || !(obj instanceof POJOAttachment)){
				obj = new POJOAttachment(new ArrayList<FileClient>());
				model.set(property, obj);
			}
			
			List<FileClient> attachments = ((POJOAttachment)obj).getValue();
			for(final FileClient attachment : attachments){
				response += attachment.getAnchor() + " (" +  attachment.getFormattedSize() + ")<br />";
			}
			
			
			if(mode == Mode.EDIT){
				if(!keyMap.containsKey(model)){
					final UploadWindow window = new UploadWindow(ClientUtils.getSysPrefix(), model, field);
					window.getFieldContainer().setCallback(new IUploadContainerCallback(){
						public void onAttsChanged(List<FileClient> atts) {
							model.set(property, atts);
							AttachmentGridCellRenderer.this.afterUpdate(atts, rowIndex);
						}

						public void onStatusChanged(FileClient att) {
							AttachmentGridCellRenderer.this.onStatusChanged(rowIndex);
						}});
					
					ClickableLink link = new ClickableLink("Add/Edit Files...", new ClickableLinkListener<GridEvent<ModelData>>(){
						
						public void onClick(GridEvent<ModelData> e) {
							window.show();
						}});
					
					addLink(link);
					
					keyMap.put(model, link);
					windowMap.put(link, window);
					response += link.getHtml() + "<br />";
				}else{
					ClickableLink link = keyMap.get(model);
					
					final UploadWindow window = windowMap.get(link);
					
					String text = "";
					int inProgress = window.getInProgressUploads();
					int queued = window.getQueuedUploads();
					
					text += (inProgress > 0 ? "Uploading " + inProgress +  (inProgress > 1 ? " Files..." : " File...") : "")  + 
							(queued > 0 ? "Queued " + queued +  (queued > 1 ? " Files..." : " File...") : "");
					text +=	!text.equals("") ? " Click to view status" : "Add/Edit Files...";
					
					link.setText(text);
					
					response += link.getHtml() + "<br />";
				}
			}
		}
		return response;
	}
	
	private ArrayList<AttachmentInfoClient> syncAttachments(ListStore<AttachmentInfoClient> sourceStore, ListStore<AttachmentInfoClient> targetStore){
		for(AttachmentInfoClient sourceAtt : sourceStore.getModels()){
			AttachmentInfoClient att = targetStore.findModel(AttachmentInfoClient.REPO_FILE_ID, sourceAtt.getRepoFileId());
			if(att != null){
				int n = targetStore.indexOf(att);
				targetStore.remove(att);
				targetStore.insert(sourceAtt, n);
				continue;
			}
			if(sourceAtt.getRequestFileId() != 0){
				att = targetStore.findModel(AttachmentInfoClient.REQUEST_FILE_ID, sourceAtt.getRequestFileId());
				if(att != null){
					int n = targetStore.indexOf(att);
					targetStore.remove(att);
					targetStore.insert(sourceAtt, n);
					continue;
				}
			}
		}
		
		ArrayList<AttachmentInfoClient> atts = new ArrayList<AttachmentInfoClient>();
		for(AttachmentInfoClient att : targetStore.getModels()){
			atts.add(att);
		}
		
		return atts;
	}
	
	public boolean isBusyUploading(){
		for(UploadWindow window : windowMap.values()){
			if(window.getInProgressUploads() > 0 || window.getQueuedUploads() > 0)
				return true;
		}
		return false;
	}
	
	public void hideWindows(){
		for(UploadWindow w : windowMap.values()){
			w.hide(true);
		}
	}
	
	
	public void afterUpdate(List<FileClient> atts,int row){};
	public void onStatusChanged(int row){};
	
}
