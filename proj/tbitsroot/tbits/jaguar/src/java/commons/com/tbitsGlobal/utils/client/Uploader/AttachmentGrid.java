package commons.com.tbitsGlobal.utils.client.Uploader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;

import commons.com.tbitsGlobal.utils.client.Mode;
import commons.com.tbitsGlobal.utils.client.TbitsGridView;
import commons.com.tbitsGlobal.utils.client.GridCellRenderers.LinkCellRendererPlugin;
import commons.com.tbitsGlobal.utils.client.domainObjects.FileClient;

public abstract class AttachmentGrid extends Grid<FileClient>{
	protected boolean canUpdate;
	
	protected IUploadContainerCallback callback;
	
	protected HashMap<FileClient, TbitsProgressBar> progressBarMap = new HashMap<FileClient, TbitsProgressBar>();
	
	protected TbitsGridView gridView;

	public AttachmentGrid(Mode mode) {
		super(new ListStore<FileClient>(), new ColumnModel(new ArrayList<ColumnConfig>()));
		
		this.gridView = new TbitsGridView();
		this.canUpdate = true;
		
		this.setStripeRows(true);
		this.setTrackMouseOver(true);
		
		this.setView(this.gridView);
		
//		this.setStyleAttribute("borderLeft", "5px solid #618BBE");
		this.getView().setEmptyText("No files present");
//		this.setAutoExpandColumn(AttachmentInfoClient.FILE_NAME);
		
		this.getStore().addStoreListener(new StoreListener<FileClient>(){
			@Override
			public void storeAdd(StoreEvent<FileClient> se) {
				super.storeAdd(se);
				callListeners();
			}
			
			@Override
			public void storeClear(StoreEvent<FileClient> se) {
				super.storeClear(se);
				callListeners();
			}
			
			@Override
			public void storeRemove(StoreEvent<FileClient> se) {
				super.storeRemove(se);
				callListeners();
			}
			
			@Override
			public void storeUpdate(StoreEvent<FileClient> se) {
				super.storeUpdate(se);
				callListeners();
			}
		});
		
		this.addPlugin(new LinkCellRendererPlugin());
		
		ArrayList<ColumnConfig> myConfigs = new ArrayList<ColumnConfig>();
		
		RowNumberer rownum = new RowNumberer();
		myConfigs.add(rownum);
		this.addPlugin(rownum);
		
		CheckBoxSelectionModel<FileClient> check = new CheckBoxSelectionModel<FileClient>();
		check.setSelectionMode(SelectionMode.MULTI);
		myConfigs.add(check.getColumn());
		this.setSelectionModel(check);
		this.addPlugin(check);
		
		ColumnConfig name = new ColumnConfig();
		name.setId(FileClient.FILE_NAME);
		name.setHeader("Name");
		name.setWidth(200);
		name.setRenderer(new GridCellRenderer<FileClient>(){
			public Object render(FileClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<FileClient> store, Grid<FileClient> grid) {
				return model.getAnchor();
			}});
		myConfigs.add(name);
		
		ColumnConfig size = new ColumnConfig();
		size.setId("size");
		size.setHeader("Size");
		size.setWidth(100);
		size.setRenderer(new GridCellRenderer<FileClient>(){
			public Object render(FileClient model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<FileClient> store,
					Grid<FileClient> grid) {
				return model.getFormattedSize();
			}});
		myConfigs.add(size);
		
		if(mode == Mode.EDIT){
			GridCellRenderer<FileClient> buttonRenderer = getButtonRenderer();
			ColumnConfig status = new ColumnConfig();
			status.setId(FileClient.STATUS);
			status.setHeader("Status");
			status.setWidth(200);
			status.setRenderer(buttonRenderer);
			myConfigs.add(status);
		}
		
		cm = new ColumnModel(myConfigs);
		this.reconfigure(store, cm);
		
	}
	
	protected abstract GridCellRenderer<FileClient> getButtonRenderer();
	
	protected void preloadAttachments(List<FileClient> attachments){
		for(FileClient attachment : attachments){
			addAttachment(attachment, FileClient.STATUS_UPLOADED);
		}
	}
	
	public FileClient addAttachment() {
		FileClient attachment = new FileClient();
		this.getStore().add(attachment);
		return attachment;
	}

	public void addAttachment(FileClient attachment, String status){
		if(store.findModel(attachment) == null){
			attachment.setStatus(status);
			store.add(attachment);
		}
	}
	
	public void callListeners() {
		if(this.callback != null)
			this.callback.onAttsChanged(store.findModels(FileClient.STATUS, FileClient.STATUS_UPLOADED));
	}
	
	public List<FileClient> getFiles(){
		List<FileClient> uploadedFiles = this.getStore().findModels(FileClient.STATUS, FileClient.STATUS_UPLOADED);
		return uploadedFiles;
	}

	public void setCanUpdate(boolean canUpdate) {
		this.canUpdate = canUpdate;
	}

	public boolean isCanUpdate() {
		return canUpdate;
	}
	
	public void setCallback(IUploadContainerCallback callback) {
		this.callback = callback;
	}

	public IUploadContainerCallback getCallback() {
		return callback;
	}
}
