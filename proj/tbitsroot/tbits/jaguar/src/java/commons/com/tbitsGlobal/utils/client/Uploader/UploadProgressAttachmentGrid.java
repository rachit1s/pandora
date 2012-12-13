package commons.com.tbitsGlobal.utils.client.Uploader;

import gwtupload.client.IUploadStatus.UploadCancelHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLink;
import commons.com.tbitsGlobal.utils.client.widgets.TbitsHyperLinkEvent;

public class UploadProgressAttachmentGrid extends AttachmentGrid implements IUploadProgress{
	public interface Listener{
		public void handle(List<FileClient> atts);
	}
	
	protected HashMap<FileClient, ArrayList<UploadCancelHandler>> cancelHandlerMap = 
		new HashMap<FileClient, ArrayList<UploadCancelHandler>>();
	
	public UploadProgressAttachmentGrid(Mode mode) {
		super(mode);
	}
	@Override
	public void addCancelHandler(FileClient att, final UploadCancelHandler handler) {
		if(!cancelHandlerMap.containsKey(att))
			cancelHandlerMap.put(att, new ArrayList<UploadCancelHandler>());
		cancelHandlerMap.get(att).add(handler);
	}

	public void setProgress(FileClient att, int done, int total) {
		if(att != null){
			TbitsProgressBar bar = progressBarMap.get(att);
			if(bar != null)
				bar.setProgress(done, total);
		}
	}
	
	public void updateAttachment(FileClient att){
		updateAttachment(att, true);
	}
	
	public void updateAttachment(FileClient att, boolean callListeners){
		int index = this.getStore().indexOf(att);
		if(index >= 0)
			this.gridView.refreshRow(index);
		
		if(callListeners && this.callback != null){
			if(att.getStatus() != null && att.getStatus().equals(FileClient.STATUS_UPLOADED))
				callListeners();
			else
				this.callback.onStatusChanged(att);
		}
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
						List<UploadCancelHandler> handlers = cancelHandlerMap.get(model);
		    			if(handlers != null){
		    				for(UploadCancelHandler handler : handlers)
		    					handler.onCancel();
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
//		    			  ArrayList<UploadCancelHandler> handlers = cancelHandlerMap.get(model);
//		    			  if(handlers != null){
//		    				  for(UploadCancelHandler handler : handlers)
//		    					  handler.onCancel();
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
