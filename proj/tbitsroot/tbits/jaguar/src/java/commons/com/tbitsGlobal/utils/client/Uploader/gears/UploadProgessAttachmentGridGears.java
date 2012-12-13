package commons.com.tbitsGlobal.utils.client.Uploader.gears;

import java.util.HashMap;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.user.client.ui.Widget;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadHandler;
import com.tbitsGlobal.uploader.gears.client.interfaces.IUploadListenerWidget;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.Uploader.AttachmentGrid;
import commons.com.tbitsGlobal.utils.client.Uploader.TbitsProgressBar;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.log.Log;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;

public class UploadProgessAttachmentGridGears extends AttachmentGrid implements IUploadListenerWidget{
	
	private HashMap<Integer, IUploadHandler> uploadHandlerMap = new HashMap<Integer, IUploadHandler>();
	
	protected HashMap<FileClient, TbitsProgressBar> progressBarMap = new HashMap<FileClient, TbitsProgressBar>();
	
	public UploadProgessAttachmentGridGears(Mode mode) {
		super(mode);
	}
	
	public Widget getWidget() {
		return this;
	}

	public void onComplete(int uniqueId) {
		IUploadHandler uploadHandler = uploadHandlerMap.get(uniqueId);
		FileClient att = getAttFromHash(uniqueId);
		if(att != null){
			att.setUploaded();
			String resposeText = uploadHandler.getRequest().getResponseText();
			try{
				resposeText = resposeText.replaceAll("[^0-9]", "");
				int repoId = Integer.parseInt(resposeText);
				att.setRepoFileId(repoId);
			}catch(Exception e){
				Log.error("Repo File Id not recieved", e);
			}
			int rowIndex = this.getStore().indexOf(att);
			this.gridView.refreshRow(rowIndex);
			
			if(callback != null)
				callback.onStatusChanged(att);
			callListeners();
		}
	}

	public void onError(int uniqueId) {
		IUploadHandler uploadHandler = uploadHandlerMap.get(uniqueId);
		String msg = uploadHandler.getRequest().getStatus() + " " + uploadHandler.getRequest().getStatusText();
		FileClient att = getAttFromHash(uniqueId);
		if(att != null){
			att.setFailed();
			att.setError(msg);
			int rowIndex = this.getStore().indexOf(att);
			this.gridView.refreshRow(rowIndex);
			
			if(callback != null)
				callback.onStatusChanged(att);
			callListeners();
		}
		
	}

	public void onProgress(int uniqueId, int loaded, int total) {
		FileClient att = getAttFromHash(uniqueId);
		if(att != null){
			TbitsProgressBar bar = progressBarMap.get(att);
			if(bar != null)
				bar.setProgress(loaded, total);
		}
	}

	public int onQueue(File file) {
		FileClient attachment = new FileClient();
		attachment.setFileName(file.getName());
		attachment.setSize(file.getBlob().getLength());
		
		int hashcode = file.hashCode();
		attachment.set("hash", hashcode);
		this.getStore().add(attachment);
		
		return hashcode;
	}

	public void onUpload(IUploadHandler uploadHandler) {
		uploadHandlerMap.put(uploadHandler.getUniqueId(), uploadHandler);
		FileClient att = getAttFromHash(uploadHandler.getUniqueId());
		if(att != null){
			att.setUploading();
			int rowIndex = this.getStore().indexOf(att);
			this.gridView.refreshRow(rowIndex);
		}
	}
	
	private FileClient getAttFromHash(int hash){
		return this.getStore().findModel("hash", hash);
	}

	@Override
	protected GridCellRenderer<FileClient> getButtonRenderer() {
		GridCellRenderer<FileClient> buttonRenderer = new GridCellRenderer<FileClient>() {  
		      public Object render(final FileClient model, String property, ColumnData config, final int rowIndex,  
		          final int colIndex, ListStore<FileClient> store, final Grid<FileClient> grid) {
		    	  if(model.getStatus() == null)
		    		  return null;
		    	  
		    	  TbitsHyperLink link = new TbitsHyperLink();
		    	  if(model.getStatus() != null){
		    		  link.setText(model.getStatus());
		    		  if(model.getStatus().equals(FileClient.STATUS_ERROR)){
		    			  if(model.getError() != null && !model.getError().equals(""))
		    				  link.setToolTip(model.getError());
		    		  }
		    	  }
		    	  link.addSelectionListener(new SelectionListener<TbitsHyperLinkEvent>(){
						@Override
						public void componentSelected(TbitsHyperLinkEvent ce) {
							if(model.getPropertyNames().contains("hash")){
				    			  int hashCode = (Integer)model.get("hash");
				    			  IUploadHandler uploadHandler = uploadHandlerMap.get(hashCode);
				    			  if(uploadHandler != null){
				    				  if(model.getStatus().equals(FileClient.STATUS_ERROR)){
				    					  uploadHandler.getCallBack().onRepeat(uploadHandler);
				    				  }else if(model.getStatus().equals(FileClient.STATUS_UPLOADED)){
						    		  }else{
						    			  uploadHandler.getCallBack().onCancel(uploadHandler);
						    		  }
				    				 
				    			  }
			    			  }
			    			  grid.getStore().remove(model);
						}});
		    	  
//		    	  Button b = new Button();
//		    	  if(model.getStatus() != null){
//		    		  b.setText(model.getStatus());
//		    		  if(model.getStatus().equals(FileClient.STATUS_ERROR)){
//		    			  b.setIcon(IconHelper.create(IconConstants.ERROR));
//		    			  if(model.getError() != null && !model.getError().equals(""))
//		    				  b.setToolTip(model.getError());
//		    		  }else if(model.getStatus().equals(FileClient.STATUS_UPLOADED)){
//		    			  b.setIcon(IconHelper.create(IconConstants.COMPLETE));
//		    		  }else if(model.getStatus().equals(FileClient.STATUS_CANCELLED)){
//		    			  b.setIcon(IconHelper.create(IconConstants.STOP));
//		    		  }
//		    	  }
//		        
//		    	  b.addSelectionListener(new SelectionListener<ButtonEvent>() {  
//		    		  @Override  
//		    		  public void componentSelected(ButtonEvent ce) {  
//		    			  if(model.getPropertyNames().contains("hash")){
//			    			  int hashCode = (Integer)model.get("hash");
//			    			  IUploadHandler uploadHandler = uploadHandlerMap.get(hashCode);
//			    			  if(uploadHandler != null){
//			    				  if(model.getStatus().equals(FileClient.STATUS_ERROR)){
//			    					  uploadHandler.getCallBack().onRepeat(uploadHandler);
//			    				  }else if(model.getStatus().equals(FileClient.STATUS_UPLOADED)){
//					    		  }else{
//					    			  uploadHandler.getCallBack().onCancel(uploadHandler);
//					    		  }
//			    				 
//			    			  }
//		    			  }
//		    			  grid.getStore().remove(model);
//		    		  }  
//		    	  });
		    	  
		    	  if(model.getStatus().equals(FileClient.STATUS_UPLOADING)){
		    		  if(!progressBarMap.containsKey(model))
		    			  progressBarMap.put(model, new TbitsProgressBar());
//		    		  b.setWidth(50);
		    		  TbitsProgressBar bar = progressBarMap.get(model);
		    		  LayoutContainer container = new LayoutContainer(new ColumnLayout());
		    		  container.add(bar, new com.extjs.gxt.ui.client.widget.layout.ColumnData(130));
		    		  container.add(link, new com.extjs.gxt.ui.client.widget.layout.ColumnData(50));
//		    		  container.add(b, new com.extjs.gxt.ui.client.widget.layout.ColumnData(50));
		    		  return container;
		    	  }
		    	  
		    	  if(!canUpdate && model.getRepoFileId() != 0)
		    		  link.setEnabled(false);
		    	  
//		    	  if(!canUpdate && model.getRepoFileId() != 0)
//		    		  b.setEnabled(false);
		    	  return link;  
		      }
		    };  
		return buttonRenderer;
	}
}
